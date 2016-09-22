package tk.martijn_heil.wac_core.command.common.bukkit.provider.sender;


import com.sk89q.intake.parametric.AbstractModule;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import tk.martijn_heil.elytraoptions.command.common.Sender;

public class BukkitSenderModule extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind(CommandSender.class).annotatedWith(Sender.class).toProvider(new BukkitSenderProvider<>());
        bind(ConsoleCommandSender.class).annotatedWith(Sender.class).toProvider(new BukkitSenderProvider<>());
        bind(BlockCommandSender.class).annotatedWith(Sender.class).toProvider(new BukkitSenderProvider<>());
        bind(Player.class).annotatedWith(Sender.class).toProvider(new BukkitSenderProvider<>());
    }
}
