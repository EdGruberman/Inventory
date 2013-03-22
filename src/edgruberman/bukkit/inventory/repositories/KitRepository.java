package edgruberman.bukkit.inventory.repositories;

import edgruberman.bukkit.inventory.Kit;

public class KitRepository extends CachedRepository<String, Kit>{

    public KitRepository(final Repository<String, Kit> source) {
        super(source);
    }

    public Kit create(final String name) {
        Kit result = this.load(name);
        if (result != null) return result;

        result = new Kit(name);
        this.cache.put(name.toLowerCase(), result);
        return result;
    }

    @Override
    public Kit load(final String name) {
        return super.load(name.toLowerCase());
    }

    public void save(final Kit kit) {
        this.save(kit.getName().toLowerCase(), kit);
    }

    public void delete(final Kit kit) {
        this.delete(kit.getName().toLowerCase());
    }

}
