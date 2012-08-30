package edgruberman.bukkit.kitteh;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.kitteh.util.BufferedYamlConfiguration;

public class Manager {

    private final Plugin plugin;
    private final Map<String, Kit> kits = new HashMap<String, Kit>();
    private final BufferedYamlConfiguration ledger;

    public Manager(final Plugin plugin, final BufferedYamlConfiguration ledger) {
        this.plugin = plugin;
        this.ledger = ledger;
    }

    public void registerKit(final Kit kit) {
        this.kits.put(kit.getName().toLowerCase(), kit);
        this.plugin.getLogger().log(Level.CONFIG, "Registered \"{0}\" kit: [{1}]", new Object[] { kit.getName(), kit.describe() });
    }

    public Kit getKit(final String kit) {
        return this.kits.get(kit.toLowerCase());
    }

    public void give(final Player player, final Kit kit, final int quantity, final String reason) {
        this.plugin.getLogger().log(Level.FINEST, "Giving kit to {0}; {1} ({2}) [{3}]: {4}", new Object[] { player.getName(), kit.getName(), quantity, kit.describe(), reason });
        this.adjust(player.getName(), kit, -quantity, reason);
        for (int i = 0; i < quantity; i++) kit.give(player);
    }

    public void adjust(final String player, final Kit kit, final int quantity, final String reason) {
        if (!this.ledger.isConfigurationSection("players")) this.ledger.createSection("players");
        final ConfigurationSection players = this.ledger.getConfigurationSection("players");

        if (!players.isConfigurationSection(player)) players.createSection(player);
        final ConfigurationSection ledgerPlayer = players.getConfigurationSection(player);

        if (!ledgerPlayer.isConfigurationSection("balance")) ledgerPlayer.createSection("balance");
        final ConfigurationSection balance = ledgerPlayer.getConfigurationSection("balance");

        if (!balance.isInt(kit.getName())) balance.set(kit.getName(), 0);
        int balanceKit = balance.getInt(kit.getName());
        balanceKit += quantity;

        balance.set(kit.getName(), (balanceKit != 0 ? balanceKit : null));

        //if (balance.getKeys(false).size() == 0) ledgerPlayer.set("balance", null);

        // record history
        if (!ledgerPlayer.isConfigurationSection("history")) ledgerPlayer.createSection("history");
        final ConfigurationSection history = ledgerPlayer.getConfigurationSection("history");

        final ConfigurationSection transaction = history.createSection(String.valueOf(System.currentTimeMillis()));
        transaction.set("kit", kit.getName());
        transaction.set("quantity", quantity);
        transaction.set("reason", reason);

        this.ledger.queueSave();
    }

    public TreeMap<Kit, Integer> balance(final String player) {
        final TreeMap<Kit, Integer> balance = new TreeMap<Kit, Integer>();

        final ConfigurationSection players = this.ledger.getConfigurationSection("players");
        if (players == null) return balance;

        final ConfigurationSection ledgerPlayer = players.getConfigurationSection(player);
        if (ledgerPlayer == null) return balance;

        final ConfigurationSection playerBalance = ledgerPlayer.getConfigurationSection("balance");
        if (playerBalance == null) return balance;

        for (final String kitName : playerBalance.getKeys(false)) {
            final Kit kit = this.getKit(kitName);
            if (kit == null) {
                this.plugin.getLogger().warning("Unrecognized kit in " + ledgerPlayer.getCurrentPath() + ": " + kitName);
                continue;
            }

            balance.put(kit, playerBalance.getInt(kitName));
        }

        return balance;
    }

    public int balance(final String player, final Kit kit) {
        final ConfigurationSection players = this.ledger.getConfigurationSection("players");
        if (players == null) return 0;

        final ConfigurationSection ledgerPlayer = players.getConfigurationSection(player);
        if (ledgerPlayer == null) return 0;

        final ConfigurationSection playerBalance = ledgerPlayer.getConfigurationSection("balance");
        if (playerBalance == null) return 0;

        return playerBalance.getInt(kit.getName(), 0);
    }

    public ConfigurationSection history(final String player) {
        final ConfigurationSection players = this.ledger.getConfigurationSection("players");
        if (players == null) return null;

        final ConfigurationSection ledgerPlayer = players.getConfigurationSection(player);
        if (ledgerPlayer == null) return null;

        return ledgerPlayer.getConfigurationSection("history");
    }

}
