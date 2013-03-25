package edgruberman.bukkit.inventory.sessions;

import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;

public class DeliverySession extends Session {

    protected final DeliveryRepository repository;
    protected final Delivery delivery;

    public DeliverySession(final Player customer, final DeliveryRepository repository, final Delivery delivery) {
        super(customer, delivery.getList());
        this.repository = repository;
        this.delivery = delivery;
    }

    @Override
    protected void onEnd() {
        final int viewers = this.list.getViewers().size();
        if (viewers == 1 && this.list.isContentsEmpty()) {
            this.repository.remove(this.delivery);
            return;
        }

        if ((viewers == 1) && (this.list.trim() > 0)) this.list.setTitles();
        this.repository.put(this.delivery);
    }

}
