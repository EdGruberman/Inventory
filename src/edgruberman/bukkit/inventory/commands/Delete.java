package edgruberman.bukkit.inventory.commands;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.KitInventory;
import edgruberman.bukkit.inventory.commands.util.ArgumentContingency;
import edgruberman.bukkit.inventory.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.inventory.commands.util.ExecutionRequest;
import edgruberman.bukkit.inventory.commands.util.StringParameter;
import edgruberman.bukkit.inventory.commands.util.UnknownArgumentContingency;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.inventory.sessions.Session;

public final class Delete extends ConfigurationExecutor {

    private final Clerk clerk;

    private final StringParameter kit;

    public Delete(final ConfigurationCourier courier, final Clerk clerk) {
        super(courier);
        this.clerk = clerk;

        this.kit = this.addRequired(StringParameter.Factory.create("kit"));
    }

    // usage: /<command> kit
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws ArgumentContingency {
        final String name = request.parse(this.kit);
        final InventoryList kit = this.clerk.getInventory(KitInventory.class, name);
        if (kit == null) throw new UnknownArgumentContingency(request, this.kit);

        kit.removeAll();
        final boolean trimmed = kit.trim() > 0;
        if (trimmed) kit.formatTitles(this.courier.translate("title-kit"), kit.getName());

        boolean use = false;
        for (final Session session : this.clerk.sessionsFor(kit)) {
            if (trimmed) session.refresh();
            use = true;
        }
        if (!use) this.clerk.removeInventory(kit);

        this.courier.send(request.getSender(), "delete", kit.getName());
        return true;
    }

}
