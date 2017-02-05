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
 
 import org.bukkit.event.Listener
 import org.bukkit.event.EventHandler
 
 class GameModeSwitchingListener : Listener {
     @EventHandler(ignoredCancelled = true)
     fun onPlayerMove(e: PlayerMoveEvent) {
         wp = WacPlayer.valueOf(e.player)
         if(wp.isGameModeSwitching) e.isCancelled = true
     }
 }
