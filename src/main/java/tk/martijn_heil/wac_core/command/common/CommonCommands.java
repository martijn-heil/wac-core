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

package tk.martijn_heil.wac_core.command.common;


import com.sk89q.intake.Command;
import com.sk89q.intake.CommandCallable;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.parametric.annotation.Optional;
import org.bukkit.command.CommandSender;

public class CommonCommands
{
    private Dispatcher dispatcher;


    public CommonCommands()
    {

    }

    @Command(aliases = {"help", "?", "chunkRelightBlock"}, desc = "Show tk.martijn_heil.wac_core.command help", usage = "[tk.martijn_heil.wac_core.command=overview]")
    public void help(@Sender CommandSender sender, @Optional CommandCallable callable)
    {
        if(callable != null)
        {
            CommonUtils.sendCommandHelp(callable, sender);
        }
        else
        {
            CommonUtils.sendCommandHelp(dispatcher, sender);
        }
    }


    /**
     * This is a bit hacky, CommonCommands depends on the dispatcher it belongs to, but when we *have* to register
     * the CommonCommands, the dispatcher is not initialized yet. So we have to do late-initialization as you can see below
     *
     * @param dispatcher The dispatcher this belongs to.
     */
    public void lateInit(Dispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }
}
