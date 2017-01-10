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
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerToggleSneakEvent


class LongTermSneakListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onPlayerToggleSneak(e: PlayerToggleSneakEvent) {
        if(WacPlayer.valueOf(e.player).isLongTermSneaking) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if(WacPlayer.valueOf(e.player).isLongTermSneaking) e.player.isSneaking = true
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerRespawn(e: PlayerRespawnEvent) {
        if(WacPlayer.valueOf(e.player).isLongTermSneaking) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(WacCore.plugin, {
                e.player.isSneaking = true
            }, 20)
        }
    }
}