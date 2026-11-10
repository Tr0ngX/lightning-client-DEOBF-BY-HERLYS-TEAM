package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.player.Player;

public class PlayerDeathEvent extends Event {
    private final Player player;

    @Generated
    public Player getPlayer() {
        return this.player;
    }

    @Generated
    public PlayerDeathEvent(Player arg0) {
        this.player = arg0;
    }

}
