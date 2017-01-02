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
        return false;
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
            throw new ProvisionException("Only " + senderClass.getSimpleName() + "'s can execute this tk.martijn_heil.wac_core.command");
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
