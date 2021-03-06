package edgruberman.bukkit.inventory.craftbukkit;

import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftInventoryCustom;
import org.bukkit.inventory.Inventory;

public class CraftBukkit_v1_6_R3 extends CraftBukkit {

    @Override
    public void setTitle(final Inventory inventory, final String title) {
        if (!(inventory instanceof CraftInventoryCustom))
            throw new IllegalArgumentException("Inventory is not a " + CraftInventoryCustom.class.getName() + " instance: " + inventory.getClass().getName());

        final CraftInventoryCustom cic = (CraftInventoryCustom) inventory;
        try {
            CraftBukkit.set(cic.getInventory(), "title", title);
        } catch (final Exception e) {
            throw new IllegalStateException("unable to set Inventory title on " + inventory.getClass().getName() + "#name to " + title, e);
        }
    }

}
