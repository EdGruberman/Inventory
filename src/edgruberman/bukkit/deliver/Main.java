package edgruberman.bukkit.deliver;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.HandlerList;

import edgruberman.bukkit.deliver.commands.Define;
import edgruberman.bukkit.deliver.commands.Edit;
import edgruberman.bukkit.deliver.commands.Reload;
import edgruberman.bukkit.deliver.craftbukkit.CraftBukkit;
import edgruberman.bukkit.deliver.messaging.ConfigurationCourier;
import edgruberman.bukkit.deliver.repositories.BufferedYamlRepository;
import edgruberman.bukkit.deliver.repositories.KitRepository;
import edgruberman.bukkit.deliver.repositories.LedgerRepository;
import edgruberman.bukkit.deliver.sessions.Clerk;
import edgruberman.bukkit.deliver.util.CustomPlugin;
import edgruberman.bukkit.deliver.util.ItemStackUtil;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;
    public static CraftBukkit craftBukkit = null;

    private KitRepository kits = null;
    private LedgerRepository ledgers = null;
    private Clerk clerk = null;

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

        final BufferedYamlRepository<Ledger> yamlLedgers = this.initializeRepository("ledgers.yml");
        this.ledgers = ( yamlLedgers != null ? new LedgerRepository(yamlLedgers) : null);

        if (this.kits == null || this.ledgers == null) {
            this.getLogger().log(Level.SEVERE, "Disabling plugin; Unusable repository");
            this.setEnabled(false);
            return;
        }

        this.clerk = new Clerk(this.ledgers, this.getConfig().getBoolean("record-withdrawals"), this);
        Bukkit.getPluginManager().registerEvents(this.clerk, this);

        this.getCommand("deliver:edit").setExecutor(new Edit(this.ledgers, this));
        this.getCommand("deliver:define").setExecutor(new Define(this.kits, this));
        this.getCommand("deliver:kit").setExecutor(new edgruberman.bukkit.deliver.commands.Kit(this.kits, this.ledgers));
        this.getCommand("deliver:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        if (this.kits != null) this.kits.destroy();
        if (this.ledgers != null) this.ledgers.destroy();
        if (this.clerk != null) HandlerList.unregisterAll(this.clerk);
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
