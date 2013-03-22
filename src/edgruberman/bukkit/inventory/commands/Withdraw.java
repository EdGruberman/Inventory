package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;
import edgruberman.bukkit.inventory.sessions.DeliveryWithdraw;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Withdraw extends TokenizedExecutor implements Listener {

    private final DeliveryRepository deliveries;
    private final Plugin plugin;

    public Withdraw(final DeliveryRepository deliveries, final Plugin plugin) {
        this.deliveries = deliveries;
        this.plugin = plugin;
    }

    // usage: /<command>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return false;
        }

        final Delivery requested = this.deliveries.load(sender.getName());
        if (requested == null || requested.getBalance().isEmpty()) {
            Main.courier.send(sender, "withdraw-empty", sender.getName());
            return true;
        }

        final DeliveryWithdraw withdraw = new DeliveryWithdraw((Player) sender, this.deliveries, requested);
        Bukkit.getPluginManager().registerEvents(withdraw, this.plugin);
        return true;
    }

    @EventHandler(ignoreCancelled = false) // chest right clicks in air are by default cancelled since they do nothing
    public void onRequest(final PlayerInteractEvent interact) {
        if (interact.getItem() != null && interact.getItem().getTypeId() != Material.CHEST.getId()) return; // ignore when a chest item is not held
        if (interact.getAction() != Action.RIGHT_CLICK_AIR) return; // ignore if attempting to place chest
        if (!interact.getPlayer().hasPermission("inventory.withdraw.chest")) return; // ignore if not allowed
        interact.setCancelled(true);
        this.onCommand(interact.getPlayer(), (Command) null, (String) null, (List<String>) null);
    }

}
