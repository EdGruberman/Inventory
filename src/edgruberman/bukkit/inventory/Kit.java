package edgruberman.bukkit.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Kit")
public class Kit implements ConfigurationSerializable {

    private final KeyedInventoryList list;

    public Kit(final String name) {
        this(name, Collections.<CustomInventory>emptyList());
    }

    private Kit(final String name, final Collection<CustomInventory> elements) {
        this.list = new KeyedInventoryList(name, Main.courier.translate("title-kit"), elements);
    }

    public KeyedInventoryList getList() {
        return this.list;
    }

    @Override
    public Map<String, Object> serialize() {
        return this.list.serialize();
    }

    @SuppressWarnings("unchecked")
    public static Kit deserialize(final Map<String, Object> serialized) {
        final String key = (String) serialized.get("key");
        final List<CustomInventory>  elements = (ArrayList<CustomInventory>) serialized.get("elements");
        return new Kit(key, elements);
    }

}
