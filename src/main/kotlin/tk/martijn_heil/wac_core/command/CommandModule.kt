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

package tk.martijn_heil.wac_core.command

import com.sk89q.intake.dispatcher.Dispatcher
import com.sk89q.intake.dispatcher.SimpleDispatcher
import org.bukkit.plugin.Plugin
import tk.martijn_heil.wac_core.command.common.bukkit.BukkitUtils
import java.util.logging.Logger


object CommandModule {
    lateinit private var plugin: Plugin
    lateinit private var logger: Logger
    private var dispatcher = SimpleDispatcher()

    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger
        logger.info("Initializing CommandModule..")
        BukkitUtils.registerDispatcher(dispatcher, plugin, listOf("wac-core", "wac", "lg", "luchtgames", "nin"))
    }

    fun registerDispatcher(dispatcher: Dispatcher) {
        dispatcher.registerCommand(dispatcher)
    }
}