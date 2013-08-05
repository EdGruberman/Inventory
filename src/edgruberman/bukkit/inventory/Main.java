package edgruberman.bukkit.inventory;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
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
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.inventory.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;
    public static CraftBukkit craftBukkit = null;

    private Clerk clerk = null;

    @Override
    public void onLoad() {
        this.putConfigMinimum("4.0.0");
        this.putConfigMinimum("language.yml", "4.0.0");
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

        final File kits = new File(this.getDataFolder(), this.getConfig().getString("kit-folder"));
        final File deliveries = new File(this.getDataFolder(), this.getConfig().getString("delivery-folder"));
        this.clerk = new Clerk(this, kits, deliveries);

        final Withdraw withdraw = new Withdraw(this.clerk);
        Bukkit.getPluginManager().registerEvents(withdraw, this);

        this.getCommand("inventory:withdraw").setExecutor(withdraw);
        this.getCommand("inventory:edit").setExecutor(new Edit(this.clerk));
        this.getCommand("inventory:empty").setExecutor(new Empty(this.clerk));
        this.getCommand("inventory:define").setExecutor(new Define(this.clerk));
        this.getCommand("inventory:kit").setExecutor(new edgruberman.bukkit.inventory.commands.Kit(this.clerk, Main.courier.getSection("items-summary")));
        this.getCommand("inventory:delete").setExecutor(new Delete(this.clerk));
        this.getCommand("inventory:move").setExecutor(new Move());
        this.getCommand("inventory:copy").setExecutor(new Copy());
        this.getCommand("inventory:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        if (this.clerk != null) this.clerk.destroy(Main.courier.format("session-destroy-disable").get(0));
        Main.courier = null;
        Main.craftBukkit = null;
        HandlerList.unregisterAll(this);
    }

}
