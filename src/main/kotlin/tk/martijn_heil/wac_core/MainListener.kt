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

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerPickupItemEvent


class MainListener : Listener {

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun tntInPlayerInventoryLimit1(e: InventoryClickEvent) {

        // If the player is trying to drag & drop TNT into their inventory.
        if(e.clickedInventory == e.whoClicked.inventory && e.click == ClickType.LEFT && e.cursor.type == Material.TNT &&
                e.whoClicked.inventory.contains(Material.TNT) && !e.whoClicked.hasPermission(WacCore.Permission.BYPASS_TNTLIMIT.str)) {
            e.isCancelled = true
            e.whoClicked.sendMessage(WacCore.messages.getString("error.event.cancelled.inventory.moreThanOneTnt"))
        }
    }

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun tntInPlayerInventoryLimit2(e: InventoryClickEvent) {

        // If the player is trying to shift click TNT into their inventory.
        if(e.clickedInventory != e.whoClicked.inventory && e.click == ClickType.SHIFT_LEFT && e.currentItem.type == Material.TNT &&
            e.whoClicked.inventory.contains(Material.TNT) && !e.whoClicked.hasPermission(WacCore.Permission.BYPASS_TNTLIMIT.str)) {
            e.isCancelled = true
            e.whoClicked.sendMessage(WacCore.messages.getString("error.event.cancelled.inventory.moreThanOneTnt"))
        }
    }

    /**
     * Prevent a player from having more than 1 TNT block in their inventory.
     */
    @EventHandler(ignoreCancelled = true)
    fun tntInPlayerInventoryLimit3(e: PlayerPickupItemEvent) {

        // If the player tries to pick up TNT from the ground.
        if(!e.player.hasPermission(WacCore.Permission.BYPASS_TNTLIMIT.str) && e.item.type == Material.TNT &&
                e.player.inventory.contains(Material.TNT)) {
            e.isCancelled = true
            e.player.sendMessage(WacCore.messages.getString("error.event.cancelled.inventory.moreThanOneTnt"))
        }
    }
}