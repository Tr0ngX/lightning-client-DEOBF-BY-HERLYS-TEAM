package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.player.Player;

public class PlayerPopEvent extends Event {
    private final Player player;
    private final int pops;

    @Generated
    public Player getPlayer() {
        return this.player;
    }

    @Generated
    public int getPops() {
        return this.pops;
    }

    @Generated
    public PlayerPopEvent(Player arg0, int arg1) {
        this.player = arg0;
        this.pops = arg1;
    }

}
