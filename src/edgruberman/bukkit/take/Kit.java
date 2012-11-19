package edgruberman.bukkit.take;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/** collection of stacks of items **/
public class Kit {

    private final String name;
    private final List<ItemStack> contents = new ArrayList<ItemStack>();

    public Kit(final String name, final List<ItemStack> contents) {
        this.name = name;
        this.contents.addAll(contents);
    }

    public String getName() {
        return this.name;
    }

    public List<ItemStack> getContents() {
        return Collections.unmodifiableList(this.contents);
    }

    /** add directly to inventory, throw items that don't fit onto ground in front of player **/
    public void give(final Player player) {
        for (final ItemStack item : this.contents) {
            final Collection<ItemStack> ungiven = player.getInventory().addItem(item).values();
            if (ungiven.size() >= 0)
                for (final ItemStack remaining : ungiven)
                    player.getOpenInventory().setItem(InventoryView.OUTSIDE, remaining);
        }
    }

    public StringBuilder describe() {
        final StringBuilder description = new StringBuilder();
        for (final ItemStack item : this.contents) {
            if (description.length() != 0) description.append(Main.courier.format("+contents.+delimiter"));
            description.append(Main.courier.format("+contents.+item", item.getType().name(), item.getDurability(), item.getAmount()));
        }
        return description;
    }

    @Override
    public String toString() {
        return this.describe().toString();
    }

}
