package edgruberman.bukkit.inventory.craftbukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public abstract class CraftBukkit {

    public static CraftBukkit create() throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Class<?> provider = Class.forName(CraftBukkit.class.getPackage().getName() + "." + CraftBukkit.class.getSimpleName() + "_" + CraftBukkit.version());
        return (CraftBukkit) provider.getConstructor().newInstance();
    }

    private static String version() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        if (version.equals("craftbukkit")) version = "pre";
        return version;
    }



    /** change inventory title (text that appears at top of gui window) */
    public abstract void setTitle(Inventory inventory, String title);



    protected static void set(final Object object, final String fieldName, final Object value) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        final Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(object, value);
     }

}
