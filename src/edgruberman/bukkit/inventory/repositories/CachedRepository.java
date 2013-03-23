package edgruberman.bukkit.inventory.repositories;

import java.util.HashMap;

// TODO weak hash map?  time out?  be smarter?
public class CachedRepository<K, V> implements Repository<K, V> {

    protected final Repository<K, V> source;
    protected final HashMap<K, V> cache = new HashMap<K, V>();

    public CachedRepository(final Repository<K, V> source) {
        this.source = source;
    }

    @Override
    public V load(final K key) {
        V value = this.cache.get(key);
        if (value != null) return value;

        value = this.source.load(key);
        if (value == null) return null;

        this.cache.put(key, value);
        return value;
    }

    @Override
    public void save(final K key, final V value) {
        this.cache.put(key, value);
        this.source.save(key, value);
    }

    @Override
    public void delete(final K key) {
        this.cache.remove(key);
        this.source.delete(key);
    }

    @Override
    public void destroy() {
        this.cache.clear();
        this.source.destroy();
    }

}
