package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.DeliverySession;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Edit extends TokenizedExecutor {

    private final Clerk clerk;

    public Edit(final Clerk clerk) {
        this.clerk = clerk;
    }

    // usage: /<command> <Player>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return false;
        }

        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final String target = Bukkit.getOfflinePlayer(args.get(0)).getName();
        final Delivery active = this.clerk.getDelivery(target.toLowerCase());
        if (active == null) this.clerk.createDelivery(target);

        this.clerk.openSession(new DeliverySession((Player) sender, this.clerk, active));
        return true;
    }

}
