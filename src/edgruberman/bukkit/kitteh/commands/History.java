package edgruberman.bukkit.kitteh.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import edgruberman.bukkit.kitteh.Main;
import edgruberman.bukkit.kitteh.Manager;

public final class History extends Executor {

    private static final int PAGE_SIZE = 9;

    private final Manager manager;

    public History(final Manager manager) {
        this.manager = manager;
    }

    // usage: /<command>[ <Page>[ <Player>]]
    @Override
    protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 2 && !(sender instanceof Player)) {
            Main.courier.send(sender, "messages.requiresPlayer", label);
            return true;
        }

        if (args.size() >= 2 && !sender.hasPermission("kitteh.history.all")) {
            Main.courier.send(sender, "messages.historyDenied", args.get(0));
            return true;
        }

        final String target = ( args.size() >= 2 ? Bukkit.getOfflinePlayer(args.get(1)).getName() : sender.getName() );

        final ConfigurationSection history = this.manager.history(target);
        if (history == null) {
            Main.courier.send(sender, "messages.historyNone", args.get(0));
            return true;
        }

        // index accessible, newest to oldest
        final List<String> keys = History.asSortedList(history.getKeys(false), Collections.reverseOrder());

        final int pageTotal = (keys.size() / History.PAGE_SIZE) + 1;
        final int pageCurrent = ( args.size() >= 1 ? History.parseInt(args.get(0), 1) : 1 );
        if (pageCurrent <= 0 || pageCurrent > pageTotal) {
            Main.courier.send(sender, "messages.unknownPage", pageCurrent);
            return false;
        }

        final int first = (pageCurrent - 1) * History.PAGE_SIZE;
        final int last = Math.min(first + History.PAGE_SIZE, keys.size()) - 1;

        for (int i = first; i <= last; i++) {
            final ConfigurationSection entry = history.getConfigurationSection(keys.get(i));
            Main.courier.send(sender, "messages.history.line", new Date(Long.parseLong(keys.get(i))), entry.getString("kit"), entry.getInt("quantity"), entry.getString("reason"));
        }

        Main.courier.send(sender, "messages.history.summary", pageCurrent, pageTotal, keys.size());
        return true;
    }

    private static Integer parseInt(final String s, final Integer def) {
        try { return Integer.parseInt(s);
        } catch (final NumberFormatException e) { return def; }
    }

    private static <T extends Comparable<? super T>> List<T> asSortedList(final Collection<T> c, final Comparator<? super T> comp) {
      final List<T> list = new ArrayList<T>(c);
      java.util.Collections.sort(list, comp);
      return list;
    }

}
