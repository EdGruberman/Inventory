package edgruberman.bukkit.parcelservice.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.parcelservice.Kit;
import edgruberman.bukkit.parcelservice.Main;
import edgruberman.bukkit.parcelservice.Manager;
import edgruberman.bukkit.parcelservice.util.TokenizedExecutor;

public final class Take extends TokenizedExecutor {

    private final Manager manager;
    private final Show show;

    public Take(final Manager manager, final Show show) {
        this.manager = manager;
        this.show = show;
    }

    // usage: /<command> <Kit>[ <Quantity>]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return true;
        }

        if (args.size() < 1) return this.show.onCommand(sender, command, label, args);

        final Kit kit = this.manager.getKit(args.get(0));
        if (kit == null) {
            Main.courier.send(sender, "unknown-kit", args.get(0));
            return true;
        }

        Integer requested = null;
        if (args.size() >= 2) {
            requested = Take.parseInt(args.get(1), null);
            if (requested == null || requested <= 0) {
                Main.courier.send(sender, "unknown-argument", "<Quantity>", args.get(1));
                return false;
            }
        }

        final int balance = this.manager.balance(sender.getName(), kit);
        if (requested == null) requested = balance;
        if (balance <= 0 || requested > balance) {
            System.out.println(balance + " " + requested);
            Main.courier.send(sender, "take-unavailable", kit.getName(), balance, requested);
            return true;
        }

        this.manager.give((Player) sender, kit, requested, "Received");
        return true;
    }

    private static Integer parseInt(final String s, final Integer def) {
        try { return Integer.parseInt(s);
        } catch (final NumberFormatException e) { return def; }
    }

}
