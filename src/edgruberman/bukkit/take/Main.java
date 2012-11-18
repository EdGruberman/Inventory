package edgruberman.bukkit.take;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import edgruberman.bukkit.take.commands.Add;
import edgruberman.bukkit.take.commands.Log;
import edgruberman.bukkit.take.commands.Reload;
import edgruberman.bukkit.take.commands.Show;
import edgruberman.bukkit.take.commands.Take;
import edgruberman.bukkit.take.messaging.ConfigurationCourier;
import edgruberman.bukkit.take.util.BufferedYamlConfiguration;
import edgruberman.bukkit.take.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;

    @Override
    public void onLoad() { this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "2.0.0a0"); }

    @Override
    public void onEnable() {
        Bukkit.getOfflinePlayers(); // set casing for offline player names based on previous connections

        this.reloadConfig();
        Main.courier = ConfigurationCourier.Factory.create(this).setPath("messages").setColorCode("colorCode").build();

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
        this.getCommand("take:show").setExecutor(show);
        this.getCommand("take:take").setExecutor(new Take(manager, show));
        this.getCommand("take:log").setExecutor(new Log(manager));
        this.getCommand("take:add").setExecutor(new Add(manager));
        this.getCommand("take:reload").setExecutor(new Reload(this));
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
