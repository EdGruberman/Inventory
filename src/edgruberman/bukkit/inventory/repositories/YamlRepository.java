package edgruberman.bukkit.inventory.repositories;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.inventory.util.BufferedYamlConfiguration;

/** individual buffered YAML files for each key */
public class YamlRepository<V extends ConfigurationSerializable> implements Repository<String, V> {

    protected final Plugin plugin;
    protected final File folder;
    protected final int rate;

    protected final Map<String, BufferedYamlConfiguration> yaml = new HashMap<String, BufferedYamlConfiguration>();

    public YamlRepository(final Plugin plugin, final File folder, final int rate) {
        this.plugin = plugin;
        this.folder = folder;
        this.rate = rate;
    }

    @Override
    public boolean contains(final String key) {
        final File file = new File(this.folder, key + ".yml");
        return file.exists();
    }

    @Override
    public V get(final String key) {
        BufferedYamlConfiguration buffer = this.yaml.get(key);
        if (buffer == null) {
            final File file = new File(this.folder, key + ".yml");
            if (!file.exists()) return null;

            buffer = new BufferedYamlConfiguration(this.plugin, file, this.rate);
            try {
                buffer.load();
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to load repository YAML file " + buffer.getFile(), e);
            }
            this.yaml.put(key, buffer);
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
    public void put(final String key, final V value) {
        BufferedYamlConfiguration buffer = this.yaml.get(key);
        if (buffer == null) {
            buffer = new BufferedYamlConfiguration(this.plugin, new File(this.folder, key + ".yml"), this.rate);
            this.yaml.put(key, buffer);
        }

        buffer.set(key, value);
        buffer.queueSave();
    }

    @Override
    public void remove(final String key) {
        final BufferedYamlConfiguration buffer = this.yaml.remove(key);
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
        for (final BufferedYamlConfiguration buffer : this.yaml.values())
            if (buffer.isQueued()) buffer.save();

        this.yaml.clear();
    }

}
