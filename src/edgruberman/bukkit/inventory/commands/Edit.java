package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;
import edgruberman.bukkit.inventory.sessions.BalanceEdit;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Edit extends TokenizedExecutor {

    private final DeliveryRepository deliveries;
    private final Plugin plugin;

    public Edit(final DeliveryRepository deliveries, final Plugin plugin) {
        this.deliveries = deliveries;
        this.plugin = plugin;
    }

    // usage: /<command> <Player>[ <Reason>]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return false;
        }

        if (args.size() == 0) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final String reason = ( args.size() >= 2 ? TokenizedExecutor.join(args.subList(1, args.size())) : Main.courier.format("reason-default") );
        final String player = Bukkit.getOfflinePlayer(args.get(0)).getName();
        final Delivery active = this.deliveries.create(player);
        Bukkit.getPluginManager().registerEvents(new BalanceEdit((Player) sender, this.deliveries, active, reason), this.plugin);
        return true;
    }

}
