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

package tk.martijn_heil.wac_core.gamemodeswitching

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tk.martijn_heil.wac_core.WacCore
import java.util.*
import java.util.logging.Logger


object GameModeSwitchingModule {
    private val switchingStates = HashMap<UUID, Boolean>()
    lateinit private var plugin: Plugin
    lateinit private var logger: Logger

    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger

        logger.info("Initializing GameModeSwitchingModule..")

        plugin.server.pluginManager.registerEvents(GameModeSwitchingListener, plugin)
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            Bukkit.getOnlinePlayers().forEach {
                if(isGameModeSwitching(it)) it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false), true)
            }
        }, 0, 20)
    }

    fun setGameModeSwitching(p: Player, value: Boolean) = switchingStates.put(p.uniqueId, value)
    fun isGameModeSwitching(p: Player) = switchingStates[p.uniqueId] ?: false


    private object GameModeSwitchingListener : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onPlayerMove(e: PlayerMoveEvent) {
            val isSwitching = switchingStates[e.player.uniqueId] ?: false
            if(isSwitching) e.isCancelled = true
        }

        @EventHandler(ignoreCancelled = true)
        fun onPlayerGameModeChange(e: PlayerGameModeChangeEvent) {
            val p = e.player

            if(!e.player.hasPermission(WacCore.Permission.BYPASS__GAMEMODE_SWITCH_PENALTY.toString()) && !isGameModeSwitching(p)) {
                setGameModeSwitching(p, true)
                e.player.sendMessage(ChatColor.RED.toString() + "Je bent nu aan het wisselen naar " + e.newGameMode + " mode, dit duurt 30 seconden waarin je kwetsbaar bent.")

                when {
                    (e.newGameMode == GameMode.SURVIVAL || e.newGameMode == GameMode.ADVENTURE) -> {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(WacCore.plugin, {
                            setGameModeSwitching(p, false)
                        }, 600)
                    }

                    (e.newGameMode == GameMode.CREATIVE || e.newGameMode == GameMode.SPECTATOR) -> {
                        e.isCancelled = true
                        Bukkit.getScheduler().scheduleSyncDelayedTask(WacCore.plugin, {
                            // Note: the order here is important! First set the player's gamemode, then set the switching state to false.
                            e.player.gameMode = e.newGameMode
                            setGameModeSwitching(p, false)
                        }, 600)
                    }
                }
            }
        }
    }
}