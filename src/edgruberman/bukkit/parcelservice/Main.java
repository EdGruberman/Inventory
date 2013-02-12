package edgruberman.bukkit.parcelservice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.parcelservice.commands.Add;
import edgruberman.bukkit.parcelservice.commands.Log;
import edgruberman.bukkit.parcelservice.commands.Reload;
import edgruberman.bukkit.parcelservice.commands.Show;
import edgruberman.bukkit.parcelservice.commands.Take;
import edgruberman.bukkit.parcelservice.messaging.ConfigurationCourier;
import edgruberman.bukkit.parcelservice.util.BufferedYamlConfiguration;
import edgruberman.bukkit.parcelservice.util.CustomPlugin;
import edgruberman.bukkit.parcelservice.util.ItemData;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;

    @Override
    public void onLoad() {
        this.putConfigMinimum("3.0.0a0");
        this.putConfigMinimum("language.yml", "3.0.0a0");
    }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.create(this).setBase(this.loadConfig("language.yml")).setFormatCode("format-code").build();

        final BufferedYamlConfiguration ledger = this.loadConfig(new File(this.getDataFolder(), "ledger.yml"));
        final Manager manager = new Manager(this, ledger);

        final ConfigurationSection kitsConfig = this.getConfig().getConfigurationSection("kits");
        for (final String name : kitsConfig.getKeys(false)) {
            try {
                manager.registerKit(Main.parseKit(kitsConfig.getConfigurationSection(name)));
            } catch (final Exception e) {
                this.getLogger().warning("Unable to add \"" + name + "\" kit; " + e);
            }
        }

        final Show show = new Show(manager);
        this.getCommand("parcelservice:show").setExecutor(show);
        this.getCommand("parcelservice:take").setExecutor(new Take(manager, show));
        this.getCommand("parcelservice:log").setExecutor(new Log(manager));
        this.getCommand("parcelservice:add").setExecutor(new Add(manager));
        this.getCommand("parcelservice:reload").setExecutor(new Reload(this));
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

    private static Kit parseKit(final ConfigurationSection kit) {
        final ConfigurationSection contents = kit.getConfigurationSection("contents");
        final List<ItemStack> parsed = new ArrayList<ItemStack>();
        for (final String key : contents.getKeys(false)) {
            final ItemData data = ItemData.parse(key);
            final Integer amount = contents.getInt(key);
            final ItemStack stack = data.toItemStack(amount);
            parsed.add(stack);
        }

        return new Kit(kit.getName(), parsed);
    }

}
