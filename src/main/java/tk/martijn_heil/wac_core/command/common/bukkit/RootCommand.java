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
