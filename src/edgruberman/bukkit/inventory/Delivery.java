package edgruberman.bukkit.inventory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

/** a player account with transaction and balance tracking */
@SerializableAs("Delivery")
public final class Delivery implements ConfigurationSerializable {

    private final String player;
    private final Pallet balance;

    public Delivery(final String player) {
        this(player, new Pallet());
        this.relabel();
    }

    private Delivery(final String player, final Pallet balance) {
        this.player = player;
        this.balance = balance;
    }

    public String getPlayer() {
        return this.player;
    }

    public Pallet getBalance() {
        return this.balance;
    }

    public void relabel() {
        this.balance.label("box-delivery", this.player);
    }

    public Collection<ItemStack> modifyBalance(final Collection<ItemStack> items) {
        final int before = this.balance.getBoxes().size();
        final Collection<ItemStack> failures = this.balance.modify(items);
        if (this.balance.getBoxes().size() != before) this.relabel();
        return failures;
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("player", this.player);
        result.put("balance", this.balance);
        return result;
    }

    public static Delivery deserialize(final Map<String, Object> serialized) {
        // TODO move check for updated case information for player name to somewhere more appropriate (whenever open pallet?) relabel is necessary to
        final String player = Bukkit.getOfflinePlayer((String) serialized.get("player")).getName();
        final Pallet balance = (Pallet) serialized.get("balance");
        return new Delivery(player, balance);
    }

}
