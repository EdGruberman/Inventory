package edgruberman.bukkit.inventory.sessions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;

/** prevents any additions */
public class PullSession extends EditSession {

    public PullSession(final ConfigurationCourier courier, final Player customer, final Clerk clerk, final InventoryList delivery, final String title) {
        super(courier, customer, clerk, delivery, title);
    }

    /** do not add a box if full */
    @Override
    public void next() {
        final int current = this.index++;
        if (this.index > this.inventory.size() - 1) this.index = 0;
        if (current != this.index) this.refresh();
    }

    @Override
    @SuppressWarnings("deprecation") // Player.updateInventory has been deprecated for a while with no alternative available yet
    public void onClick(final InventoryClickEvent click) {
        final int last = click.getView().getTopInventory().getSize() - 1;

        // cancel shift clicks on items outside of delivery inventory to prevent indirect moving of items into delivery inventory
        if (click.isShiftClick() && (click.getCurrentItem().getTypeId() != Material.AIR.getId()) && (click.getRawSlot() > last)) click.setCancelled(true);

        // cancel clicks with item on cursor in delivery inventory to prevent direct placement of items into delivery inventory
        if ((click.getCursor().getTypeId() != Material.AIR.getId()) && (click.getRawSlot() >= 0) && (click.getRawSlot() <= last)) click.setCancelled(true);

        if (click.isCancelled()) {
            click.setCursor(click.getCursor());
            ((Player) click.getWhoClicked()).updateInventory();
            this.courier.send((Player) click.getWhoClicked(), "withdraw-only");
        }
    }

    @Override
    @SuppressWarnings("deprecation") // Player.updateInventory has been deprecated for a while with no alternative available yet
    protected void onDrag(final InventoryDragEvent drag) {
        final int last = drag.getView().getTopInventory().getSize() - 1;

        // cancel drags that place into delivery inventory
        for (final int raw : drag.getRawSlots()) {
            if (raw <= last) drag.setCancelled(true);
        }

        if (drag.isCancelled()) {
            drag.setCursor(drag.getCursor());
            ((Player) drag.getWhoClicked()).updateInventory();
            this.courier.send((Player) drag.getWhoClicked(), "withdraw-only");
        }
    }

}
