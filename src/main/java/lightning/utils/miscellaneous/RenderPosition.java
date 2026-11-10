package lightning.utils.miscellaneous;

import lightning.LightningClient;
import lightning.modules.impl.core.RendersModule;
import lightning.utils.animations.Easing;
import lombok.Generated;
import net.minecraft.core.BlockPos;

public class RenderPosition {
    private BlockPos pos;
    private long startTime;

    public RenderPosition(BlockPos arg0) {
        this.pos = arg0;
        this.startTime = System.currentTimeMillis();
    }

    public boolean equals(Object arg0) {
        if ((arg0 instanceof RenderPosition)) return ((RenderPosition) arg0).pos.equals(this.pos);
        return false;
    }

    public float get() {
        return Easing.ease((1.0f - Easing.toDelta(this.startTime, ((RendersModule) LightningClient.MODULE_MANAGER.getModule(RendersModule.class)).duration.getValue().intValue())), Easing.Method.EASE_IN_CUBIC);
    }

    @Generated
    public void setPos(BlockPos arg0) {
        this.pos = arg0;
    }

    @Generated
    public void setStartTime(long arg0) {
        this.startTime = arg0;
    }

    @Generated
    public BlockPos getPos() {
        return this.pos;
    }

    @Generated
    public long getStartTime() {
        return this.startTime;
    }

}
