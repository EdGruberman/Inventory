package edgruberman.bukkit.kitteh;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import edgruberman.bukkit.kitteh.commands.Adjust;
import edgruberman.bukkit.kitteh.commands.History;
import edgruberman.bukkit.kitteh.commands.Reload;
import edgruberman.bukkit.kitteh.commands.Show;
import edgruberman.bukkit.kitteh.commands.Take;
import edgruberman.bukkit.kitteh.messaging.ConfigurationCourier;
import edgruberman.bukkit.kitteh.messaging.Courier;
import edgruberman.bukkit.kitteh.util.BufferedYamlConfiguration;
import edgruberman.bukkit.kitteh.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static Courier courier;

    @Override
    public void onLoad() { this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "1.1.0"); }

    @Override
    public void onEnable() {
        Bukkit.getOfflinePlayers(); // set casing for offline player names based on previous connections

        this.reloadConfig();
        Main.courier = new ConfigurationCourier(this);

        final BufferedYamlConfiguration ledger = this.loadConfig(new File(this.getDataFolder(), "ledger.yml"));
        final Manager manager = new Manager(this, ledger);

        final ConfigurationSection kitsConfig = this.getConfig().getConfigurationSection("kits");
        for (final String name : kitsConfig.getKeys(false)) {
            final ConfigurationSection items = kitsConfig.getConfigurationSection(name).getConfigurationSection("items");
            final Kit kit = new Kit(name);
            for (final String materialData : items.getKeys(false)) {
                try { kit.add(materialData, items.getInt(materialData));
                } catch (final Exception e) {
                    this.getLogger().warning("Unable to add \"" + materialData + "\" to kit \"" + name + "\"; " + e);
                }
            }
            manager.registerKit(kit);
        }

        final Show show = new Show(manager);
        this.getCommand("kitteh:show").setExecutor(show);
        this.getCommand("kitteh:take").setExecutor(new Take(manager, show));
        this.getCommand("kitteh:history").setExecutor(new History(manager));
        this.getCommand("kitteh:adjust").setExecutor(new Adjust(manager));
        this.getCommand("kitteh:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        Main.courier = null;
    }

    private BufferedYamlConfiguration loadConfig(final File source) {
        final BufferedYamlConfiguration config = new BufferedYamlConfiguration(this, source, 5000);
        try {
            return config.load();
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to load configuration file: " + source, e);
        }
    }

}
