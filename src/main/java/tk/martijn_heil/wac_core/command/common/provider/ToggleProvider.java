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
