package edgruberman.bukkit.parcelservice.commands;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.parcelservice.Ledger;
import edgruberman.bukkit.parcelservice.Main;
import edgruberman.bukkit.parcelservice.Transaction;
import edgruberman.bukkit.parcelservice.repositories.KitRepository;
import edgruberman.bukkit.parcelservice.repositories.LedgerRepository;
import edgruberman.bukkit.parcelservice.util.ItemStackUtil;
import edgruberman.bukkit.parcelservice.util.TokenizedExecutor;

public final class Kit extends TokenizedExecutor {

    private final KitRepository kits;
    private final LedgerRepository ledgers;

    public Kit(final KitRepository kits, final LedgerRepository ledgers) {
        this.kits = kits;
        this.ledgers = ledgers;
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

        final edgruberman.bukkit.parcelservice.Kit kit = this.kits.load(args.get(0));
        if (kit == null) {
            Main.courier.send(sender, "unknown-argument", "<Kit>", args.get(0));
            return false;
        }

        // try to ensure proper case for player name since new Ledger could be created
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
        final List<ItemStack> changes = kit.getContents().joined();
        if (quantity != 1) ItemStackUtil.multiplyAmount(changes, quantity);

        final String provided = ( args.size() >= 4 ? TokenizedExecutor.join(args.subList(3, args.size())) : Main.courier.format("kit-default-reason") );
        final String reason = Main.courier.format("kit-reason-format", sender.getName(), provided, kit.getName(), quantity);

        final Ledger target = this.ledgers.create(player);
        final Transaction transaction = new Transaction(new Date(), reason, changes);

        final Collection<ItemStack> failures = target.modifyBalance(transaction.getChanges());
        transaction.getFailures().addAll(ItemStackUtil.multiplyAmount(failures , -1));
        target.record(transaction);
        this.ledgers.save(target);

        Main.courier.send(sender, "kit", kit.getName(), target.getPlayer(), quantity, reason, transaction.getFailures().isEmpty()?0:1 );
        if (!transaction.getFailures().isEmpty()) Main.courier.send(sender, "failures", transaction.getFailures().size(), target.getPlayer(), ItemStackUtil.summarize(transaction.getFailures()));

        return true;
    }

}
