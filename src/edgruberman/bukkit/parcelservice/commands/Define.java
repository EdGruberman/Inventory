package edgruberman.bukkit.parcelservice.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.parcelservice.Main;
import edgruberman.bukkit.parcelservice.repositories.KitRepository;
import edgruberman.bukkit.parcelservice.sessions.KitDefine;
import edgruberman.bukkit.parcelservice.util.TokenizedExecutor;

public final class Define extends TokenizedExecutor {

    private final KitRepository kits;
    private final Plugin plugin;

    public Define(final KitRepository kits, final Plugin plugin) {
        this.kits = kits;
        this.plugin = plugin;
    }

    // usage: /<command> <Kit>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() == 0) {
            Main.courier.send(sender, "requires-argument", "<Kit>");
            return false;
        }

        final edgruberman.bukkit.parcelservice.Kit kit = this.kits.create(args.get(0));
        Bukkit.getPluginManager().registerEvents(new KitDefine((Player) sender, this.kits, kit), this.plugin);
        return true;
    }

}
