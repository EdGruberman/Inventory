package edgruberman.bukkit.inventory.sessions;

import org.bukkit.entity.Player;

import edgruberman.bukkit.inventory.Clerk;
import edgruberman.bukkit.inventory.Kit;

public class KitSession extends Session {

    private final Clerk clerk;
    private final Kit kit;

    public KitSession(final Player customer, final Clerk clerk, final Kit kit) {
        super(customer, kit);
        this.clerk = clerk;
        this.kit = kit;
    }

    @Override
    protected void onEnd() {
        final int viewers = this.list.getViewers().size();
        if (viewers == 1 && this.list.isContentsEmpty()) {
            this.clerk.removeKit(this.kit);
            return;
        }

        if ((viewers == 1) && (this.list.trim() > 0)) this.list.formatTitles();
        this.clerk.putKit(this.kit);
    }

}
