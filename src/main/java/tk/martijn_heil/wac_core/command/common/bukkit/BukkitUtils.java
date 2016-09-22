package tk.martijn_heil.wac_core.command.common.bukkit;


import com.sk89q.intake.dispatcher.Dispatcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public class BukkitUtils
{
    public static void registerDispatcher(Dispatcher dispatcher, Plugin plugin)
    {
        try
        {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.register(plugin.getName().toLowerCase(), RootCommand.of(dispatcher));
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }
}
