package edgruberman.bukkit.inventory.sessions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;

/** withdrawal manager */
public class Clerk implements Listener {

    private final DeliveryRepository deliveries;
    private final boolean record;
    private final Plugin plugin;

    public Clerk(final DeliveryRepository deliveries, final boolean record, final Plugin plugin) {
        this.deliveries = deliveries;
        this.record = record;
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = false) // chest right clicks in air are by default cancelled since they do nothing
    public void onRequest(final PlayerInteractEvent interact) {
        if (interact.getItem() != null && interact.getItem().getTypeId() != Material.CHEST.getId()) return; // ignore when a chest item is not held
        if (interact.getAction() != Action.RIGHT_CLICK_AIR) return; // ignore if attempting to place chest
        if (!interact.getPlayer().hasPermission("inventory.delivery")) return; // ignore if not allowed
        interact.setCancelled(true);

        final Delivery requested = this.deliveries.load(interact.getPlayer().getName());
        if (requested == null || requested.getBalance().isEmpty()) {
            Main.courier.send(interact.getPlayer(), "empty-balance", interact.getPlayer().getName());
            return;
        }

        final String reason = Main.courier.format("reason-withdraw");
        final DeliveryWithdraw withdraw = new DeliveryWithdraw(interact.getPlayer(), this.deliveries, requested, reason, this.record);
        Bukkit.getPluginManager().registerEvents(withdraw, this.plugin);
    }

}
