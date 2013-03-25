package edgruberman.bukkit.inventory.sessions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;

/** prevents any additions */
public class DeliveryWithdraw extends DeliverySession {

    public DeliveryWithdraw(final Player customer, final DeliveryRepository deliveries, final Delivery delivery) {
        super(customer, deliveries, delivery);
    }

    /** do not add a box if full */
    @Override
    public void next() {
        final int current = this.index++;
        if (this.index > this.list.size() - 1) this.index = 0;
        if (current != this.index) this.refresh();
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

}
