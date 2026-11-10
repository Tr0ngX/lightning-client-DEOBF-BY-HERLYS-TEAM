package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

public class RemoveFireworkEvent extends Event {
    private final FireworkRocketEntity entity;

    @Generated
    public FireworkRocketEntity getEntity() {
        return this.entity;
    }

    @Generated
    public RemoveFireworkEvent(FireworkRocketEntity arg0) {
        this.entity = arg0;
    }

}
