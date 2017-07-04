package tk.martijn_heil.wac_core.craft.util.nms

import net.minecraft.server.v1_11_R1.Block
import net.minecraft.server.v1_11_R1.BlockPosition
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld
import org.bukkit.material.Button
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import tk.martijn_heil.wac_core.craft.util.MassBlockUpdate
import java.util.*
import java.util.concurrent.TimeUnit


class CraftMassBlockUpdate(private val plugin: Plugin, private val world: World) : MassBlockUpdate {
    override var relightingStrategy: MassBlockUpdate.RelightingStrategy = MassBlockUpdate.RelightingStrategy.IMMEDIATE
    private var deferredBlocks: Queue<DeferredBlock> = ArrayDeque<DeferredBlock>()
    private var relightTask: BukkitTask? = null
    private var maxRelightTimePerTick = TimeUnit.NANOSECONDS.convert(1, TimeUnit.MILLISECONDS)

    private var minX = Integer.MAX_VALUE
    private var minZ = Integer.MAX_VALUE
    private var maxX = Integer.MIN_VALUE
    private var maxZ = Integer.MIN_VALUE
    private var blocksModified = 0

    override fun setBlockState(x: Int, y: Int, z: Int, state: BlockState): Boolean {
        val stateData = state.data
        if(stateData is Button) {
            stateData.isPowered = false
            state.data = stateData
        }

        val res = setBlock(x, y, z, state.typeId, state.rawData.toInt())
        if(!res) return false
        val newBlock = world.getBlockAt(x, y, z)

        if(state is Sign) {
            val toSign = newBlock.state as Sign
            val fromSign = state
            for (i in 0..fromSign.lines.size-1) {
                toSign.setLine(i, fromSign.getLine(i))
            }
            toSign.update(true, false)
        }

//        if(state is Chest) {
//            val newChest = newBlock.state as Chest
//            newChest.inventory.contents = state.blockInventory.contents
//            newChest.update(true, false)
//        }

        return true
    }

    override fun setBlock(x: Int, y: Int, z: Int, material: Material) = setBlock(x, y, z, material.id)
    override fun setBlock(x: Int, y: Int, z: Int, blockId: Int) = setBlock(x, y, z, blockId, 0)

    override fun setBlock(x: Int, y: Int, z: Int, blockId: Int, data: Int): Boolean {
//        val b = world.getBlockAt(x, y, z)
//        val state = b.state
//        state.type = Material.getMaterial(blockId)
//        state.data = MaterialData(blockId, data.toByte())
//        state.update(true, false)
//        return true

        minX = Math.min(minX, x)
        minZ = Math.min(minZ, z)
        maxX = Math.max(maxX, x)
        maxZ = Math.max(maxZ, z)

        blocksModified++
        val oldBlockId = world.getBlockTypeIdAt(x, y, z)
        val res = setBlockFast(world, x, y, z, blockId, data.toByte())

        if (relightingStrategy != MassBlockUpdate.RelightingStrategy.NEVER) {
            if (getBlockLightBlocking(oldBlockId) != getBlockLightBlocking(blockId) || getBlockLightEmission(oldBlockId) != getBlockLightEmission(blockId)) {
                // lighting or light blocking by this block has changed; force a recalculation
                if (relightingStrategy == MassBlockUpdate.RelightingStrategy.IMMEDIATE) {
                    recalculateBlockLighting(world, x, y, z)
                } else if (relightingStrategy == MassBlockUpdate.RelightingStrategy.DEFERRED || relightingStrategy == MassBlockUpdate.RelightingStrategy.HYBRID) {
                    deferredBlocks.add(DeferredBlock(x, y, z))
                }
            }
        }
        return res
    }

    override fun notifyClients() {
        if (relightingStrategy == MassBlockUpdate.RelightingStrategy.DEFERRED || relightingStrategy == MassBlockUpdate.RelightingStrategy.HYBRID) {
            relightTask = Bukkit.getScheduler().runTaskTimer(plugin, CraftMassBlockUpdateRunnable(), 1L, 1L)
        }
//        if (relightingStrategy != MassBlockUpdate.RelightingStrategy.DEFERRED) {
//            for (cc in calculateChunks()) {
//                world.refreshChunk(cc.x, cc.z)
//            }
//        }
    }

