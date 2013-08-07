package edgruberman.bukkit.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/** Inventory adapter */
@SerializableAs("Inventory")
public final class CustomInventory implements Inventory, ConfigurationSerializable, Cloneable {

    public static final int SIZE = 54;

    private final Inventory inventory;

    public CustomInventory() {
        this(Bukkit.createInventory(null, CustomInventory.SIZE));
    }

    public CustomInventory(final Inventory inventory) {
        this.inventory = inventory;
    }

    public void open(final Player player) {
        player.openInventory(this.inventory);
    }

    public CustomInventory setTitle(final String title) {
        Main.craftBukkit.setTitle(this.inventory, title);
        return this;
    }

    /** non-empty/non-null/non-air stacks */
    public List<ItemStack> getPopulated() {
        final List<ItemStack> result = new ArrayList<ItemStack>();
        for (final ItemStack stack : this.getContents()) {
            if (stack != null && stack.getTypeId() != Material.AIR.getId()) {
                result.add(stack);
            }
        }

        return result;
    }

    public boolean isEmpty() {
        for (final ItemStack content : this.inventory.getContents()) {
            if (content != null) return false;
        }

        return true;
    }

    public boolean isFull() {
        return this.inventory.firstEmpty() == -1;
    }

    @Override
    public CustomInventory clone() {
        final Inventory inventory = Bukkit.createInventory(this.inventory.getHolder(), this.inventory.getSize(), this.inventory.getTitle());
        inventory.setContents(this.inventory.getContents());
        return new CustomInventory(inventory);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("title", this.inventory.getTitle());

        // only store slots with items in them
        final Map<String, ItemStack> contents = new LinkedHashMap<String, ItemStack>();
        final ItemStack[] array = this.inventory.getContents();
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) contents.put(String.valueOf(i), array[i]);
        }
        result.put("contents", contents);

        return result;
    }

    public static CustomInventory deserialize(final Map<String, Object> serialized) {
        final CustomInventory result = new CustomInventory();

        result.setTitle((String) serialized.get("title"));

        @SuppressWarnings("unchecked")
        final Map<String, ItemStack> contents = (Map<String, ItemStack>) serialized.get("contents");
        for(final Map.Entry<String, ItemStack> entry : contents.entrySet()) {
            result.inventory.setItem(Integer.parseInt(entry.getKey()), entry.getValue());
        }

        return result;
    }

    // ---- adapter methods ----

    @Override
    public int getSize() {
        return this.inventory.getSize();
    }

    @Override
    public int getMaxStackSize() {
        return this.inventory.getMaxStackSize();
    }

    @Override
    public void setMaxStackSize(final int size) {
        this.inventory.setMaxStackSize(size);
    }

    @Override
    public String getName() {
        return this.inventory.getName();
    }

    @Override
    public ItemStack getItem(final int index) {
        return this.inventory.getItem(index);
    }

    @Override
    public void setItem(final int index, final ItemStack item) {
        this.inventory.setItem(index, item);
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(final ItemStack... items) throws IllegalArgumentException {
        return this.inventory.addItem(items);
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(final ItemStack... items) throws IllegalArgumentException {
        return this.inventory.removeItem(items);
    }

    @Override
    public ItemStack[] getContents() {
        return this.inventory.getContents();
    }

    @Override
    public void setContents(final ItemStack[] items) throws IllegalArgumentException {
        this.inventory.setContents(items);
    }

    @Override
    public boolean contains(final int materialId) {
        return this.inventory.contains(materialId);
    }

    @Override
    public boolean contains(final Material material) throws IllegalArgumentException {
        return this.inventory.contains(material);
    }

    @Override
    public boolean contains(final ItemStack item) {
        return this.inventory.contains(item);
    }

    @Override
    public boolean contains(final int materialId, final int amount) {
        return this.inventory.contains(materialId, amount);
    }

    @Override
    public boolean contains(final Material material, final int amount) throws IllegalArgumentException {
        return this.inventory.contains(material, amount);
    }

    @Override
    public boolean contains(final ItemStack item, final int amount) {
        return this.inventory.contains(item, amount);
    }

    @Override
    public boolean containsAtLeast(final ItemStack item, final int amount) {
        return this.inventory.containsAtLeast(item, amount);
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(final int materialId) {
        return this.inventory.all(materialId);
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(final Material material) throws IllegalArgumentException {
        return this.inventory.all(material);
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(final ItemStack item) {
        return this.inventory.all(item);
    }

    @Override
    public int first(final int materialId) {
        return this.inventory.first(materialId);
    }

    @Override
    public int first(final Material material) throws IllegalArgumentException {
        return this.inventory.first(material);
    }

    @Override
    public int first(final ItemStack item) {
        return this.inventory.first(item);
    }

    @Override
    public int firstEmpty() {
        return this.inventory.firstEmpty();
    }

    @Override
    public void remove(final int materialId) {
        this.inventory.remove(materialId);
    }

    @Override
    public void remove(final Material material) throws IllegalArgumentException {
        this.inventory.remove(material);
    }

    @Override
    public void remove(final ItemStack item) {
        this.inventory.remove(item);
    }

    @Override
    public void clear(final int index) {
        this.inventory.clear(index);
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.inventory.getViewers();
    }

    @Override
    public String getTitle() {
        return this.inventory.getTitle();
    }

    @Override
    public InventoryType getType() {
        return this.inventory.getType();
    }

    @Override
    public InventoryHolder getHolder() {
        return this.inventory.getHolder();
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        return this.inventory.iterator();
    }

    @Override
    public ListIterator<ItemStack> iterator(final int index) {
        return this.inventory.iterator(index);
    }

}