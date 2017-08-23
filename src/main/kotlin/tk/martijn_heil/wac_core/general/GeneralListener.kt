/*
 *     wac-core
 *     Copyright (C) 2016 Martijn Heil
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.martijn_heil.wac_core.general

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Ocelot
import org.bukkit.entity.Tameable
import org.bukkit.entity.Wolf
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.logging.Logger


class GeneralListener(val logger: Logger, val plugin: Plugin) : Listener {

//    private val clearedChunks = HashSet<Int>()
//
//    init {
//        logger.info("Adding all cleared chunks in the database to the in-memory cache..")
//        val stmnt = WacCore.dbconn!!.prepareStatement("SELECT * FROM wac_core_cleared_chunks")
//        val result = stmnt.executeQuery()
//        while(result.next()) {
//            clearedChunks.add(result.getInt("coordinates"))
//        }
//    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerLogin(e: PlayerJoinEvent) {
        logger.fine("Ensuring that " + e.player.name + " is present in database..")
        //ensurePresenceInDatabase(chunkPropagateSkylightOcclusion.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onCreatureSpawn(e: CreatureSpawnEvent) {
        if(e.spawnReason == CreatureSpawnEvent.SpawnReason.LIGHTNING && e.entity.type == EntityType.SKELETON_HORSE) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEnderPearl(e: PlayerTeleportEvent) {
        if(e.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            e.isCancelled = true
            e.player.sendMessage(ChatColor.RED.toString() + "Ender pearls zijn uitgeschakeld!")
            e.player.inventory.addItem(ItemStack(Material.ENDER_PEARL, 1))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerChorusFruitTeleport(e: PlayerTeleportEvent) {
        if(e.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            e.isCancelled = true
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        e.player.location.world.getEntitiesByClasses(Ocelot::class.java, Wolf::class.java).forEach {
            if (it is Tameable && it.owner == e.player && !((it is Ocelot && it.isSitting) || (it is Wolf && it.isSitting))) {
                it.teleport(e.to)
            }
        }
    }

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    fun onChunkLoad(chunkPropagateSkylightOcclusion: ChunkLoadEvent) {
//        if(!clearedChunks.contains(encodeChunkCoords(chunkPropagateSkylightOcclusion.chunk.x, chunkPropagateSkylightOcclusion.chunk.z))) {
//            logger.info("Clearing chunk.. (x: " + chunkPropagateSkylightOcclusion.chunk.x + ", z: " + chunkPropagateSkylightOcclusion.chunk.z + ")")
//            clearChunk(chunkPropagateSkylightOcclusion.chunk)
//
//            val encoded = encodeChunkCoords(chunkPropagateSkylightOcclusion.chunk.x, chunkPropagateSkylightOcclusion.chunk.z)
//            clearedChunks.add(encoded)
//
//            @Suppress("DEPRECATION")
//            plugin.server.scheduler.scheduleAsyncDelayedTask(plugin, {
//                val stmnt = WacCore.dbconn!!.prepareStatement("INSERT INTO wac_core_cleared_chunks VALUES(?)")
//                stmnt.setInt(1, encoded)
//                stmnt.executeUpdate()
//            }, 0)
//        }
//    }

//    private fun encodeChunkCoords(x: Int, z: Int): Int {
//        return z or (x shl 16)
//    }
//
//    private fun decodeChunkCoords(data: Int): Pair<Int, Int> {
//        val x = data ushr 16
//        val z = data or 0b00000000000000001111111111111111
//        return Pair(x, z)
//    }
//
//    private fun clearChunk(chunk: Chunk) {
//        for(y in 0..255) {
//            for(x in 0..15) {
//                for(z in 0..15) {
//                    val block = chunk.getBlock(x, y, z)
//                    if(block.state is InventoryHolder) {
//                        (block.state as InventoryHolder).inventory.clear()
//                    }
//                }
//            }
//        }
//    }
}
