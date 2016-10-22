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
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerPickupItemEvent


class MainListener() : Listener {

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun tntInPlayerInventoryLimit1(e: InventoryClickEvent) {
        // All drag & drop actions.

        // If the player tries to drag and drop a single item into their inventory, or if they drag and drop an entire stack into their inventory.
        // So, if the player has no TNT in his inventory yet, but tries to put an entire stack in at once, that is also prevented.
        if (e.clickedInventory == e.whoClicked.inventory && !e.whoClicked.hasPermission(WacCore.Permission.BYPASS_TNTLIMIT.str) &&
                (
                        (
                                (e.whoClicked.inventory.contains(Material.TNT) && e.cursor.type == Material.TNT) &&
                                        (e.click == ClickType.LEFT || e.click == ClickType.RIGHT)
                                )

                                || (e.click == ClickType.LEFT && e.cursor.amount > 1 && e.cursor.type == Material.TNT)
                        )
        ) {
            e.isCancelled = true
            e.whoClicked.sendMessage(ChatColor.RED.toString() + WacCore.messages.getString("error.event.cancelled.inventory.moreThanOneTnt"))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun tntInPlayerInventoryLimit254(e: InventoryDragEvent) {

        // Drag/distribution actions.
        if(itemsPlacedByInventoryDragContainTNT(e) && (e.whoClicked.inventory.contains(Material.TNT) || countItemsPlacedByInventoryDrag(e) > 1)) {
            e.isCancelled = true
            e.whoClicked.sendMessage(ChatColor.RED.toString() + WacCore.messages.getString("error.event.cancelled.inventory.moreThanOneTnt"))
        }
    }

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun tntInPlayerInventoryLimit2(e: InventoryClickEvent) {

        // If the player is trying to shift click TNT into their inventory.
        if(e.clickedInventory != e.whoClicked.inventory && (e.click == ClickType.SHIFT_LEFT || e.click == ClickType.SHIFT_RIGHT)
                && e.currentItem.type == Material.TNT && (e.whoClicked.inventory.contains(Material.TNT) || e.currentItem.amount > 1)
                && !e.whoClicked.hasPermission(WacCore.Permission.BYPASS_TNTLIMIT.str)) {
            e.isCancelled = true
            e.whoClicked.sendMessage(ChatColor.RED.toString() + WacCore.messages.getString("error.event.cancelled.inventory.moreThanOneTnt"))
        }
    }

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun tntInPlayerInventoryLimit4(e: PlayerPickupItemEvent) {
        // If the player tries to pick up TNT from the ground.
        if(!e.player.hasPermission(WacCore.Permission.BYPASS_TNTLIMIT.str) && e.item.itemStack.type == Material.TNT) {

            // If the player already has TNT in his inventory, cancel the event.
            if(e.player.inventory.contains(Material.TNT)) {
                e.isCancelled = true
            } else if(e.item.itemStack.amount > 1) { // if it is 1, just allow the player to pick it up normally.
                // If the player does not yet have TNT in his inventory, let him pick up 1 TNT from the stack, but leave the rest lying on the ground

                e.isCancelled = true // If we don't cancel it, the player will pick up the whole item stack.

                val i = e.item.itemStack.clone()
                i.amount = 1

                val result = e.player.inventory.addItem(i)
                if(result.isEmpty()) { // successfully added 1 TNT to the player's inventory.

                    // remove the 1 item we just added to the player's inventory from the ground.
                    // Decrementing the amount directly via e.item.itemStack.amount-- does apperantly not work..
                    val i2 = e.item.itemStack.clone()
                    i2.amount--
                    e.item.itemStack = i2;
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

    fun itemsPlacedByInventoryDragContainTNT(e: InventoryDragEvent): Boolean {
        e.newItems.forEach {
            if(it.value.type == Material.TNT) return true;
        }

        return false;
    }
}