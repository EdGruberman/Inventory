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
        ConfigurationSerialization.registerClass(CustomInventory.class);
    }

    private final Plugin plugin;
    private final KitRepository kitRepository;
    private final DeliveryRepository deliveryRepository;
    private final Map<KeyedInventoryList, List<Session>> sessions = new HashMap<KeyedInventoryList, List<Session>>();

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

    public void openSession(final Session session) {
        if (!this.sessions.containsKey(session.getList())) this.sessions.put(session.getList(), new ArrayList<Session>());
        this.sessions.get(session.getList()).add(session);

        session.addObserver(this);
        session.getList().get(session.getIndex()).open(session.getCustomer());
        Bukkit.getPluginManager().registerEvents(session, this.plugin);
    }

    @Override
    public void update(final Observable o, final Object arg) {
        final Session session = (Session) o;
        final List<Session> sessions = this.sessions.get(session.getList());
        sessions.remove(session);
        if (sessions.size() == 0) this.sessions.remove(session.getList());
    }

    public List<Session> sessionsFor(final Kit kit) {
        final List<Session> result = this.sessions.get(kit.getList());
        if (result != null) return result;
        return Collections.emptyList();
    }

    public List<Session> sessionsFor(final Delivery delivery) {
        final List<Session> result = this.sessions.get(delivery.getList());
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
