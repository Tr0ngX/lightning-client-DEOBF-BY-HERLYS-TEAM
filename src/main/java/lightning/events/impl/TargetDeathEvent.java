package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.player.Player;

public class TargetDeathEvent extends Event {
    private final Player player;

    @Generated
    public TargetDeathEvent(Player arg0) {
        this.player = arg0;
    }

    @Generated
    public Player getPlayer() {
        return this.player;
    }

}
