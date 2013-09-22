package edgruberman.bukkit.inventory.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.DeliveryInventory;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.commands.util.ArgumentContingency;
import edgruberman.bukkit.inventory.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.inventory.commands.util.ExecutionRequest;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.inventory.sessions.PullSession;

public final class Withdraw extends ConfigurationExecutor implements Listener {

    private final Clerk clerk;
    private final Command withdraw;

    public Withdraw(final ConfigurationCourier courier, final Clerk clerk, final Command withdraw) {
        super(courier);
        this.clerk = clerk;
        this.withdraw = withdraw;

        this.requirePlayer();
    }

    // usage: /<command>
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws ArgumentContingency {
        final InventoryList requested = this.clerk.getInventory(DeliveryInventory.class, request.getSender().getName());
        if (requested == null || requested.isEmpty()) {
            this.courier.send(request.getSender(), "withdraw-empty", request.getSender().getName());
            return true;
        }

        this.clerk.openSession(new PullSession(this.courier, (Player) request.getSender(), this.clerk, requested, this.courier.translate("title-delivery")));
        return true;
    }

    @EventHandler(ignoreCancelled = false) // chest right clicks in air are by default cancelled since they do nothing
    public void onRequest(final PlayerInteractEvent interact) {
        if (interact.getItem() != null && interact.getItem().getType() != Material.CHEST) return; // ignore when a chest item is not held
        if (interact.getAction() != Action.RIGHT_CLICK_AIR) return; // ignore if attempting to place chest
        if (!interact.getPlayer().hasPermission("inventory.withdraw.chest")) return; // ignore if not allowed
        interact.setCancelled(true);

        this.withdraw.execute(interact.getPlayer(), "withdraw-interact", new String[0]);
    }

}
