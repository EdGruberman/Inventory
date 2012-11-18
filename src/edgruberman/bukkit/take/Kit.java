package edgruberman.bukkit.take;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class Kit {

    private final String name;
    private final List<ItemStack> items = new ArrayList<ItemStack>();

    public Kit(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public StringBuilder describe() {
        final StringBuilder description = new StringBuilder();
        for (final ItemStack item : this.items) {
            if (description.length() != 0) description.append(Main.courier.format("+contents.+delimiter"));
            description.append(Main.courier.format("+contents.+item", item.getType().name(), item.getDurability(), item.getAmount()));
        }

        return description;
    }

    /**
     * @param materialData "[#](Material)[/(Data)]" (e.g. "DIRT", "POTION/32767", "#0357/16", "17", "17/8")
     * @throws IllegalArgumentException if Material is unable to be matched
     * @throws NumberFormatException if Data is unable to be parsed to a short
     */
    public void add(String materialData, final int amount) throws IllegalArgumentException, NumberFormatException {
        if (materialData.startsWith("#")) materialData = materialData.substring(1);
        final String[] values = materialData.split("/");
        final Material material = Material.matchMaterial(values[0]);
        if (material == null) throw new IllegalArgumentException("Unrecognized Material: " + values[0]);
        final Short data = ( values.length >= 2 ? Short.valueOf(values[1]) : null );
        this.items.add(data == null ? new ItemStack(material, amount) : new ItemStack(material, amount, data));
    }

    public void give(final Player player) {
        for (final ItemStack item : this.items)
            player.getOpenInventory().setItem(InventoryView.OUTSIDE, item);
    }

}
