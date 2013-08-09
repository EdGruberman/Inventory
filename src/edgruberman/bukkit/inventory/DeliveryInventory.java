package edgruberman.bukkit.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Delivery")
public class DeliveryInventory extends InventoryList implements ConfigurationSerializable {
    private static final long serialVersionUID = 1L;

    public static DeliveryInventory deserialize(final Map<String, Object> serialized) {
        final String name = (String) serialized.get("key");
        @SuppressWarnings("unchecked")
        final List<InventoryAdapter> elements = (ArrayList<InventoryAdapter>) serialized.get("elements");
        return new DeliveryInventory(name, elements);
    }



    public DeliveryInventory(final String name) {
        super(name);
    }

    public DeliveryInventory(final String name, final Collection<InventoryAdapter> elements) {
        super(name, elements);
    }

}
