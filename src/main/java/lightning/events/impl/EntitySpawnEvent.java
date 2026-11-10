package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.Entity;

public class EntitySpawnEvent extends Event {
    private final Entity entity;

    @Generated
    public EntitySpawnEvent(Entity arg0) {
        this.entity = arg0;
    }

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

}
