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

import org.bukkit.OfflinePlayer
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
}