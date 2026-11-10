package lightning.utils.mixins;

import com.mojang.blaze3d.vertex.VertexConsumer;

public enum NoopVertexConsumer implements VertexConsumer {
    INSTANCE;


    private NoopVertexConsumer() {
    }

    public VertexConsumer addVertex(float arg0, float arg1, float arg2) {
        return this;
    }

    public VertexConsumer setColor(int arg0, int arg1, int arg2, int arg3) {
        return this;
    }

    public VertexConsumer setColor(int arg0) {
        return this;
    }

    public VertexConsumer setLineWidth(float arg0) {
        return this;
    }

    public VertexConsumer setUv(float arg0, float arg1) {
        return this;
    }

    public VertexConsumer setUv1(int arg0, int arg1) {
        return this;
    }

    public VertexConsumer setUv2(int arg0, int arg1) {
        return this;
    }

    public VertexConsumer setNormal(float arg0, float arg1, float arg2) {
        return this;
    }

}
