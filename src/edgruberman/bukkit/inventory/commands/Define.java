package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.KitInventory;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.EditSession;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Define extends TokenizedExecutor {

    private final Clerk clerk;
    private final String title;

    public Define(final Clerk clerk, final String title) {
        this.clerk = clerk;
        this.title = title;
    }

    // usage: /<command> kit
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return false;
        }

        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "kit", 0);
            return false;
        }

        final String name = args.get(0);
        InventoryList kit = this.clerk.getInventory(KitInventory.class, name);
        if (kit == null) {
            kit = KitInventory.create(name, this.title);
            this.clerk.putInventory(kit);
        }

        this.clerk.openSession(new EditSession((Player) sender, this.clerk, kit, this.title));
        return true;
    }

}
