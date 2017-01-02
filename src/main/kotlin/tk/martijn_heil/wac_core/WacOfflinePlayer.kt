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

import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.io.Serializable


open class WacOfflinePlayer(val offlinePlayer: OfflinePlayer) : Serializable {

    // Blame Feike for insisting to use PermissionsEx for semantic grouping of players..
    // Which is a pretty bad design choice, as you are mixing semantic grouping with permission grouping.
    // It leads to pretty ugly code too.
    var kingdom: Kingdom?
        set(kingdom) {
            val rsp = Bukkit.getServer().servicesManager.getRegistration(Permission::class.java) ?: throw Exception("Couldn't get permission service.");
            val vaultPerms = rsp.provider

            if(this.kingdom != null) vaultPerms.playerRemoveGroup(getDefaultWorld().name, offlinePlayer, this.kingdom!!.groupName)
            if(kingdom != null) vaultPerms.playerAddGroup(getDefaultWorld().name, offlinePlayer, kingdom.groupName)
        }

        get() {
            val rsp = Bukkit.getServer().servicesManager.getRegistration(Permission::class.java) ?: throw Exception("Couldn't get permission service.");
            val vaultPerms = rsp.provider

            for(kd: Kingdom in Kingdom.values()) {
                for(group: String in vaultPerms.getPlayerGroups(getDefaultWorld().name, offlinePlayer)) {
                    if(kd.groupName == group) return kd
                }
            }

            return null;
        }

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
}
