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

package tk.martijn_heil.wac_core.craft

import at.pavlov.cannons.API.CannonsAPI
import at.pavlov.cannons.Cannons
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import tk.martijn_heil.wac_core.WacCore
import tk.martijn_heil.wac_core.craft.vessel.sail.SimpleSailingVessel
import java.util.*
import java.util.logging.LogRecord
import java.util.logging.Logger


object SailingModule : AutoCloseable {
    private val random = Random()
    private val ships: MutableCollection<SimpleSailingVessel> = ArrayList()
    lateinit var cannonsAPI: CannonsAPI

    var windFrom: Int = 0 // Wind coming from x degrees
        private set

    fun init() {
        cannonsAPI = (Bukkit.getPluginManager().getPlugin("Cannons") as Cannons).cannonsAPI

        Bukkit.getScheduler().scheduleSyncRepeatingTask(WacCore.plugin, {
            windFrom = random.nextInt(360)
        }, 0, 72000) // Every hour

        Bukkit.getScheduler().scheduleSyncRepeatingTask(WacCore.plugin, {
            Bukkit.broadcastMessage(ChatColor.AQUA.toString() + "[Wind] " + ChatColor.GRAY + "The wind now blows from $windFrom°.")
        }, 0, 6000) // Every five minutes

        Bukkit.getPluginManager().registerEvents(SailingModuleListener, WacCore.plugin)
        //Bukkit.getPluginManager().registerEvents(TeleportFix(WacCore.plugin), WacCore.plugin)
        //Bukkit.getPluginManager().registerEvents(TeleportFix2(WacCore.plugin), WacCore.plugin)
    }

    override fun close() {
        ships.forEach { it.close() }
    }

    private object SailingModuleListener : Listener {
        @EventHandler(ignoreCancelled = true, priority = MONITOR)
        fun onPlayerInteract(e: PlayerInteractEvent) {
            if(e.clickedBlock != null &&
                    (e.clickedBlock.type == Material.SIGN ||
                            e.clickedBlock.type == Material.SIGN_POST ||
                            e.clickedBlock.type == Material.WALL_SIGN) && (e.clickedBlock.state as Sign).lines[0] == "[Ship]") {
                try {
                    ships.add(SimpleSailingVessel(WacCore.logger, e.clickedBlock.location))
                } catch(ex: IllegalStateException) {
                    e.player.sendMessage(ChatColor.RED.toString() + "Error: " + ex.message)
                } catch(ex: Exception) {
                    e.player.sendMessage(ChatColor.RED.toString() + "An internal server error occurred." + ex.message)
                    ex.printStackTrace()
                }
            }
        }
    }

    private object SailingModuleLogger : Logger("SailingModuleLogger", null) {
        private val target = WacCore.logger
        override fun log(record: LogRecord?) {
            if(record != null) {
                record.message = " [SailingModule] " + record.message
                target.log(record)
            }
        }
    }
}