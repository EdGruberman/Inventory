package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.repositories.KitRepository;
import edgruberman.bukkit.inventory.sessions.KitDefine;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Define extends TokenizedExecutor {

    private final KitRepository kits;
    private final Main plugin;

    public Define(final KitRepository kits, final Main plugin) {
        this.kits = kits;
        this.plugin = plugin;
    }

    // usage: /<command> <Kit>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return false;
        }

        if (args.size() == 0) {
            Main.courier.send(sender, "requires-argument", "<Kit>");
            return false;
        }

        final edgruberman.bukkit.inventory.Kit kit = this.kits.create(args.get(0));
        this.plugin.register(new KitDefine((Player) sender, this.kits, kit));
        return true;
    }

}
