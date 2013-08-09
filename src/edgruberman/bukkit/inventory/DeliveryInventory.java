package edgruberman.bukkit.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Delivery")
public class DeliveryInventory extends InventoryList implements ConfigurationSerializable {

    public static DeliveryInventory deserialize(final Map<String, Object> serialized) {
        final String name = (String) serialized.get("key");
        @SuppressWarnings("unchecked")
        final List<InventoryAdapter> elements = (ArrayList<InventoryAdapter>) serialized.get("elements");
        return new DeliveryInventory(name, elements);
    }

    public static DeliveryInventory create(final String name, final String title) {
        final DeliveryInventory delivery = new DeliveryInventory(name);
        delivery.formatTitles(title, delivery.getName());
        return delivery;
    }



    public DeliveryInventory(final String name) {
        super(name);
    }

    public DeliveryInventory(final String name, final Collection<InventoryAdapter> elements) {
        super(name, elements);
    }

}
