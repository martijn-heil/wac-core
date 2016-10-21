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

package tk.martijn_heil.wac_core;

import org.bukkit.OfflinePlayer;
import tk.martijn_heil.wac_core.playerclass.PlayerClass;
import java.io.Serializable;


open class WacOfflinePlayer(val offlinePlayer: OfflinePlayer) : Serializable {

    init {
        // Check if player is registered in the database yet.
        val stmnt = WacCore.dbconn.prepareStatement("SELECT 1 FROM wac_core_players WHERE uuid=?");
        stmnt.setString(1, offlinePlayer.uniqueId.toString());
        val result = stmnt.executeQuery();
        if(!result.next()) { // player isn't yet present in the database.
            val stmnt2 = WacCore.dbconn.prepareStatement("INSERT INTO wac_core_players (uuid) VALUES(?)");
            stmnt2.setString(1, offlinePlayer.uniqueId.toString());
            stmnt2.executeUpdate();
        }
    }

    var playerClass: PlayerClass
        get() {
            val stmnt = WacCore.dbconn.prepareStatement("SELECT * FROM wac_core_players WHERE uuid=?");
            stmnt.setString(1, offlinePlayer.uniqueId.toString());
            val result = stmnt.executeQuery();
            if(!result.next()) throw Exception("Could not find player with uuid " + offlinePlayer.uniqueId + " in database.");
            val id = result.getString("player_class");

            if(id == null) {
                return PlayerClass.default;
            } else {
                val pc = PlayerClass.fromId(id);
                if(pc == null) {
                    this.playerClass = PlayerClass.default;
                    return PlayerClass.default;
                } else {
                    return pc;
                }
            }
        }

        set(value) {
            val stmnt = WacCore.dbconn.prepareStatement("UPDATE wac_core_players SET player_class=? WHERE uuid=?");
            stmnt.setString(1, value.id);
            stmnt.setString(2, offlinePlayer.uniqueId.toString());
            stmnt.executeUpdate();
        }
}
