package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Empty extends TokenizedExecutor {

    private final DeliveryRepository deliveries;

    public Empty(final DeliveryRepository deliveries) {
        this.deliveries = deliveries;
    }

    // usage: /<command> <Player>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() == 0) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final String player = Bukkit.getOfflinePlayer(args.get(0)).getName();
        final Delivery active = this.deliveries.load(player);
        if (active != null && !active.getBalance().isEmpty()) active.getBalance().clear();
        Main.courier.send(sender, "empty", player);
        return true;
    }

}
