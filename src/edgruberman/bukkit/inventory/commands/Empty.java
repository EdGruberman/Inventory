package edgruberman.bukkit.inventory.commands;

import org.bukkit.Server;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.DeliveryInventory;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.commands.util.ArgumentContingency;
import edgruberman.bukkit.inventory.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.inventory.commands.util.ExecutionRequest;
import edgruberman.bukkit.inventory.commands.util.OfflinePlayerParameter;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.inventory.sessions.Session;

public final class Empty extends ConfigurationExecutor {

    private final Clerk clerk;

    private final OfflinePlayerParameter player;

    public Empty(final ConfigurationCourier courier, final Server server, final Clerk clerk) {
        super(courier);
        this.clerk = clerk;

        this.player = this.addRequired(OfflinePlayerParameter.Factory.create("player", server));
    }

    // usage: /<command> player
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws ArgumentContingency {
        final String player = request.parse(this.player).getName();

        final InventoryList delivery = this.clerk.getInventory(DeliveryInventory.class, player);
        if (delivery != null && !delivery.isContentsEmpty()) delivery.removeAll();
        final boolean trimmed = delivery.trim() > 0;
        if (trimmed) delivery.formatTitles(this.courier.translate("title-delivery"), delivery.getName());

        boolean use = false;
        for (final Session session : this.clerk.sessionsFor(delivery)) {
            if (trimmed) session.refresh();
            use = true;
        }
        if (!use) this.clerk.removeInventory(delivery);

        this.courier.send(request.getSender(), "empty", delivery.getName());
        return true;
    }

}
