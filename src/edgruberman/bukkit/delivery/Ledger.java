package edgruberman.bukkit.delivery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

/** a player account with transaction and balance tracking */
@SerializableAs("Ledger")
public final class Ledger implements ConfigurationSerializable {

    private final String player;
    private final Pallet balance;
    private final List<Transaction> log;

    public Ledger(final String player) {
        this(player, new Pallet(), new ArrayList<Transaction>());
        this.relabel();
    }

    private Ledger(final String player, final Pallet balance, final List<Transaction> log) {
        this.player = player;
        this.balance = balance;
        this.log = log;
    }

    public String getPlayer() {
        return this.player;
    }

    public Pallet getBalance() {
        return this.balance;
    }

    public List<Transaction> getLog() {
        return Collections.unmodifiableList(this.log);
    }

    public void relabel() {
        this.balance.label(Main.courier.format("box-balance", "{0}", "{1}", this.player));
    }

    public Collection<ItemStack> modifyBalance(final Collection<ItemStack> items) {
        final int before = this.balance.getBoxes().size();
        final Collection<ItemStack> failures = this.balance.modify(items);
        if (this.balance.getBoxes().size() != before) this.relabel();
        return failures;
    }

    public void record(final Transaction transaction) {
        this.log.add(transaction);
    }

    public boolean empty() {
        return (this.log.size() == 0) && this.balance.empty();
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("player", this.player);
        result.put("balance", this.balance);
        result.put("log", this.log);
        return result;
    }

    public static Ledger deserialize(final Map<String, Object> serialized) {
        // TODO move check for updated case information for player name to somewhere more appropriate (whenever open pallet?) relabel is necessary to
        final String player = Bukkit.getOfflinePlayer((String) serialized.get("player")).getName();

        final Pallet balance = (Pallet) serialized.get("balance");

        @SuppressWarnings("unchecked")
        final List<Transaction> log = (List<Transaction>) serialized.get("log");

        return new Ledger(player, balance, log);
    }

}
