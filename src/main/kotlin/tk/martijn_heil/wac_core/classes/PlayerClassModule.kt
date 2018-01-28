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

package tk.martijn_heil.wac_core.classes

import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import tk.martijn_heil.wac_core.WacCore
import tk.martijn_heil.wac_core.toCompressedString
import java.util.*

private val cache = HashMap<UUID, PlayerClass>()

var OfflinePlayer.playerClass: PlayerClass
    get() {
        val tmp = cache[this.uniqueId]
        if(tmp != null) {
            return tmp
        }
        else {
            val stmnt = WacCore.dbconn!!.prepareStatement("SELECT 1 FROM wac_core_players WHERE uuid=?")
            stmnt.setString(1, this.uniqueId.toCompressedString())
            val result = stmnt.executeQuery()
            if(!result.next()) throw IllegalStateException("Player(uuid: '" + this.uniqueId.toString() + "') not found in database.")
            val playerClass = PlayerClass.valueOf(result.getString("player_class"))
            stmnt.close()
            cache[this.uniqueId] = playerClass
            return playerClass
        }
    }
    set(value) { cache[this.uniqueId] = value }

object PlayerClassModule : AutoCloseable {
    lateinit var plugin: Plugin

    fun init(plugin: Plugin) {
        this.plugin = plugin
        plugin.server.pluginManager.registerEvents(PlayerClassModuleListener, plugin)
    }

    override fun close() {
        cache.forEach {
            val stmnt = WacCore.dbconn!!.prepareStatement("UPDATE wac_core_players SET player_class = ? WHERE uuid = ?")
            stmnt.setString(1, it.value.toString())
            stmnt.setString(2, it.key.toCompressedString())
            stmnt.executeUpdate()
            stmnt.close()
        }
    }

    private object PlayerClassModuleListener : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onSignPlace(e: SignChangeEvent) {
            if(e.getLine(0) == "[PlayerClass]") {
                if(!e.player.hasPermission("wac-core.signs.playerclass.create")) {
                    e.player.sendMessage(ChatColor.RED.toString() + "Je hebt geen toestemming om een join player class bordje te maken!")
                    e.isCancelled = true
                    return
                }

                val playerClass: PlayerClass
                try {
                    playerClass = PlayerClass.valueOf(e.lines[1].toUpperCase())
                } catch(ex: IllegalArgumentException) {
                    e.player.sendMessage(ChatColor.RED.toString() + "Player class \"" + e.getLine(1) + "\" bestaat niet!")
                    return
                }

                e.setLine(0, ChatColor.DARK_RED.toString() + ChatColor.MAGIC + "[PlayerClass]")
                e.setLine(1, ChatColor.BLUE.toString() + "Klik hier om")
                e.setLine(2, ChatColor.AQUA.toString() + playerClass.toString())
                e.setLine(3, ChatColor.BLUE.toString() + "te worden.")
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        fun onClickSign(e: PlayerInteractEvent) {
            if(e.hasBlock() && e.clickedBlock.state is Sign &&
                    (e.clickedBlock.state as Sign).getLine(0) == ChatColor.DARK_RED.toString() + ChatColor.MAGIC + "[PlayerClass]") {
                val playerClassName = ChatColor.stripColor((e.clickedBlock.state as Sign).getLine(2))
                val pc: PlayerClass
                try {
                    pc = PlayerClass.valueOf(playerClassName.toUpperCase())
                } catch(ex: IllegalArgumentException) {
                    e.player.sendMessage(ChatColor.RED.toString() + "Deze player class kon niet gevonden worden, neem contact op met een staff lid.")
                    return
                }
                e.player.playerClass = pc
                e.player.sendMessage(ChatColor.GOLD.toString() + "Je bent nu " + pc.name.toLowerCase())
            }
        }
    }
}