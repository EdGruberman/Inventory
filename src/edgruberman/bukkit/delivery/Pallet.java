package edgruberman.bukkit.delivery;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** collection of Boxes */
@SerializableAs("Pallet")
public final class Pallet implements ConfigurationSerializable, Cloneable {

    private final List<Box> boxes = new ArrayList<Box>();

    public Pallet() {
        this.addBox();
    }

    private Pallet(final List<Box> boxes) {
        this.boxes.addAll(boxes);
    }

    public List<Box> getBoxes() {
        return this.boxes;
    }

    public Box addBox() {
        final Box box = new Box();
        this.boxes.add(box);
        return box;
    }

    /** @param format 0 = index, 1 = total */
    public void label(final String format) {
        final int total = this.boxes.size();
        for (int i = 0; i < total; i++) {
            final Box box = this.boxes.get(i);
            box.label(MessageFormat.format(format, i + 1, total));
        }
    }

    /**
     * automatically add or remove boxes as necessary
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

    /** automatically add boxes as necessary */
    public void add(final ItemStack stack) {
        final ItemStack clone = stack.clone(); // TODO not clone for recursive calls

        final Map<Integer, ItemStack> remaining = this.boxes.get(this.boxes.size() - 1).getInventory().addItem(clone);
        if (remaining.size() == 0) return;

        this.boxes.add(new Box());
        for (final ItemStack still : remaining.values()) this.add(still);
    }

    /**
     * automatically remove boxes as necessary
     * @param stack positive and negative amounts will both be removed according to their absolute value
     * @return items that could not be removed
     */
    public Collection<ItemStack> remove(final ItemStack stack) {
        final ItemStack clone = stack.clone();
        clone.setAmount(Math.abs(stack.getAmount()));

        Map<Integer, ItemStack> remaining = Collections.emptyMap();
        for (final Box box : this.boxes) {
            remaining = box.getInventory().removeItem(clone);
            if (remaining.size() == 0) break;
        }

        this.trim();
        return remaining.values();
    }

    public List<ItemStack> joined() {
        final List<ItemStack> result = new ArrayList<ItemStack>();

        for (final Box box : this.boxes) {
            for (final ItemStack original : box.getInventory().getContents()) {
                if (original == null) continue;

                boolean similar = false;
                for (final ItemStack compressed : result) {
                    if (compressed.isSimilar(original)) {
                        compressed.setAmount(compressed.getAmount() + original.getAmount());
                        similar = true;
                        break;
                    }
                }

                if (!similar) result.add(original.clone());
            }
        }

        return result;
    }

    public boolean empty() {
        for (final Box box : this.boxes)
            if (!box.empty()) return false;

        return true;
    }

    /** clear empty boxes off end of pallet */
    public boolean trim() {
        boolean removed = false;
        for (int i = this.boxes.size() - 1; i >= 0; i--) {
            final Box last = this.boxes.get(i);
            if (last.getInventory().getContents().length == 0) {
                this.boxes.remove(i);
                removed = true;
            }
        }
        return removed;
    }

    public List<Player> viewers() {
        final List<Player> result = new ArrayList<Player>();

        for (final Box box : this.boxes)
            for (final HumanEntity viewer : box.getInventory().getViewers())
                result.add((Player) viewer);

        return result;
    }

    @Override
    public Pallet clone() {
        final List<Box> cloned = new ArrayList<Box>();
        for (final Box box : this.boxes)
            cloned.add(box.clone());

        return new Pallet(cloned);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("boxes", this.boxes);
        return result;
    }

    public static Pallet deserialize(final Map<String, Object> serialized) {
        @SuppressWarnings("unchecked")
        final List<Box> boxes = (List<Box>) serialized.get("boxes");
        return new Pallet(boxes);
    }

}
