package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class AttackBlockEvent extends Event {
    private final BlockPos position;
    private final Direction direction;

    @Generated
    public AttackBlockEvent(BlockPos arg0, Direction arg1) {
        this.position = arg0;
        this.direction = arg1;
    }

    @Generated
    public BlockPos getPosition() {
        return this.position;
    }

    @Generated
    public Direction getDirection() {
        return this.direction;
    }

}
