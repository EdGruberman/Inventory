package edgruberman.bukkit.inventory.repositories;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.inventory.repositories.YamlRepository.SimpleString;
import edgruberman.bukkit.inventory.util.BufferedYamlConfiguration;

/** individual buffered YAML files for each key */
public class YamlRepository<V extends ConfigurationSerializable> implements Repository<SimpleString, V> {

    protected final Plugin plugin;
    protected final File folder;
    protected final int rate;

    protected final Map<SimpleString, BufferedYamlConfiguration> yaml = new HashMap<SimpleString, BufferedYamlConfiguration>();

    public YamlRepository(final Plugin plugin, final File folder, final int rate) {
        this.plugin = plugin;
        this.folder = folder;
        this.rate = rate;
    }

    private File fileFor(final SimpleString key) {
        return new File(this.folder, key.getValue() + ".yml");
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
            final V value = (V) buffer.get(key.getValue());
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

        buffer.set(key.getValue(), value);
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



    public static class SimpleString {

        public static final Pattern filter = Pattern.compile("[\\/:*\\?\"<>\\|]+");
        public static final String replacement = "_";

        public static SimpleString of(final String requested) {
            return new SimpleString(requested);
        }



        private final String value;

        public SimpleString(final String requested) {
            this.value = SimpleString.filter.matcher(requested).replaceAll(SimpleString.replacement);
        }

        public String getValue() {
            return this.value;
        }

        public SimpleString toLowerCase() {
            return SimpleString.of(this.value.toLowerCase());
        }

        @Override
        public int hashCode() {
            return this.value.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return this.value.equals(obj);
        }

    }

}
