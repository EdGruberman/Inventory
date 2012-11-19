package edgruberman.bukkit.take;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.take.commands.Add;
import edgruberman.bukkit.take.commands.Log;
import edgruberman.bukkit.take.commands.Reload;
import edgruberman.bukkit.take.commands.Show;
import edgruberman.bukkit.take.commands.Take;
import edgruberman.bukkit.take.messaging.ConfigurationCourier;
import edgruberman.bukkit.take.util.BufferedYamlConfiguration;
import edgruberman.bukkit.take.util.CustomPlugin;
import edgruberman.bukkit.take.util.ItemData;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;

    @Override
    public void onLoad() { this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "2.0.0"); }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.create(this).setPath("messages").setColorCode("colorCode").build();

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
