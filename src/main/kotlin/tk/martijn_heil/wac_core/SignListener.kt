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
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent


class SignListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onSignPlace(e: SignChangeEvent) {
        if(e.getLine(0) == "[JoinKingdom]") {
            if(!e.player.hasPermission("wac-core.signs.joinkingdom.create")) {
                e.player.sendMessage(ChatColor.RED.toString() + "Je hebt geen toestemming om een join kingdom bordje te maken!")
                e.isCancelled = true
                return
            }

            val kingdom = Kingdom.fromKingdomName(e.lines[1])
            if (kingdom == null) {
                e.player.sendMessage(ChatColor.RED.toString() + "Kingdom \"" + e.getLine(1) + "\" bestaat niet!")
                return
            }

            e.setLine(0, ChatColor.DARK_RED.toString() + ChatColor.MAGIC + "[JoinKingdom]")
            e.setLine(1, ChatColor.BLUE.toString() + "Klik hier om")
            e.setLine(2, ChatColor.AQUA.toString() + kingdom.kingdomName)
            e.setLine(3, ChatColor.BLUE.toString() + "te joinen.")
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onClickSign(e: PlayerInteractEvent) {
        if(e.hasBlock() && e.clickedBlock.state is Sign &&
                (e.clickedBlock.state as Sign).getLine(0) == ChatColor.DARK_RED.toString() + ChatColor.MAGIC + "[JoinKingdom]") {

            val p = WacPlayer(e.player)

            if(p.kingdom != null) {
                e.player.sendMessage(ChatColor.RED.toString() + "Je zit al in een kingdom! " +
                        "Als je per ongeluk het verkeerde kingdom bent gejoined, neem dan contact op met een staff lid.")
                return
            }

            val kingdomName = ChatColor.stripColor((e.clickedBlock.state as Sign).getLine(2))
            val kd = Kingdom.fromKingdomName(kingdomName)
            if(kd == null) {
                e.player.sendMessage(ChatColor.RED.toString() + "Dit kingdom kon niet gevonden worden, neem contact op met een staff lid.")
                return
            }

            p.kingdom = kd
            e.player.sendMessage(ChatColor.GOLD.toString() + "Je bent " + kd.kingdomName + " gejoined. " +
                    "Doe " + ChatColor.RED + "/f home" + ChatColor.GOLD + " om naar de kingdom spawn te gaan!")
        }
    }
}