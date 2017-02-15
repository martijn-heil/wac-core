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

package tk.martijn_heil.wac_core.kingdom

import com.massivecraft.factions.Rel
import com.massivecraft.factions.entity.Faction
import com.massivecraft.factions.entity.FactionColl
import com.massivecraft.factions.entity.MPlayer
import com.massivecraft.massivecore.ps.PS
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tk.martijn_heil.wac_core.getPlayersInRadius
import java.util.*
import java.util.logging.Logger


object KingdomModule {
    lateinit private var logger: Logger
    lateinit private var plugin: Plugin


    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger
        logger.info("Initializing KingdomModule..")

        logger.info("Scheduling tasks..")
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            val loc = Kingdom.UNDEAD.home
            val radius = 120

            getPlayersInRadius(loc, radius).forEach {
                if(getKingdom(it) != Kingdom.UNDEAD) {
                    it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 700, 1, false, false), true)
                    it.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 700, 1, false, false), true)
                    it.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 700, 1, false, false), true)
                    it.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 700, 1, false, false), true)
                }
            }
        }, 0L, 600L)
    }

    fun getKingdom(p: OfflinePlayer): Kingdom? = Kingdom.fromFaction(MPlayer.get(p.uniqueId).faction)

    fun setKingdom(p: OfflinePlayer, newKingdom: Kingdom?) {
        if (newKingdom == null) return
        MPlayer.get(p.uniqueId).faction = newKingdom.faction
    }


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


        val members: List<OfflinePlayer>
            get() {
                val members = ArrayList<OfflinePlayer>()
                for (offlinePlayer in Bukkit.getServer().offlinePlayers) {
                    if (getKingdom(offlinePlayer) == this) members.add(offlinePlayer)
                }
                return members
            }

        var home: Location
            get() = faction.home.asBukkitLocation()
            set(value) {
                faction.home = PS.valueOf(value)
            }

        var leader: OfflinePlayer
            get() = faction.leader.player
            set(value) {
                MPlayer.get(value.uniqueId).role = Rel.LEADER
            }

        companion object {
            fun fromFaction(f: Faction): Kingdom? {
                values().forEach { if(it.faction.id == f.id) return it }
                return null
            }

            fun fromKingdomName(name: String): Kingdom? {
                values().forEach { if(it.kingdomName == name) return it }
                return null
            }
        }
    }
}