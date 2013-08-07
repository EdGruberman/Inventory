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
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.Session;
import edgruberman.bukkit.inventory.util.ItemStackExecutor;

public final class Kit extends ItemStackExecutor {

    private final Clerk clerk;

    public Kit(final Clerk clerk, final ConfigurationSection listFormat) {
        super(listFormat);
        this.clerk = clerk;
    }

    // usage: /<command> <Kit>[ <Player>[ <Quantity>]]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "<Kit>");
            return false;
        }

        if ((args.size() < 2) && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final InventoryList kit = this.clerk.getKit(args.get(0).toLowerCase());
        if (kit == null) {
            Main.courier.send(sender, "unknown-argument", "<Kit>", args.get(0));
            return false;
        }

        // try to ensure proper case for player name since new Delivery could be created
        final String player = ( args.size() >= 2 ? Bukkit.getOfflinePlayer(args.get(1)).getName() : sender.getName() );

        int quantity = 1;
        if (args.size() >= 3) {
            try {
                quantity = Integer.parseInt(args.get(2));
            } catch(final Exception e) {
                Main.courier.send(sender, "unknown-argument", "<Quantity>", args.get(2));
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

        InventoryList target = this.clerk.getDelivery(player.toLowerCase());
        if (target == null) target = this.clerk.createDelivery(player);

        final int before = target.size();
        final Collection<ItemStack> failures = target.modify(changes);
        if (target.size() != before) {
            target.formatTitles(this.clerk.getKitTitle(), target.getName());
            // TODO refresh sessions
        }

        for (final ItemStack stack : failures) stack.setAmount(stack.getAmount() * -1);
        this.clerk.putDelivery(target);

        for (final Session session : this.clerk.sessionsFor(target)) session.refresh();

        Main.courier.send(sender, "kit", kit.getName(), target.getName(), quantity, failures.size());
        if (!failures.isEmpty()) Main.courier.send(sender, "failures", kit.getName(), target.getName(), this.summarize(failures), failures.size());
        return true;
    }

}
