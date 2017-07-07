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

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import tk.martijn_heil.wac_core.craft.Rotation
import tk.martijn_heil.wac_core.craft.Rotation.CLOCKWISE
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