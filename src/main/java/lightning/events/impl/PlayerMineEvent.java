package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.core.BlockPos;

public class PlayerMineEvent extends Event {
    private final int actorID;
    private final BlockPos position;

    @Generated
    public int getActorID() {
        return this.actorID;
    }

    @Generated
    public BlockPos getPosition() {
        return this.position;
    }

    @Generated
    public PlayerMineEvent(int arg0, BlockPos arg1) {
        this.actorID = arg0;
        this.position = arg1;
    }

}
