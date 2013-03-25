package edgruberman.bukkit.inventory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

public class KeyedInventoryList extends InventoryList {
    private static final long serialVersionUID = 1L;

    protected final String key;
    protected final String pattern;

    public KeyedInventoryList(final String key, final String pattern) {
        this(key, pattern, Collections.<CustomInventory>emptyList());
    }

    public KeyedInventoryList(final String key, final String pattern, final Collection<CustomInventory> elements) {
        super(elements);
        this.key = key;
        this.pattern = pattern;
        if (elements.isEmpty()) this.add(new CustomInventory());
        this.setTitles();
    }

    public String getKey() {
        return this.key;
    }

    public void setTitles() {
        this.formatTitles(this.pattern, this.key);
    }

    @Override
    public Collection<ItemStack> modify(final Collection<ItemStack> items) {
        final int before = this.size();
        final Collection<ItemStack> failures = super.modify(items);
        if (this.size() != before) {
            this.setTitles();
            // TODO refresh sessions
        }
        return failures;
    }

    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("key", this.key);
        result.put("elements", this.subList(0, this.size()));
        return result;
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
        final KeyedInventoryList other = (KeyedInventoryList) obj;
        if (!this.key.equals(other.key)) return false;
        return true;
    }

}
