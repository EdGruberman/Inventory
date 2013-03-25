package edgruberman.bukkit.inventory.repositories;

import java.util.HashMap;

// TODO weak hash map?  time out?  be smarter?
public class CachingRepository<K, V> implements Repository<K, V> {

    protected final Repository<K, V> source;
    protected final HashMap<K, V> cache = new HashMap<K, V>();

    public CachingRepository(final Repository<K, V> source) {
        this.source = source;
    }

    @Override
    public boolean contains(final K key) {
        return this.source.contains(key);
    }

    @Override
    public V get(final K key) {
        V value = this.cache.get(key);
        if (value != null) return value;

        value = this.source.get(key);
        if (value == null) return null;

        this.cache.put(key, value);
        return value;
    }

    @Override
    public void put(final K key, final V value) {
        this.cache.put(key, value);
        this.source.put(key, value);
    }

    @Override
    public void remove(final K key) {
        this.cache.remove(key);
        this.source.remove(key);
    }

    @Override
    public void destroy() {
        this.cache.clear();
        this.source.destroy();
    }

}
