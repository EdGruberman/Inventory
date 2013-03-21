package edgruberman.bukkit.deliver.util;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtil {

    private static ConfigurationSection format;

    public static void setFormat(final ConfigurationSection format) {
        ItemStackUtil.format = format;
    }

    public static JoinList summarize(final Collection<ItemStack> stacks) {
        final JoinList joined = new JoinList(ItemStackUtil.format);
        for (final ItemStack stack : stacks) joined.add(ItemStackUtil.summarize(stack));
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
