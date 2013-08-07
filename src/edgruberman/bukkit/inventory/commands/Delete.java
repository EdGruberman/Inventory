package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.Session;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Delete extends TokenizedExecutor {

    private final Clerk clerk;

    public Delete(final Clerk clerk) {
        this.clerk = clerk;
    }

    // usage: /<command> <Kit>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 1) {
            Main.courier.send(sender, "requires-argument", "<Kit>");
            return false;
        }

        final edgruberman.bukkit.inventory.Kit kit = this.clerk.getKit(args.get(0));
        if (kit == null) {
            Main.courier.send(sender, "unknown-argument", "<Kit>", args.get(0));
            return true;
        }

        kit.removeAll();
        final boolean trimmed = kit.trim() > 0;
        if (trimmed) kit.formatTitles();

        boolean use = false;
        for (final Session session : this.clerk.sessionsFor(kit)) {
            if (trimmed) session.refresh();
            use = true;
        }
        if (!use) this.clerk.removeKit(kit);

        Main.courier.send(sender, "delete", kit.getName());
        return true;
    }

}
