package lightning.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.events.Event;
import lombok.Generated;

public class RenderWorldEvent extends Event {
    private final PoseStack matrices;
    private final float tickDelta;

    @Generated
    public PoseStack getMatrices() {
        return this.matrices;
    }

    @Generated
    public float getTickDelta() {
        return this.tickDelta;
    }

    @Generated
    public RenderWorldEvent(PoseStack arg0, float arg1) {
        this.matrices = arg0;
        this.tickDelta = arg1;
    }


    public class Post extends Event {
        private final PoseStack matrices;
        private final float tickDelta;

        @Generated
        public PoseStack getMatrices() {
            return this.matrices;
        }

        @Generated
        public float getTickDelta() {
            return this.tickDelta;
        }

        @Generated
        public Post(PoseStack arg0, float arg1) {
            this.matrices = arg0;
            this.tickDelta = arg1;
        }

    }
}
