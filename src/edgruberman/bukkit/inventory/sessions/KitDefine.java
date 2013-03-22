package edgruberman.bukkit.inventory.sessions;

import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Kit;
import edgruberman.bukkit.inventory.Transaction;
import edgruberman.bukkit.inventory.repositories.KitRepository;

/** indirect interaction with kit contents that allows automatic expansion */
public class KitDefine extends Session {

    private final KitRepository kits;
    private final Kit active;

    public KitDefine(final Player customer, final KitRepository kits, final Kit active) {
        super(customer, active.getContents().clone(), null);
        this.kits = kits;
        this.active = active;
    }

    @Override
    public void next() {
        if (this.index == this.pallet.getBoxes().size() - 1
                && this.pallet.getBoxes().get(this.index).isFull()) {
            this.pallet.addBox();
            this.pallet.label("box-kit", this.active.getName());
        }

        super.next();
    }

    @Override
    protected void onEnd(final Transaction transaction) {
        if (this.pallet.isEmpty()) {
            this.kits.delete(this.active);
            return;
        }

        this.active.getContents().setBoxes(this.pallet.getBoxes());
        this.kits.save(this.active);
        this.active.setDefiner(null);
    }

}
