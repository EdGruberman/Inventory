package edgruberman.bukkit.kitteh.commands;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.kitteh.Kit;
import edgruberman.bukkit.kitteh.Main;
import edgruberman.bukkit.kitteh.Manager;

public final class Show extends Executor {

    private final Manager manager;

    public Show(final Manager manager) {
        this.manager = manager;
    }

    // usage: /<command>[ <Player>]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() == 0 && !(sender instanceof Player)) {
            Main.courier.send(sender, "messages.requiresPlayer", label);
            return true;
        }

        if (args.size() >= 1 && !sender.hasPermission("kitteh.show.all")) {
            Main.courier.send(sender, "messages.showDenied", args.get(0));
            return true;
        }

        final String target = (args.size() >= 1 ? Bukkit.getOfflinePlayer(args.get(0)).getName() : sender.getName());
        final Map<Kit, Integer> balance = this.manager.balance(target);
        if (balance.size() == 0) {
            Main.courier.send(sender, "messages.showNone", target);
            return true;
        }

        for (final Map.Entry<Kit, Integer> available : balance.entrySet())
            Main.courier.send(sender, "messages.show.kit", available.getKey().getName(), available.getValue(), available.getKey().describe().toString());

        Main.courier.send(sender, "messages.show.instruction");
        return true;
    }

}
