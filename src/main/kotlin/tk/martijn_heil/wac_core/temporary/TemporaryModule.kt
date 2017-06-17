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

package tk.martijn_heil.wac_core.temporary

import org.bukkit.ChatColor
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.lang.Math.pow
import java.lang.Math.sqrt
import java.util.*


object TemporaryModule {
    lateinit private var plugin: Plugin
    lateinit private var server: Server
    private val centerLoc = Pair(8500, 4500)
    private val radius = 5000
    private val playersInZone = HashMap<Player, Boolean>()
    private val TIME_NIGHT: Long = 18000

    fun init(plugin: Plugin) {
        this.plugin = plugin
        this.server = plugin.server

        server.scheduler.scheduleSyncRepeatingTask(plugin, {
            server.onlinePlayers.forEach {
                val loc = it.location
                if(distance(centerLoc, Pair(loc.x.toInt(), loc.z.toInt())) <= radius) {
                    if(!playersInZone.contains(it)) { // The player entered the zone.
                        if(it.playerTime == it.world.time) { // The player doesn't have a custom player time.
                            playersInZone.put(it, true)
                            it.setPlayerTime(TIME_NIGHT, false)
                        } else {
                            playersInZone.put(it, false)
                        }
                        it.sendMessage(ChatColor.RED.toString() + "Je gaat nu de geïnfecteerde zone binnen..")
                    }
                } else if(playersInZone.contains(it)) { // The player left the zone.
                    val resetPlayerTime = playersInZone.remove(it)
                    if(resetPlayerTime != null && resetPlayerTime) {
                        it.resetPlayerTime()
                    }
                    it.sendMessage(ChatColor.GREEN.toString() + "Je verlaat nu de geïnfecteerde zone.")
                }
            }
        }, 0, 20)
    }

    fun distance(firstLoc: Pair<Int, Int>, secondLoc: Pair<Int, Int>) = sqrt(pow(firstLoc.first.toDouble() -
            secondLoc.first.toDouble(), 2.0) + pow(firstLoc.second.toDouble() + secondLoc.second.toDouble(), 2.0))
}