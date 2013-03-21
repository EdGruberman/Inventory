package edgruberman.bukkit.inventory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** record of item stack flow */
@SerializableAs("Transaction")
public class Transaction implements ConfigurationSerializable {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

    private final Date occurred;
    private final String source;
    private final String reason;
    private final List<ItemStack> changes;
    private final List<ItemStack> failures;

    public Transaction(final Date occurred, final CommandSender source, final String reason, final List<ItemStack> changes) {
        this(occurred, Transaction.formatSource(source), reason, changes, new ArrayList<ItemStack>());
    }

    private Transaction(final Date occurred, final String source, final String reason, final List<ItemStack> changes, final List<ItemStack> failures) {
        this.occurred = occurred;
        this.source = source;
        this.reason = reason;
        this.changes = changes;
        this.failures = failures;
    }

    public Date getOccurred() {
        return this.occurred;
    }

    public String getSource() {
        return this.source;
    }

    public String getReason() {
        return this.reason;
    }

    public List<ItemStack> getChanges() {
        return this.changes;
    }

    public List<ItemStack> getFailures() {
        return this.failures;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final Map.Entry<String, Object> entry : this.serialize().entrySet())
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        return sb.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("occurred", Transaction.DATE_FORMAT.format(this.occurred));
        result.put("source", this.source);
        result.put("reason", this.reason);
        result.put("changes", this.changes);
        if (!this.failures.isEmpty()) result.put("failures", this.failures);
        return result;
    }

    public static Transaction deserialize(final Map<String, Object> serialized) {
        Date occurred;
        try {
            occurred = Transaction.DATE_FORMAT.parse((String) serialized.get("occurred"));
        } catch (final ParseException e) {
            throw new IllegalArgumentException(e);
        }

        final String source = (String) serialized.get("source");

        final String reason = (String) serialized.get("reason");

        @SuppressWarnings("unchecked")
        final List<ItemStack> changes = (List<ItemStack>) serialized.get("changes");

        @SuppressWarnings("unchecked")
        final List<ItemStack> failures = ( serialized.containsKey("failures") ? (List<ItemStack>) serialized.get("failures") : new ArrayList<ItemStack>() );

        return new Transaction(occurred, source, reason, changes, failures);
    }

    private static String formatSource(final CommandSender source) {
        return ( source instanceof Player ? source.getName() : "{" + source.getName() + "}" );
    }

}
