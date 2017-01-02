/*
 * MIT License
 *
 * Copyright (c) 2016 Martijn Heil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tk.martijn_heil.wac_core

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.Inventory


class MaxOfItemListener() : Listener {

    private val maxPerPlayerInventory: Map<Material, Int> = mapOf(
            Pair(Material.TNT, 1)
    )

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun onDragAndDrop(e: InventoryClickEvent) {
        // All drag & drop actions.

        // If the player tries to drag and drop a single item into their inventory, or if they drag and drop an entire stack into their inventory.
        // So, if the player has no TNT in his inventory yet, but tries to put an entire stack in at once, that is also prevented.
        if(e.clickedInventory == e.whoClicked.inventory && !e.whoClicked.hasPermission(WacCore.Permission.BYPASS_ITEMLIMIT.str)) {
            maxPerPlayerInventory.forEach {
                val m = it.key
                val maxAmount = it.value
                if (
                ((e.cursor.type == m && (e.cursor.amount + countItemsOfTypeInInventory(m, e.whoClicked.inventory) > maxAmount)) && (e.click == ClickType.LEFT))

                        || (e.cursor.type == m &&  e.click == ClickType.RIGHT && countItemsOfTypeInInventory(m, e.whoClicked.inventory) >= maxAmount)

                        || (e.click == ClickType.LEFT && e.cursor.amount > maxAmount && e.cursor.type == m)
                ) {
                    e.isCancelled = true
                    e.whoClicked.sendMessage(ChatColor.RED.toString() + getStringWithArgs(WacCore.messages,
                            arrayOf(maxAmount.toString(), m.toString()), "error.event.cancelled.inventory.moreOfItemThanAllowed"))
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryDrag(e: InventoryDragEvent) {
        // Drag/distribution actions.
        maxPerPlayerInventory.forEach {
            val m = it.key
            val maxAmount = it.value

            if(itemsPlacedByInventoryDragContain(m, e) && (e.whoClicked.inventory.contains(m, maxAmount) ||
                    (countItemsPlacedByInventoryDrag(e) + countItemsOfTypeInInventory(m, e.whoClicked.inventory)) > maxAmount)) {
                e.isCancelled = true
                e.whoClicked.sendMessage(ChatColor.RED.toString() + getStringWithArgs(WacCore.messages,
                        arrayOf(maxAmount.toString(), m.toString()), "error.event.cancelled.inventory.moreOfItemThanAllowed"))
            }
        }
    }

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun onPlayerShiftClickItemIntoPlayerInventory(e: InventoryClickEvent) {
        maxPerPlayerInventory.forEach {
            val m = it.key
            val maxAmount = it.value

            // If the player is trying to shift click TNT into their inventory.
            if(e.clickedInventory != e.whoClicked.inventory && (e.click == ClickType.SHIFT_LEFT || e.click == ClickType.SHIFT_RIGHT)
                    && e.currentItem.type == m && (e.whoClicked.inventory.contains(m, maxAmount) || (e.currentItem.amount + e.currentItem.amount) > maxAmount)
                    && !e.whoClicked.hasPermission(WacCore.Permission.BYPASS_ITEMLIMIT.str)) {
                e.isCancelled = true
                e.whoClicked.sendMessage(ChatColor.RED.toString() + getStringWithArgs(WacCore.messages,
                        arrayOf(maxAmount.toString(), m.toString()), "error.event.cancelled.inventory.moreOfItemThanAllowed"))
            }
        }
    }

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun onPlayerPickupItem(e: PlayerPickupItemEvent) {
        maxPerPlayerInventory.forEach {
            val m = it.key
            val maxAmount = it.value

            // If the player tries to pick up this item type from the ground.
            if(!e.player.hasPermission(WacCore.Permission.BYPASS_ITEMLIMIT.str) && e.item.itemStack.type == m) {

                // If the player already has the max amount of this item type in his inventory, cancel the event.
                if(e.player.inventory.contains(m, maxAmount)) {
                    e.isCancelled = true
                } else if(e.item.itemStack.amount > maxAmount && !e.player.inventory.contains(m, maxAmount)) {
                    // if it is maxAmount, just allow the player to pick it up normally.
                    // If the player does not yet have this item type in his inventory,
                    // let him pick up maxAmount of this item type from the stack, but leave the rest lying on the ground

                    e.isCancelled = true // If we don't cancel it, the player will pick up the whole item stack.

                    val i = e.item.itemStack.clone()
                    i.amount = maxAmount

                    val result = e.player.inventory.addItem(i)
                    if(result.isEmpty()) { // successfully added maxAmount of this item type to the player's inventory.

                        // remove the maxAmount items we just added to the player's inventory from the ground.
                        // Decrementing the amount directly via e.item.itemStack.amount-- does apperantly not work..
                        val i2 = e.item.itemStack.clone()
                        i2.amount -= maxAmount
                        e.item.itemStack = i2;
                    }
                }
            }
        }
    }

    fun countItemsPlacedByInventoryDrag(e: InventoryDragEvent): Int {
        var count: Int = 0;

        e.newItems.forEach {
            count += it.value.amount
        }

        return count;
    }

    fun itemsPlacedByInventoryDragContain(m: Material, e: InventoryDragEvent): Boolean {
        e.newItems.forEach {
            if(it.value.type == m) return true;
        }

        return false;
    }

    fun countItemsOfTypeInInventory(m: Material, i: Inventory): Int {
        var count = 0;

        i.contents.forEach {
            if(it != null && it.type == m) {
                count += it.amount
            }
        }

        return count
    }
}