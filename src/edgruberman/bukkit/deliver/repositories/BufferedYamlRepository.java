package edgruberman.bukkit.deliver.repositories;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.deliver.Box;
import edgruberman.bukkit.deliver.Kit;
import edgruberman.bukkit.deliver.Ledger;
import edgruberman.bukkit.deliver.Pallet;
import edgruberman.bukkit.deliver.Transaction;
import edgruberman.bukkit.deliver.util.BufferedYamlConfiguration;

public class BufferedYamlRepository<V extends ConfigurationSerializable> implements Repository<String, V> {

    static {
        ConfigurationSerialization.registerClass(Box.class);
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(Ledger.class);
        ConfigurationSerialization.registerClass(Pallet.class);
        ConfigurationSerialization.registerClass(Transaction.class);
    }

    protected final BufferedYamlConfiguration config;

    public BufferedYamlRepository(final Plugin plugin, final File yaml, final int rate) throws IOException, InvalidConfigurationException {
        this.config = new BufferedYamlConfiguration(plugin, yaml, rate);
        this.config.load();
    }

    @Override
    public V load(final String path) {
        try {
            @SuppressWarnings("unchecked")
            final V value = (V) this.config.get(path);
            return value;
        } catch (final ClassCastException e) {
            return null;
        }
    }

    @Override
    public void save(final String path, final V value) {
        this.config.set(path, value);
        this.config.queueSave();
    }

    @Override
    public void delete(final String path) {
        this.config.set(path, null);
        this.config.queueSave();
    }

    @Override
    public void destroy() {
        if (this.config.isQueued()) this.config.save();
    }

}
