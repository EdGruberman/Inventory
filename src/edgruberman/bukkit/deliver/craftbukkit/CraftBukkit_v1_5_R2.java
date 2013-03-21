package edgruberman.bukkit.deliver.craftbukkit;

import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftInventoryCustom;
import org.bukkit.inventory.Inventory;

public class CraftBukkit_v1_5_R2 extends CraftBukkit {

    @Override
    public void entitle(final Inventory inventory, final String title) {
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
