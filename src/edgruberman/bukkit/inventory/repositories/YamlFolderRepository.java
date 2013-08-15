package edgruberman.bukkit.inventory.repositories;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.inventory.repositories.YamlFolderRepository.SimpleString;
import edgruberman.bukkit.inventory.util.BufferedYamlConfiguration;

/** individual buffered YAML files for each key */
public class YamlFolderRepository<V extends ConfigurationSerializable> implements Repository<SimpleString, V> {

    protected final Plugin plugin;
    protected final File folder;
    protected final int rate;

    protected final Map<SimpleString, BufferedYamlConfiguration> yaml = new HashMap<SimpleString, BufferedYamlConfiguration>();

    public YamlFolderRepository(final Plugin plugin, final File folder, final int rate) {
        this.plugin = plugin;
        this.folder = folder;
        this.rate = rate;
    }

    private File fileFor(final Key key) {
        return new File(this.folder, key + ".yml");
    }

    @Override
    public boolean contains(final SimpleString key) {
        return this.fileFor(key).exists();
    }

    @Override
    public V get(final SimpleString key) {
        // prefer buffer over existing file
        BufferedYamlConfiguration buffer = this.yaml.get(key);
        if (buffer == null) {
            final File file = this.fileFor(key);
            if (!file.exists()) return null;

            buffer = new BufferedYamlConfiguration(this.plugin, file, this.rate);
            try {
                buffer.load();
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to load repository YAML file: " + buffer.getFile(), e);
            }
            this.yaml.put(key, buffer);
        }

        try {
            @SuppressWarnings("unchecked")
            final V value = (V) buffer.get(key.toString());
            return value;

        } catch (final ClassCastException e) {
            throw new IllegalStateException("Unable to get value from repository for key: " + key, e);
        }
    }

    @Override
    public void put(final SimpleString key, final V value) {
        BufferedYamlConfiguration buffer = this.yaml.get(key);
        if (buffer == null) {
            buffer = new BufferedYamlConfiguration(this.plugin, this.fileFor(key), this.rate);
            this.yaml.put(key, buffer);
        }

        buffer.set(key.toString(), value);
        buffer.queueSave();
    }

    @Override
    public void remove(final SimpleString key) {
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
        for (final BufferedYamlConfiguration buffer : this.yaml.values()) {
            if (buffer.isQueued()) buffer.save();
        }

        this.yaml.clear();
    }

    @Override
    public SimpleString createKey(final String value) {
        return new SimpleString(value);
    }



    public static class SimpleString extends Repository.Key.StringKey {

        public static final Pattern filter = Pattern.compile("[\\/:*\\?\"<>\\|]+");
        public static final String replacement = "_";

        public SimpleString(final String requested) {
            super(SimpleString.filter.matcher(requested).replaceAll(SimpleString.replacement).toLowerCase());
        }

    }

}
