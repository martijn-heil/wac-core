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
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import tk.martijn_heil.wac_core.craft.Rotation
import tk.martijn_heil.wac_core.craft.util.BlockProtector
import tk.martijn_heil.wac_core.craft.util.detect
import tk.martijn_heil.wac_core.craft.util.getRotatedLocation
import java.util.*


class SimpleSail(private val plugin: Plugin, private var sign: Sign) : Sail, AutoCloseable {

    private var world: World = sign.world
    private val protectedBlocks = ArrayList<Location>()
    private val blockProtector = BlockProtector(plugin)
    val name: String?

    private val listener = object : Listener {
        @EventHandler(ignoreCancelled = true, priority = MONITOR)
        fun onPlayerInteract(e: PlayerInteractEvent) {
            if(e.clickedBlock == sign.block) isHoisted = !isHoisted
        }
    }

    private var blocks: ArrayList<Block>

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
            blocks = ArrayList(detect(Location(sign.location.world, sign.location.x, sign.location.y - 1, sign.location.z ), listOf(Material.WOOL), 500))
        } catch(ex: Exception) {
            throw IllegalStateException("Could not detect sail (sign at " + sign.location.x + "x " + sign.location.y + " y" + sign.location.z + " z): " + ex.message)
        }

        Bukkit.getPluginManager().registerEvents(listener, plugin)
        blockProtector.protectedBlocks.add(sign.block.location)
        blockProtector.protectedBlocks.add(sign.block.getRelative((sign.data as org.bukkit.material.Sign).attachedFace).location)

        name = if(sign.lines.size >= 2) sign.lines[1] else null
        isHoisted = false
    }

    override val maxSurfaceArea: Int = blocks.size
    override val currentSurfaceArea: Int
        get() = blocks.size

    fun updateLocation(relativeX: Int, relativeY: Int, relativeZ: Int) {
        val tmpBlocks = ArrayList<Block>()
        blocks.forEach { tmpBlocks.add(world.getBlockAt(it.x + relativeX, it.y + relativeY, it.z + relativeZ)) }
        blocks = tmpBlocks
        sign = world.getBlockAt(sign.x + relativeX, sign.y + relativeY, sign.z + relativeZ).state as Sign
        protectedBlocks.forEach {
            it.x += relativeX
            it.y += relativeY
            it.z += relativeZ
        }
    }

    fun updateLocationRotated(rotationPoint: Location, rotation: Rotation) {
        val tmpBlocks = ArrayList<Block>()
        blocks.forEach { tmpBlocks.add(world.getBlockAt(getRotatedLocation(rotationPoint, rotation, it.location))) }
        blocks = tmpBlocks
        sign = world.getBlockAt(getRotatedLocation(rotationPoint, rotation, sign.location)).state as Sign
        protectedBlocks.forEach { getRotatedLocation(it, rotationPoint, rotation, it) }
    }

    override fun close() {
        isHoisted = true
        HandlerList.unregisterAll(listener)
    }
}