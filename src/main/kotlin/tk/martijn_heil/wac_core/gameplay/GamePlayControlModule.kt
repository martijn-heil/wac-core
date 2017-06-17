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

package tk.martijn_heil.wac_core.gameplay

import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Horse
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.logging.Logger


object GamePlayControlModule {
    lateinit private var plugin: Plugin
    lateinit private var logger: Logger

    val swimmingState = HashMap<UUID, Int>()
    val maxSwimmingDuration = 60


    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger

        plugin.server.pluginManager.registerEvents(GeneralGamePlayControlListener, plugin)

        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            plugin.server.onlinePlayers.forEach {
                it.inventory.forEach {
                    if(it != null) {
                        if(it.containsEnchantment(Enchantment.DEPTH_STRIDER)) it.removeEnchantment(Enchantment.DEPTH_STRIDER)
                        if(it.containsEnchantment(Enchantment.FROST_WALKER)) it.removeEnchantment(Enchantment.FROST_WALKER)
                    }
                }
            }
        }, 0L, 20L)

        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            plugin.server.onlinePlayers.forEach {
                val biome = it.location.block.biome

                if(!it.isDead) {
                    val timeInLiquid = swimmingState[it.uniqueId] ?: 0
                    fun getMessage(time: Int) = if(time != 1) ChatColor.RED.toString() + "Pas op! Als je over $time seconden nog niet uit het water bent, verdrink je!"
                                                else ChatColor.RED.toString() + "Pas op! Als je over $time seconde nog niet uit het water bent, verdrink je!"

                    val b = it.location.block

                    if(!(it.gameMode == GameMode.SPECTATOR || it.gameMode == GameMode.CREATIVE) && !it.isInvulnerable &&
                            (biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.FROZEN_OCEAN) &&
                            b.isLiquid && (b.getRelative(BlockFace.DOWN).isLiquid || b.getRelative(BlockFace.UP).isLiquid)) {

                        if (timeInLiquid >= maxSwimmingDuration) {
                            it.sendMessage(ChatColor.DARK_RED.toString() + "Je bent verdronken!")
                            it.health = 0.0
                        } else {
                            swimmingState.put(it.uniqueId, timeInLiquid + 1)

                            when (timeInLiquid) {
                                maxSwimmingDuration - 5 -> it.sendMessage(getMessage(5))
                                maxSwimmingDuration - 10 -> it.sendMessage(getMessage(10))
                                maxSwimmingDuration - 30 -> it.sendMessage(getMessage(30))
                            }
                        }
                    } else {
                        swimmingState.remove(it.uniqueId)
                    }
                }
            }
        }, 0, 20)

        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            plugin.server.worlds.forEach {
                it.getEntitiesByClass(Horse::class.java).forEach {
                    val attr = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                    if(attr.baseValue < 60) {
                        attr.baseValue = ThreadLocalRandom.current().nextInt(60, 80 + 1).toDouble()
                    }
                }
            }
        }, 0, 100)
    }

    private object GeneralGamePlayControlListener : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onVehicleMove(e: VehicleEnterEvent) {
            if(e.vehicle.type == EntityType.BOAT && e.entered.type == EntityType.PLAYER) e.isCancelled = true
        }

        @EventHandler(ignoreCancelled = true)
        fun onPlayerDeath(e: PlayerDeathEvent) {
            if((swimmingState[e.entity.uniqueId] ?: 0) >= maxSwimmingDuration) {
                e.deathMessage = e.entity.name + " drowned."
            }
        }

//        @EventHandler(ignoreCancelled = true)
//        fun onBlockPlace(e: BlockPlaceEvent) {
//            if((e.block.biome == Biome.OCEAN || e.block.biome == Biome.DEEP_OCEAN || e.block.biome == Biome.DEEP_OCEAN) &&
//                    !e.block.getRelative(BlockFace.DOWN).isLiquid && e.block.getRelative(BlockFace.UP).isLiquid && e.block.location.y <= e.block.world.seaLevel &&
//                    (e.player.gameMode == GameMode.SURVIVAL || e.player.gameMode == GameMode.ADVENTURE) &&
//                    !e.player.hasPermission(WacCore.Permission.BYPASS_OCEAN_BUILD_LIMITS.str))
//            {
//                e.isCancelled = true
//                e.player.sendMessage(ChatColor.RED.toString() + "Je kan hier geen blok plaatsen.")
//            }
//        }

//        @EventHandler(ignoreCancelled = true)
//        fun onBlockPlace(e: BlockPlaceEvent) {
//            if((e.block.biome == Biome.OCEAN || e.block.biome == Biome.DEEP_OCEAN || e.block.biome == Biome.FROZEN_OCEAN) &&
//                    (e.player.gameMode == GameMode.SURVIVAL || e.player.gameMode == GameMode.ADVENTURE) &&
//                    !e.player.hasPermission(WacCore.Permission.BYPASS_OCEAN_BUILD_LIMITS.str) &&
//                    e.block.location.y <= e.block.world.seaLevel) {
//                e.isCancelled = true
//                e.player.sendMessage(ChatColor.RED.toString() + "Je kan hier geen blok plaatsen.")
//            }
//        }
//
//        @EventHandler(ignoreCancelled = true)
//        fun onBlockBreak(e: BlockBreakEvent) {
//            if((e.block.biome == Biome.OCEAN || e.block.biome == Biome.DEEP_OCEAN || e.block.biome == Biome.FROZEN_OCEAN) &&
//                    (e.player.gameMode == GameMode.SURVIVAL || e.player.gameMode == GameMode.ADVENTURE) &&
//                    !e.player.hasPermission(WacCore.Permission.BYPASS_OCEAN_BUILD_LIMITS.str) &&
//                    e.block.location.y < e.block.world.seaLevel) {
//                e.isCancelled = true
//                e.player.sendMessage(ChatColor.RED.toString() + "Je kan hier geen blokken slopen.")
//            }
//        }
    }
}