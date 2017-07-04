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

package tk.martijn_heil.wac_core.craft.vessel.sail

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGHEST
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import tk.martijn_heil.wac_core.WacCore
import tk.martijn_heil.wac_core.craft.Rotation
import tk.martijn_heil.wac_core.craft.util.detect
import tk.martijn_heil.wac_core.craft.util.getRotatedLocation
import java.io.Closeable
import java.util.*


class SimpleSail(private var sign: Sign) : Sail, AutoCloseable, Closeable {

    private var world: World = sign.world

    private val listener = object : Listener {
        @EventHandler(ignoreCancelled = true, priority = HIGHEST)
        fun onEntityExplode(e: EntityExplodeEvent) {
            e.blockList().remove(sign.block)
            e.blockList().remove(sign.block.getRelative((sign.data as org.bukkit.material.Sign).attachedFace))
        }

        @EventHandler(ignoreCancelled = true, priority = HIGHEST)
        fun onBlockPhysics(e: BlockPhysicsEvent) {
            if(e.block == sign.block) e.isCancelled = true
        }

        @EventHandler(ignoreCancelled = true, priority = MONITOR)
        fun onPlayerInteract(e: PlayerInteractEvent) {
            if(e.clickedBlock == sign.block) isHoisted = !isHoisted
        }

        @EventHandler(ignoreCancelled = true)
        fun onBlockBreak(e: BlockBreakEvent) {
            val signData = (sign.data as org.bukkit.material.Sign)

            if (e.block == sign.block || e.block == sign.block.getRelative(signData.attachedFace)) {
                e.isCancelled = true
            }
        }
    }

    private var blocks: HashSet<Block>

    override var isHoisted: Boolean = true
        set(value) {
            if(value != field) {
                if(value) {
                    blocks.forEach { it.type = Material.WOOL }
                } else {
                    blocks.forEach { it.type = Material.AIR }
                }
            }

            field = value
        }

    init {
        try {
            blocks = HashSet(detect(Location(sign.location.world, sign.location.x, sign.location.y - 1, sign.location.z ), listOf(Material.WOOL), 500))
        } catch(ex: Exception) {
            throw IllegalStateException("Could not detect sail (sign at " + sign.location.x + "x " + sign.location.y + " y" + sign.location.z + " z): " + ex.message)
        }

        Bukkit.getPluginManager().registerEvents(listener, WacCore.plugin)
        isHoisted = false
    }

    override val maxSurfaceArea: Int = blocks.size
    override val currentSurfaceArea: Int
        get() = blocks.filter { it.type == Material.WOOL }.size

    fun updateLocation(relativeX: Int, relativeZ: Int) {
        val tmpBlocks = HashSet<Block>()
        blocks.forEach { tmpBlocks.add(world.getBlockAt(it.x + relativeX, it.y, it.z + relativeZ)) }
        blocks = tmpBlocks
        sign = world.getBlockAt(sign.x + relativeX, sign.y, sign.z + relativeZ).state as Sign
    }

    fun updateLocationRotated(rotationPoint: Location, rotation: Rotation) {
        val tmpBlocks = HashSet<Block>()
        blocks.forEach { tmpBlocks.add(world.getBlockAt(getRotatedLocation(rotationPoint, rotation, it.location))) }
        blocks = tmpBlocks
        sign = world.getBlockAt(getRotatedLocation(rotationPoint, rotation, sign.location)).state as Sign
    }

    override fun close() {
        isHoisted = true
        HandlerList.unregisterAll(listener)
    }
}