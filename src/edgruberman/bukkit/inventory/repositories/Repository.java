package edgruberman.bukkit.inventory.repositories;

public interface Repository<K, V> {

    /** @return true if an value exists for key */
    public abstract boolean contains(K key);

    /** @return value for key if found, null otherwise */
    public abstract V get(K key);

    public abstract void put(K key, V value);

    public abstract void remove(K key);

    /** perform final clean-up */
    public abstract void destroy();

}
