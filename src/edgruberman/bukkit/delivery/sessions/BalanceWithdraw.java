package edgruberman.bukkit.delivery.sessions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import edgruberman.bukkit.delivery.Ledger;
import edgruberman.bukkit.delivery.Main;
import edgruberman.bukkit.delivery.Transaction;
import edgruberman.bukkit.delivery.repositories.LedgerRepository;

/** direct interaction with ledger balance that prevents any additions */
public class BalanceWithdraw extends Session {

    private final LedgerRepository ledgers;
    private final Ledger active;
    private final boolean record;

    public BalanceWithdraw(final Player customer, final LedgerRepository ledgers, final Ledger active, final String reason, final boolean record) {
        super(customer, active.getBalance(), reason);
        this.ledgers = ledgers;
        this.active = active;
        this.record = record;
    }

    @Override
    @SuppressWarnings("deprecation") // Player.updateInventory has been deprecated for a while with no alternative available yet
    public void onClick(final InventoryClickEvent click) {
        final int last = click.getView().getTopInventory().getSize() - 1;

        // cancel shift clicks on items outside of box to prevent indirect moving of items into box
        if (click.isShiftClick() && (click.getCurrentItem().getTypeId() != Material.AIR.getId()) && (click.getRawSlot() > last)) click.setCancelled(true);

        // cancel clicks with item on cursor in box to prevent direct placement of items into box
        if ((click.getCursor().getTypeId() != Material.AIR.getId()) && (click.getRawSlot() >= 0) && (click.getRawSlot() <= last)) click.setCancelled(true);

        if (click.isCancelled()) {
            click.setCursor(click.getCursor());
            ((Player) click.getWhoClicked()).updateInventory();
            Main.courier.send((Player) click.getWhoClicked(), "withdraw-only");
        }
    }

    @Override
    protected void onEnd(final Transaction transaction) {
        if (this.pallet.trim()) this.pallet.label(Main.courier.format("box-balance", "{0}", "{1}", this.active.getPlayer()));

        if (transaction.getChanges().isEmpty()) {
            if (this.active.empty()) this.ledgers.delete(this.active);
            return;
        }

        if (this.record) this.active.record(transaction);
        this.ledgers.save(this.active);
    }

}
