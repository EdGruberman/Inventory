package edgruberman.bukkit.inventory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

/**
 * associates a string with a list of inventories
 * which can then be leveraged for display titles and serialization
 */
@SerializableAs("InventoryList")
public class NamedCustomInventoryList extends CustomInventoryList implements ConfigurationSerializable {
    private static final long serialVersionUID = 1L;

    protected final String name;
    protected final String title;

    public NamedCustomInventoryList(final String name, final String title) {
        this(name, title, Collections.<CustomInventory>emptyList());
    }

    public NamedCustomInventoryList(final String name, final String title, final Collection<CustomInventory> elements) {
        super(elements);
        this.name = name;
        this.title = title;
        if (elements.isEmpty()) this.add(new CustomInventory());
        this.formatTitles();
    }

    public String getName() {
        return this.name;
    }

    public void formatTitles() {
        this.formatTitles(this.title, this.name);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("key", this.name);
        result.put("elements", this.subList(0, this.size()));
        return result;
    }

}
