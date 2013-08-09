package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.util.ItemStackExecutor;

public final class Copy extends ItemStackExecutor {

    // usage: /<command> [player]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return false;
        }

        final Player target = (args.size() >= 1 ? Bukkit.getPlayerExact(args.get(0)) : (Player) sender );
        if (target == null) {
            Main.courier.send(sender, "unknown-argument", "player", 0, args.get(0));
            return true;
        }

        final int slot = target.getInventory().firstEmpty();
        if (slot == -1) {
            Main.courier.send(sender, "full", target.getName());
            return true;
        }

        final Player source = (Player) sender;
        final ItemStack clone = source.getItemInHand().clone();
        target.getInventory().setItem(slot, clone);
        Main.courier.send(sender, "copy", target.getName(), ItemStackExecutor.summarize(clone));
        return true;
    }

}
