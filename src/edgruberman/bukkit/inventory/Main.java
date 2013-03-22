package edgruberman.bukkit.inventory;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import edgruberman.bukkit.inventory.commands.Copy;
import edgruberman.bukkit.inventory.commands.Define;
import edgruberman.bukkit.inventory.commands.Delete;
import edgruberman.bukkit.inventory.commands.Edit;
import edgruberman.bukkit.inventory.commands.Empty;
import edgruberman.bukkit.inventory.commands.Move;
import edgruberman.bukkit.inventory.commands.Reload;
import edgruberman.bukkit.inventory.commands.Withdraw;
import edgruberman.bukkit.inventory.craftbukkit.CraftBukkit;
import edgruberman.bukkit.inventory.messaging.ConfigurationCourier;
import edgruberman.bukkit.inventory.repositories.BufferedYamlRepository;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;
import edgruberman.bukkit.inventory.repositories.KitRepository;
import edgruberman.bukkit.inventory.util.CustomPlugin;
import edgruberman.bukkit.inventory.util.ItemStackUtil;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;
    public static CraftBukkit craftBukkit = null;

    private KitRepository kits = null;
    private DeliveryRepository deliveries = null;

    @Override
    public void onLoad() {
        this.putConfigMinimum("config.yml", "3.0.0");
        this.putConfigMinimum("language.yml", "4.0.0a1");
    }

    @Override
    public void onEnable() {
        try {
            Main.craftBukkit = CraftBukkit.create();
        } catch (final Exception e) {
            this.getLogger().log(Level.SEVERE, "Unsupported CraftBukkit version {0}; {1}", new Object[] { Bukkit.getVersion(), e });
            this.getLogger().log(Level.SEVERE, "Disabling plugin; Dependencies not met; Check for updates at: {0}", this.getDescription().getWebsite());
            this.setEnabled(false);
            return;
        }

        this.reloadConfig();
        Main.courier = ConfigurationCourier.create(this).setBase(this.loadConfig("language.yml")).setFormatCode("format-code").build();
        ItemStackUtil.setFormat(Main.courier.getSection("items-summary"));

        final BufferedYamlRepository<Kit> yamlKits = this.initializeRepository("kits.yml");
        this.kits = ( yamlKits != null ? new KitRepository(yamlKits) : null);

        final BufferedYamlRepository<Delivery> yamlDeliveries = this.initializeRepository("deliveries.yml");
        this.deliveries = ( yamlDeliveries != null ? new DeliveryRepository(yamlDeliveries) : null);

        if (this.kits == null || this.deliveries == null) {
            this.getLogger().log(Level.SEVERE, "Disabling plugin; Unusable repository");
            this.setEnabled(false);
            return;
        }

        final boolean record = this.getConfig().getBoolean("record-withdrawals");
        final Withdraw withdraw = new Withdraw(this.deliveries, this, record);
        Bukkit.getPluginManager().registerEvents(withdraw, this);

        this.getCommand("inventory:withdraw").setExecutor(withdraw);
        this.getCommand("inventory:edit").setExecutor(new Edit(this.deliveries, this));
        this.getCommand("inventory:empty").setExecutor(new Empty(this.deliveries));
        this.getCommand("inventory:define").setExecutor(new Define(this.kits, this));
        this.getCommand("inventory:kit").setExecutor(new edgruberman.bukkit.inventory.commands.Kit(this.kits, this.deliveries));
        this.getCommand("inventory:delete").setExecutor(new Delete(this.kits));
        this.getCommand("inventory:move").setExecutor(new Move());
        this.getCommand("inventory:copy").setExecutor(new Copy());
        this.getCommand("inventory:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        if (this.kits != null) this.kits.destroy();
        if (this.deliveries != null) this.deliveries.destroy();
        Main.courier = null;
        Main.craftBukkit = null;
    }

    private <T extends ConfigurationSerializable> BufferedYamlRepository<T> initializeRepository(final String file) {
        final File source = new File(this.getDataFolder(), file);
        try {
            return new BufferedYamlRepository<T>(this, source, 30000);

        } catch (final Exception e) {
            this.getLogger().log(Level.SEVERE, "Unable to load repository YAML file {0}; {1}", new Object[] { file, e });
            return null;
        }
    }

}
