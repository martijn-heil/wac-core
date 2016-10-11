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
    }

    companion object {
        lateinit var dbconn: Connection;
        lateinit var messages: ResourceBundle;
    }
}
