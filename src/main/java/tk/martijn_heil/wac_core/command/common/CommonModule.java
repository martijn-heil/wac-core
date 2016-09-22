package tk.martijn_heil.wac_core.command.common;


import com.sk89q.intake.parametric.AbstractModule;
import tk.martijn_heil.elytraoptions.command.common.provider.ToggleProvider;

public class CommonModule extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind(Boolean.class).annotatedWith(Toggle.class).toProvider(new ToggleProvider());
    }
}
