package edgruberman.bukkit.inventory.util;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public abstract class ItemStackExecutor extends TokenizedExecutor {

    private final ConfigurationSection listFormat;

    protected ItemStackExecutor() {
        this(null);
    }

    protected ItemStackExecutor(final ConfigurationSection listFormat) {
        this.listFormat = listFormat;
    }

    public JoinList<StringBuilder> summarize(final Collection<ItemStack> stacks) {
        final JoinList<StringBuilder> joined = new JoinList<StringBuilder>(this.listFormat);
        for (final ItemStack stack : stacks) joined.add(ItemStackExecutor.summarize(stack));
        return joined;
    }



    public static StringBuilder summarize(final ItemStack stack) {
        final StringBuilder sb = new StringBuilder(stack.getType().name());
        if (stack.getDurability() != 0) sb.append('/').append(stack.getDurability());
        if (stack.hasItemMeta()) sb.append('*');
        if (stack.getAmount() != 0) sb.append('x').append(stack.getAmount());
        return sb;
    }

}
