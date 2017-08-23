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
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Logger


object KingdomModule {
    lateinit private var logger: Logger
    lateinit private var plugin: Plugin


    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger
        logger.info("Initializing KingdomModule..")

        logger.info("Registering event listeners..")
        plugin.server.pluginManager.registerEvents(SignListener, plugin)
    }

    fun getKingdom(p: OfflinePlayer): Kingdom? = Kingdom.fromFaction(MPlayer.get(p.uniqueId).faction)

    fun setKingdom(p: OfflinePlayer, newKingdom: Kingdom?) {
        if (newKingdom == null) return
        MPlayer.get(p.uniqueId).faction = newKingdom.faction
    }


    enum class Kingdom(val displayName: String, val simpleName: String = displayName) {
        IMPERIALS("Imperials"),
        ROYALS("Royals");

        val faction: Faction
            get() {
                return FactionColl.get().getByName(simpleName) ?: throw RuntimeException("Faction " + this.simpleName + " could not be found for kingdom " + this.name + ".")
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

            fun fromSimpleName(name: String): Kingdom? {
                values().forEach { if(it.simpleName == name) return it }
                return null
            }
        }
    }

    private object SignListener : Listener {

        @EventHandler(ignoreCancelled = true)
        fun onSignPlace(e: SignChangeEvent) {
            if(e.getLine(0) == "[JoinKingdom]") {
                if(!e.player.hasPermission("wac-core.signs.joinkingdom.create")) {
                    e.player.sendMessage(ChatColor.RED.toString() + "Je hebt geen toestemming om een join kingdom bordje te maken!")
                    e.isCancelled = true
                    return
                }

                val kingdom = KingdomModule.Kingdom.fromSimpleName(e.lines[1])
                if (kingdom == null) {
                    e.player.sendMessage(ChatColor.RED.toString() + "Kingdom \"" + e.getLine(1) + "\" bestaat niet!")
                    return
                }

                e.setLine(0, ChatColor.DARK_RED.toString() + ChatColor.MAGIC + "[JoinKingdom]")
                e.setLine(1, ChatColor.BLUE.toString() + "Klik hier om")
                e.setLine(2, ChatColor.AQUA.toString() + kingdom.simpleName)
                e.setLine(3, ChatColor.BLUE.toString() + "te joinen.")
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        fun onClickSign(e: PlayerInteractEvent) {
            if(e.hasBlock() && e.clickedBlock.state is Sign &&
                    (e.clickedBlock.state as Sign).getLine(0) == ChatColor.DARK_RED.toString() + ChatColor.MAGIC + "[JoinKingdom]") {

                if(KingdomModule.getKingdom(e.player) != null) {
                    e.player.sendMessage(ChatColor.RED.toString() + "Je zit al in een kingdom! " +
                            "Als je per ongeluk het verkeerde kingdom bent gejoined, neem dan contact op met een staff lid.")
                    return
                }

                val kingdomName = ChatColor.stripColor((e.clickedBlock.state as Sign).getLine(2))
                val kd = KingdomModule.Kingdom.fromSimpleName(kingdomName)
                if(kd == null) {
                    e.player.sendMessage(ChatColor.RED.toString() + "Dit kingdom kon niet gevonden worden, neem contact op met een staff lid.")
                    return
                }

                KingdomModule.setKingdom(e.player, kd)
                e.player.sendMessage(ChatColor.GOLD.toString() + "Je bent " + kd.displayName + " gejoined. " +
                        "Doe " + ChatColor.RED + "/f home" + ChatColor.GOLD + " om naar de kingdom spawn te gaan!")
            }
        }
    }
}