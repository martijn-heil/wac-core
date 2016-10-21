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

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import tk.martijn_heil.wac_core.itemproperty.ItemPropertyListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

class WacCore : JavaPlugin() {

    override fun onEnable() {
        logger.fine("Saving default config..");
        saveDefaultConfig();

        logger.info("Migrating database if needed..");
        val dbUrl = config.getString("db.url");
        val dbUsername = config.getString("db.username");
        val dbPassword = config.getString("db.password");
        // Storing the password in a char array doesn't improve much..
        // it's stored in plaintext in the "config" object anyway.. :/

        val flyway = Flyway();
        flyway.setDataSource(dbUrl, dbUsername, dbPassword);
        flyway.classLoader = this.classLoader;
        try {
            flyway.migrate();
        } catch (ex: FlywayException) {
            logger.severe(ex.message);
            this.isEnabled = false;
            return;
        }

        try {
            dbconn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (ex: SQLException) {
            logger.severe(ex.message);
            this.isEnabled = false;
            return;
        }

        messages = ResourceBundle.getBundle("messages.messages");

        logger.info("Registering event listeners..");
        Bukkit.getPluginManager().registerEvents(ItemPropertyListener(), this);
        Bukkit.getPluginManager().registerEvents(MainListener(), this);
    }

    companion object {
        lateinit var dbconn: Connection;
        lateinit var messages: ResourceBundle;
    }

    enum class Permission(val str: String) {
        BYPASS_TNTLIMIT("wac-core.bypass.tntlimit");

        override fun toString() = str
    }
}
