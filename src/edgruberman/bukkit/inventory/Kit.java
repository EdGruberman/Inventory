package edgruberman.bukkit.inventory;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

/** collection of ItemStacks */
@SerializableAs("Kit")
public class Kit implements ConfigurationSerializable {

    private final String name;
    private final Pallet contents;

    private String definer = null;

    public Kit(final String name) {
        this.name = name;
        this.contents = new Pallet();
        this.relabel();
    }

    private Kit(final String name, final Pallet contents) {
        this.name = name;
        this.contents = contents;
    }

    public String getName() {
        return this.name;
    }

    public Pallet getContents() {
        return this.contents;
    }

    public void relabel() {
        this.contents.label(Main.courier.format("box-kit", "{0}", "{1}", this.name));
    }

    public boolean beingDefined() {
        return this.getDefiner() != null;
    }

    public Player getDefiner() {
        if (this.definer == null) return null;
        return Bukkit.getPlayerExact(this.definer);
    }

    public void setDefiner(final Player definer) {
        if (definer == null) {
            this.definer = null;
            return;
        }

        this.definer = definer.getName();
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

}
