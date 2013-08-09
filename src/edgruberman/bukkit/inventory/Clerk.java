package edgruberman.bukkit.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.inventory.repositories.Repository;
import edgruberman.bukkit.inventory.sessions.Session;

/** repository and session manager */
public class Clerk implements Observer {

    static {
        ConfigurationSerialization.registerClass(InventoryAdapter.class);
        ConfigurationSerialization.registerClass(DeliveryInventory.class);
        ConfigurationSerialization.registerClass(KitInventory.class);
    }

    private final Plugin plugin;
    private final Map<Class<? extends InventoryList>, Repository<Repository.Key, InventoryList>> repositories
            = new HashMap<Class<? extends InventoryList>, Repository<Repository.Key, InventoryList>>();
    private final Map<InventoryList, List<Session>> sessions = new HashMap<InventoryList, List<Session>>();

    Clerk(final Plugin plugin) {
        this.plugin = plugin;
    }

    public Repository<Repository.Key, InventoryList> putRepository(final Class<? extends InventoryList> type
            , final Repository<? extends Repository.Key, ? extends InventoryList> repository) {
        @SuppressWarnings("unchecked")
        final Repository<Repository.Key, InventoryList> cast = (Repository<Repository.Key, InventoryList>) repository;
        return this.repositories.put(type, cast);
    }

    /** @return value for key; null if not found */
    public InventoryList getInventory(final Class<? extends InventoryList> type, final String key) {
        final Repository<Repository.Key, InventoryList> repository = this.repositories.get(type);
        return repository.get(repository.createKey(key));
    }

    /** replaces existing value */
    public void putInventory(final InventoryList inventory) {
        final Repository<Repository.Key, InventoryList> repository = this.repositories.get(inventory.getClass());
        repository.put(repository.createKey(inventory.getName()), inventory);
    }

    // TODO end viewer sessions before removing
    /** delete value associated with key */
    public void removeInventory(final InventoryList inventory) {
        final Repository<Repository.Key, InventoryList> repository = this.repositories.get(inventory.getClass());
        repository.remove(repository.createKey(inventory.getName()));
    }

    @Override
    public void update(final Observable o, final Object arg) {
        final Session session = (Session) o;
        final List<Session> sessions = this.sessions.get(session.getInventory());
        sessions.remove(session);
        if (sessions.size() == 0) this.sessions.remove(session.getInventory());
    }

    public void openSession(final Session session) {
        if (!this.sessions.containsKey(session.getInventory())) this.sessions.put(session.getInventory(), new ArrayList<Session>());
        this.sessions.get(session.getInventory()).add(session);

        session.addObserver(this);
        session.getInventory().get(session.getIndex()).open(session.getCustomer());
        Bukkit.getPluginManager().registerEvents(session, this.plugin);
    }

    public List<Session> sessionsFor(final InventoryList inventory) {
        final List<Session> result = this.sessions.get(inventory);
        if (result != null) return result;
        return Collections.emptyList();
    }

    public void destroySessions(final InventoryList inventory, final String reason) {
        this.destroySessions(this.sessionsFor(inventory), reason);
    }

    public void destroySessions(final String reason) {
        for (final List<Session> list : this.sessions.values())
            this.destroySessions(list, reason);
    }

    private void destroySessions(final List<Session> sessions, final String reason) {
        final List<Session> clone = new ArrayList<Session>(sessions);
        for (final Session session : clone) session.destroy(reason);
        this.sessions.clear();
    }

    public void destroy(final String reason) {
        this.destroySessions(reason);
        for (final Repository<Repository.Key, InventoryList> repository : this.repositories.values()) repository.destroy();
    }

}
