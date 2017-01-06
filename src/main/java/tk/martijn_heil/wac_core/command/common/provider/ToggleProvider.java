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

package tk.martijn_heil.wac_core.command.common.provider;


import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.ArgumentParseException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * A boolean provider, but instead of using true/false, on/off
 */
public class ToggleProvider implements Provider<Boolean>
{

    @Override
    public boolean isProvided()
    {
        return false;
    }


    @Nullable
    @Override
    public Boolean get(CommandArgs commandArgs, List<? extends Annotation> list) throws ArgumentException, ProvisionException
    {
        String next = commandArgs.next();
        boolean activated;

        if(next.equals("on"))
        {
            activated = true;
        }
        else if (next.equals("off"))
        {
            activated = false;
        }
        else
        {
            throw new ArgumentParseException(next + " is not a valid toggle value. Expected on/off");
        }

        return activated;
    }


    @Override
    public List<String> getSuggestions(String s)
    {
        return Arrays.asList("off", "on");
    }
}
