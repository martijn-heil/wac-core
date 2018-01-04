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

package tk.martijn_heil.wac_core.craft.util

import at.pavlov.cannons.cannon.Cannon
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Material.AIR
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent
import tk.martijn_heil.wac_core.craft.Rotation
import tk.martijn_heil.wac_core.craft.Rotation.CLOCKWISE
import tk.martijn_heil.wac_core.craft.SailingModule
import java.util.*


fun detect(startLocation: Location, allowedBlocks: Collection<Material>, maxSize: Int): Collection<Block> {
    val blocks = ArrayList<Block>()

    val s = Stack<Location>()
    s.push(startLocation)

    while(s.count() > 0) {
        if(blocks.size >= maxSize) throw Exception("Maximum detection size ($maxSize) exceeded.")

        val loc = s.pop()
        if(blocks.contains(loc.block) || !allowedBlocks.contains(loc.block.type)) continue

        blocks.add(loc.block)

        for (modX in -1..1) {
            for(modY in -1..1) {
                for(modZ in -1..1) {
                    s.push(Location(loc.world, loc.x + modX, loc.y + modY, loc.z + modZ))
                }
            }
        }
    }

    if(blocks.isEmpty()) throw Exception("Could not detect any allowed blocks.")
    return blocks
}

fun getRotatedLocation(rotationPoint: Location, rotation: Rotation, loc: Location): Location {
    val newRelativeX = if (rotation == CLOCKWISE) rotationPoint.z - loc.z else -(rotationPoint.z - loc.z)
    val newRelativeZ = if(rotation == CLOCKWISE) -(rotationPoint.x - loc.x) else rotationPoint.x - loc.x
    return Location(loc.world, rotationPoint.x + newRelativeX, loc.y, rotationPoint.z + newRelativeZ)
}

fun getRotatedLocation(output: Location, rotationPoint: Location, rotation: Rotation, loc: Location) {
    val newRelativeX = if (rotation == CLOCKWISE) rotationPoint.z - loc.z else -(rotationPoint.z - loc.z)
    val newRelativeZ = if(rotation == CLOCKWISE) -(rotationPoint.x - loc.x) else rotationPoint.x - loc.x
    output.x += newRelativeX
    output.y += loc.y
    output.z += newRelativeZ
}


fun detectAirBlocksBelowSeaLevel(world: World, box: BoundingBox): Collection<BlockPos> {
    val minX = box.minX.toInt()
    val maxX = box.maxX.toInt()
    val minY = box.minY.toInt()
    val minZ = box.minZ.toInt()
    val maxZ = box.maxZ.toInt()
    if(minY > world.seaLevel) return emptyList()

    val list = ArrayList<BlockPos>()
    for(x in minX..maxX) {
        for(y in minY..world.seaLevel) {
            for(z in minZ..maxZ) {
                if(world.getBlockAt(x, y, z).type == AIR) list.add(BlockPos(x, y, z))
            }
        }
    }
    return list
}

fun detectCannons(locations: List<Location>): Collection<Cannon> {
    val cannons = ArrayList<Cannon>()
    Bukkit.getOnlinePlayers().forEach { p -> cannons.addAll(SailingModule.cannonsAPI.getCannons(locations, p.uniqueId)) }
    return cannons
}

fun rotateEntities(entities: Collection<Entity>, rotationPoint: Location, rotation: Rotation) {
    entities.forEach {
        val loc = it.location
        val newLoc = getRotatedLocation(rotationPoint, rotation, loc)

        newLoc.yaw = if (rotation == Rotation.CLOCKWISE) loc.yaw + 90 else loc.yaw - 90
        if (newLoc.yaw < -179) newLoc.yaw += 360
        if (newLoc.yaw > 180) newLoc.yaw -= 360
        newLoc.pitch = loc.pitch

        // Correction for teleport behaviour
        when (rotation) {
            Rotation.CLOCKWISE -> newLoc.x += 1
            Rotation.ANTICLOCKWISE -> newLoc.z += 1
        }

        it.teleport(newLoc, PlayerTeleportEvent.TeleportCause.PLUGIN)
    }
}