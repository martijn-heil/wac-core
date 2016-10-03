package tk.martijn_heil.wac_core

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.flywaydb.core.Flyway
import tk.martijn_heil.wac_core.itemproperty.ItemPropertyListener
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class WacCore : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig();

        val dbUrl = config.getString("db.url");
        val dbUsername = config.getString("db.username");
        val dbPassword = config.getString("db.password")
        // Storing the password in a char array doesn't improve much..
        // it's stored in plaintext in the "config" object anyway.. :/

        val flyway = Flyway();
        flyway.setDataSource(dbUrl, dbUsername, dbPassword);
        flyway.classLoader = this.classLoader;
        flyway.migrate();

        dbconn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        messages = ResourceBundle.getBundle("messages.messages");

        Bukkit.getPluginManager().registerEvents(ItemPropertyListener(), this);
    }

    override fun onDisable() {

    }

    companion object {
        lateinit var dbconn: Connection;
        lateinit var messages: ResourceBundle;
    }
}