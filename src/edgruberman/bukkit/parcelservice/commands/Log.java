package edgruberman.bukkit.parcelservice.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import edgruberman.bukkit.parcelservice.Main;
import edgruberman.bukkit.parcelservice.Manager;
import edgruberman.bukkit.parcelservice.util.TokenizedExecutor;

public final class Log extends TokenizedExecutor {

    private static final int PAGE_SIZE = 10;

    private final Manager manager;

    public Log(final Manager manager) {
        this.manager = manager;
    }

    // usage: /<command>[ <Page>[ <Player>]]
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (args.size() < 2 && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return true;
        }

        if (args.size() >= 2 && !sender.hasPermission("take.log.all")) {
            Main.courier.send(sender, "log-denied", args.get(1));
            return true;
        }

        final String target = ( args.size() >= 2 ? Bukkit.getOfflinePlayer(args.get(1)).getName() : sender.getName() );

        final ConfigurationSection history = this.manager.log(target);
        if (history == null) {
            Main.courier.send(sender, "log-none", target);
            return true;
        }

        // index accessible, newest to oldest
        final List<String> keys = Log.asSortedList(history.getKeys(false), Collections.reverseOrder());

        final int headerSize = Main.courier.compose("log.header", target).toString().split("\\n").length;
        final int footerSize = Main.courier.compose("log.footer").toString().split("\\n").length;
        final int lineCount = Log.PAGE_SIZE - headerSize - footerSize;

        final int pageTotal = (keys.size() / lineCount) + ( keys.size() % lineCount > 0 ? 1 : 0 );
        final int pageCurrent = ( args.size() >= 1 ? Log.parseInt(args.get(0), 1) : 1 );
        if (pageCurrent <= 0 || pageCurrent > pageTotal) {
            Main.courier.send(sender, "unknown-page", pageCurrent);
            return false;
        }

        final int first = (pageCurrent - 1) * lineCount;
        final int last = Math.min(first + lineCount, keys.size()) - 1;

        Main.courier.send(sender, "log.header", target);

        for (int i = first; i <= last; i++) {
            final ConfigurationSection entry = history.getConfigurationSection(keys.get(i));
            Main.courier.send(sender, "log.line", Manager.parseDate(keys.get(i)), entry.getString("kit"), entry.getInt("quantity"), entry.getString("reason"));
        }

        Main.courier.send(sender, "log.footer", pageCurrent, pageTotal, keys.size());
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
