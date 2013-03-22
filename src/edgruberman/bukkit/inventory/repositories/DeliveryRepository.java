package edgruberman.bukkit.inventory.repositories;

import edgruberman.bukkit.inventory.Delivery;

public class DeliveryRepository extends CachedRepository<String, Delivery>{

    public DeliveryRepository(final Repository<String, Delivery> source) {
        super(source);
    }

    public Delivery create(final String player) {
        Delivery result = this.load(player);
        if (result != null) return result;

        result = new Delivery(player);
        this.cache.put(player.toLowerCase(), result);
        return result;
    }

    @Override
    public Delivery load(final String player) {
        return super.load(player.toLowerCase());
    }

    public void save(final Delivery delivery) {
        this.save(delivery.getPlayer().toLowerCase(), delivery);
    }

    public void delete(final Delivery delivery) {
        this.delete(delivery.getPlayer().toLowerCase());
    }

}
