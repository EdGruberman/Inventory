package edgruberman.bukkit.inventory.sessions;

import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Delivery;
import edgruberman.bukkit.inventory.repositories.DeliveryRepository;

public class DeliverySession extends Session {

    protected final DeliveryRepository repository;
    protected final Delivery delivery;

    public DeliverySession(final Player customer, final DeliveryRepository repository, final Delivery delivery) {
        super(customer, delivery.getBalance());
        this.repository = repository;
        this.delivery = delivery;
    }

    @Override
    public Delivery getKey() {
        return this.delivery;
    }

    @Override
    protected void onExpand() {
        this.pallet.label("box-delivery", this.delivery.getPlayer());
    }

    @Override
    protected void onEnd() {
        final int viewers = this.pallet.viewers().size();
        if (viewers == 1 && this.pallet.isEmpty()) {
            this.repository.delete(this.delivery);
            return;
        }

        if (viewers == 1 && this.pallet.trim()) this.pallet.label("box-delivery", this.delivery.getPlayer());
        this.repository.save(this.delivery);
    }

}
