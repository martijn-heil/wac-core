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
 
package tk.martijn_heil.wac_core

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerMoveEvent

 
class GameModeSwitchingListener : Listener {
     @EventHandler(ignoreCancelled = true)
     fun onPlayerMove(e: PlayerMoveEvent) {
         val wp = WacPlayer.valueOf(e.player)
         if(wp.isGameModeSwitching) e.isCancelled = true
     }
 
     @EventHandler(ignoreCancelled = true)
     fun onPlayerGameModeChange(e: PlayerGameModeChangeEvent) {
        val p = WacPlayer.valueOf(e.player)
         if(p.isGameModeSwitching) return

        if(!e.player.hasPermission(WacCore.Permission.BYPASS__GAMEMODE_SWITCH_PENALTY.toString()) && !p.isGameModeSwitching) {
            p.isGameModeSwitching = true
            e.player.sendMessage(ChatColor.RED.toString() + "Je bent nu aan het wisselen naar " + e.newGameMode + " mode, dit duurt 30 seconden waarin je kwetsbaar bent.")

            when {
                (e.newGameMode == GameMode.SURVIVAL || e.newGameMode == GameMode.ADVENTURE) -> {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(WacCore.plugin, {
                        p.isGameModeSwitching = false
                    }, 600)
                }

                (e.newGameMode == GameMode.CREATIVE || e.newGameMode == GameMode.SPECTATOR) -> {
                    e.isCancelled = true
                    Bukkit.getScheduler().scheduleSyncDelayedTask(WacCore.plugin, {
                        // Note: the order here is important! First set the player's gamemode, then set the switching state to false.
                        e.player.gameMode = e.newGameMode
                        p.isGameModeSwitching = false
                    }, 600)
                }
            }
        }
    }
 }
