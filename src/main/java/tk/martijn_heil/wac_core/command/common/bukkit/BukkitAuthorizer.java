package tk.martijn_heil.wac_core.command.common.bukkit;


import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.util.auth.Authorizer;
import org.bukkit.permissions.Permissible;

public class BukkitAuthorizer implements Authorizer
{

    @Override
    public boolean testPermission(Namespace namespace, String perm)
    {
        Permissible permissible = namespace.get(Permissible.class);
        return permissible != null && permissible.hasPermission(perm);
    }
}
