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
        ConfigurationSerialization.registerClass(InventoryAdapter.class);
        ConfigurationSerialization.registerClass(InventoryList.class);
    }

    private final Plugin plugin;
    private final CachingRepository<SimpleString, InventoryList> kits;
    private final CachingRepository<SimpleString, InventoryList> deliveries;
    private final Map<InventoryList, List<Session>> sessions = new HashMap<InventoryList, List<Session>>();

    Clerk(final Plugin plugin, final File kits, final File deliveries) {
        this.plugin = plugin;

        final Repository<SimpleString, InventoryList> yamlKits = new YamlRepository<InventoryList>(this.plugin, kits, 30000);
        this.kits = new CachingRepository<SimpleString, InventoryList>(yamlKits);

        final Repository<SimpleString, InventoryList> yamlDeliveries = new YamlRepository<InventoryList>(this.plugin, deliveries, 30000);
        this.deliveries = new CachingRepository<SimpleString, InventoryList>(yamlDeliveries);
    }
//
//    public NamedCustomInventoryList createInventory(final String name) {
//        final SimpleString simple = SimpleString.of(name);
//        final Kit result = new NamedCustomInventoryList(simple.getValue());
//        this.kits.put(simple.toLowerCase(), result);
//        return result;
//    }
//
//    public NamedCustomInventoryList getInventory(final String key) {
//        final SimpleString simple = SimpleString.of(key);
//        if (!simple.equals(key)) return null;
//        return this.kits.get(simple);
//    }
//
//    public void putInventory(final NamedCustomInventoryList inventory) {
//        this.kits.put(SimpleString.of(inventory.getName().toLowerCase()), inventory);
//    }
//
//    public void removeInventory(final NamedCustomInventoryList inventory) {
//        this.kits.remove(SimpleString.of(inventory.getName().toLowerCase()));
//    }

    public InventoryList createKit(final String name) {
        final SimpleString simple = SimpleString.of(name);
        final InventoryList result = new InventoryList(simple.getValue());
        this.kits.put(simple.toLowerCase(), result);
        return result;
    }

    public InventoryList getKit(final String key) {
        final SimpleString simple = SimpleString.of(key);
        if (!simple.equals(key)) return null;
        return this.kits.get(simple);
    }

    public void putKit(final InventoryList kit) {
        this.kits.put(SimpleString.of(kit.getName().toLowerCase()), kit);
    }

    public void removeKit(final InventoryList kit) {
        this.kits.remove(SimpleString.of(kit.getName().toLowerCase()));
    }

    public String getKitTitle() {
        return Main.courier.translate("title-kit").get(0);
    }

    public InventoryList createDelivery(final String name) {
        final SimpleString simple = SimpleString.of(name);
        final InventoryList result = new InventoryList(simple.getValue());
        this.deliveries.put(simple.toLowerCase(), result);
        return result;
    }

    public InventoryList getDelivery(final String key) {
        final SimpleString simple = SimpleString.of(key);
        if (!simple.equals(key)) return null;
        return this.deliveries.get(simple);
    }

    public void putDelivery(final InventoryList delivery) {
        this.deliveries.put(SimpleString.of(delivery.getName().toLowerCase()), delivery);
    }

    public void removeDelivery(final InventoryList delivery) {
        this.deliveries.remove(SimpleString.of(delivery.getName().toLowerCase()));
    }

    public String getDeliveryTitle() {
        return Main.courier.translate("title-delivery").get(0);
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
        this.kits.destroy();
        this.deliveries.destroy();
    }

}
