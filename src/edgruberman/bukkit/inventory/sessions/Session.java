package edgruberman.bukkit.inventory.sessions;

import java.util.Observable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import edgruberman.bukkit.inventory.CustomInventory;
import edgruberman.bukkit.inventory.KeyedInventoryList;
import edgruberman.bukkit.inventory.Main;

/** inventory list interaction manager */
public abstract class Session extends Observable implements Listener {

    protected final Player customer;
    protected final KeyedInventoryList list;

    protected int index = -1;
    protected boolean refresh = false;

    /** @param initial set of similar items, single instance of items excluding amount */
    public Session(final Player customer, final KeyedInventoryList list) {
        this.customer = customer;
        this.list = list;
        this.index = this.list.size() - 1;
    }

    public Player getCustomer() {
        return this.customer;
    }

    public KeyedInventoryList getList() {
        return this.list;
    }

    public int getIndex() {
        return this.index;
    }

    public void refresh() {
        this.refresh = true;
        if (this.index > this.list.size() - 1) this.index = 0;
        this.customer.closeInventory();
        this.list.get(this.index).open(this.customer);
        this.refresh = false;
    }

    public void next() {
        if ((this.index == this.list.size() - 1) && this.list.get(this.index).isFull()) {
            this.list.add(new CustomInventory());
            this.list.setTitles();
        }

        final int current = this.index++;
        if (this.index > this.list.size() - 1) this.index = 0;
        if (current != this.index) this.refresh();
    }

    public void previous() {
        final int current = this.index--;
        if (this.index < 0) this.index = this.list.size() - 1;
        if (current != this.index) this.list.get(this.index).open(this.customer);
    }

    @EventHandler(ignoreCancelled = true)
    public void click(final InventoryClickEvent click) {
        if (!this.customer.equals(click.getWhoClicked())) return; // ignore when not this customer
        this.onClick(click);
        if (click.isCancelled()) return;

        // left or right click outside with nothing on cursor to navigate boxes forwards or backwards
        if (click.getRawSlot() == -999 && click.getCursor().getTypeId() == Material.AIR.getId()) { // TODO Fix SlotType.OUTSIDE not being properly identified; BUKKIT-2768
            if (click.isLeftClick()) {
                this.next();
            } else {
                this.previous();
            }
            return;
        }
    }

    protected void onClick(final InventoryClickEvent click) {};

    @EventHandler
    public void close(final InventoryCloseEvent close) {
        if (this.refresh) return; // ignore when refreshing view
        if (!this.customer.equals(close.getPlayer())) return; // ignore when not this customer
        this.end();
    }

    @EventHandler
    public void quit(final PlayerQuitEvent quit) {
        if (!this.customer.equals(quit.getPlayer())) return; // ignore when not this customer
        this.end();
    }

    protected void end() {
        HandlerList.unregisterAll(this);
        this.onEnd();
        this.setChanged();
        this.notifyObservers();
    }

    protected void onEnd() {};

    public void destroy(final String reason) {
        try {
            this.end();
        } catch (final Exception e) {
            // ignore to avoid implementation preventing complete destruction
        }
        this.customer.getOpenInventory().close();
        Main.courier.send(this.customer, "session-destroy", reason);
    }

}
