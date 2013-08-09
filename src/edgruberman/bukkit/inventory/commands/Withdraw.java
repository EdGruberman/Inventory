package edgruberman.bukkit.inventory.commands;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.DeliveryInventory;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.sessions.PullSession;
import edgruberman.bukkit.inventory.util.TokenizedExecutor;

public final class Withdraw extends TokenizedExecutor implements Listener {

    private final Clerk clerk;
    private final String title;

    public Withdraw(final Clerk clerk, final String title) {
        this.clerk = clerk;
        this.title = title;
    }

    // usage: /<command>
    @Override
    protected boolean onCommand(final CommandSender sender, final Command command, final String label, final List<String> args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player", label);
            return false;
        }

        final InventoryList requested = this.clerk.getInventory(DeliveryInventory.class, sender.getName());
        if (requested == null || requested.isEmpty()) {
            Main.courier.send(sender, "withdraw-empty", sender.getName());
            return true;
        }

        this.clerk.openSession(new PullSession((Player) sender, this.clerk, requested, this.title));
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
