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
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tk.martijn_heil.wac_core.itemproperty.ItemPropertyListener
import java.sql.Connection
import java.util.*
import java.util.logging.Logger


class WacCore : JavaPlugin() {

    override fun onEnable() {
        Companion.logger = logger
        logger.fine("Saving default config..")
        saveDefaultConfig()

        logger.info("Migrating database if needed..")
        val dbUrl = config.getString("db.url")
        val dbUsername = config.getString("db.username")
        val dbPassword = config.getString("db.password")
        // Storing the password in a char array doesn't improve much..
        // it's stored in plaintext in the "config" object anyway.. :/

        // This is a hack, we use a custom classloader to replace Flyway's VersionPrinter class with
        // Our custom version of that class, which is located in the resources folder.
        // The main reason for this is that with Bukkit, Flyway was having classpath issues determining it's version.
        // Due to that the whole plugin crashed on startup.
        val hackyLoader = HackyClassLoader(this.classLoader)
        val cls = hackyLoader.loadClass("tk.martijn_heil.wac_core.HackyClass")
        cls!!.getMethod("doStuff", String::class.java, String::class.java, String::class.java, ClassLoader::class.java).invoke(null, dbUrl, dbUsername, dbPassword, this.classLoader)


        messages = ResourceBundle.getBundle("messages.messages")

        logger.info("Registering event listeners..")
        Bukkit.getPluginManager().registerEvents(ItemPropertyListener(), this)
        Bukkit.getPluginManager().registerEvents(MaxOfItemListener(), this)
        Bukkit.getPluginManager().registerEvents(GeneralListener(), this)
        Bukkit.getPluginManager().registerEvents(SignListener(), this)

        logger.info("Ensuring database presence of all players currently online..")
        for (player in Bukkit.getServer().onlinePlayers) {
            ensurePresenceInDatabase(player)
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            val loc = Kingdom.UNDEAD.home
            val radius = 120

            getPlayersInRadius(loc, radius).forEach {
                if(WacPlayer(it).kingdom != Kingdom.UNDEAD) {
                    it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 700, 1, false, false), true)
                    it.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 700, 1, false, false), true)
                    it.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 700, 1, false, false), true)
                    it.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 700, 1, false, false), true)
                }
            }
        }, 0L, 600L)
    }

    companion object {
        lateinit var dbconn: Connection
        lateinit var messages: ResourceBundle
        lateinit var logger: Logger
    }

    enum class Permission(val str: String) {
        BYPASS_ITEMLIMIT("wac-core.bypass.itemlimit");

        override fun toString() = str
    }
}
