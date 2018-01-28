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
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import tk.martijn_heil.wac_core.classes.PlayerClassModule
import tk.martijn_heil.wac_core.craft.SailingModule
import tk.martijn_heil.wac_core.gameplay.GamePlayControlModule
import tk.martijn_heil.wac_core.general.GeneralModule
import tk.martijn_heil.wac_core.kingdom.KingdomModule
import tk.martijn_heil.wac_core.namehiding.NameHidingModule
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.logging.Logger


class WacCore : JavaPlugin() {
    val debug = false

    override fun onEnable() {
        try {
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
            val migrationResult = cls!!.getMethod("doStuff", String::class.java, String::class.java, String::class.java, ClassLoader::class.java).invoke(null, dbUrl, dbUsername, dbPassword, this.classLoader) as Boolean
            if(!migrationResult) {
                this.isEnabled = false
                return
            }

            try {
                WacCore.dbconn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
            } catch (ex: SQLException) {
                WacCore.logger.severe(ex.message)
                WacCore.logger.severe("Disabling plugin due to database error..")
                this.isEnabled = false
                return
            }


            messages = ResourceBundle.getBundle("messages.messages")

            logger.info("Ensuring database presence of all players currently online..")
            for (player in Bukkit.getServer().onlinePlayers) {
                ensurePresenceInDatabase(player)
            }


            logger.info("Setting up ProtocolLib things..")
            protocolManager = ProtocolLibrary.getProtocolManager()

            if (!debug) KingdomModule.init(this, PrefixedLogger("WacCoreKingdomModuleLogger", "KingdomModule", logger))
            GeneralModule.init(this, PrefixedLogger("WacCoreGeneralModuleLogger", "GeneralModule", logger))
            GamePlayControlModule.init(this, PrefixedLogger("WacCoreGamePlayControlModuleLogger", "GamePlayControlModule", logger))
            CustomResourcePackModule.init(this, PrefixedLogger("WacCoreCustomResourcePackModule", "CustomResourcePackModule", logger))
            //CommandModule.init(this, PrefixedLogger("WacCoreCommandModuleLogger", "CommandModule", logger))
            //GameModeSwitchingModule.init(this, PrefixedLogger("GameModeSwitchingModuleLogger", "GameModeSwitchingModule", logger))
            SailingModule.init(this, PrefixedLogger("WacCoreSailingModuleLogger", "SailingModule", logger))
            SprintRestrictionModule.init(this, PrefixedLogger("WacCoreSprintRestrictionModuleLogger", "SprintRestrictionModule", logger))
            HealthModule.init(this, PrefixedLogger("WacCoreHealthModuleLogger", "HealthModule", logger))
            NameHidingModule.init(this, PrefixedLogger("WacCoreNameHidingModuleLogger", "NameHidingModule", logger), protocolManager)
            CrackshotHook.init(this, PrefixedLogger("WacCoreCrackShootHookLogger", "CrackShotHook", logger), protocolManager)
            PlayerClassModule.init(this)
            //TemporaryModule.init(this)
            if(!debug) HackyModule.init(PrefixedLogger("HackyModuleLogger", "HackyModule", logger))
        } catch (t: Throwable) {
            t.printStackTrace()
            throw t
        }
    }

    override fun onDisable() {
        SailingModule.close()
        HackyModule.close()
        PlayerClassModule.close()
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
        BYPASS__OCEAN_BUILD_LIMITS("wac-core.bypass.ocean-build-limits");

        override fun toString() = str
    }
}
