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
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.plugin.Plugin
import tk.martijn_heil.wac_core.getDefaultWorld
import java.util.*


object TemporaryModule {
    lateinit private var plugin: Plugin
    lateinit private var server: Server
    private val centerLoc = Location(getDefaultWorld(), 8500.0, 1.0, 4500.0)
    private val radius = 2500
    private val playersInZone = ArrayList<Player>()
    private val TIME_NIGHT: Long = 18000

    fun init(plugin: Plugin) {
        this.plugin = plugin
        this.server = plugin.server

        server.scheduler.scheduleSyncRepeatingTask(plugin, {
            server.onlinePlayers.forEach {
                val loc = it.location
                loc.y = 1.0
                if(centerLoc.world == loc.world && centerLoc.distance(loc) <= radius) {
                    if(!playersInZone.contains(it)) { // The player entered the zone.
                        playersInZone.add(it)
                        it.setPlayerTime(TIME_NIGHT, false)
                        it.sendMessage(ChatColor.RED.toString() + "Je gaat nu de geïnfecteerde zone binnen..")
                    }
                } else if(playersInZone.contains(it)) { // The player left the zone.
                    playersInZone.remove(it)
                    it.resetPlayerTime()
                    it.sendMessage(ChatColor.GREEN.toString() + "Je verlaat nu de geïnfecteerde zone.")
                }
            }
        }, 0, 20)
        server.pluginManager.registerEvents(TemporaryModuleListener, plugin)
    }

    private object TemporaryModuleListener : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onMobSpawn(e: CreatureSpawnEvent) {
            if(e.spawnReason != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                val loc = e.entity.location
                loc.y = 1.0
                if(e.entity is Monster && centerLoc.world == loc.world && centerLoc.distance(loc) <= radius) {
                    e.isCancelled = true
                }
            }
        }
    }
}