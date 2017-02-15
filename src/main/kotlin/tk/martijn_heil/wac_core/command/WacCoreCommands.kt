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

import com.sk89q.intake.Command
import com.sk89q.intake.Require
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import tk.martijn_heil.wac_core.autosneak.AutoSneakModule
import tk.martijn_heil.wac_core.command.common.Sender
import tk.martijn_heil.wac_core.command.common.Target


class WacCoreCommands {

    @Command(aliases = arrayOf("sneak"), desc = "Toggle sneak")
    @Require("wac-core.command.sneak")
    fun sneak(@Sender sender: CommandSender, @Target("wac-core.command.sneak.others") target: OfflinePlayer) {
        val newValue = !AutoSneakModule.isAutoSneaking(target)
        AutoSneakModule.setAutoSneaking(target, newValue)
        sender.sendMessage(target.name + if(newValue) " is now sneaking." else " is no longer sneaking.")
    }
}