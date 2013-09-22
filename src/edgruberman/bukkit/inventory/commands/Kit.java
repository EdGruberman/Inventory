package edgruberman.bukkit.inventory.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.DeliveryInventory;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.KitInventory;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.commands.util.ArgumentContingency;
import edgruberman.bukkit.inventory.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.inventory.commands.util.ExecutionRequest;
import edgruberman.bukkit.inventory.commands.util.IntegerParameter;
import edgruberman.bukkit.inventory.commands.util.JoinList;
import edgruberman.bukkit.inventory.commands.util.OfflinePlayerParameter;
import edgruberman.bukkit.inventory.commands.util.StringParameter;
import edgruberman.bukkit.inventory.commands.util.UnknownArgumentContingency;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.inventory.sessions.Session;

public final class Kit extends ConfigurationExecutor {

    private final Clerk clerk;

    private final StringParameter kit;
    private final OfflinePlayerParameter player;
    private final IntegerParameter quantity;

    public Kit(final ConfigurationCourier courier, final Server server, final Clerk clerk) {
        super(courier);
        this.clerk = clerk;

        this.kit = this.addRequired(StringParameter.Factory.create("kit"));
        this.player = this.addOptional(OfflinePlayerParameter.Factory.create("player", server));
        this.quantity = this.addOptional(IntegerParameter.Factory.create("quantity").setDefaultValue(1));
    }

    // usage: /<command> kit [player] [quantity]
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws ArgumentContingency {
        final String name = request.parse(this.kit);
        final InventoryList kit = this.clerk.getInventory(KitInventory.class, name);
        if (kit == null) throw new UnknownArgumentContingency(request, this.kit);

        // try to ensure proper case for player name since new Delivery could be created
        final String player = request.parse(this.player).getName();

        final Integer quantity = request.parse(this.quantity);
        final List<ItemStack> changes = Kit.multiply(kit.getContents(), quantity);

        final String title = this.courier.translate("title-delivery");

        InventoryList delivery = this.clerk.getInventory(DeliveryInventory.class, player);
        if (delivery == null) delivery = DeliveryInventory.create(player, title);

        final int before = delivery.size();
        final Collection<ItemStack> failures = delivery.modify(changes);
        for (final ItemStack stack : failures) stack.setAmount(stack.getAmount() * -1);
        if (delivery.size() != before) delivery.formatTitles(title, delivery.getName());
        // TODO refresh sessions

        this.clerk.putInventory(delivery);

        for (final Session session : this.clerk.sessionsFor(delivery)) session.refresh();

        this.courier.send(request.getSender(), "kit", kit.getName(), delivery.getName(), quantity, failures.size());
        if (!failures.isEmpty()) this.courier.send(request.getSender(), "failures", kit.getName(), delivery.getName(), this.summarize(failures), failures.size());
        return true;
    }

    private static List<ItemStack> multiply(final List<ItemStack> items, final int quantity) {
        if (quantity == 1) return new ArrayList<ItemStack>(items);

        final int signum = (int) Math.signum(quantity);
        final int abs = Math.abs(quantity);

        // clone stack by stack to respect stack sizes provided
        final List<ItemStack> result = new ArrayList<ItemStack>();
        for (int i = 0; i < abs; i++) {
            for (final ItemStack change : items) {
                final ItemStack clone = change.clone();
                clone.setAmount(clone.getAmount() * signum);
                result.add(clone);
            }
        }

        return result;
    }

    private JoinList<ItemStackSummarizer> summarize(final Collection<ItemStack> stacks) {
        final JoinList<ItemStackSummarizer> result = this.<ItemStackSummarizer>joinFactory().config(this.courier.getBase()).prefix("items-summary-").build();
        for (final ItemStack stack : stacks) result.add(new ItemStackSummarizer(stack));
        return result;
    }

    private final class ItemStackSummarizer {

        private final ItemStack stack;

        ItemStackSummarizer(final ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public String toString() {
            return Main.summarize(this.stack).toString();
        }

    }

}
