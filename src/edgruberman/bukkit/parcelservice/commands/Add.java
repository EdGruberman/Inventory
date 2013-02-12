package edgruberman.bukkit.parcelservice.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.parcelservice.Kit;
import edgruberman.bukkit.parcelservice.Main;
import edgruberman.bukkit.parcelservice.Manager;

public final class Add extends Executor {

    private final Manager manager;

    public Add(final Manager manager) {
        this.manager = manager;
    }

    // usage: /<command> <Kit> <Player> <Quantity>[ <Reason>]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "<Kit>");
            return false;
        }

        if (args.size() < 2) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        if (args.size() < 3) {
            Main.courier.send(sender, "requires-argument", "<Quantity>");
            return false;
        }

        final Kit kit = this.manager.getKit(args.get(0));
        if (kit == null) {
            Main.courier.send(sender, "unknown-kit", args.get(0));
            return false;
        }

        final Integer quantity = Add.parseInt(args.get(2), null);
        if (quantity == null) {
            Main.courier.send(sender, "unknown-argument", "<Quantity>", args.get(2));
            return false;
        }

        final String player = Bukkit.getOfflinePlayer(args.get(1)).getName();
        final String reason = ( args.size() >= 4 ? ChatColor.translateAlternateColorCodes('&', args.get(3)) : "{" + sender.getName() + "}");
        this.manager.adjust(player, kit, quantity, reason);
        final int balance = this.manager.balance(player, kit);
        Main.courier.send(sender, "adjust", player, kit.getName(), quantity, reason, balance);
        return true;
    }

    private static Integer parseInt(final String s, final Integer def) {
        try { return Integer.parseInt(s);
        } catch (final NumberFormatException e) { return def; }
    }

}
