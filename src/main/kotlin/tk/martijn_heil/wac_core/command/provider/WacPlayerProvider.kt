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

package tk.martijn_heil.wac_core.command.provider

import com.sk89q.intake.argument.CommandArgs
import com.sk89q.intake.parametric.Provider
import org.bukkit.OfflinePlayer
import tk.martijn_heil.wac_core.WacCore
import tk.martijn_heil.wac_core.WacPlayer


class WacPlayerProvider(val offlinePlayerProvider: Provider<OfflinePlayer>) : Provider<WacPlayer> {

    override fun isProvided() = false
    override fun getSuggestions(prefix: String?): MutableList<String>? = offlinePlayerProvider.getSuggestions(prefix)


    override fun get(arguments: CommandArgs?, modifiers: MutableList<out Annotation>?): WacPlayer? {
        val offlinePlayer = offlinePlayerProvider.get(arguments, modifiers)
        if(offlinePlayer != null) {
            WacCore.logger.fine("OfflinePlayer was provided, returning WacPlayer.valueOf(...)")
            return WacPlayer.valueOf((offlinePlayer))
        } else {
            WacCore.logger.fine("OfflinePlayer was not provided, returning null.")
            return null
        }
    }
}