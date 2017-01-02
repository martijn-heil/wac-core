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
import java.util.logging.Logger

class WacCore : JavaPlugin() {

    override fun onEnable() {
        Companion.logger = logger;
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
        flyway.isBaselineOnMigrate = true;
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
        Bukkit.getPluginManager().registerEvents(MaxOfItemListener(), this);
        Bukkit.getPluginManager().registerEvents(GeneralListener(), this);
    }

    companion object {
        lateinit var dbconn: Connection;
        lateinit var messages: ResourceBundle;
        lateinit var logger: Logger
    }

    enum class Permission(val str: String) {
        BYPASS_ITEMLIMIT("wac-core.bypass.itemlimit");

        override fun toString() = str
    }
}
