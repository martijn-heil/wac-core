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

package tk.martijn_heil.wac_core.general

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Ocelot
import org.bukkit.entity.Tameable
import org.bukkit.entity.Wolf
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.logging.Logger


class GeneralListener(val logger: Logger, val plugin: Plugin) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerLogin(e: PlayerJoinEvent) {
        logger.fine("Ensuring that " + e.player.name + " is present in database..")
        //ensurePresenceInDatabase(chunkPropagateSkylightOcclusion.player)
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
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        e.player.location.world.getEntitiesByClasses(Ocelot::class.java, Wolf::class.java).forEach {
            if (it is Tameable && it.owner == e.player && !((it is Ocelot && it.isSitting) || (it is Wolf && it.isSitting))) {
                it.teleport(e.to)
            }
        }
    }
}
