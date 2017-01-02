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

package tk.martijn_heil.wac_core.command.common.bukkit.provider;


import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.ArgumentParseException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import tk.martijn_heil.wac_core.command.common.Target;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerProvider implements Provider<Player>
{
    private final Server server;
    private boolean orSender = false;


    public PlayerProvider(Server server, boolean orSender)
    {
        this.server = server;
        this.orSender = orSender;
    }

    @Override
    public boolean isProvided()
    {
        return false;
    }


    @Nullable
    @Override
    public Player get(CommandArgs commandArgs, List<? extends Annotation> list) throws ArgumentException, ProvisionException
    {
        Player p = null;
        Class senderClass = (Class) commandArgs.getNamespace().get("senderClass");

        if(orSender)
        {
            for (Annotation annotation : list)
            {
                if(annotation instanceof Target)
                {
                    String requiredPermission = ((Target) annotation).value();

                    if(!requiredPermission.isEmpty())
                    {
                        if(!commandArgs.getNamespace().get(Permissible.class).hasPermission(requiredPermission))
                        {
                            throw new ProvisionException("You need " + requiredPermission);
                        }
                    }
                }
            }

            if(commandArgs.hasNext())
            {
                String arg = commandArgs.next();

                if(arg.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) // it's a UUID.
                {
                    p = server.getPlayer(UUID.fromString(arg));
                }
                else
                {
                    p = server.getPlayer(arg);
                }
            }
            else if (commandArgs.getNamespace().get("sender") instanceof Player)
            {
                p = (Player) commandArgs.getNamespace().get("sender");
            }
            else
            {
                commandArgs.next(); // generate MissingArgumentException
            }
        }
        else
        {
            String arg = commandArgs.next();

            if(arg.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) // it's a UUID.
            {
                p = server.getPlayer(UUID.fromString(arg));
            }
            else
            {
                p = server.getPlayer(arg);
            }
        }

        if(p == null) throw new ArgumentParseException("Player not found");

        return p;
    }


    @Override
    public List<String> getSuggestions(String s)
    {
        List<Player> matches = Bukkit.matchPlayer(s);
        List<String> suggestions = new ArrayList<>();
        matches.forEach(match -> suggestions.add(match.getName()));
        return suggestions;
    }
}
