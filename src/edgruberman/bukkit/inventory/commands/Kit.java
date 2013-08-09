package edgruberman.bukkit.inventory.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.DeliveryInventory;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.KitInventory;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.Session;
import edgruberman.bukkit.inventory.util.ItemStackExecutor;

public final class Kit extends ItemStackExecutor {

    private final Clerk clerk;
    private final String title;

    public Kit(final Clerk clerk, final ConfigurationSection listFormat, final String title) {
        super(listFormat);
        this.clerk = clerk;
        this.title = title;
    }

    // usage: /<command> kit [player] [quantity]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "kit", 0);
            return false;
        }

        if ((args.size() < 2) && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-argument", "player", 0);
            return false;
        }

        final InventoryList kit = this.clerk.getInventory(KitInventory.class, args.get(0));
        if (kit == null) {
            Main.courier.send(sender, "unknown-argument", "kit", 0, args.get(0));
            return false;
        }

        // try to ensure proper case for player name since new Delivery could be created
        final String player = ( args.size() >= 2 ? Bukkit.getOfflinePlayer(args.get(1)).getName() : sender.getName() );

        int quantity = 1;
        if (args.size() >= 3) {
            try {
                quantity = Integer.parseInt(args.get(2));
            } catch(final Exception e) {
                Main.courier.send(sender, "unknown-argument", "quantity", 0, args.get(2));
                return false;
            }
        }

        final List<ItemStack> changes = kit.getContents();
        if (quantity != 1) {
            // clone contents stack by stack to respect stack sizes provided
            final List<ItemStack> multiplied = new ArrayList<ItemStack>();
            for (int i = 0; i < Math.abs(quantity) - 1; i++) {
                for (final ItemStack change : changes) {
                    final ItemStack clone = change.clone();
                    clone.setAmount(clone.getAmount() * (int) Math.signum(quantity));
                    multiplied.add(clone);
                }
            }
            changes.addAll(multiplied);
        }

        InventoryList delivery = this.clerk.getInventory(DeliveryInventory.class, player);
        if (delivery == null) {
            delivery = new DeliveryInventory(player);
            this.clerk.putInventory(delivery);
        }

        final int before = delivery.size();
        final Collection<ItemStack> failures = delivery.modify(changes);
        if (delivery.size() != before) {
            delivery.formatTitles(this.title, delivery.getName());
            // TODO refresh sessions
        }

        for (final ItemStack stack : failures) stack.setAmount(stack.getAmount() * -1);
        this.clerk.putInventory(delivery);

        for (final Session session : this.clerk.sessionsFor(delivery)) session.refresh();

        Main.courier.send(sender, "kit", kit.getName(), delivery.getName(), quantity, failures.size());
        if (!failures.isEmpty()) Main.courier.send(sender, "failures", kit.getName(), delivery.getName(), this.summarize(failures), failures.size());
        return true;
    }

}
