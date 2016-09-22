package tk.martijn_heil.wac_core.command.common;


import com.sk89q.intake.Command;
import com.sk89q.intake.CommandCallable;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.parametric.annotation.Optional;
import org.bukkit.command.CommandSender;

public class CommonCommands
{
    private Dispatcher dispatcher;


    public CommonCommands()
    {

    }

    @Command(aliases = {"help", "?", "h"}, desc = "Show tk.martijn_heil.wac_core.command help", usage = "[tk.martijn_heil.wac_core.command=overview]")
    public void help(@Sender CommandSender sender, @Optional CommandCallable callable)
    {
        if(callable != null)
        {
            CommonUtils.sendCommandHelp(callable, sender);
        }
        else
        {
            CommonUtils.sendCommandHelp(dispatcher, sender);
        }
    }


    /**
     * This is a bit hacky, CommonCommands depends on the dispatcher it belongs to, but when we *have* to register
     * the CommonCommands, the dispatcher is not initialized yet. So we have to do late-initialization as you can see below
     *
     * @param dispatcher The dispatcher this belongs to.
     */
    public void lateInit(Dispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }
}
