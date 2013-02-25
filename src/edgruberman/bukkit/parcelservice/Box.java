package edgruberman.bukkit.parcelservice;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/** collection of ItemStacks */
@SerializableAs("Box")
public final class Box implements ConfigurationSerializable, Cloneable {

    public static final int SLOTS = 54;

    private final Inventory inventory;

    public Box() {
        this(Bukkit.createInventory(null, Box.SLOTS));
    }

    private Box(final Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Box label(final String title) {
        Main.craftBukkit.entitle(this.inventory, title);
        return this;
    }

    public InventoryView open(final Player player) {
        return player.openInventory(this.inventory);
    }

    public boolean empty() {
        for (final ItemStack content : this.inventory.getContents())
            if (content != null) return false;

        return true;
    }

    public boolean full() {
        return this.inventory.firstEmpty() == -1;
    }

    @Override
    public Box clone() {
        final Inventory inventory = Bukkit.createInventory(this.inventory.getHolder(), this.inventory.getSize(), this.inventory.getTitle());
        inventory.setContents(this.inventory.getContents());
        final Box cloned = new Box(inventory);
        cloned.label(this.getInventory().getTitle());
        return cloned;
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("title", this.inventory.getTitle());

        // only store slots with items in them
        final Map<String, ItemStack> contents = new LinkedHashMap<String, ItemStack>();
        final ItemStack[] array = this.inventory.getContents();
        for (int i = 0; i < array.length; i++)
            if (array[i] != null)
                contents.put(String.valueOf(i), array[i]);
        result.put("contents", contents);

        return result;
    }

    public static Box deserialize(final Map<String, Object> serialized) {
        final Box result = new Box();

        result.label((String) serialized.get("title"));

        @SuppressWarnings("unchecked")
        final Map<String, ItemStack> contents = (Map<String, ItemStack>) serialized.get("contents");
        for(final Map.Entry<String, ItemStack> entry : contents.entrySet())
            result.inventory.setItem(Integer.parseInt(entry.getKey()), entry.getValue());

        return result;
    }

}
