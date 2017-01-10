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

package tk.martijn_heil.wac_core.command.common.bukkit.provider.sender;


import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class BukkitSenderProvider<T> implements Provider<T>
{

    @Override
    public boolean isProvided()
    {
        return true;
    }


    @Nullable
    @Override
    public T get(CommandArgs commandArgs, List<? extends Annotation> list) throws ArgumentException, ProvisionException
    {
        Namespace namespace = commandArgs.getNamespace();
        Class senderClass = (Class) namespace.get("senderClass");

        if(senderClass == null)
        {
            throw new ProvisionException("Sender's class was not set on namespace");
        }

        T sender;

        if (!namespace.containsKey("sender"))
        {
            throw new ProvisionException("Sender was not set on namespace");
        }

        try
        {
            sender = (T) namespace.get("sender");
        }
        catch(ClassCastException ex)
        {
            throw new ProvisionException("Only " + senderClass.getSimpleName() + "'s can execute this command");
        }


        if (sender == null) throw new ProvisionException("Sender was set on namespace, but is null");

        return sender;
    }


    @Override
    public List<String> getSuggestions(String s)
    {
        return Collections.emptyList();
    }
}
