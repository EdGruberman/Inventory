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

import edgruberman.bukkit.inventory.repositories.DeliveryRepository;
import edgruberman.bukkit.inventory.repositories.KitRepository;
import edgruberman.bukkit.inventory.repositories.YamlRepository;
import edgruberman.bukkit.inventory.sessions.Session;

/** session and repository manager */
public class Clerk implements Observer {

    static {
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(Delivery.class);
        ConfigurationSerialization.registerClass(Pallet.class);
        ConfigurationSerialization.registerClass(Box.class);
    }

    private final Plugin plugin;
    private final KitRepository kitRepository;
    private final DeliveryRepository deliveryRepository;
    private final Map<Object, List<Session>> sessions = new HashMap<Object, List<Session>>();

    Clerk(final Plugin plugin, final File kits, final File deliveries) {
        this.plugin = plugin;

        final YamlRepository<Kit> yamlKits = new YamlRepository<Kit>(this.plugin, kits, 30000);
        this.kitRepository = new KitRepository(yamlKits);

        final YamlRepository<Delivery> yamlDeliveries = new YamlRepository<Delivery>(this.plugin, deliveries, 30000);
        this.deliveryRepository = new DeliveryRepository(yamlDeliveries);
    }

    public KitRepository getKitRepository() {
        return this.kitRepository;
    }

    public DeliveryRepository getDeliveryRepository() {
        return this.deliveryRepository;
    }

    public void startSession(final Session session) {
        final Object key = session.getKey();
        if (!this.sessions.containsKey(key)) this.sessions.put(key, new ArrayList<Session>());
        this.sessions.get(session.getKey()).add(session);

        session.addObserver(this);
        session.start();
        Bukkit.getPluginManager().registerEvents(session, this.plugin);
    }

    @Override
    public void update(final Observable o, final Object arg) {
        final Session session = (Session) o;
        final List<Session> sessions = this.sessions.get(session.getKey());
        sessions.remove(session);
        if (sessions.size() == 0) this.sessions.remove(session.getKey());
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
        this.kitRepository.destroy();
        this.deliveryRepository.destroy();
    }

}
