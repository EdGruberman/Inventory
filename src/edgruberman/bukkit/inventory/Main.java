package edgruberman.bukkit.inventory;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;

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
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;
import edgruberman.bukkit.inventory.repositories.KitRepository;
import edgruberman.bukkit.inventory.repositories.YamlRepository;
import edgruberman.bukkit.inventory.util.CustomPlugin;
import edgruberman.bukkit.inventory.util.ItemStackUtil;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;
    public static CraftBukkit craftBukkit = null;

    static {
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(Delivery.class);
        ConfigurationSerialization.registerClass(Pallet.class);
        ConfigurationSerialization.registerClass(Box.class);
    }

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

        final File kitFolder = new File(this.getDataFolder(), this.getConfig().getString("kit-folder"));
        final YamlRepository<Kit> yamlKits = new YamlRepository<Kit>(this, kitFolder, 30000);
        this.kits = ( yamlKits != null ? new KitRepository(yamlKits) : null);

        final File deliveryFolder = new File(this.getDataFolder(), this.getConfig().getString("delivery-folder"));
        final YamlRepository<Delivery> yamlDeliveries = new YamlRepository<Delivery>(this, deliveryFolder, 30000);
        this.deliveries = ( yamlDeliveries != null ? new DeliveryRepository(yamlDeliveries) : null);

        if (this.kits == null || this.deliveries == null) {
            this.getLogger().log(Level.SEVERE, "Disabling plugin; Unusable repository");
            this.setEnabled(false);
            return;
        }

        final Withdraw withdraw = new Withdraw(this.deliveries, this);
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
        HandlerList.unregisterAll(this);
    }

}
