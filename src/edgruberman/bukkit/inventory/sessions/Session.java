package edgruberman.bukkit.inventory.sessions;

import java.util.Observable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerQuitEvent;

import edgruberman.bukkit.inventory.InventoryAdapter;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;

/** inventory list interaction manager */
public abstract class Session extends Observable implements Listener {

    protected final ConfigurationCourier courier;
    protected final Player customer;
    protected final InventoryList inventory;
    protected final String title;

    protected int index = -1;
    protected boolean refresh = false;

    /** @param initial set of similar items, single instance of items excluding amount */
    public Session(final ConfigurationCourier courier, final Player customer, final InventoryList inventory, final String title) {
        this.courier = courier;
        this.customer = customer;
        this.inventory = inventory;
        this.title = title;
        this.index = this.inventory.size() - 1;
    }

    public Player getCustomer() {
        return this.customer;
    }

    public InventoryList getInventory() {
        return this.inventory;
    }

    public int getIndex() {
        return this.index;
    }

    public void refresh() {
        this.refresh = true;
        if (this.index > this.inventory.size() - 1) this.index = 0;
        this.customer.closeInventory();
        this.inventory.get(this.index).open(this.customer);
        this.refresh = false;
    }

    public void next() {
        if ((this.index == this.inventory.size() - 1) && this.inventory.get(this.index).isFull()) {
            this.inventory.add(new InventoryAdapter());
            this.inventory.formatTitles(this.title, this.inventory.getName());
        }

        final int current = this.index++;
        if (this.index > this.inventory.size() - 1) this.index = 0;
        if (current != this.index) this.refresh();
    }

    public void previous() {
        final int current = this.index--;
        if (this.index < 0) this.index = this.inventory.size() - 1;
        if (current != this.index) this.inventory.get(this.index).open(this.customer);
    }

    @EventHandler(ignoreCancelled = true)
    public void click(final InventoryClickEvent click) {
        if (!this.customer.equals(click.getWhoClicked())) return; // ignore when not this customer
        this.onClick(click);
        if (click.isCancelled()) return;

        // left or right click outside with nothing on cursor to navigate boxes forwards or backwards
        if (click.getSlotType() == SlotType.OUTSIDE && click.getCursor().getType() == Material.AIR) {
            if (click.isLeftClick()) {
                this.next();
            } else {
                this.previous();
            }
            return;
        }
    }

    protected void onClick(final InventoryClickEvent click) {}

    @EventHandler(ignoreCancelled = true)
    public void drag(final InventoryDragEvent drag) {
        if (!this.customer.equals(drag.getWhoClicked())) return; // ignore when not this customer
        this.onDrag(drag);
    }

    protected void onDrag(final InventoryDragEvent drag) {}

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

    protected void onEnd() {}

    public void destroy(final String reason) {
        try {
            this.end();
        } catch (final Exception e) {
            // ignore to avoid implementation preventing complete destruction
        }
        this.customer.getOpenInventory().close();
        this.courier.send(this.customer, "session-destroy", reason);
    }

}
