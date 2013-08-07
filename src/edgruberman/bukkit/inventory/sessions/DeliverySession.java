package edgruberman.bukkit.inventory.sessions;

import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.Delivery;

public class DeliverySession extends Session {

    protected final Clerk clerk;
    protected final Delivery delivery;

    public DeliverySession(final Player customer, final Clerk clerk, final Delivery delivery) {
        super(customer, delivery);
        this.clerk = clerk;
        this.delivery = delivery;
    }

    @Override
    protected void onEnd() {
        final int viewers = this.list.getViewers().size();
        if (viewers == 1 && this.list.isContentsEmpty()) {
            this.clerk.removeDelivery(this.delivery);
            return;
        }

        if ((viewers == 1) && (this.list.trim() > 0)) this.list.formatTitles();
        this.clerk.putDelivery(this.delivery);
    }

}
