package edgruberman.bukkit.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Delivery")
public final class Delivery extends NamedCustomInventoryList implements ConfigurationSerializable {
    private static final long serialVersionUID = 1L;

    public Delivery(final String player) {
        this(player, Collections.<CustomInventory>emptyList());
    }

    private Delivery(final String player, final Collection<CustomInventory> elements) {
        super(player.toLowerCase(), Main.courier.translate("title-delivery").get(0), elements);
    }

    @SuppressWarnings("unchecked")
    public static Delivery deserialize(final Map<String, Object> serialized) {
        // TODO move check for updated case information for player name to somewhere more appropriate (whenever open pallet?) relabel is necessary to
        final String key = Bukkit.getOfflinePlayer((String) serialized.get("key")).getName();
        final List<CustomInventory> elements = (ArrayList<CustomInventory>) serialized.get("elements");
        return new Delivery(key, elements);
    }

}
