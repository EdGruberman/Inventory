package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.Session;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Empty extends TokenizedExecutor {

    private final Clerk clerk;

    public Empty(final Clerk clerk) {
        this.clerk = clerk;
    }

    // usage: /<command> <Player>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final String player = Bukkit.getOfflinePlayer(args.get(0)).getName();
        final Delivery delivery = this.clerk.getDelivery(player.toLowerCase());
        if (delivery != null && !delivery.isContentsEmpty()) delivery.removeAll();
        final boolean trimmed = delivery.trim() > 0;
        if (trimmed) delivery.formatTitles();

        boolean use = false;
        for (final Session session : this.clerk.sessionsFor(delivery)) {
            if (trimmed) session.refresh();
            use = true;
        }
        if (!use) this.clerk.removeDelivery(delivery);

        Main.courier.send(sender, "empty", delivery.getName());
        return true;
    }

}
