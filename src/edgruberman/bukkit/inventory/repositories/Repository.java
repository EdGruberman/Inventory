package edgruberman.bukkit.inventory.repositories;

public interface Repository<K, V> {

    /** @return true if an value exists for key */
    public abstract boolean contains(K key);

    /** @return value for key; null if not found */
    public abstract V get(K key);

    public abstract void put(K key, V value);

    /** delete value associated with key */
    public abstract void remove(K key);

    /** prepare repository for garbage collection */
    public abstract void destroy();

}
