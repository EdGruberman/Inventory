package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.KitSession;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Define extends TokenizedExecutor {

    private final Clerk clerk;

    public Define(final Clerk clerk) {
        this.clerk = clerk;
    }

    // usage: /<command> <Kit>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return false;
        }

        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "<Kit>");
            return false;
        }

        final String name = args.get(0);
        edgruberman.bukkit.inventory.Kit kit = this.clerk.getKit(name.toLowerCase());
        if (kit == null) kit = this.clerk.createKit(name);

        this.clerk.openSession(new KitSession((Player) sender, this.clerk, kit));
        return true;
    }

}
