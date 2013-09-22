package edgruberman.bukkit.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Kit")
public class KitInventory extends InventoryList implements ConfigurationSerializable {

    public static KitInventory deserialize(final Map<String, Object> serialized) {
        final String name = (String) serialized.get("key");
        @SuppressWarnings("unchecked")
        final List<InventoryAdapter> elements = (ArrayList<InventoryAdapter>) serialized.get("elements");
        return new KitInventory(name, elements);
    }

    public static KitInventory create(final String name, final String title) {
        final KitInventory kit = new KitInventory(name);
        kit.formatTitles(title, kit.getName());
        return kit;
    }



    public KitInventory(final String name) {
        super(name);
    }

    public KitInventory(final String name, final Collection<InventoryAdapter> elements) {
        super(name, elements);
    }

}
