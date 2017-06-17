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

import at.pavlov.cannons.cannon.Cannon
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.*
import org.bukkit.block.BlockState
import org.bukkit.block.Sign
import org.bukkit.entity.Entity
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
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.material.Attachable
import org.bukkit.material.Directional
import org.bukkit.material.Door
import org.bukkit.material.Ladder
import org.bukkit.util.Vector
import tk.martijn_heil.wac_core.WacCore
import tk.martijn_heil.wac_core.WacCore.Companion.plugin
import tk.martijn_heil.wac_core.craft.Rotation
import tk.martijn_heil.wac_core.craft.Rotation.ANTICLOCKWISE
import tk.martijn_heil.wac_core.craft.Rotation.CLOCKWISE
import tk.martijn_heil.wac_core.craft.SailingModule.cannonsAPI
import tk.martijn_heil.wac_core.craft.SailingModule.windFrom
import tk.martijn_heil.wac_core.craft.util.BoundingBox
import tk.martijn_heil.wac_core.craft.util.MassBlockUpdate
import tk.martijn_heil.wac_core.craft.util.MassBlockUpdate.RelightingStrategy.NEVER
import tk.martijn_heil.wac_core.craft.util.detect
import tk.martijn_heil.wac_core.craft.util.getRotatedLocation
import tk.martijn_heil.wac_core.craft.util.nms.CraftMassBlockUpdate
import tk.martijn_heil.wac_core.craft.vessel.HasRudder
import tk.martijn_heil.wac_core.craft.vessel.Ship
import tk.martijn_heil.wac_core.craft.vessel.SimpleRudder
import java.lang.Math.*
import java.util.*
import java.util.logging.Logger

/*
TODO:
    Fix: Ship filling up with water if under water line.
 */
open class SimpleSailingVessel protected constructor(private val logger: Logger, private val detectionLoc: Location) : Ship, HasSail, HasRudder, AutoCloseable {
    lateinit open protected var world: World
    open protected var allowedBlocks: Collection<Material> = Material.values().filter { it != AIR && it != WATER && it != STATIONARY_WATER && it != LAVA && it != STATIONARY_LAVA }
    lateinit open protected var rudder: SimpleRudder
    open protected var blocks: ArrayList<Block> = ArrayList()
    open protected var normalMaxSpeed: Int = 10000                     // in metres per hour
    open protected var speedPerSquareMetreOfSail: Double = 0.0         // in metres per hour
    open protected val accelerationPerUpdate: Int get() = normalMaxSpeed / 8 // in metres per hour
    open protected val decelerationPerUpdate: Int get() = normalMaxSpeed / 8 // in metres per hour
    open protected var updateInterval = 40                             // interval in Minecraft game ticks. A single tick is 50ms
    lateinit open protected var rotationPoint: Location
    open protected var updateTaskId = 0
    open protected var boundingBox = BoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    override val sails = ArrayList<SimpleSail>()
    override val minWindAngle = 40
    open protected var maxSize = 5000
    open protected var isUpdatingLocation = false
        set(value) {
            sails.forEach { it.isUpdatingLocation = value }
            rudder.isUpdatingLocation = value
            field = value
        }

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


    override val onBoard: List<Player>
        get() = world.players.filter { isOnBoard(it) }

    override var speed: Double = 0.0 // in metres per second
        protected set(value) {
            field = value // important we set this first
            var angle = 360 - heading.toDouble() + 90
            if(angle > 360) angle -= 360
            val radians = toRadians(angle)
            increaseX = round(0.0 + (distancePerUpdate * cos(radians))).toInt()
            increaseZ = round(0.0 - (distancePerUpdate * sin(radians))).toInt()
        }

    open protected val onBoardEntities: Collection<Entity>
        get() {
            val widthX = (boundingBox.maxX - boundingBox.minX)
            val widthY = (boundingBox.maxY - boundingBox.minY)
            val widthZ = (boundingBox.maxZ - boundingBox.minZ)
            val centerX = boundingBox.minX + widthX / 2
            val centerY = boundingBox.minY + widthY / 2
            val centerZ = boundingBox.minZ + widthZ / 2
            // For some reason that filter is really required
            return world.getNearbyEntities(Location(world, centerX, centerY, centerZ), widthX, widthY, widthZ).filter { boundingBox.contains(it.location)}
        }

    override var heading: Int = 0
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

