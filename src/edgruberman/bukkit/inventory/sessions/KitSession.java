package edgruberman.bukkit.inventory.sessions;

import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Kit;
import edgruberman.bukkit.inventory.repositories.KitRepository;

public class KitSession extends Session {

    private final KitRepository repository;
    private final Kit kit;

    public KitSession(final Player customer, final KitRepository repository, final Kit kit) {
        super(customer, kit.getList());
        this.repository = repository;
        this.kit = kit;
    }

    @Override
    protected void onEnd() {
        final int viewers = this.list.getViewers().size();
        if (viewers == 1 && this.list.isContentsEmpty()) {
            this.repository.remove(this.kit);
            return;
        }

        if ((viewers == 1) && (this.list.trim() > 0)) this.list.setTitles();
        this.repository.put(this.kit);
    }

}
