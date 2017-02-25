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

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack


class InventoryListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onDragAndDrop(e: InventoryClickEvent) {
        // All drag & drop actions.

        if(e.inventory == e.whoClicked.inventory && e.cursor != null && e.whoClicked is Player) {
            if(e.click == ClickType.RIGHT) { // Player tries to drop a single item of the item stack on his cursor into his inventory.
                val tmpItemStack = e.cursor.clone() // Make a temporary copy of the ItemStack, and set it's amount to 1
                tmpItemStack.amount = 1             // As the player only attempts to drop a single item of the item stack into his inventory.
                val result = canPlayerReceiveItemStack(e.whoClicked as Player, tmpItemStack)
                if(!result.first) {
                    e.isCancelled = true
                    if(result.second != null) {
                        e.whoClicked.sendMessage(result.second)
                    }
                }
            } else if(e.click == ClickType.LEFT) { // Player tries to drop the entire stack on his cursor into his inventory.
                val result = canPlayerReceiveItemStack(e.whoClicked as Player, e.cursor)
                if(!result.first) {
                    e.isCancelled = true
                    if(result.second != null) {
                        e.whoClicked.sendMessage(result.second)
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryDrag(e: InventoryDragEvent) {
        // Drag/distribution actions.
        if(e.whoClicked is Player) {
            if(e.inventory == e.whoClicked.inventory) {
                e.newItems.forEach {
                    val result = canPlayerReceiveItemStack(e.whoClicked as Player, it.value)
                    if (!result.first) {
                        e.isCancelled = true
                        if (result.second != null) {
                            e.whoClicked.sendMessage(result.second)
                        }
                    }
                }
            } else if (e.inventory != e.whoClicked.inventory) {
                e.newItems.forEach {
                    val result = canPlayerStoreItemStack(e.whoClicked as Player, it.value)
                    if (!result.first) {
                        e.isCancelled = true
                        if (result.second != null) {
                            e.whoClicked.sendMessage(result.second)
                        }
                    }
                }
            }
        }

        if (e.inventory == e.whoClicked.inventory && e.whoClicked is Player) {
            e.newItems.forEach {
                val result = canPlayerReceiveItemStack(e.whoClicked as Player, it.value)
                if (!result.first) {
                    e.isCancelled = true
                    if (result.second != null) {
                        e.whoClicked.sendMessage(result.second)
                    }
                }
            }
        }
    }

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun onPlayerShiftClickItemIntoPlayerInventory(e: InventoryClickEvent) {
        // If the player is trying to shift click TNT into their inventory.
        if (e.inventory != e.whoClicked.inventory && (e.click == ClickType.SHIFT_LEFT || e.click == ClickType.SHIFT_RIGHT)
                && e.whoClicked is Player) {

            val result = canPlayerReceiveItemStack(e.whoClicked as Player, e.currentItem)
            if (!result.first) {
                e.isCancelled = true

                if (result.second != null) {
                    e.whoClicked.sendMessage(result.second)
                }
            }
        }
    }

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun onPlayerPickupItem(e: PlayerPickupItemEvent) {
        if(!canPlayerReceiveItemStack(e.player, e.item.itemStack).first) e.isCancelled = true
    }


    /**
     * @returns A pair, True if the player can receive the item stack, and possibly a reason.
     */
    fun canPlayerReceiveItemStack(p: Player, itemStack: ItemStack): Pair<Boolean, String?> {
        return Pair(true, null)
        // TODO implement some actual rules.
    }

    fun canPlayerDropItemStack(p: Player, itemStack: ItemStack): Pair<Boolean, String?> {
        return Pair(true, null)
    }

    fun canPlayerStoreItemStack(p: Player, itemStack: ItemStack): Pair<Boolean, String?> {
        return Pair(true, null)
    }

    fun canPlayerCraftWithItemSTack(p: Player, itemStack: ItemStack): Pair<Boolean, String?> {
        return Pair(true, null)
    }

    fun canPlayerCraftItemStack(p: Player, itemStack: ItemStack): Pair<Boolean, String?> {
        return Pair(true, null)
    }
}