    override fun setMaxRelightTimePerTick(value: Long, timeUnit: TimeUnit) {
        maxRelightTimePerTick = timeUnit.toNanos(value)
    }

    override val blocksToRelight: Int
        get() = deferredBlocks.size

    fun setDeferredBufferSize(size: Int) {
        if (!deferredBlocks.isEmpty()) {
            // resizing an existing buffer is not supported
            throw IllegalStateException("setDeferredBufferSize() called after block updates made")
        }
        if (relightingStrategy !== MassBlockUpdate.RelightingStrategy.DEFERRED && relightingStrategy !== MassBlockUpdate.RelightingStrategy.HYBRID) {
            // reduce accidental memory wastage if called when not needed
            throw IllegalStateException("setDeferredBufferSize() called when relighting strategy not DEFERRED or HYBRID")
        }
        deferredBlocks = ArrayDeque<CraftMassBlockUpdate.DeferredBlock>(size)
    }

    private fun canAffectLighting(world: World, x: Int, y: Int, z: Int): Boolean {
        val base = world.getBlockAt(x, y, z)
        val east = base.getRelative(BlockFace.EAST)
        val west = base.getRelative(BlockFace.WEST)
        val up = base.getRelative(BlockFace.UP)
        val down = base.getRelative(BlockFace.DOWN)
        val south = base.getRelative(BlockFace.SOUTH)
        val north = base.getRelative(BlockFace.NORTH)

        return east.type.isTransparent ||
                west.type.isTransparent ||
                up.type.isTransparent ||
                down.type.isTransparent ||
                south.type.isTransparent ||
                north.type.isTransparent
    }

    private fun calculateChunks(): Set<ChunkCoords> {
        val res = HashSet<ChunkCoords>()
        if (blocksModified == 0) {
            return res
        }
        val x1 = minX shr 4
        val x2 = maxX shr 4
        val z1 = minZ shr 4
        val z2 = maxZ shr 4
        for (x in x1..x2) {
            for (z in z1..z2) {
                res.add(ChunkCoords(x, z))
            }
        }
        return res
    }

    private inner class CraftMassBlockUpdateRunnable : Runnable {
        override fun run() {
            val now = System.nanoTime()
            var n = 1

            while (deferredBlocks.peek() != null) {
                val db = deferredBlocks.poll()
                // Don't consider blocks that are completely surrounded by other non-transparent blocks
                if (canAffectLighting(world, db.x, db.y, db.z)) {
                    recalculateBlockLighting(world, db.x, db.y, db.z)
                    if (n++ % MAX_BLOCKS_PER_TIME_CHECK == 0) {
                        if (System.nanoTime() - now > maxRelightTimePerTick) {
                            break
                        }
                    }
                }
            }

            if (deferredBlocks.isEmpty()) {
                relightTask!!.cancel()
                relightTask = null
                val touched = calculateChunks()
                for (cc in touched) {
                    world.refreshChunk(cc.x, cc.z)
                }
            }
        }
    }

    private inner class ChunkCoords(val x: Int, val z: Int) {

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false

            val that = o as ChunkCoords?

            if (x != that!!.x) return false
            if (z != that.z) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x
            result = 31 * result + z
            return result
        }
    }

    private data class DeferredBlock(val x: Int, val y: Int, val z: Int)

    companion object {
        private val MAX_BLOCKS_PER_TIME_CHECK = 1000
    }
}

private fun setBlockFast(world: World, x: Int, y: Int, z: Int, blockId: Int, data: Byte): Boolean {
    val w = (world as CraftWorld).handle
    val bp = BlockPosition(x, y, z)
    val combined = (data.toInt() shl 12) or blockId
    val ibd = net.minecraft.server.v1_11_R1.Block.getByCombinedId(combined)
    return w.setTypeAndData(bp, ibd, 0x02) // World#setBlockState/setTypeAndData(BlockPos, IBlockState/IBlockData, flags)
}

