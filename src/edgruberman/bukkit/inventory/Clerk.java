package edgruberman.bukkit.inventory;

import java.io.File;
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

import edgruberman.bukkit.inventory.repositories.CachingRepository;
import edgruberman.bukkit.inventory.repositories.Repository;
import edgruberman.bukkit.inventory.repositories.YamlRepository;
import edgruberman.bukkit.inventory.repositories.YamlRepository.SimpleString;
import edgruberman.bukkit.inventory.sessions.Session;

/** session and repository manager */
public class Clerk implements Observer {

    static {
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(Delivery.class);
        ConfigurationSerialization.registerClass(CustomInventory.class);
    }

    private final Plugin plugin;
    private final CachingRepository<SimpleString, Kit> kits;
    private final CachingRepository<SimpleString, Delivery> deliveries;
    private final Map<NamedCustomInventoryList, List<Session>> sessions = new HashMap<NamedCustomInventoryList, List<Session>>();

    Clerk(final Plugin plugin, final File kits, final File deliveries) {
        this.plugin = plugin;

        final Repository<SimpleString, Kit> yamlKits = new YamlRepository<Kit>(this.plugin, kits, 30000);
        this.kits = new CachingRepository<SimpleString, Kit>(yamlKits);

        final Repository<SimpleString, Delivery> yamlDeliveries = new YamlRepository<Delivery>(this.plugin, deliveries, 30000);
        this.deliveries = new CachingRepository<SimpleString, Delivery>(yamlDeliveries);
    }

    public Kit createKit(final String name) {
        final SimpleString simple = SimpleString.of(name);
        final Kit result = new Kit(simple.getValue());
        this.kits.put(simple.toLowerCase(), result);
        return result;
    }

    public Kit getKit(final String key) {
        final SimpleString simple = SimpleString.of(key);
        if (!simple.equals(key)) return null;
        return this.kits.get(simple);
    }

    public void putKit(final Kit kit) {
        this.kits.put(SimpleString.of(kit.getName().toLowerCase()), kit);
    }

    public void removeKit(final Kit kit) {
        this.kits.remove(SimpleString.of(kit.getName().toLowerCase()));
    }

    public Delivery createDelivery(final String name) {
        final SimpleString simple = SimpleString.of(name);
        final Delivery result = new Delivery(simple.getValue());
        this.deliveries.put(simple.toLowerCase(), result);
        return result;
    }

    public Delivery getDelivery(final String key) {
        final SimpleString simple = SimpleString.of(key);
        if (!simple.equals(key)) return null;
        return this.deliveries.get(simple);
    }

    public void putDelivery(final Delivery delivery) {
        this.deliveries.put(SimpleString.of(delivery.getName().toLowerCase()), delivery);
    }

    public void removeDelivery(final Delivery delivery) {
        this.deliveries.remove(SimpleString.of(delivery.getName().toLowerCase()));
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

    public List<Session> sessionsFor(final Kit kit) {
        final List<Session> result = this.sessions.get(kit);
        if (result != null) return result;
        return Collections.emptyList();
    }

    public List<Session> sessionsFor(final Delivery delivery) {
        final List<Session> result = this.sessions.get(delivery);
        if (result != null) return result;
        return Collections.emptyList();
    }

    public void destroySessions(final Kit kit, final String reason) {
        this.destroySessions(this.sessionsFor(kit), reason);
    }

    public void destroySessions(final Delivery delivery, final String reason) {
        this.destroySessions(this.sessionsFor(delivery), reason);
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
        this.kits.destroy();
        this.deliveries.destroy();
    }

}
