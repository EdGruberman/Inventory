package edgruberman.bukkit.inventory.commands;

import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.inventory.Ledger;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.Transaction;
import edgruberman.bukkit.inventory.repositories.LedgerRepository;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Empty extends TokenizedExecutor {

    private final LedgerRepository ledgers;

    public Empty(final LedgerRepository ledgers) {
        this.ledgers = ledgers;
    }

    // usage: /<command> <Player>[ <Reason>]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() == 0) {
            Main.courier.send(sender, "requires-argument", "<Player>");
            return false;
        }

        final String reason = ( args.size() >= 2 ? TokenizedExecutor.join(args.subList(1, args.size())) : Main.courier.format("reason-default") );
        final String player = Bukkit.getOfflinePlayer(args.get(0)).getName();
        final Ledger active = this.ledgers.load(player);
        if (active != null && !active.getBalance().isEmpty()) {
            active.record(new Transaction(new Date(), sender.getName(), reason, active.getBalance().joined()));
            active.getBalance().clear();
        }

        Main.courier.send(sender, "empty", player);
        return true;
    }

}
