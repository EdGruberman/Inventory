package edgruberman.bukkit.inventory.commands;

import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.inventory.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.inventory.commands.util.ExecutionRequest;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;

public final class Reload extends ConfigurationExecutor {

    private final Plugin plugin;

    public Reload(final ConfigurationCourier courier, final Plugin plugin) {
        super(courier);
        this.plugin = plugin;
    }

    @Override
    public boolean executeImplementation(final ExecutionRequest request) {
        this.plugin.onDisable();
        this.plugin.onEnable();
        this.courier.send(request.getSender(), "reload", this.plugin.getName());
        return true;
    }

}
