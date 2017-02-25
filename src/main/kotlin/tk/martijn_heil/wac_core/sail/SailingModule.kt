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

@file:Suppress("ArrayInDataClass")

package tk.martijn_heil.wac_core.sail

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Logger


object SailingModule {
    lateinit private var plugin: Plugin
    lateinit private var logger: Logger
    private var ships = ArrayList<Ship>()


    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger
    }

/*
    Rudder ASCII art

        \ | /
       -- + --
        / | \
 */

    private fun getSurroundingBlocks(b: Block): Array<Block> {
        throw NotImplementedError()
    }

    private fun detectShip(loc: Basic3DWorldPosition): Ship {
        val world = loc.world
        val hull = ArrayList<Basic3DPosition>()

        val s = Stack<Basic3DPosition>()
        s.push(Basic3DPosition(loc.x, loc.y, loc.z))

        while(!s.isEmpty()) {
            val value = s.pop()
            getSurroundingBlocks(world.getBlockAt(value.x, value.y, value.z)).forEach {
                val pos = Basic3DPosition(it.x, it.y, it.z)
                if(it.type != Material.WOOL && !hull.contains(pos)) {
                    hull.add(pos)
                    s.push(pos)
                }
            }
        }
    }


    private fun getWaterLevelAt(loc: Basic2DWorldPosition) {

    }

    private fun getWindSpeedAt(loc: Basic2DWorldPosition): Int {
        return 20
    }

    private fun getWaveHeightAt(loc: Location): Int {
        return 1
    }


    private data class Basic3DWorldPosition(val world: World, val x: Int, val y: Int, val z: Int)
    private data class Basic2DWorldPosition(val world: World, val x: Int, val z: Int)
    private data class Basic3DPosition(val x: Int, val y: Int, val z: Int)
    private data class Basic2DPosition(val x: Int, val z: Int)
    private data class Basic2DVelocity(val xVelocity: Int, val zVelocity: Int)

    private enum class CardinalDirection {
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST
    }


    private data class Sail(
            val blocks: ArrayList<Basic3DPosition>,
            var isHoisted: Boolean
    )

    private data class Ship(
        val hull: Array<Basic3DPosition>,
        val sails: HashMap<UUID, Sail>,
        val velocity: Basic2DVelocity,
        var heading: CardinalDirection,
        val world: World,
        val tickInterval: Int
    )
}