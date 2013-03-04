package edgruberman.bukkit.delivery.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.delivery.Ledger;
import edgruberman.bukkit.delivery.Main;
import edgruberman.bukkit.delivery.repositories.LedgerRepository;
import edgruberman.bukkit.delivery.sessions.BalanceEdit;
import edgruberman.bukkit.delivery.util.TokenizedExecutor;

public final class Edit extends TokenizedExecutor {

    private final LedgerRepository ledgers;
    private final Plugin plugin;

    public Edit(final LedgerRepository ledgers, final Plugin plugin) {
        this.ledgers = ledgers;
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

        final String provided = ( args.size() >= 2 ? TokenizedExecutor.join(args.subList(1, args.size())) : Main.courier.format("edit-default-reason") );
        final String reason = Main.courier.format("edit-reason-format", sender.getName(), provided);

        final String player = Bukkit.getOfflinePlayer(args.get(0)).getName();
        final Ledger active = this.ledgers.create(player);
        Bukkit.getPluginManager().registerEvents(new BalanceEdit((Player) sender, this.ledgers, active, reason), this.plugin);
        return true;
    }

}