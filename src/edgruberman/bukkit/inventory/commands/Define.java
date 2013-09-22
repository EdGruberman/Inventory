package edgruberman.bukkit.inventory.commands;

import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.KitInventory;
import edgruberman.bukkit.inventory.commands.util.ArgumentContingency;
import edgruberman.bukkit.inventory.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.inventory.commands.util.ExecutionRequest;
import edgruberman.bukkit.inventory.commands.util.StringParameter;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.inventory.sessions.EditSession;

public final class Define extends ConfigurationExecutor {

    private final Clerk clerk;

    private final StringParameter kit;

    public Define(final ConfigurationCourier courier, final Clerk clerk) {
        super(courier);
        this.clerk = clerk;

        this.requirePlayer();
        this.kit = this.addRequired(StringParameter.Factory.create("kit"));
    }

    // usage: /<command> kit
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws ArgumentContingency {
        final String name = request.parse(this.kit);

        final String title = this.courier.translate("title-kit");

        InventoryList kit = this.clerk.getInventory(KitInventory.class, name);
        if (kit == null) {
            kit = KitInventory.create(name, title);
            this.clerk.putInventory(kit);
        }

        this.clerk.openSession(new EditSession(this.courier, (Player) request.getSender(), this.clerk, kit, title));
        return true;
    }

}
