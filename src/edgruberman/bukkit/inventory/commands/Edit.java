package edgruberman.bukkit.inventory.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.DeliveryInventory;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.commands.util.CancellationContingency;
import edgruberman.bukkit.inventory.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.inventory.commands.util.ExecutionRequest;
import edgruberman.bukkit.inventory.commands.util.OfflinePlayerParameter;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.inventory.sessions.EditSession;

public final class Edit extends ConfigurationExecutor {

    private final Clerk clerk;

    private final OfflinePlayerParameter player;

    public Edit(final ConfigurationCourier courier, final Server server, final Clerk clerk) {
        super(courier);
        this.clerk = clerk;

        this.requirePlayer();
        this.player = this.addRequired(OfflinePlayerParameter.Factory.create("player", server));
    }

    // usage: /<command> player
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws CancellationContingency {
        final OfflinePlayer target = request.parse(this.player);

        final String title = this.courier.translate("title-delivery");

        InventoryList delivery = this.clerk.getInventory(DeliveryInventory.class, target.getName());
        if (delivery == null) {
            delivery = DeliveryInventory.create(target.getName(), title);
            this.clerk.putInventory(delivery);
        }

        this.clerk.openSession(new EditSession(this.courier, (Player) request.getSender(), this.clerk, delivery, title));
        return true;
    }

}
