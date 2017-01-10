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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class PlayerProvider implements Provider<Player>
{
    private final Server server;
    private final Provider<OfflinePlayer> offlinePlayerProvider;


    public PlayerProvider(Server server, Provider<OfflinePlayer> offlinePlayerProvider)
    {
        this.server = server;
        this.offlinePlayerProvider = offlinePlayerProvider;
    }

    @Override
    public boolean isProvided()
    {
        return false;
    }


    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public Player get(CommandArgs commandArgs, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException
    {
        OfflinePlayer op = offlinePlayerProvider.get(commandArgs, modifiers);
        if(op != null) {
            if(!op.isOnline()) throw new ArgumentParseException("Player not found");

            return op.getPlayer();
        } else {
            throw new ArgumentParseException("Player not found");
        }
    }


    @Override
    @SuppressWarnings("deprecation")
    public List<String> getSuggestions(String s)
    {
        // TODO UUID suggestions?
        List<Player> matches = Bukkit.matchPlayer(s);
        List<String> suggestions = new ArrayList<>();
        matches.forEach(match -> suggestions.add(match.getName()));
        return suggestions;
    }
}
