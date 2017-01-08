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

import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.SkeletonHorse
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent


class GeneralListener() : Listener {

    /**
     * Undead are not targeted by mobs.
     */
    @EventHandler(ignoreCancelled = true)
    fun onEntityTarget(e: EntityTargetEvent) {
        if (e.target is Player && WacPlayer(e.target as Player).kingdom == Kingdom.UNDEAD ) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerLogin(e: PlayerJoinEvent) {
        WacCore.logger.fine("Ensuring that " + e.player.name + " is present in database..")
        ensurePresenceInDatabase(e.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onCreateSpawn(e: CreatureSpawnEvent) {
        if(e.spawnReason == CreatureSpawnEvent.SpawnReason.LIGHTNING && e.entity is SkeletonHorse) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEnderPearl(e: PlayerInteractEvent) {
        if(e.action == Action.RIGHT_CLICK_AIR || e.action == Action.RIGHT_CLICK_BLOCK
                && e.player.inventory.itemInMainHand.type == Material.ENDER_PEARL) {
            e.isCancelled = true
            e.player.sendMessage(ChatColor.RED.toString() + "Ender pearls zijn uitgeschakeld!")
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onPlayerGameModeChange(e: PlayerGameModeChangeEvent) {
        if(e.newGameMode == GameMode.SPECTATOR && !e.player.hasPermission("wac-core.gamemode.spectator")) {
            e.isCancelled = true
            e.player.sendMessage(ChatColor.RED.toString() + "Jij mag niet in spectator mode!")
        }
    }
}