                for(i in 1..amount) {
                    rotate(rotation)
                }

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
                if(state is Sign && state.lines[0] == "[Ship]" && isPartOfShip(e.clickedBlock)) {
                    e.isCancelled = true
                }
            }
        }

        @EventHandler(ignoreCancelled = true, priority = MONITOR)
        fun onBlockBreak(e: BlockBreakEvent) {
            if(isUpdatingLocation) e.block.drops.clear()
            else if(!isUpdatingLocation && boundingBox.contains(e.block)) blocks.remove(e.block)
        }

        @EventHandler(ignoreCancelled = true)
        fun onBlockPhysics(e: BlockPhysicsEvent) {
            if(isUpdatingLocation && boundingBox.contains(e.block)) e.isCancelled = true
        }

        @EventHandler(ignoreCancelled = true, priority = MONITOR)
        fun onBlockPlace(e: BlockPlaceEvent) {
            if(isUpdatingLocation) return

            for (modX in -1..1) {
                for(modY in -1..1) {
                    for(modZ in -1..1) {
                        if(isPartOfShip(world.getBlockAt(e.block.x + modX, e.block.y + modY, e.block.z + modZ))) {
                            val blockState = e.block.state
                            val blockData = blockState.data
                            addNewBlock(e.block)
                            if(blockData is Door) addNewBlock(world.getBlockAt(e.block.x, e.block.y + if(blockData.isTopHalf) -1 else 1, e.block.z))
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
        } catch(e: IllegalStateException) {
            return false
        }
        return true
    }

    open protected fun init() {
        try {
            // Detect vessel
            try {
                logger.info("Detecting sailing vessel at " + detectionLoc.x + "x " + detectionLoc.y + "y " + detectionLoc.z + "z")
                blocks = ArrayList(detect(detectionLoc, allowedBlocks, maxSize))
            } catch(e: Exception) {
                logger.info("Failed to detect sailing vessel: " + (e.message ?: "unknown error"))
                throw IllegalStateException(e.message)
            }


            // Detect BoundingBox
            val first = blocks.first()
            boundingBox.minX = first.x.toDouble()
            boundingBox.maxX = first.x.toDouble()
            boundingBox.minY = first.y.toDouble()
            boundingBox.maxY = first.y.toDouble()
            boundingBox.maxZ = first.z.toDouble()
            boundingBox.minZ = first.z.toDouble()

            blocks.forEach {
                if (it.x < boundingBox.minX) boundingBox.minX = it.x.toDouble()
                if (it.x > boundingBox.maxX) boundingBox.maxX = it.x.toDouble()
                if (it.y < boundingBox.minY) boundingBox.minY = it.y.toDouble()
                if (it.y > boundingBox.maxY) boundingBox.maxY = it.y.toDouble()
                if (it.z < boundingBox.minZ) boundingBox.minZ = it.z.toDouble()
                if (it.z > boundingBox.maxZ) boundingBox.maxZ = it.z.toDouble()
            }

            logger.info("Detected boundingBox: \n" +
                            "minX: " + boundingBox.minX.toInt() + "\n" +
                            "maxX: " + boundingBox.maxX.toInt() + "\n" +
                            "\n" +
                            "minY: " + boundingBox.minY.toInt() + "\n" +
                            "maxY: " + boundingBox.maxY.toInt() + "\n" +
                            "\n" +
                            "minZ: " + boundingBox.minZ.toInt() + "\n" +
                            "maxZ: " + boundingBox.maxZ.toInt()
            )


            val signs = blocks.map { it.state }.filter { it is Sign }.map { it as Sign }

            // Detect rudder
            val rudderSign = signs.find { it.lines[0] == "[Rudder]" } ?: throw IllegalStateException("No rudder found.")
            logger.info("Found rudder sign at " + rudderSign.x + " " + rudderSign.y + " " + rudderSign.z)
            rudder = SimpleRudder(rudderSign)
            heading = rudder.course
            rudder.onChangeCourseCallback = { onChangeCourseCallback(it) }

            // Detect sails
            signs.filter { it.lines[0] == "[Sail]" }.forEach {
                logger.fine("Found sail sign at " + it.x + " " + it.y + " " + it.z)
                sails.add(SimpleSail(it))
            }
            if (sails.isEmpty()) throw IllegalStateException("No sails found.")
            var maxSailSurfaceArea = 0
            sails.forEach { maxSailSurfaceArea += it.maxSurfaceArea }
            speedPerSquareMetreOfSail = normalMaxSpeed / maxSailSurfaceArea.toDouble()

            // Detect rotation point
            val tmpCentrePoint = signs.find { it.lines[0] == "[RotationPoint]" }
            if(tmpCentrePoint == null) {
                logger.warning("Could not detect rotation point")
                throw IllegalStateException("Could not detect rotation point.")
            }
            logger.info("Found rotation point at " + tmpCentrePoint.x + " " + tmpCentrePoint.y + " " + tmpCentrePoint.z)
            rotationPoint = tmpCentrePoint.location

            val updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
                try {
                    update()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }, 0, updateInterval.toLong())
            if (updateTaskId == -1) { throw Exception("Could not schedule Bukkit task.") }

            Bukkit.getPluginManager().registerEvents(listener, WacCore.plugin)


        } catch(t: Throwable) {
            sails.forEach { it.isHoisted = !it.isHoisted }
            throw t
        }
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

        move(increaseX, increaseZ)
    }

    private fun rotate(rotation: Rotation) {
        isUpdatingLocation = true

        fun getNewLocation(loc: Location): Location = getRotatedLocation(rotationPoint, rotation, loc)

        fun setBlockStateFast(fromState: BlockState, x: Int, y: Int, z: Int, massBlockUpdate: MassBlockUpdate) {
            val materialData = fromState.data
            if(materialData is Directional) {
                if(materialData is Attachable && fromState.type != LADDER) {
                    when (rotation) {
                        CLOCKWISE -> {
                            when (materialData.facing) {
                                NORTH -> materialData.setFacingDirection(EAST)
                                EAST -> materialData.setFacingDirection(SOUTH)
                                SOUTH -> materialData.setFacingDirection(WEST)
                                WEST -> materialData.setFacingDirection(NORTH)
                            }
                        }

                        ANTICLOCKWISE -> {
                            when (materialData.facing) {
                                NORTH -> materialData.setFacingDirection(WEST)
                                EAST -> materialData.setFacingDirection(NORTH)
                                SOUTH -> materialData.setFacingDirection(EAST)
                                WEST -> materialData.setFacingDirection(SOUTH)
                            }
                        }
                    }
                } else {
                    when (rotation) {
                        CLOCKWISE -> {
                            when (materialData.facing) {
                                NORTH -> materialData.setFacingDirection(WEST)
                                EAST -> materialData.setFacingDirection(NORTH)
                                SOUTH -> materialData.setFacingDirection(EAST)
                                WEST -> materialData.setFacingDirection(SOUTH)
                            }
                        }

                        ANTICLOCKWISE -> {
                            when (materialData.facing) {
                                NORTH -> materialData.setFacingDirection(EAST)
                                EAST -> materialData.setFacingDirection(SOUTH)
                                SOUTH -> materialData.setFacingDirection(WEST)
                                WEST -> materialData.setFacingDirection(NORTH)
                            }
                        }
                    }
                }

                fromState.data = materialData
            }

            massBlockUpdate.setBlockState(x, y, z, fromState)
        }

        val torches = ArrayList<BlockState>()
        val oldBlockStates = ArrayList<BlockState>(blocks.size)
        for (b in blocks) {
            val newBlock = world.getBlockAt(getNewLocation(b.location))
            if (newBlock.type.isSolid && !isPartOfShip(newBlock)) { // Collision
                // Notify everyone on board
                onBoard.forEach {
                    it.sendMessage("Could not rotate ship. Ship is obstructed by "
                            + newBlock.type.toString() + " at " + newBlock.x + ", " + newBlock.y + ", " + newBlock.z)
                }
                isUpdatingLocation = false
                throw IllegalStateException()
            }
            if(b.type == TORCH) torches.add(b.state) else oldBlockStates.add(b.state)
        }


        val onBoardEntities = onBoardEntities
        val cannons = ArrayList<Cannon>()
        onBoardEntities.filter { it is Player }.forEach { p ->
            cannonsAPI.getCannons(oldBlockStates.map { it.location }, p.uniqueId).forEach { cannons.add(it) }
        }


        blocks.clear()
        val massBlockUpdate: MassBlockUpdate = CraftMassBlockUpdate(plugin, world)
        massBlockUpdate.relightingStrategy = NEVER

        for (s in oldBlockStates) {
            val newBlock = world.getBlockAt(getNewLocation(s.location))
            setBlockStateFast(s, newBlock.x, newBlock.y, newBlock.z, massBlockUpdate)
            blocks.add(newBlock)
        }

        torches.forEach {
            val newBlock = world.getBlockAt(getNewLocation(it.location))
            setBlockStateFast(it, newBlock.x, newBlock.y, newBlock.z, massBlockUpdate)
            blocks.add(newBlock)
        }

        onBoardEntities.forEach {
            val loc = it.location
            val newLoc = getNewLocation(it.location)

            newLoc.yaw = if(rotation == CLOCKWISE) loc.yaw + 90 else loc.yaw - 90
            if(newLoc.yaw < -179) newLoc.yaw += 360
            if(newLoc.yaw > 180) newLoc.yaw -= 360
            newLoc.pitch = loc.pitch

            // Correction for teleport behaviour
            when(rotation) {
                CLOCKWISE -> newLoc.x += 1
                ANTICLOCKWISE -> newLoc.z += 1
            }

            it.teleport(newLoc, PlayerTeleportEvent.TeleportCause.PLUGIN)
        }

        // Update bounding box
        val first = getNewLocation(Location(world, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ))
        val second = getNewLocation(Location(world, boundingBox.minX, boundingBox.minY, boundingBox.minZ))
        boundingBox.minX = first.x
        boundingBox.maxX = first.x
        boundingBox.minZ = first.z
        boundingBox.maxZ = first.z
        if(second.x.toInt() < boundingBox.minX) boundingBox.minX = second.x
        if(second.x.toInt() > boundingBox.maxX) boundingBox.maxX = second.x
        if(second.z.toInt() < boundingBox.minZ) boundingBox.minZ = second.z
        if(second.z.toInt() > boundingBox.maxZ) boundingBox.maxZ = second.z

        cannons.forEach { if(rotation == CLOCKWISE) it.rotateRight(rotationPoint.toVector()) else it.rotateLeft(rotationPoint.toVector()) }

        for (s in oldBlockStates) {
            if(!isPartOfShip(s.block)) {
                if (s.y < world.seaLevel) massBlockUpdate.setBlock(s.x, s.y, s.z, WATER.id) else massBlockUpdate.setBlock(s.x, s.y, s.z, AIR.id)
            }
        }

        for (s in torches) {
            if(!isPartOfShip(s.block)) {
                if (s.y < world.seaLevel) massBlockUpdate.setBlock(s.x, s.y, s.z, WATER.id) else massBlockUpdate.setBlock(s.x, s.y, s.z, AIR.id)
            }
        }

        massBlockUpdate.notifyClients()
        sails.forEach { it.updateLocationRotated(rotationPoint, rotation) }
        rudder.updateLocationRotated(rotationPoint, rotation)
        isUpdatingLocation = false
    }


    private fun isOnBoard(e: Entity): Boolean = boundingBox.contains(e.location)//isPartOfShip(e.location.block) ||
//            isPartOfShip(e.world.getBlockAt(e.location.x.toInt(), e.location.y.toInt() - 1, e.location.z.toInt())) ||
//            isPartOfShip(e.world.getBlockAt(e.location.x.toInt(), e.location.y.toInt() - 2, e.location.z.toInt())) ||
//            isPartOfShip(e.world.getBlockAt(e.location.x.toInt(), e.location.y.toInt() - 3, e.location.z.toInt()))


    private fun move(relativeX: Int, relativeZ: Int) {
        if (relativeX == 0 && relativeZ == 0) return
        isUpdatingLocation = true

        val torches = ArrayList<BlockState>()
        val oldBlockStates = ArrayList<BlockState>(blocks.size)
        for (b in blocks) {
            val newBlock = world.getBlockAt(b.x + relativeX, b.y, b.z + relativeZ)
            if (newBlock.type.isSolid && !isPartOfShip(newBlock)) { // Collision
                // Notify everyone on board
                onBoard.forEach {
                    it.sendMessage("Ship's path is obstructed by " + newBlock.type.toString() +
                            " at " + newBlock.x + ", " + newBlock.y + ", " + newBlock.z)
                }
                return
            }
            if(b.type == TORCH) torches.add(b.state) else oldBlockStates.add(b.state)
        }

        val onBoardEntities = onBoardEntities
//        onBoardEntities.forEach {
//            val loc = it.location
//            loc.y -= 1
//            if(it is Player && world.getBlockAt(loc).type.isSolid) it.sendBlockChange(loc, GLASS, 0)
//        }

        val cannons = ArrayList<Cannon>()
        onBoardEntities.filter { it is Player }.forEach { p -> cannonsAPI.getCannons(oldBlockStates.map { it.location }, p.uniqueId).forEach { cannons.add(it) } }

        blocks.clear()
        val massBlockUpdate: MassBlockUpdate = CraftMassBlockUpdate(plugin, world)
        massBlockUpdate.relightingStrategy = NEVER

        for (s in oldBlockStates) {
            val newBlock = world.getBlockAt(s.x + relativeX, s.y, s.z + relativeZ)
            massBlockUpdate.setBlockState(newBlock.x, newBlock.y, newBlock.z, s)
            blocks.add(newBlock)
        }

        torches.forEach {
            val newBlock = world.getBlockAt(it.x + relativeX, it.y, it.z + relativeZ)
            massBlockUpdate.setBlockState(newBlock.x, newBlock.y, newBlock.z, it)
            blocks.add(newBlock)
        }

        onBoardEntities.forEach {
            val newLoc = it.location
            newLoc.x += relativeX
            newLoc.z += relativeZ
            val blockState = newLoc.block.state
            val blockData = blockState.data
            if (blockData is Ladder) { // This is to prevent players getting stuck in ladders
                val amount = 0.05
                when (blockData.facing) {
                    SOUTH -> if (newLoc.z < newLoc.z + amount) newLoc.z += amount
                    WEST -> if (newLoc.x > newLoc.x - amount) newLoc.x -= amount
                    NORTH -> if (newLoc.z > newLoc.z - amount) newLoc.z -= amount
                    EAST -> if (newLoc.x < newLoc.x + amount) newLoc.x += amount
                }
            }
            it.teleport(newLoc, PlayerTeleportEvent.TeleportCause.PLUGIN)
        }

        boundingBox.minX += relativeX
        boundingBox.maxX += relativeX
        boundingBox.minZ += relativeZ
        boundingBox.maxZ += relativeZ

        rotationPoint.x += relativeX
        rotationPoint.z += relativeZ

        cannons.forEach { it.move(Vector(relativeX, 0, relativeZ)) }

        for (s in oldBlockStates) {
            if (!isPartOfShip(s.block)) {
                if (s.y < world.seaLevel) massBlockUpdate.setBlock(s.x, s.y, s.z, WATER.id) else massBlockUpdate.setBlock(s.x, s.y, s.z, AIR.id)
            }
        }

        for(s in torches) {
            if (!isPartOfShip(s.block)) {
                if (s.y < world.seaLevel) massBlockUpdate.setBlock(s.x, s.y, s.z, WATER.id) else massBlockUpdate.setBlock(s.x, s.y, s.z, AIR.id)
            }
        }

        massBlockUpdate.notifyClients()
        sails.forEach { it.updateLocation(relativeX, relativeZ) }
        rudder.updateLocation(relativeX, relativeZ)
        isUpdatingLocation = false
    }

    fun addNewBlock(b: Block) {
        blocks.add(b)
        if(b.x > boundingBox.maxX) boundingBox.maxX = b.x.toDouble()
        if(b.x < boundingBox.minX) boundingBox.maxX = b.x.toDouble()
        if(b.y > boundingBox.maxY) boundingBox.maxY = b.y.toDouble()
        if(b.y < boundingBox.minY) boundingBox.minY = b.y.toDouble()
        if(b.z > boundingBox.maxZ) boundingBox.maxZ = b.z.toDouble()
        if(b.z < boundingBox.minZ) boundingBox.minZ = b.z.toDouble()
    }


    // Optimized function for checking if a certain block is part of a ship, fast.
    fun isPartOfShip(b: Block): Boolean = boundingBox.contains(b) && blocks.contains(b)

    override fun close() {
        HandlerList.unregisterAll(listener)
        sails.forEach { it.close() }
        rudder.close()
        Bukkit.getScheduler().cancelTask(updateTaskId)
    }

    companion object {
        fun detect(logger: Logger, detectionLoc: Location): SimpleSailingVessel {
            val ship = SimpleSailingVessel(logger, detectionLoc)
            ship.init()
            return ship
        }
    }
}