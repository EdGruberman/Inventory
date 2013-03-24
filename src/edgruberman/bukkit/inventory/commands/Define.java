package edgruberman.bukkit.inventory.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.KitSession;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Define extends TokenizedExecutor {

    private final Clerk clerk;

    public Define(final Clerk clerk) {
        this.clerk = clerk;
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

        final String name = args.get(0);
        edgruberman.bukkit.inventory.Kit kit = this.clerk.getKitRepository().load(args.get(0));
        if (kit == null && !Define.isFilenameValid(name)) {
            Main.courier.send(sender, "define-invalid", args.get(0));
            return true;
        }

        if (kit == null) kit = this.clerk.getKitRepository().create(args.get(0));
        this.clerk.startSession(new KitSession((Player) sender, this.clerk.getKitRepository(), kit));
        return true;
    }

    private static boolean isFilenameValid(final String name) {
        final File f = new File(name);
        try {
           f.getCanonicalPath();
           return true;
        }
        catch (final IOException e) {
           return false;
        }
    }

}
