package edgruberman.bukkit.inventory.repositories;

import java.util.HashMap;

/** caches objects in memory to avoid duplicating expensive repository calls */
public class CachingRepository<K extends Repository.Key, V> implements Repository<K, V> {

    public static <L extends Repository.Key, W> CachingRepository<L, W> of(final Repository<L, W> source) {
        return new CachingRepository<L, W>(source);
    }

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

    @Override
    public K createKey(final String value) {
        return this.source.createKey(value);
    }

}
