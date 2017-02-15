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

package tk.martijn_heil.wac_core.command.common.bukkit.provider;


import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.ArgumentParseException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;
import tk.martijn_heil.wac_core.WacCore;
import tk.martijn_heil.wac_core.command.common.Target;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OfflinePlayerProvider implements Provider<OfflinePlayer>
{
    private final Server server;


    public OfflinePlayerProvider(Server server)
    {
        this.server = server;
    }

    @Override
    public boolean isProvided()
    {
        return false;
    }



    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public OfflinePlayer get(CommandArgs commandArgs, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException
    {
        CommandSender sender = (CommandSender) commandArgs.getNamespace().get("sender");
        Target targetAnnotation = null;

        boolean isTarget = false;
        for(Annotation annotation : modifiers) {
           if(annotation instanceof Target) {
               WacCore.logger.fine("Target annotation found!");
               isTarget = true;
               targetAnnotation = (Target) annotation;
               break;
           }
        }

        OfflinePlayer p = null;

        if(commandArgs.hasNext())
        {
            String arg = commandArgs.next();

            if(arg.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) // it's a UUID.
            {
                p = server.getOfflinePlayer(UUID.fromString(arg));
            }
            else
            {
                p = server.getOfflinePlayer(arg);
            }
        }
        else if (isTarget && sender instanceof OfflinePlayer)
        {
            p = (OfflinePlayer) sender;
        }
        else
        {
            // Generate MissingArgumentException
            commandArgs.next();
        }

        if(p == null) {
            throw new ArgumentParseException("Player not found");
        }

        if(isTarget && !p.equals(sender) && !commandArgs.getNamespace().get(Permissible.class).hasPermission(targetAnnotation.value()))
        {
            throw new ArgumentParseException("You need " + targetAnnotation.value());
        }

        return p;
    }


    @Override
    public List<String> getSuggestions(String s)
    {
        // TODO implement
        return Collections.emptyList();
    }
}
