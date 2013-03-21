package edgruberman.bukkit.deliver.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.deliver.Main;
import edgruberman.bukkit.deliver.repositories.KitRepository;
import edgruberman.bukkit.deliver.util.TokenizedExecutor;

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

        final edgruberman.bukkit.deliver.Kit kit = this.kits.load(args.get(0));
        if (kit == null) {
            Main.courier.send(sender, "unknown-argument", "<Kit>", args.get(0));
            return true;
        }

        final Player definer = kit.getDefiner();
        if (definer != null) {
            Main.courier.send(sender, "define-wait", kit.getName(), kit.getDefiner().getDisplayName());
            return true;
        }

        this.kits.delete(kit);
        Main.courier.send(sender, "delete", kit.getName());
        return true;
    }

}
