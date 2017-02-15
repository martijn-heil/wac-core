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
import tk.martijn_heil.wac_core.autosneak.AutoSneakModule
import tk.martijn_heil.wac_core.command.WacCoreCommands
import tk.martijn_heil.wac_core.command.common.bukkit.BukkitAuthorizer
import tk.martijn_heil.wac_core.command.common.bukkit.BukkitUtils
import tk.martijn_heil.wac_core.command.common.bukkit.provider.BukkitModule
import tk.martijn_heil.wac_core.command.common.bukkit.provider.sender.BukkitSenderModule
import tk.martijn_heil.wac_core.gamemodeswitching.GameModeSwitchingModule
import tk.martijn_heil.wac_core.itemproperty.ItemPropertyListener
import tk.martijn_heil.wac_core.kingdom.KingdomModule
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.logging.Logger


class WacCore : JavaPlugin() {
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

        logger.info("Ensuring database presence of all players currently online..")
        for (player in Bukkit.getServer().onlinePlayers) {
            ensurePresenceInDatabase(player)
        }


        logger.info("Building and registering commands..")
        val injector = Intake.createInjector()
        injector.install(PrimitivesModule())
        injector.install(BukkitModule(Bukkit.getServer()))
        injector.install(BukkitSenderModule())

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



        logger.info("Setting up ProtocolLib things..")
        protocolManager = ProtocolLibrary.getProtocolManager()

        KingdomModule.init(this, logger)
        AutoSneakModule.init(protocolManager, this, logger)
        GameModeSwitchingModule.init(this, logger)
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
        lateinit var protocolManager: ProtocolManager
    }

    enum class Permission(val str: String) {
        BYPASS__ITEM_LIMIT("wac-core.bypass.item-limit"),
        BYPASS__GAMEMODE_SWITCH_PENALTY("wac-core.bypass.gamemode-switch-penalty"),
        GAMEMODE__SPECTATOR("wac-core.gamemode.spectator");

        override fun toString() = str
    }
}
