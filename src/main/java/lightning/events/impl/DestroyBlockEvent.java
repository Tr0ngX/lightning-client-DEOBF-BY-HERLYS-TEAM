package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.core.BlockPos;

public class DestroyBlockEvent extends Event {
    private final BlockPos position;

    @Generated
    public DestroyBlockEvent(BlockPos arg0) {
        this.position = arg0;
    }

    @Generated
    public BlockPos getPosition() {
        return this.position;
    }

}
