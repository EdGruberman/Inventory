package edgruberman.bukkit.inventory.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.Transaction;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;
import edgruberman.bukkit.inventory.repositories.KitRepository;
import edgruberman.bukkit.inventory.util.ItemStackUtil;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Kit extends TokenizedExecutor {

    private final KitRepository kits;
    private final DeliveryRepository deliveries;

    public Kit(final KitRepository kits, final DeliveryRepository deliveries) {
        this.kits = kits;
        this.deliveries = deliveries;
    }

    // usage: /<command> <Kit>[ <Player>[ <Quantity>[ <Reason>]]]
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

        final edgruberman.bukkit.inventory.Kit kit = this.kits.load(args.get(0));
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

        final List<ItemStack> changes = kit.getContents().items();
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

        final String provided = ( args.size() >= 4 ? TokenizedExecutor.join(args.subList(3, args.size())) : Main.courier.format("reason-default") );
        final String reason = Main.courier.format("reason-kit", provided, kit.getName(), quantity);
        final Transaction transaction = new Transaction(new Date(), sender, reason, changes);

        final Delivery target = this.deliveries.create(player);
        final Collection<ItemStack> failures = target.modifyBalance(transaction.getChanges());
        for (final ItemStack stack : failures) stack.setAmount(stack.getAmount() * -1);
        transaction.getFailures().addAll(failures);
        target.record(transaction);
        this.deliveries.save(target);

        Main.courier.send(sender, "kit", kit.getName(), target.getPlayer(), quantity, reason, transaction.getFailures().isEmpty()?0:1 );
        if (!transaction.getFailures().isEmpty()) Main.courier.send(sender, "failures", transaction.getFailures().size(), target.getPlayer(), ItemStackUtil.summarize(transaction.getFailures()));

        return true;
    }

}
