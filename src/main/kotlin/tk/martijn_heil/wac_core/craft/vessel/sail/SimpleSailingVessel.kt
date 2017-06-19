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
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGHEST
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.material.Door
import org.bukkit.plugin.Plugin
import tk.martijn_heil.wac_core.WacCore
import tk.martijn_heil.wac_core.craft.Rotation
import tk.martijn_heil.wac_core.craft.Rotation.ANTICLOCKWISE
import tk.martijn_heil.wac_core.craft.Rotation.CLOCKWISE
import tk.martijn_heil.wac_core.craft.SailingModule.windFrom
import tk.martijn_heil.wac_core.craft.exception.CouldNotMoveCraftException
import tk.martijn_heil.wac_core.craft.util.detect
import tk.martijn_heil.wac_core.craft.vessel.HasRudder
import tk.martijn_heil.wac_core.craft.vessel.SimpleRudder
import tk.martijn_heil.wac_core.craft.vessel.SimpleShip
import java.io.Closeable
import java.lang.Math.*
import java.util.*
import java.util.logging.Logger

/*
TODO:
    Fix: Ship filling up with water if under water line.
 */
open class SimpleSailingVessel protected constructor(private val plugin: Plugin, private val logger: Logger, blocks: Collection<Block>, rotationPoint: Location,
                                                     override val sails: Collection<SimpleSail>, private val rudder: SimpleRudder) : SimpleShip(plugin, blocks, rotationPoint), HasSail, HasRudder, AutoCloseable {
    open protected var normalMaxSpeed: Int = 10000                     // in metres per hour
    open protected var speedPerSquareMetreOfSail: Double = 0.0         // in metres per hour
    open protected val accelerationPerUpdate: Int get() = normalMaxSpeed / 8 // in metres per hour
    open protected val decelerationPerUpdate: Int get() = normalMaxSpeed / 8 // in metres per hour
    open protected var updateInterval = 40                             // interval in Minecraft game ticks. A single tick is 50ms
    open protected var updateTaskId = 0
    override val minWindAngle = 40

    open protected val currentMaxSpeed: Double
        get() = currentSailSurfaceArea * speedPerSquareMetreOfSail

    open protected val currentSailSurfaceArea: Int
        get() {
            var surfaceArea = 0
            sails.forEach { surfaceArea += it.currentSurfaceArea }
            return surfaceArea
        }

    open protected val distancePerUpdate: Double
        get() = speed / 60 / 60 * (updateInterval / 20).toDouble()

    open protected var increaseX: Int = 0
    open protected var increaseZ: Int = 0

    open protected var speed: Double = 0.0 // in metres per second
        protected set(value) {
            field = value // important we set this first
            var angle = 360 - heading.toDouble() + 90
            if(angle > 360) angle -= 360
            val radians = toRadians(angle)
            increaseX = round(0.0 + (distancePerUpdate * cos(radians))).toInt()
            increaseZ = round(0.0 - (distancePerUpdate * sin(radians))).toInt()
        }

    open protected var heading: Int = 0
        set(value) {
            if(value == 360) throw IllegalArgumentException("360 degrees should be 0 degrees.")
            if(value < 0 || value > 360) throw IllegalArgumentException("Invalid number of degrees.")


            if(currentlyFacing == -1) { // Need to initialize it
                when {
                    value > 315 || value < 45 -> currentlyFacing = 0
                    value > 45 && value < 135 -> currentlyFacing = 90
                    value > 135 && value < 225 -> currentlyFacing = 180
                    value > 225 && value < 315 -> currentlyFacing = 270
                }
            }

            var shouldFaceNext = 0
            when {
                value > 315 || value < 45 -> shouldFaceNext = 0
                value > 45 && value < 135 -> shouldFaceNext = 90
                value > 135 && value < 225 -> shouldFaceNext = 180
                value > 225 && value < 315 -> shouldFaceNext = 270
            }

            if(shouldFaceNext != currentlyFacing) {
                var rotation = CLOCKWISE
                var amount = 0

                when (currentlyFacing) {
                    0 -> {
                        when (shouldFaceNext) {
                            90 -> { rotation = CLOCKWISE; amount = 1 }
                            180 -> { rotation = CLOCKWISE; amount = 2 }
                            270 -> { rotation = ANTICLOCKWISE; amount = 1 }
                        }
                    }
                    90 -> {
                        when (shouldFaceNext) {
                            0 -> { rotation = ANTICLOCKWISE; amount = 1 }
                            180 -> { rotation = CLOCKWISE; amount = 1 }
                            270 -> { rotation = CLOCKWISE; amount = 2 }
                        }
                    }
                    180 -> {
                        when (shouldFaceNext) {
                            0 -> { rotation = CLOCKWISE; amount = 2 }
                            90 -> { rotation = ANTICLOCKWISE; amount = 1 }
                            270 -> { rotation = CLOCKWISE; amount = 1 }
                        }
                    }
                    270 -> {
                        when (shouldFaceNext) {
                            0 -> { rotation = CLOCKWISE; amount = 1 }
                            90 -> { rotation = CLOCKWISE; amount = 2 }
                            180 -> { rotation = ANTICLOCKWISE; amount = 1 }
                        }
                    }
                }

                for(i in 1..amount) { rotate(rotation) }

                currentlyFacing = shouldFaceNext
            }

            var angle = 360 - value.toDouble() + 90
            if(angle > 360) angle -= 360
            val radians = toRadians(angle)
            increaseX = round(0.0 + (distancePerUpdate * cos(radians))).toInt()
            increaseZ = round(0.0 - (distancePerUpdate * sin(radians))).toInt()
            field = value
        }
    var currentlyFacing: Int = -1

    open protected val listener = object : Listener {
        @EventHandler(ignoreCancelled = true, priority = HIGHEST)
        fun onPlayerInteract(e: PlayerInteractEvent) {
            if(e.clickedBlock != null) {
                val state = e.clickedBlock.state
                if(state is Sign && state.lines[0] == "[Ship]" && containsBlock(e.clickedBlock)) {
                    e.isCancelled = true
                }
            }
        }

        @EventHandler(ignoreCancelled = true, priority = MONITOR)
        fun onBlockBreak(e: BlockBreakEvent) {
            if(boundingBox.contains(e.block)) removeBlock(e.block)
        }

        @EventHandler(ignoreCancelled = true)
        fun onBlockPhysics(e: BlockPhysicsEvent) {
            if(boundingBox.contains(e.block)) e.isCancelled = true
        }

        @EventHandler(ignoreCancelled = true, priority = MONITOR)
        fun onBlockPlace(e: BlockPlaceEvent) {
            for (modX in -1..1) {
                for(modY in -1..1) {
                    for(modZ in -1..1) {
                        if(containsBlock(world.getBlockAt(e.block.x + modX, e.block.y + modY, e.block.z + modZ))) {
                            val blockState = e.block.state
                            val blockData = blockState.data
                            addBlock(e.block)
                            if(blockData is Door) addBlock(world.getBlockAt(e.block.x, e.block.y + if(blockData.isTopHalf) -1 else 1, e.block.z))
                        }
                    }
                }
            }
        }

        @EventHandler(ignoreCancelled = true)
        fun onCreatureSpawn(e: CreatureSpawnEvent) { // No annoying mobs spawning on the ship.
            if(boundingBox.contains(e.location)) e.isCancelled = true
        }
    }

    fun onChangeCourseCallback(heading: Int): Boolean {
        try {
            this.heading = heading
        } catch(e: CouldNotMoveCraftException) {
            onBoardEntities.filter { it is Player }.forEach { it.sendMessage(ChatColor.RED.toString() + e.message) }
            return false
        }
        return true
    }

    open protected fun init() {
        heading = rudder.course
        rudder.onChangeCourseCallback = { onChangeCourseCallback(it) }

        var maxSailSurfaceArea = 0
        sails.forEach { maxSailSurfaceArea += it.maxSurfaceArea }
        speedPerSquareMetreOfSail = normalMaxSpeed / maxSailSurfaceArea.toDouble()

        val updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(WacCore.plugin, {
            try {
                update()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }, 0, updateInterval.toLong())
        if (updateTaskId == -1) { throw Exception("Could not schedule Bukkit task.") }
        Bukkit.getPluginManager().registerEvents(listener, WacCore.plugin)
    }

    private fun update() {
        var angleToWind = windFrom - heading
        if (angleToWind > 180) angleToWind = 360 - angleToWind
        if(angleToWind < 0) angleToWind = -(angleToWind)

        val currentMaxSpeed = this.currentMaxSpeed // just cache the value, less recomputing
        if (angleToWind < minWindAngle) {
            if (speed > 0) {
                speed -= decelerationPerUpdate
                if(speed < 0) speed = 0.0
            }
        } else {
            if (speed < currentMaxSpeed) { // Should accelerate
                if((currentMaxSpeed - speed) < accelerationPerUpdate) {
                    speed = currentMaxSpeed
                } else {
                    speed += accelerationPerUpdate
                }
            } else if (speed > currentMaxSpeed) { // Should decelerate
                speed -= decelerationPerUpdate
                if(speed < currentMaxSpeed) speed = currentMaxSpeed
            }
        }

        val newLoc = location.clone()
        newLoc.x += increaseX
        newLoc.z += increaseZ
        try {
            location = newLoc
        } catch(e: CouldNotMoveCraftException) {
            onBoardEntities.filter { it is Player }.forEach { it.sendMessage(ChatColor.RED.toString() + e.message) }
        }
    }

    override var location: Location
        get() = super.location
        set(value) {
            super.location = value
        }

    override fun move(relativeX: Int, relativeY: Int, relativeZ: Int) {
        super.move(relativeX, relativeY, relativeZ)
        rudder.updateLocation(relativeX, relativeZ)
        sails.forEach { it.updateLocation(relativeX, relativeZ) }
    }

    override fun rotate(rotation: Rotation) {
        super.rotate(rotation)
        rudder.updateLocationRotated(rotationPoint, rotation)
        sails.forEach { it.updateLocationRotated(rotationPoint, rotation) }
    }

    override fun close() {
        HandlerList.unregisterAll(listener)
        sails.forEach { if(it is Closeable) it.close() }
        rudder.close()
        Bukkit.getScheduler().cancelTask(updateTaskId)
    }

    companion object {
        fun detect(plugin: Plugin, logger: Logger, detectionLoc: Location): SimpleSailingVessel {
            val sails: MutableCollection<SimpleSail> = ArrayList()
            try {
                val maxSize = 5000
                val allowedBlocks: Collection<Material> = Material.values().filter { it != AIR && it != WATER && it != STATIONARY_WATER && it != LAVA && it != STATIONARY_LAVA }
                val blocks: Collection<Block>
                // Detect vessel
                try {
                    logger.info("Detecting sailing vessel at " + detectionLoc.x + "x " + detectionLoc.y + "y " + detectionLoc.z + "z")
                    blocks = detect(detectionLoc, allowedBlocks, maxSize)
                } catch(e: Exception) {
                    logger.info("Failed to detect sailing vessel: " + (e.message ?: "unknown error"))
                    throw IllegalStateException(e.message)
                }
                val signs = blocks.map { it.state }.filter { it is Sign }.map { it as Sign }
                val rotationPointSign = signs.find { it.lines[0] == "[RotationPoint]" }
                if(rotationPointSign == null) {
                    logger.warning("Could not detect rotation point")
                    throw IllegalStateException("Could not detect rotation point.")
                }
                val rotationPoint = rotationPointSign.location

                // Detect rudder
                val rudderSign = signs.find { it.lines[0] == "[Rudder]" } ?: throw IllegalStateException("No rudder found.")
                logger.info("Found rudder sign at " + rudderSign.x + " " + rudderSign.y + " " + rudderSign.z)
                val rudder = SimpleRudder(rudderSign)

                // Detect sails
                signs.filter { it.lines[0] == "[Sail]" }.forEach {
                    logger.fine("Found sail sign at " + it.x + " " + it.y + " " + it.z)
                    sails.add(SimpleSail(it))
                }
                if (sails.isEmpty()) throw IllegalStateException("No sails found.")

                val simpleSailingVessel = SimpleSailingVessel(plugin, logger, blocks, rotationPoint, sails, rudder)
                simpleSailingVessel.init()
                return simpleSailingVessel
            } catch(t: Throwable) {
                sails.forEach { it.isHoisted = true }
                throw t
            }
        }
    }
}