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

import com.sk89q.intake.Intake
import com.sk89q.intake.fluent.CommandGraph
import com.sk89q.intake.parametric.ParametricBuilder
import com.sk89q.intake.parametric.provider.PrimitivesModule
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import tk.martijn_heil.wac_core.command.common.bukkit.BukkitAuthorizer
import tk.martijn_heil.wac_core.command.common.bukkit.BukkitUtils
import tk.martijn_heil.wac_core.command.common.bukkit.provider.BukkitModule
import tk.martijn_heil.wac_core.command.common.bukkit.provider.sender.BukkitSenderModule
import java.util.logging.Logger


object CommandModule {
    lateinit private var plugin: Plugin
    lateinit private var logger: Logger

    fun init(plugin: Plugin, logger: Logger) {
        this.plugin = plugin
        this.logger = logger

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


        BukkitUtils.registerDispatcher(dispatcher, plugin)
    }
}