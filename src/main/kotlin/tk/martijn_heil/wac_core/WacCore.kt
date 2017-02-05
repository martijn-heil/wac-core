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

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.sk89q.intake.Intake
import com.sk89q.intake.fluent.CommandGraph
import com.sk89q.intake.parametric.ParametricBuilder
import com.sk89q.intake.parametric.provider.PrimitivesModule
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tk.martijn_heil.wac_core.command.WacCoreCommands
import tk.martijn_heil.wac_core.command.WacCoreModule
import tk.martijn_heil.wac_core.command.common.bukkit.BukkitAuthorizer
import tk.martijn_heil.wac_core.command.common.bukkit.BukkitUtils
import tk.martijn_heil.wac_core.command.common.bukkit.provider.BukkitModule
import tk.martijn_heil.wac_core.command.common.bukkit.provider.sender.BukkitSenderModule
import tk.martijn_heil.wac_core.itemproperty.ItemPropertyListener
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.logging.Logger


class WacCore : JavaPlugin() {

    lateinit private var protocolManager: ProtocolManager

    override fun onEnable() {
        Companion.plugin = this
        Companion.logger = logger
        logger.fine("Saving default config..")
        saveDefaultConfig()

        logger.info("Migrating database if needed..")
        dbUrl = config.getString("db.url")
        dbUsername = config.getString("db.username")
        dbPassword = config.getString("db.password")
        // Storing the password in a char array doesn't improve much..
        // it's stored in plaintext in the "config" object anyway.. :/

        // This is a hack, we use a custom classloader to replace Flyway's VersionPrinter class with
        // Our custom version of that class, which is located in the resources folder.
        // The main reason for this is that with Bukkit, Flyway was having classpath issues determining it's version.
        // Due to that the whole plugin crashed on startup.
        val hackyLoader = HackyClassLoader(this.classLoader)
        val cls = hackyLoader.loadClass("tk.martijn_heil.wac_core.HackyClass")
        cls!!.getMethod("doStuff", String::class.java, String::class.java, String::class.java, ClassLoader::class.java).invoke(null, dbUrl, dbUsername, dbPassword, this.classLoader)


        try {
            WacCore.dbconn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
        } catch (ex: SQLException) {
            WacCore.logger.severe(ex.message)
            WacCore.logger.severe("Disabling plugin due to database error..")
            this.isEnabled = false
        }


        messages = ResourceBundle.getBundle("messages.messages")

        logger.info("Registering event listeners..")
        Bukkit.getPluginManager().registerEvents(ItemPropertyListener(), this)
        Bukkit.getPluginManager().registerEvents(MaxOfItemListener(), this)
        Bukkit.getPluginManager().registerEvents(GeneralListener(), this)
        Bukkit.getPluginManager().registerEvents(SignListener(), this)
        Bukkit.getPluginManager().registerEvents(LongTermSneakListener(), this)
        Bukkit.getPluginManager().registerEvents(GameModeSwitchingListener(), this)

        logger.info("Ensuring database presence of all players currently online..")
        for (player in Bukkit.getServer().onlinePlayers) {
            ensurePresenceInDatabase(player)
        }

        playerManager = WacPlayerManager()

        logger.info("Building and registering commands..")
        val injector = Intake.createInjector()
        injector.install(PrimitivesModule())
        injector.install(BukkitModule(Bukkit.getServer()))
        injector.install(BukkitSenderModule())
        injector.install(WacCoreModule())

        val builder = ParametricBuilder(injector)
        builder.authorizer = BukkitAuthorizer()


        val dispatcher = CommandGraph()
                .builder(builder)
                .commands()
                .group("wac-core", "wac", "lg", "luchtgames")
                .registerMethods(WacCoreCommands())
                .parent()
                .graph()
                .dispatcher


        BukkitUtils.registerDispatcher(dispatcher, this)

        logger.info("Scheduling tasks..")
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

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            Bukkit.getOnlinePlayers().forEach {
                val p = WacPlayer.valueOf(it)
                if(p.isGameModeSwitching) {
                    it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false), true)
                }
            }
        }, 0, 20)

        logger.info("Setting up ProtocolLib things..")
        protocolManager = ProtocolLibrary.getProtocolManager()

//        protocolManager.addPacketListener(object : PacketAdapter(this, ListenerPriority.NORMAL, PacketType.fromLegacy(0x20, PacketType.Sender.SERVER)) {
//            fun isElvenCityChunk(): Boolean {
//                return false
//            }
//
//            fun getFakeChunk(x: Int, y: Int) {
//
//            }
//
//            override fun onPacketSending(e: PacketEvent?) {
//                if(e == null) return
//                val packet = e.packet
//
//                packet.
//            }
//        })
    }

    companion object {

        private lateinit var dbUrl: String
        private lateinit var dbUsername: String
        private lateinit var dbPassword: String

        var dbconn: Connection? = null
            get() {
                if(field!!.isClosed) field = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)

                return field
            }

        lateinit var messages: ResourceBundle
        lateinit var logger: Logger
        lateinit var plugin: Plugin
        lateinit var playerManager: WacPlayerManager
    }

    enum class Permission(val str: String) {
        BYPASS__ITEM_LIMIT("wac-core.bypass.item-limit"),
        BYPASS__GAMEMODE_SWITCH_PENALTY("wac-core.bypass.gamemode-switch-penalty"),
        GAMEMODE__SPECTATOR("wac-core.gamemode.spectator");

        override fun toString() = str
    }
}
