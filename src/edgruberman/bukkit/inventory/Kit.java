package edgruberman.bukkit.inventory;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

/** collection of ItemStacks */
@SerializableAs("Kit")
public class Kit implements ConfigurationSerializable {

    private final String name;
    private final Pallet contents;
    private final String key;

    public Kit(final String name) {
        this.name = name;
        this.contents = new Pallet();
        this.key = this.name.toLowerCase();
        this.relabel();
    }

    private Kit(final String name, final Pallet contents) {
        this.name = name;
        this.contents = contents;
        this.key = this.name.toLowerCase();
    }

    public String getName() {
        return this.name;
    }

    public String getKey() {
        return this.key;
    }

    public Pallet getContents() {
        return this.contents;
    }

    public void relabel() {
        this.contents.label("box-kit", this.name);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", this.getName());
        result.put("contents", this.contents);
        return result;
    }

    public static Kit deserialize(final Map<String, Object> serialized) {
        final String name = (String) serialized.get("name");
        final Pallet contents = (Pallet) serialized.get("contents");
        return new Kit(name, contents);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.key.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final Kit other = (Kit) obj;
        if (!this.key.equals(other.key)) return false;
        return true;
    }

}
