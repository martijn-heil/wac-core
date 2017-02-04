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

import com.massivecraft.factions.entity.MPlayer
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.io.Serializable
import java.util.*


open class WacPlayer(val offlinePlayer: OfflinePlayer) : Serializable {

    var kingdom: Kingdom?
        set(kingdom) {
            if(kingdom != null) {
                MPlayer.get(offlinePlayer.uniqueId).faction = kingdom.faction
            }
        }

        get() = Kingdom.fromFaction(MPlayer.get(offlinePlayer.uniqueId).faction)

    var isLongTermSneaking: Boolean = false
        set(value) {
            if(value) {
                offlinePlayer.player?.isSneaking = value
                field = value
            } else {
                field = value
                offlinePlayer.player?.isSneaking = value
            }
        }

    var isGameModeSwitching: Boolean = false

    companion object {
        fun valueOf(uuid: UUID): WacPlayer = WacCore.playerManager.getWacPlayer(uuid)
        fun valueOf(player: Player): WacPlayer = WacCore.playerManager.getWacPlayer(player.uniqueId)
        fun valueOf(player: OfflinePlayer): WacPlayer = WacCore.playerManager.getWacPlayer(player.uniqueId)
    }
}
