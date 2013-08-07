package edgruberman.bukkit.inventory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CustomInventoryList extends ArrayList<CustomInventory> {
    private static final long serialVersionUID = 1L;

    public CustomInventoryList(final Collection<CustomInventory> elements) {
        super(elements);
    }

    /** @param pattern 0 = index, 1 = total, 2+ = arguments */
    public void formatTitles(final String pattern, final Object... arguments) {
        final Object[] concatenated = new Object[2 + arguments.length];
        System.arraycopy(arguments, 0, concatenated, 2, arguments.length);
        concatenated[1] = this.size();
        for (int i = 0; i < this.size(); i++) {
            concatenated[0] = i + 1;
            this.get(i).setTitle(MessageFormat.format(pattern, concatenated));
        }
    }

    /**
     * automatically add or remove inventories as necessary
     * @return items that could not be removed
     */
    public Collection<ItemStack> modify(final Collection<ItemStack> items) {
        final Collection<ItemStack> remaining = new ArrayList<ItemStack>();

        for (final ItemStack stack : items) {
            if (stack.getAmount() >= 0) {
                this.add(stack);
            } else {
                remaining.addAll(this.remove(stack));
            }
        }

        return remaining;
    }

    /**
     * automatically add inventories as necessary<p>
     * stacks complying with Material max size will be added to existing<br>
     * stacks exceeding Material max size will be added to first empty slot
     */
    public void add(final ItemStack stack) {
        final ItemStack clone = stack.clone();

        Map<Integer, ItemStack> remaining = Collections.emptyMap();
        if (stack.getAmount() <= stack.getMaxStackSize()) {
            for (final Inventory inv : this) {
                remaining = inv.addItem(clone);
                if (remaining.size() == 0) return;
            }
        } else {
            for (final Inventory inv : this) {
                final int empty = inv.firstEmpty();
                if (empty != -1) {
                    inv.setItem(empty, clone);
                    return;
                } else {
                    remaining = new HashMap<Integer, ItemStack>();
                    remaining.put(0, clone);
                }
            }
        }

        this.add(new CustomInventory());
        for (final ItemStack still : remaining.values()) this.add(still);
    }

    /**
     * automatically remove boxes as necessary
     * @param stack positive and negative amounts will both be removed
     * according to their absolute value
     * @return items that could not be removed
     */
    public Collection<ItemStack> remove(final ItemStack stack) {
        final ItemStack clone = stack.clone();
        clone.setAmount(Math.abs(stack.getAmount()));

        // TODO check for exact stack matches to remove first

        Map<Integer, ItemStack> remaining = Collections.emptyMap();
        for (final CustomInventory inv : this) {
            remaining = inv.removeItem(clone);
            if (remaining.size() == 0) break;
        }

        this.trim();
        return remaining.values();
    }

    public void removeAll() {
        for (final Inventory inv : this) inv.clear();
    }

    /** @return mirrors of all ItemStacks that are not null and not AIR */
    public List<ItemStack> getContents() {
        final List<ItemStack> result = new ArrayList<ItemStack>();
        for (final CustomInventory inv : this) result.addAll(inv.getPopulated());
        return result;
    }

    public boolean isContentsEmpty() {
        for (final CustomInventory inv : this)
            if (!inv.isEmpty()) return false;

        return true;
    }

    /**
     * clear empty inventories off end of list
     * @return count of inventories removed
     */
    public int trim() {
        int removed = 0;
        for (int i = this.size() - 1; i > 0; i--) {
            if (this.get(i).isEmpty()) {
                this.remove(i);
                removed++;
            }
        }
        return removed;
    }

    public Set<HumanEntity> getViewers() {
        final Set<HumanEntity> result = new HashSet<HumanEntity>();
        for (final CustomInventory inv : this) result.addAll(inv.getViewers());
        return result;
    }

}
