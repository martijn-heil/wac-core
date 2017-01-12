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

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import java.text.MessageFormat
import java.util.*


fun getStringWithArgs(bundle: ResourceBundle, args: Array<Any>, msg: String): String
{
    val formatter = MessageFormat("")
    formatter.locale = bundle.locale

    formatter.applyPattern(bundle.getString(msg))
    return formatter.format(args)
}

fun getDefaultWorld(): World = Bukkit.getServer().worlds[0]

fun ensurePresenceInDatabase(offlinePlayer: OfflinePlayer) {
    // Check if player is registered in the database yet.
    val stmnt = WacCore.dbconn!!.prepareStatement("SELECT 1 FROM wac_core_players WHERE uuid=?")
    stmnt.setString(1, offlinePlayer.uniqueId.toString())
    val result = stmnt.executeQuery()
    if(!result.next()) { // player isn't yet present in the database.
        val stmnt2 = WacCore.dbconn!!.prepareStatement("INSERT INTO wac_core_players (uuid) VALUES(?)")
        stmnt2.setString(1, offlinePlayer.uniqueId.toString())
        stmnt2.executeUpdate()
    }
}

fun getPlayersInRadius(loc: Location, radius: Int): List<Player> {
    val players = ArrayList<Player>()

    Bukkit.getOnlinePlayers().forEach {
        if(it.location.toVector().distance(loc.toVector()) <= radius) players.add(it)
    }

    return players
}