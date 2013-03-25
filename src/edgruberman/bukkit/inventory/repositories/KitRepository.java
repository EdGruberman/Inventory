package edgruberman.bukkit.inventory.repositories;

import edgruberman.bukkit.inventory.Kit;

public class KitRepository extends CachingRepository<String, Kit>{

    public KitRepository(final Repository<String, Kit> source) {
        super(source);
    }

    public Kit create(final String name) {
        Kit result = this.get(name);
        if (result != null) return result;

        result = new Kit(name);
        this.cache.put(result.getList().getKey(), result);
        return result;
    }

    @Override
    public Kit get(final String name) {
        return super.get(name.toLowerCase());
    }

    /** @see {@link #put(Kit)} */
    @Deprecated
    @Override
    public void put(final String key, final Kit kit) {
        throw new UnsupportedOperationException("use put(Kit) instead");
    }

    public void put(final Kit kit) {
        super.put(kit.getList().getKey(), kit);
    }

    /** @see {@link #remove(Kit)} */
    @Deprecated
    @Override
    public void remove(final String key) {
        throw new UnsupportedOperationException("use remove(Kit) instead");
    }

    public void remove(final Kit kit) {
        super.remove(kit.getList().getKey());
    }

}
