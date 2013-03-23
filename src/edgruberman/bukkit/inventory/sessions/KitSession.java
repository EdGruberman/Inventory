package edgruberman.bukkit.inventory.sessions;

import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Kit;
import edgruberman.bukkit.inventory.repositories.KitRepository;

/** automatic addition of boxes when full */
public class KitSession extends Session {

    private final KitRepository repository;
    private final Kit kit;

    public KitSession(final Player customer, final KitRepository repository, final Kit kit) {
        super(customer, kit.getContents());
        this.repository = repository;
        this.kit = kit;
    }

    @Override
    public Kit getKey() {
        return this.kit;
    }

    @Override
    protected void onExpand() {
        this.pallet.label("box-kit", this.kit.getName());
    }

    @Override
    protected void onEnd() {
        final int viewers = this.pallet.viewers().size();
        if (viewers == 1 && this.pallet.isEmpty()) {
            this.repository.delete(this.kit);
            return;
        }

        if (viewers == 1 && this.pallet.trim()) this.pallet.label("box-kit", this.kit.getName());
        this.repository.save(this.kit);
    }

}
