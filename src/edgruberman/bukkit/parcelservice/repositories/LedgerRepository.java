package edgruberman.bukkit.parcelservice.repositories;

import edgruberman.bukkit.parcelservice.Ledger;

public class LedgerRepository extends CachedRepository<String, Ledger>{

    public LedgerRepository(final Repository<String, Ledger> source) {
        super(source);
    }

    public Ledger create(final String player) {
        final Ledger result = this.load(player);
        if (result != null) return result;

        return new Ledger(player);
    }

    @Override
    public Ledger load(final String player) {
        return super.load(player.toLowerCase());
    }

    public void save(final Ledger ledger) {
        this.save(ledger.getPlayer().toLowerCase(), ledger);
    }

    public void delete(final Ledger ledger) {
        this.delete(ledger.getPlayer().toLowerCase());
    }

}
