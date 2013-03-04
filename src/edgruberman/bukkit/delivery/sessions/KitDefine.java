package edgruberman.bukkit.delivery.sessions;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.delivery.Kit;
import edgruberman.bukkit.delivery.Main;
import edgruberman.bukkit.delivery.Transaction;
import edgruberman.bukkit.delivery.repositories.KitRepository;
import edgruberman.bukkit.delivery.util.ItemStackUtil;

/** indirect interaction with kit contents that allows automatic expansion */
public class KitDefine extends Session {

    private final KitRepository kits;
    private final Kit active;

    public KitDefine(final Player customer, final KitRepository kits, final Kit active) {
        super(customer, active.getContents().clone(), null);
        this.kits = kits;
        this.active = active;
    }

    @Override
    public void next() {
        if (this.index == this.pallet.getBoxes().size() - 1
                && this.pallet.getBoxes().get(this.index).full()) {
            this.pallet.addBox();
            this.pallet.label(Main.courier.format("box-kit", "{0}", "{1}", this.active.getName()));
        }

        super.next();
    }

    @Override
    protected void onEnd(final Transaction transaction) {
        if (transaction.getChanges().isEmpty()) {
            if (this.active.getContents().empty()) this.kits.delete(this.active);
            return;
        }

        final Collection<ItemStack> failures = this.active.getContents().modify(transaction.getChanges());
        transaction.getFailures().addAll(ItemStackUtil.multiplyAmount(failures , -1));
        if (!transaction.getFailures().isEmpty()) Main.courier.send(this.customer, "failures", transaction.getFailures().size(), this.active.getName(), ItemStackUtil.summarize(transaction.getFailures()));

        if (this.active.getContents().empty()) this.kits.delete(this.active);
        this.kits.save(this.active);
    }

}
