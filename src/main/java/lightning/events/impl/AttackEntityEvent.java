package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class AttackEntityEvent extends Event {
    private final Player player;
    private final Entity target;

    @Generated
    public Player getPlayer() {
        return this.player;
    }

    @Generated
    public Entity getTarget() {
        return this.target;
    }

    @Generated
    public AttackEntityEvent(Player arg0, Entity arg1) {
        this.player = arg0;
        this.target = arg1;
    }

}
