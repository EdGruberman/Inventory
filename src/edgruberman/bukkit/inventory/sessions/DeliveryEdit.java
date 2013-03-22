package edgruberman.bukkit.inventory.sessions;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.Transaction;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;
import edgruberman.bukkit.inventory.util.ItemStackUtil;

/** indirect interaction with delivery balance that allows automatic expansion */
public class DeliveryEdit extends Session {

    private final DeliveryRepository deliveries;
    private final Delivery active;

    public DeliveryEdit(final Player customer, final DeliveryRepository deliveries, final Delivery active, final String reason) {
        super(customer, active.getBalance().clone(), reason);
        this.deliveries = deliveries;
        this.active = active;
    }

    @Override
    public void next() {
        if (this.index == this.pallet.getBoxes().size() - 1
                && this.pallet.getBoxes().get(this.index).isFull()) {
            this.pallet.addBox();
            this.pallet.label(Main.courier.format("box-delivery", "{0}", "{1}", this.active.getPlayer()));
        }

        super.next();
    }

    @Override
    protected void onEnd(final Transaction transaction) {
        if (transaction.getChanges().isEmpty()) {
            if (this.active.empty()) this.deliveries.delete(this.active);
            return;
        }

        final Collection<ItemStack> failures = this.active.modifyBalance(transaction.getChanges());
        for (final ItemStack stack : failures) stack.setAmount(stack.getAmount() * -1);
        transaction.getFailures().addAll(failures);
        if (!transaction.getFailures().isEmpty()) Main.courier.send(this.customer, "failures", transaction.getFailures().size(), this.active.getPlayer(), ItemStackUtil.summarize(transaction.getFailures()));

        this.active.record(transaction);
        this.deliveries.save(this.active);
    }

}
