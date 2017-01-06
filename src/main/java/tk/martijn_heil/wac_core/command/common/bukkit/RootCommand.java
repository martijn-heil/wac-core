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

package tk.martijn_heil.wac_core.command.common.bukkit;


import com.sk89q.intake.CommandException;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.util.auth.AuthorizationException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.permissions.Permissible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class RootCommand extends BukkitCommand
{
    private Dispatcher dispatcher;


    public static RootCommand of(Dispatcher dispatcher)
    {
        String prefix = dispatcher.getPrimaryAliases().iterator().next();
        String description = dispatcher.getDescription().getShortDescription();
        String usage = dispatcher.getDescription().getUsage();
        List<String> aliases = new ArrayList<>(dispatcher.getAliases());

        prefix = prefix != null ? prefix : "";
        description = description != null ? description : "";
        usage = usage != null ? usage : "";

        return new RootCommand(dispatcher, prefix, description, usage, aliases);
    }

    private RootCommand(Dispatcher dispatcher, String prefix, String description, String usage, List<String> aliases)
    {
        super(prefix, description, usage, aliases);
        this.dispatcher = dispatcher;
    }


    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args)
    {
        String command = cmd.startsWith("/") ? cmd.substring(1) : cmd;
        StringJoiner sj = new StringJoiner(" ");
        for (String arg : args)
        {
            sj.add(arg);
        }
        String joinedArgs = sj.toString();
        command = command + " " + joinedArgs;


        for (String alias : dispatcher.getAliases())
        {
            if (command.startsWith(alias))
            {
                Namespace namespace = new Namespace();
                namespace.put("sender", sender);
                namespace.put("senderClass", sender.getClass());
                namespace.put(Permissible.class, sender);

                try
                {
                    dispatcher.call(command, namespace, Collections.emptyList());
                }
                catch (CommandException | AuthorizationException cx)
                {
                    sendError(sender, cx.getMessage());
                }
                catch (InvocationCommandException icx)
                {
                    icx.printStackTrace();
                }
                break;
            }
        }

        return true;
    }

    private static void sendError(CommandSender p, String message)
    {
        p.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED + message);
    }
}
