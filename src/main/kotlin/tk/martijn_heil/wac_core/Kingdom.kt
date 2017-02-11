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

import com.massivecraft.factions.Rel
import com.massivecraft.factions.entity.Faction
import com.massivecraft.factions.entity.FactionColl
import com.massivecraft.factions.entity.MPlayer
import com.massivecraft.massivecore.ps.PS
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*


enum class Kingdom(val kingdomName: String, val factionName: String = kingdomName) {
    UNDEAD("Kronoth"),
    HUMAN_1("Astilafia"),
    HUMAN_2_DARK("Dark Ostrain", "OstrainDark"),
    HUMAN_2_LIGHT("Light Ostrain", "OstrainLight"),
    HUMAN_3("Volcair"),
    DWARVES("Dhar' Guldaruhm", "DharGuldaruhm"),
    ORCS("Zorgirhgoth"),
    GOBLINS("Krulk"),
    OGRES("Vrewikur");

    val faction: Faction
        get() {
            return FactionColl.get().getByName(factionName) ?: throw RuntimeException("Faction " + this.factionName + " could not be found for kingdom " + this.name + ".")
        }


    val members: List<WacPlayer>
        get() {
            val members = ArrayList<WacPlayer>()
            for (offlinePlayer in Bukkit.getServer().offlinePlayers) {
                val pl = WacPlayer.valueOf(offlinePlayer)
                if (pl.kingdom == this) members.add(pl)
            }

            return members
        }

    var home: Location
        get() = faction.home.asBukkitLocation()
        set(value) {
            faction.home = PS.valueOf(value)
        }

    var leader: WacPlayer
        get() = WacPlayer.valueOf(faction.leader.player)
        set(value) {
            MPlayer.get(value.offlinePlayer.uniqueId).role = Rel.LEADER
        }

    companion object {
        fun fromFaction(f: Faction): Kingdom? {
            Kingdom.values().forEach { if(it.faction.id == f.id) return it }
            return null
        }

        fun fromKingdomName(name: String): Kingdom? {
            Kingdom.values().forEach { if(it.kingdomName == name) return it }
            return null
        }
    }
}
