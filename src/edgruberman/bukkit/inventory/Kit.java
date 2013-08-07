package edgruberman.bukkit.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Kit")
public final class Kit extends NamedCustomInventoryList implements ConfigurationSerializable {
    private static final long serialVersionUID = 1L;

    Kit(final String name) {
        this(name, Collections.<CustomInventory>emptyList());
    }

    private Kit(final String name, final Collection<CustomInventory> elements) {
        super(name, Main.courier.translate("title-kit").get(0), elements);
    }

    @SuppressWarnings("unchecked")
    public static Kit deserialize(final Map<String, Object> serialized) {
        final String key = (String) serialized.get("key");
        final List<CustomInventory>  elements = (ArrayList<CustomInventory>) serialized.get("elements");
        return new Kit(key, elements);
    }

}
