package edgruberman.bukkit.deliver.sessions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.deliver.Box;
import edgruberman.bukkit.deliver.Pallet;
import edgruberman.bukkit.deliver.Transaction;

/** pallet inventory interaction */
public abstract class Session implements Listener {

    protected final Player customer;
    protected final Pallet pallet;
    protected final String reason;
    protected final List<ItemStack> initial;

    protected int index;

    /** @param initial set of similar items, single instance of items excluding amount */
    public Session(final Player customer, final Pallet pallet, final String reason) {
        this.customer = customer;
        this.pallet = pallet;
        this.reason = reason;
        this.initial = pallet.joined();

        this.onStart();
        final List<Box> boxes = pallet.getBoxes();
        final Box last = boxes.get(boxes.size() - 1);
        this.index = boxes.size() - 1;
        last.open(customer);
    }

    protected void onStart() {};

    public void next() {
         final int current = this.index++;
        if (this.index > this.pallet.getBoxes().size() - 1) this.index = 0;
        if (current != this.index) this.pallet.getBoxes().get(this.index).open(this.customer);
    }

    public void previous() {
        final int current = this.index--;
        if (this.index < 0) this.index = this.pallet.getBoxes().size() - 1;
        if (current != this.index) this.pallet.getBoxes().get(this.index).open(this.customer);
    }

    @EventHandler(ignoreCancelled = true)
    public void click(final InventoryClickEvent click) {
        if (!this.customer.equals(click.getWhoClicked())) return; // ignore when not this customer
        this.onClick(click);
        if (click.isCancelled()) return;

        // left or right click outside with nothing to navigate boxes forwards or backwards
        if (click.getRawSlot() == -999 && click.getCursor().getTypeId() == Material.AIR.getId()) { // TODO Fix SlotType.OUTSIDE not being properly identified; BUKKIT-2768
            // TODO organize pallet (remove empty spaces, sort?)
            if (click.isLeftClick()) {
                this.next();
            } else {
                this.previous();
            }
        }
    }

    protected void onClick(final InventoryClickEvent click) {};

    @EventHandler
    public void close(final InventoryCloseEvent close) {
        if (!this.customer.equals(close.getPlayer())) return; // ignore when not this customer
        this.end();
    }

    @EventHandler
    public void quit(final PlayerQuitEvent quit) {
        if (!this.customer.equals(quit.getPlayer())) return; // ignore when not this customer
        this.end();
    }

    protected void end() {
        if (this.pallet.viewers().size() > 1) return; // cancel if other players still have pallet open
        HandlerList.unregisterAll(this);
        this.onEnd(this.toTransaction());
    }

    protected abstract void onEnd(Transaction transaction);

    public Transaction toTransaction() {
        return new Transaction(new Date(), this.customer.getName(), this.reason, this.difference(this.pallet.joined()));
    }

    /** @param stacks set of similar items, single instance of items excluding amount */
    protected List<ItemStack> difference(final List<ItemStack> after) {
        final List<ItemStack> result = new ArrayList<ItemStack>();

        for (final ItemStack start : this.initial) {

            // amount changes
            boolean similar = false;
            for (final ItemStack now : after) {
                if (start.isSimilar(now)) {
                    final ItemStack diff = start.clone();
                    diff.setAmount(now.getAmount() - start.getAmount());
                    if (diff.getAmount() != 0) result.add(diff);
                    similar = true;
                }
            }

            // removed items
            if (!similar) {
                final ItemStack removed = start.clone();
                removed.setAmount(-removed.getAmount());
                result.add(removed);
            }

        }

        // new items
        for (final ItemStack now : after) {
            boolean similar = false;
            for (final ItemStack start : this.initial) {
                if (now.isSimilar(start)) similar = true;
            }
            if (!similar) result.add(now.clone());
        }

        return result;
    }

}
