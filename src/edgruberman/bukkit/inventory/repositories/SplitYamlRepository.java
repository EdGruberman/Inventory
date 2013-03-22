package edgruberman.bukkit.inventory.repositories;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.inventory.util.BufferedYamlConfiguration;

/** individual buffered YAML files for each key */
public class SplitYamlRepository<V extends ConfigurationSerializable> implements Repository<String, V> {

    protected final Plugin plugin;
    protected final File folder;
    protected final int rate;

    protected final Map<String, BufferedYamlConfiguration> loaded = new HashMap<String, BufferedYamlConfiguration>();

    public SplitYamlRepository(final Plugin plugin, final File folder, final int rate) {
        this.plugin = plugin;
        this.folder = folder;
        this.rate = rate;
    }

    @Override
    public V load(final String key) {
        BufferedYamlConfiguration buffer = this.loaded.get(key);
        if (buffer == null) {
            buffer = new BufferedYamlConfiguration(this.plugin, new File(this.folder, key + ".yml"), this.rate);
            try {
                buffer.load();
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to load repository YAML file " + buffer.getFile(), e);
            }
            this.loaded.put(key, buffer);
        }

        try {
            @SuppressWarnings("unchecked")
            final V value = (V) buffer.get(key);
            return value;
        } catch (final ClassCastException e) {
            return null;
        }
    }

    @Override
    public void save(final String key, final V value) {
        BufferedYamlConfiguration buffer = this.loaded.get(key);
        if (buffer == null) {
            buffer = new BufferedYamlConfiguration(this.plugin, new File(this.folder, key + ".yml"), this.rate);
            this.loaded.put(key, buffer);
        }

        buffer.set(key, value);
        buffer.queueSave();
    }

    @Override
    public void delete(final String key) {
        final BufferedYamlConfiguration buffer = this.loaded.get(key);
        if (buffer != null) {
            if (buffer.isQueued()) buffer.cancelSave();
            buffer.getFile().delete();
            return;
        }

        final File file = new File(this.folder, key + ".yml");
        if (file.exists()) file.delete();
    }

    @Override
    public void destroy() {
        for (final BufferedYamlConfiguration buffer : this.loaded.values())
            if (buffer.isQueued()) buffer.save();

        this.loaded.clear();
    }

}
