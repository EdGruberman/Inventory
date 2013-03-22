package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.repositories.KitRepository;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Delete extends TokenizedExecutor {

    private final KitRepository kits;

    public Delete(final KitRepository kits) {
        this.kits = kits;
    }

    // usage: /<command> <Kit>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() == 0) {
            Main.courier.send(sender, "requires-argument", "<Kit>");
            return false;
        }

        final edgruberman.bukkit.inventory.Kit kit = this.kits.load(args.get(0));
        if (kit == null) {
            Main.courier.send(sender, "unknown-argument", "<Kit>", args.get(0));
            return true;
        }

        this.kits.delete(kit);
        Main.courier.send(sender, "delete", kit.getName());
        return true;
    }

}
