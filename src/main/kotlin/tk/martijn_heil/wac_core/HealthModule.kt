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

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

object HealthModule: AutoCloseable {
    lateinit var plugin: Plugin
    lateinit var logger: Logger

    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger
        logger.info("Initializing HealthModule..")
        plugin.server.pluginManager.registerEvents(HealthModuleListener, plugin)
    }

    override fun close() {

    }

    private object HealthModuleListener : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onPlayerHealth(e: EntityRegainHealthEvent) {
            if(e.entity is Player) e.isCancelled = true
        }
    }
}