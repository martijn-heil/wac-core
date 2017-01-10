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
import java.util.*


class WacPlayerManager {
    private val players = HashMap<UUID, WacPlayer>()

    fun getWacPlayer(uuid: UUID): WacPlayer {
        if (players.containsKey(uuid)) {
            return players.get(uuid)!!
        } else {
            val p = WacPlayer(Bukkit.getOfflinePlayer(uuid))
            players.put(uuid, p)
            return p
        }
    }
}