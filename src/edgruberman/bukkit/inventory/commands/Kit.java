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
import edgruberman.bukkit.inventory.Delivery;
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
        if (args.size() == 0) {
            Main.courier.send(sender, "requires-argument", "<Kit>");
            return false;
        }

        if ((args.size() < 2) && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final edgruberman.bukkit.inventory.Kit kit = this.clerk.getKitRepository().get(args.get(0));
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

        final List<ItemStack> changes = kit.getList().getContents();
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

        final Delivery target = this.clerk.getDeliveryRepository().create(player);
        final Collection<ItemStack> failures = target.getList().modify(changes);
        for (final ItemStack stack : failures) stack.setAmount(stack.getAmount() * -1);
        this.clerk.getDeliveryRepository().put(target);

        for (final Session session : this.clerk.sessionsFor(target)) session.refresh();

        Main.courier.send(sender, "kit", kit.getList().getKey(), target.getList().getKey(), quantity, failures.size());
        if (!failures.isEmpty()) Main.courier.send(sender, "failures", kit.getList().getKey(), target.getList().getKey(), this.summarize(failures), failures.size());
        return true;
    }

}
