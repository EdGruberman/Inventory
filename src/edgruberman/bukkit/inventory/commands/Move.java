package edgruberman.bukkit.inventory.commands;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.inventory.Main;
import edgruberman.bukkit.inventory.commands.util.ArgumentContingency;
import edgruberman.bukkit.inventory.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.inventory.commands.util.ExecutionRequest;
import edgruberman.bukkit.inventory.commands.util.OnlinePlayerParameter;
import edgruberman.bukkit.inventory.messaging.Courier.ConfigurationCourier;

public final class Move extends ConfigurationExecutor {

    private final OnlinePlayerParameter player;

    public Move(final ConfigurationCourier courier, final Server server) {
        super(courier);

        this.requirePlayer();
        this.player = this.addRequired(OnlinePlayerParameter.Factory.create("player", server));
    }

    // usage: /<command> player
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws ArgumentContingency {
        final Player target = request.parse(this.player);

        final int slot = target.getInventory().firstEmpty();
        if (slot == -1) {
            this.courier.send(request.getSender(), "full", target.getName());
            return true;
        }

        final Player source = (Player) request.getSender();
        final ItemStack clone = source.getItemInHand().clone();
        source.setItemInHand(null);
        target.getInventory().setItem(slot, clone);
        this.courier.send(request.getSender(), "move", target.getName(), Main.summarize(clone));
        return true;
    }

}
