package tk.martijn_heil.wac_core.command.common.bukkit.provider;


import com.sk89q.intake.parametric.AbstractModule;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import tk.martijn_heil.wac_core.command.common.TargetOrSender;

public class BukkitModule extends AbstractModule
{
    private final Server server;


    public BukkitModule(Server server)
    {
        this.server = server;
    }

    @Override
    protected void configure()
    {
        bind(Server.class).toInstance(server);
        bind(Player.class).toProvider(new PlayerProvider(server, false));

        bind(Player.class).annotatedWith(TargetOrSender.class).toProvider(new PlayerProvider(server, true));
    }
}
