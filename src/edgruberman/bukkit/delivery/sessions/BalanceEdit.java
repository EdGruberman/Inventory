package edgruberman.bukkit.delivery.sessions;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.delivery.Ledger;
import edgruberman.bukkit.delivery.Main;
import edgruberman.bukkit.delivery.Transaction;
import edgruberman.bukkit.delivery.repositories.LedgerRepository;
import edgruberman.bukkit.delivery.util.ItemStackUtil;

/** indirect interaction with ledger balance that allows automatic expansion */
public class BalanceEdit extends Session {

    private final LedgerRepository ledgers;
    private final Ledger active;

    public BalanceEdit(final Player customer, final LedgerRepository ledgers, final Ledger active, final String reason) {
        super(customer, active.getBalance().clone(), reason);
        this.ledgers = ledgers;
        this.active = active;
    }

    @Override
    public void next() {
        if (this.index == this.pallet.getBoxes().size() - 1
                && this.pallet.getBoxes().get(this.index).full()) {
            this.pallet.addBox();
            this.pallet.label(Main.courier.format("box-balance", "{0}", "{1}", this.active.getPlayer()));
        }

        super.next();
    }

    @Override
    protected void onEnd(final Transaction transaction) {
        if (transaction.getChanges().isEmpty()) {
            if (this.active.empty()) this.ledgers.delete(this.active);
            return;
        }

        final Collection<ItemStack> failures = this.active.modifyBalance(transaction.getChanges());
        transaction.getFailures().addAll(ItemStackUtil.multiplyAmount(failures , -1));
        if (!transaction.getFailures().isEmpty()) Main.courier.send(this.customer, "failures", transaction.getFailures().size(), this.active.getPlayer(), ItemStackUtil.summarize(transaction.getFailures()));

        this.active.record(transaction);
        this.ledgers.save(this.active);
    }

}
