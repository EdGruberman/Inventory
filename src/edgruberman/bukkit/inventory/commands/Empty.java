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
        if (args.size() == 0) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final String player = Bukkit.getOfflinePlayer(args.get(0)).getName();
        final Delivery delivery = this.clerk.getDeliveryRepository().load(player);
        if (delivery != null && !delivery.getBalance().isEmpty()) delivery.getBalance().clear();

        boolean use = false;
        delivery.getBalance().clear();
        final boolean trimmed = delivery.getBalance().trim();
        if (trimmed) delivery.relabel();
        for (final Session session : this.clerk.sessionsFor(delivery)) {
            if (trimmed && session.getIndex() != 0) session.next();
            use = true;
        }

        if (use) {
            this.clerk.getDeliveryRepository().save(delivery);
        } else {
            this.clerk.getDeliveryRepository().delete(delivery);
        }

        Main.courier.send(sender, "empty", player);
        return true;
    }

}
