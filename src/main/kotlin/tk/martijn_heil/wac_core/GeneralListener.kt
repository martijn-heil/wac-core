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

import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack


class GeneralListener() : Listener {

    /**
     * Undead are not targeted by mobs.
     */
    @EventHandler(ignoreCancelled = true)
    fun onEntityTarget(e: EntityTargetEvent) {
        if (e.target is Player && WacPlayer.valueOf(e.target as Player).kingdom == Kingdom.UNDEAD ) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerLogin(e: PlayerJoinEvent) {
        WacCore.logger.fine("Ensuring that " + e.player.name + " is present in database..")
        ensurePresenceInDatabase(e.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onCreatureSpawn(e: CreatureSpawnEvent) {
        if(e.spawnReason == CreatureSpawnEvent.SpawnReason.LIGHTNING && e.entity.type == EntityType.SKELETON_HORSE) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEnderPearl(e: PlayerTeleportEvent) {
        if(e.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            e.isCancelled = true
            e.player.sendMessage(ChatColor.RED.toString() + "Ender pearls zijn uitgeschakeld!")
            e.player.inventory.addItem(ItemStack(Material.ENDER_PEARL, 1))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerChorusFruitTeleport(e: PlayerTeleportEvent) {
        if(e.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onUndeadUseSoundWand(e: PlayerInteractEvent) {
        if((e.action == Action.RIGHT_CLICK_BLOCK || e.action == Action.RIGHT_CLICK_AIR) && WacPlayer.valueOf(e.player).kingdom == Kingdom.UNDEAD && e.player.inventory.itemInMainHand != null &&
                e.player.inventory.itemInMainHand.hasItemMeta() && e.player.inventory.itemInMainHand.itemMeta.hasDisplayName() &&
        e.player.inventory.itemInMainHand.itemMeta.displayName == ChatColor.GOLD.toString() + "SoundWand") {

            getPlayersInRadius(e.player.location, 200).forEach {
                it.playSound(it.location, Sound.ENTITY_ENDERDRAGON_DEATH, 10.0f, 1.0f)
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        e.player.location.world.getEntitiesByClasses(Ocelot::class.java, Wolf::class.java).forEach {
            if (it is Tameable && it.owner == e.player && !((it is Ocelot && it.isSitting) || (it is Wolf && it.isSitting))) {
                it.teleport(e.to)
            }
        }
    }
}
