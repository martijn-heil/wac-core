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
import org.bukkit.Server
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import tk.martijn_heil.wac_core.craft.vessel.sail.Count
import tk.martijn_heil.wac_core.craft.vessel.sail.SimpleSailingVessel
import tk.martijn_heil.wac_core.craft.vessel.sail.Trireme
import tk.martijn_heil.wac_core.craft.vessel.sail.Unireme
import java.io.Closeable
import java.util.*
import java.util.logging.Logger


object SailingModule : AutoCloseable {
    lateinit private var logger: Logger
    lateinit private var plugin: Plugin
    private val server: Server by lazy { plugin.server }
    private val random = Random()
    private val ships: MutableCollection<SimpleSailingVessel> = ArrayList()
    val cannonsAPI: CannonsAPI by lazy { (Bukkit.getPluginManager().getPlugin("Cannons") as Cannons).cannonsAPI }

    var windFrom: Int = 0 // Wind coming from x degrees
        private set

    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger
        server.scheduler.scheduleSyncRepeatingTask(plugin, {
            windFrom = random.nextInt(360)
        }, 0, 72000) // Every hour

        server.scheduler.scheduleSyncRepeatingTask(plugin, {
            Bukkit.broadcastMessage(ChatColor.AQUA.toString() + "[Wind] " + ChatColor.GRAY + "The wind now blows from $windFrom°.")
        }, 0, 6000) // Every five minutes

        CraftManager.init()
    }

    override fun close() {
        CraftManager.close()
    }

    private object CraftManager : Closeable {
        val crafts: Collection<Craft> = ArrayList()
        fun init() {
            server.pluginManager.registerEvents(CraftManagerListener, plugin)
        }

        private object CraftManagerListener : Listener {
            @EventHandler(ignoreCancelled = true, priority = MONITOR)
            fun onPlayerInteract(e: PlayerInteractEvent) {
                if (e.clickedBlock != null &&
                        (e.clickedBlock.type == Material.SIGN ||
                                e.clickedBlock.type == Material.SIGN_POST ||
                                e.clickedBlock.type == Material.WALL_SIGN) && (e.clickedBlock.state as Sign).lines[0] == "[Craft]") {
                    val type = (e.clickedBlock.state as Sign).lines[1]
                    if (type == "") {
                        e.player.sendMessage(ChatColor.RED.toString() + "Error: Craft type not specified."); return
                    }


                    when (type) {
                        "Trireme" -> {
                            try {
                                ships.add(Trireme.detect(plugin, logger, e.clickedBlock.location))
                            } catch(ex: IllegalStateException) {
                                e.player.sendMessage(ChatColor.RED.toString() + "Error: " + ex.message)
                            } catch(ex: Exception) {
                                e.player.sendMessage(ChatColor.RED.toString() + "An internal server error occurred." + ex.message)
                                ex.printStackTrace()
                            }
                        }

                        "Unireme" -> {
                            try {
                                ships.add(Unireme.detect(plugin, logger, e.clickedBlock.location))
                            } catch(ex: IllegalStateException) {
                                e.player.sendMessage(ChatColor.RED.toString() + "Error: " + ex.message)
                            } catch(ex: Exception) {
                                e.player.sendMessage(ChatColor.RED.toString() + "An internal server error occurred." + ex.message)
                                ex.printStackTrace()
                            }
                        }

                        "Count" -> {
                            try {
                                ships.add(Count.detect(plugin, logger, e.clickedBlock.location))
                            } catch(ex: IllegalStateException) {
                                e.player.sendMessage(ChatColor.RED.toString() + "Error: " + ex.message)
                            } catch(ex: Exception) {
                                e.player.sendMessage(ChatColor.RED.toString() + "An internal server error occurred." + ex.message)
                                ex.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        override fun close() {
            crafts.forEach {
                if(it is Closeable) it.close()
            }
        }
    }
}