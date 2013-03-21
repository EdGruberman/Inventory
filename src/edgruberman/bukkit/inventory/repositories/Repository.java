package edgruberman.bukkit.inventory.repositories;

public interface Repository<K, V> {

    /** @return value for key if found, null otherwise */
    public abstract V load(K key);

    public abstract void save(K key, V value);

    public abstract void delete(K key);

    /** perform final clean-up */
    public abstract void destroy();

}
