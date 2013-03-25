package edgruberman.bukkit.inventory.repositories;

import edgruberman.bukkit.inventory.Delivery;

public class DeliveryRepository extends CachingRepository<String, Delivery>{

    public DeliveryRepository(final Repository<String, Delivery> source) {
        super(source);
    }

    public Delivery create(final String player) {
        final String key = player.toLowerCase();
        Delivery result = this.get(key);
        if (result != null) return result;

        result = new Delivery(player);
        this.cache.put(key, result);
        return result;
    }

    @Override
    public Delivery get(final String player) {
        return super.get(player.toLowerCase());
    }

    /** @see {@link #put(Delivery)} */
    @Deprecated
    @Override
    public void put(final String key, final Delivery delivery) {
        throw new UnsupportedOperationException("use put(Delivery) instead");
    }

    public void put(final Delivery delivery) {
        super.put(delivery.getList().getKey().toLowerCase(), delivery);
    }

    /** @see {@link #remove(Delivery)} */
    @Deprecated
    @Override
    public void remove(final String key) {
        throw new UnsupportedOperationException("use remove(Delivery) instead");
    }

    public void remove(final Delivery delivery) {
        super.remove(delivery.getList().getKey().toLowerCase());
    }

}
