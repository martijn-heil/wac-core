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
