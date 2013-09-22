package edgruberman.bukkit.inventory;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.inventory.commands.Copy;
import edgruberman.bukkit.inventory.commands.Define;
import edgruberman.bukkit.inventory.commands.Delete;
import edgruberman.bukkit.inventory.commands.Edit;
import edgruberman.bukkit.inventory.commands.Empty;
import edgruberman.bukkit.inventory.commands.Kit;
import edgruberman.bukkit.inventory.commands.Move;
import edgruberman.bukkit.inventory.commands.Reload;
import edgruberman.bukkit.inventory.commands.Withdraw;
import edgruberman.bukkit.inventory.craftbukkit.CraftBukkit;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.inventory.repositories.CachingRepository;
import edgruberman.bukkit.inventory.repositories.YamlFolderRepository;
import edgruberman.bukkit.inventory.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static CraftBukkit craftBukkit = null;

    private ConfigurationCourier courier;
    private Clerk clerk = null;

    @Override
    public void onLoad() {
        this.putConfigMinimum("4.0.0");
        this.putConfigMinimum("language.yml", "4.3.0");
    }

    @Override
    public void onEnable() {
        try {
            Main.craftBukkit = CraftBukkit.create();
        } catch (final Exception e) {
            this.getLogger().log(Level.SEVERE, "Unsupported CraftBukkit version {0}; {1}", new Object[] { Bukkit.getVersion(), e });
            this.getLogger().log(Level.SEVERE, "Disabling plugin; Dependencies not met; Check for updates at {0}", this.getDescription().getWebsite());
            this.setEnabled(false);
            return;
        }

        this.reloadConfig();
        this.courier = ConfigurationCourier.Factory.create(this).setBase(this.loadConfig("language.yml")).setFormatCode("format-code").build();

        this.clerk = new Clerk(this);
        final File kits = new File(this.getDataFolder(), this.getConfig().getString("kit-folder"));
        this.clerk.putRepository(KitInventory.class, CachingRepository.of(new YamlFolderRepository<KitInventory>(this, kits, 30000)));
        final File deliveries = new File(this.getDataFolder(), this.getConfig().getString("delivery-folder"));
        this.clerk.putRepository(DeliveryInventory.class, CachingRepository.of(new YamlFolderRepository<KitInventory>(this, deliveries, 30000)));

        final PluginCommand withdrawCommand = this.getCommand("inventory:withdraw");
        final Withdraw withdrawExecutor = new Withdraw(this.courier, this.clerk, withdrawCommand);
        Bukkit.getPluginManager().registerEvents(withdrawExecutor, this);
        withdrawCommand.setExecutor(withdrawExecutor);

        this.getCommand("inventory:edit").setExecutor(new Edit(this.courier, this.getServer(), this.clerk));
        this.getCommand("inventory:empty").setExecutor(new Empty(this.courier, this.getServer(), this.clerk));
        this.getCommand("inventory:define").setExecutor(new Define(this.courier, this.clerk));
        this.getCommand("inventory:kit").setExecutor(new Kit(this.courier, this.getServer(), this.clerk));
        this.getCommand("inventory:delete").setExecutor(new Delete(this.courier, this.clerk));
        this.getCommand("inventory:move").setExecutor(new Move(this.courier, this.getServer()));
        this.getCommand("inventory:copy").setExecutor(new Copy(this.courier, this.getServer()));
        this.getCommand("inventory:reload").setExecutor(new Reload(this.courier, this));
    }

    @Override
    public void onDisable() {
        if (this.clerk != null) this.clerk.destroy(this.courier.format("session-destroy-disable"));
        HandlerList.unregisterAll(this);
        Main.craftBukkit = null;
    }



    public static StringBuilder summarize(final ItemStack stack) {
        final StringBuilder sb = new StringBuilder(stack.getType().name());
        if (stack.getDurability() != 0) sb.append('/').append(stack.getDurability());
        if (stack.hasItemMeta()) sb.append('*');
        if (stack.getAmount() != 0) sb.append('x').append(stack.getAmount());
        return sb;
    }

}
