package edgruberman.bukkit.inventory.sessions;

import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.InventoryList;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;

public class EditSession extends Session {

    protected final Clerk clerk;

    public EditSession(final ConfigurationCourier courier, final Player customer, final Clerk clerk, final InventoryList delivery, final String title) {
        super(courier, customer, delivery, title);
        this.clerk = clerk;
    }

    @Override
    protected void onEnd() {
        final int viewers = this.inventory.getViewers().size();
        if (viewers == 1 && this.inventory.isContentsEmpty()) {
            this.clerk.removeInventory(this.inventory);
            return;
        }

        if ((viewers == 1) && (this.inventory.trim() > 0)) this.inventory.formatTitles(this.title, this.inventory.getName());
        this.clerk.putInventory(this.inventory);
    }

}
