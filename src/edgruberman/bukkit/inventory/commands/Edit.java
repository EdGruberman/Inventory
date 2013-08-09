package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.DeliveryInventory;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.EditSession;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Edit extends TokenizedExecutor {

    private final Clerk clerk;
    private final String title;

    public Edit(final Clerk clerk, final String title) {
        this.clerk = clerk;
        this.title = title;
    }

    // usage: /<command> player
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return false;
        }

        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "player", 0);
            return false;
        }

        final String target = Bukkit.getOfflinePlayer(args.get(0)).getName();
        InventoryList delivery = this.clerk.getInventory(DeliveryInventory.class, target);
        if (delivery == null) {
            delivery = new DeliveryInventory(target);
            this.clerk.putInventory(delivery);
        }

        this.clerk.openSession(new EditSession((Player) sender, this.clerk, delivery, this.title));
        return true;
    }

}