private fun getBlockLightBlocking(blockId: Int): Int {
    throw UnsupportedOperationException()
}

private fun recalculateBlockLighting(world: World, x: Int, y: Int, z: Int) {
    throw UnsupportedOperationException()
}

private fun getBlockLightEmission(blockId: Int): Int {
    throw UnsupportedOperationException()
}

// TODO: Fix commented out code
//private fun getBlockLightBlocking(blockId: Int): Int {
//    return Block.getById(blockId).()
//}
//
//private fun recalculateBlockLighting(world: World, x: Int, y: Int, z: Int) {
//    // Don't consider blocks that are completely surrounded by other non-transparent blocks
//    if (!canAffectLighting(world, x, y, z)) return
//
//    val i = x and 0x0F
//    val j = y and 0xFF
//    val k = z and 0x0F
//    val blockPos = BlockPosition(i, j, k)
//    val craftChunk = world.getChunkAt(x shr 4, z shr 4) as CraftChunk
//    val nmsChunk = craftChunk.handle
//
//    val i1 = k shl 4 or i
//    val maxY = nmsChunk.heightMap[i1]
//
//    val block: Block = nmsChunk.a(i, j, k).block //
//    val j2: Int = block
//
//    if (j2 > 0) {
//        if (j >= maxY) {
//            chunkRelightBlock(nmsChunk, i, j + 1, k)
//        }
//    } else if (j == maxY - 1) {
//        chunkRelightBlock(nmsChunk, i, j, k)
//    }
//
//    if (nmsChunk.getBrightness(EnumSkyBlock.SKY, blockPos) > 0 || nmsChunk.getBrightness(EnumSkyBlock.BLOCK, blockPos) > 0) {
//        chunkPropagateSkylightOcclusion(nmsChunk, i, k)
//    }
//
//    val w = (world as CraftWorld).handle
//    w.c(EnumSkyBlock.BLOCK, blockPos) // World#checkLightFor()
//}
//
//// private void relightBlock(int x, int y, int z) (1.11.2, mc-dev)
//// private void relightBlock(int x, int y, int z) (1.11.2, MCP)
//private var chunkRelightBlock: Method? = null
//private fun chunkRelightBlock(nmsChunk: Chunk, i: Int, j: Int, k: Int) {
//    try {
//        if (chunkRelightBlock == null) {
//            val classes = arrayOf(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
//            chunkRelightBlock = Chunk::class.java.getDeclaredMethod("relightBlock", *classes)
//            chunkRelightBlock!!.isAccessible = true
//        }
//        chunkRelightBlock!!.invoke(nmsChunk, i, j, k)
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//
//}
//
//// private void propagateSkylightOcclusion(int x, int z) (1.11.2, MCP)
//private var chunkPropagateSkylightOcclusion: Method? = null
//private fun chunkPropagateSkylightOcclusion(nmsChunk: Chunk, i: Int, j: Int) {
//    try {
//        if (chunkPropagateSkylightOcclusion == null) {
//            val classes = arrayOf(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
//            chunkPropagateSkylightOcclusion = Chunk::class.java.getDeclaredMethod("TODO", *classes)
//            chunkPropagateSkylightOcclusion!!.isAccessible = true
//        }
//        chunkPropagateSkylightOcclusion!!.invoke(nmsChunk, i, j)
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//}

private fun canAffectLighting(world: World, x: Int, y: Int, z: Int): Boolean {
    val base = world.getBlockAt(x, y, z)
    val east = base.getRelative(BlockFace.EAST)
    val west = base.getRelative(BlockFace.WEST)
    val up = base.getRelative(BlockFace.UP)
    val down = base.getRelative(BlockFace.DOWN)
    val south = base.getRelative(BlockFace.SOUTH)
    val north = base.getRelative(BlockFace.NORTH)

    return east.type.isTransparent ||
            west.type.isTransparent ||
            up.type.isTransparent ||
            down.type.isTransparent ||
            south.type.isTransparent ||
            north.type.isTransparent
}