package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.utils.IMinecraft;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class ParticleManager implements IMinecraft {
    private static final int MAX_PARTICLES = 600;
    private static final double CULL_DISTANCE_SQ = 4096.0;
    private static final int CIRCLE_SEGMENTS = 16;
    private static final float[] CIRCLE_X;
    private static final float[] CIRCLE_Y;
    private static final RenderType NO_DEPTH_PARTICLES;
    private final List particles;
    private static final RandomSource RANDOM;
    private final Quaternionf rotYaw;
    private final Quaternionf rotPitch;

    public ParticleManager() {
        this.particles = new CopyOnWriteArrayList();
        this.rotYaw = new Quaternionf();
        this.rotPitch = new Quaternionf();
    }

    public void addBouncy(Vec3 arg0, Vec3 arg1, double arg2, int arg3, int arg4, boolean arg5) {
        if (this.particles.size() >= 600) return;
        this.particles.add(new ParticleManager.BouncyParticle(arg0, arg1, arg2, arg3, arg4, arg5));
    }

    public void addFriction(Vec3 arg0, Vec3 arg1, double arg2, double arg3, int arg4, int arg5, boolean arg6) {
        if (this.particles.size() >= 600) return;
        this.particles.add(new ParticleManager.FrictionParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6));
    }

    public void tick() {
        this.particles.removeIf(ParticleManager.Particle::tick);
    }

    public void render(PoseStack arg0, float arg1) {
        if ((this.particles.isEmpty()) || (mc.gameRenderer == null)) {
            return;
        }
        Object var3 = mc.gameRenderer.mainCamera();
        if (var3 == null) return;
        Object var4 = var3.position();
        long var5 = System.currentTimeMillis();
        int var7 = 0;
        Object var8 = this.particles.iterator();
        while (var8.hasNext()) {
            Object var9 = ((ParticleManager.Particle) var8.next());
            if (!(var9.canRender(var4, arg1, var5))) {
                break label_3;
            }
            var7 = (var7 + var9.getCircleCount());
        }
        if (!var7) return;
        this.rotYaw.identity().rotationY(((float) Math.toRadians(((double) -(var3.yRot())))));
        this.rotPitch.identity().rotationX(((float) Math.toRadians(((double) var3.xRot()))));
        var8 = ((var7 * 16) * 3);
        var9 = ByteBufferBuilder.exactlySized((var8 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
        try {
            Object var10 = new BufferBuilder(var9, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            Object var11 = this.particles.iterator();
            while (var11.hasNext()) {
                Object var12 = ((ParticleManager.Particle) var11.next());
                if (!(var12.canRender(var4, arg1, var5))) {
                    break label_8;
                }
                var12.render(arg0, var10, var4, arg1, var5, this.rotYaw, this.rotPitch);
            }
            var11 = var10.build();
            Renderer3D.draw(NO_DEPTH_PARTICLES, var11);
            if (var9 == null) break label_17;
            var9.close();
        } catch (Throwable ex12) {
            var10 = ex12;
            if (var9 == null) break label_16;
            try {
                var9.close();
            } catch (Throwable ex15) {
                var11 = ex15;
                var10.addSuppressed(var11);
            }
            label_16:
            throw var10;
        }
        label_17:
    }

    public void clear() {
        this.particles.clear();
    }

    public boolean isEmpty() {
        return this.particles.isEmpty();
    }

    public int size() {
        return this.particles.size();
    }

    private static int alphaMulti(int arg0, float arg1) {
        int var2 = ((arg0 >> 24) & 255);
        int var3 = ((int) (((float) var2) * arg1));
        return (((var3 & 255) << 24) | (arg0 & 16777215));
    }

    private static void renderCircle(Matrix4f arg0, BufferBuilder arg1, float arg2, float arg3, int arg4) {
        int var5 = 0;
        while (var5 < 16) {
            float var6 = (CIRCLE_X[var5] * arg2);
            float var7 = (CIRCLE_Y[var5] * arg2);
            float var8 = (CIRCLE_X[(var5 + 1)] * arg2);
            float var9 = (CIRCLE_Y[(var5 + 1)] * arg2);
            arg1.addVertex(arg0, 0.0f, 0.0f, arg3).setColor(arg4);
            arg1.addVertex(arg0, var6, var7, arg3).setColor(arg4);
            arg1.addVertex(arg0, var8, var9, arg3).setColor(arg4);
            var5++;
        }
    }

    private static AABB expandTowards(AABB arg0, double arg1, double arg2, double arg3) {
        return new AABB((arg0.minX + (arg1 < 0.0 ? arg1 : 0.0)), (arg0.minY + (arg2 < 0.0 ? arg2 : 0.0)), (arg0.minZ + (arg3 < 0.0 ? arg3 : 0.0)), (arg0.maxX + (arg1 > 0.0 ? arg1 : 0.0)), (arg0.maxY + (arg2 > 0.0 ? arg2 : 0.0)), (arg0.maxZ + (arg3 > 0.0 ? arg3 : 0.0)));
    }

    private static float calcAlpha(double arg0) {
        if (arg0 < 0.1) return ((float) (arg0 * 10.0));
        if (arg0 > 0.5) return ((float) (1.0 - ((arg0 - 0.5) * 2.0)));
        return 1.0f;
    }

    public static Vec3 randomMotion(double arg0) {
        double var2 = ((RANDOM.nextDouble() * 2.0) * 3.141592653589793);
        double var4 = ((RANDOM.nextDouble() * 2.0) * 3.141592653589793);
        double var6 = Math.abs(Math.cos(var4));
        return new Vec3(((arg0 * Math.cos(var2)) * var6), (arg0 * -(Math.sin(var4))), ((arg0 * Math.sin(var2)) * var6));
    }

    static {
        CIRCLE_X = new float[17];
        CIRCLE_Y = new float[17];
        int var0 = 0;
        while (var0 <= 16) {
            double var1 = ((6.283185307179586 * ((double) var0)) / 16.0);
            CIRCLE_X[var0] = ((float) Math.cos(var1));
            CIRCLE_Y[var0] = ((float) Math.sin(var1));
            var0++;
        }
        var tmp0 = new RenderPipeline.Snippet[1];
        tmp0[0] = RenderPipelinesAccessor.getDebugFilledSnippet();
        NO_DEPTH_PARTICLES = RenderType.create("night_no_depth_particles", RenderSetup.builder(RenderPipeline.builder(tmp0).withLocation("lightning/no_depth_particles").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).createRenderSetup());
        RANDOM = RandomSource.create();
    }


    class BouncyParticle extends ParticleManager.BaseParticle {
        private double motionX;
        private double motionY;
        private double motionZ;

        private BouncyParticle(Vec3 arg0, Vec3 arg1, double arg2, int arg3, int arg4, boolean arg5) {
            super(arg0, arg2, arg3, arg4, arg5);
            this.motionX = arg1.x;
            this.motionY = arg1.y;
            this.motionZ = arg1.z;
            tick();
        }

        public boolean tick() {
            if ((System.currentTimeMillis() - this.startTime) > this.lifetimeMillis) return true;
            this.prevPos = this.pos;
            if ((IMinecraft.mc.level == null) || (IMinecraft.mc.player == null)) break label_3;
            var tmp0 = new AABB((this.pos.x - 0.025), (this.pos.y - 0.025), (this.pos.z - 0.025), (this.pos.x + 0.025), (this.pos.y + 0.025), (this.pos.z + 0.025));
            Object var1 = tmp0;
            if (!(IMinecraft.mc.level.getBlockCollisions(IMinecraft.mc.player, ParticleManager.expandTowards(tmp0, this.motionX, this.motionY, this.motionZ)).iterator().hasNext())) break label_3;
            if (this.motionX != 0.0 && IMinecraft.mc.level.getBlockCollisions(IMinecraft.mc.player, ParticleManager.expandTowards(var1, this.motionX, 0.0, 0.0)).iterator().hasNext()) {
                this.motionX = (this.motionX * -0.7);
            }
            if (this.motionY != 0.0 && IMinecraft.mc.level.getBlockCollisions(IMinecraft.mc.player, ParticleManager.expandTowards(var1, 0.0, this.motionY, 0.0)).iterator().hasNext()) {
                this.motionY = (this.motionY * -0.7);
            }
            if ((this.motionZ == 0.0) || (!(IMinecraft.mc.level.getBlockCollisions(IMinecraft.mc.player, ParticleManager.expandTowards(var1, 0.0, 0.0, this.motionZ)).iterator().hasNext()))) break label_3;
            this.motionZ = (this.motionZ * -0.7);
            label_3:
            this.pos = this.pos.add(this.motionX, this.motionY, this.motionZ);
            this.motionX = (this.motionX * 0.98);
            this.motionZ = (this.motionZ * 0.98);
            this.motionY = ((this.motionY - 0.08) * 0.98);
            return false;
        }

    }

    class FrictionParticle extends ParticleManager.BaseParticle {
        private final double friction;
        private Vec3 motion;

        private FrictionParticle(Vec3 arg0, Vec3 arg1, double arg2, double arg3, int arg4, int arg5, boolean arg6) {
            super(arg0, arg3, arg4, arg5, arg6);
            this.motion = arg1;
            this.friction = arg2;
            tick();
        }

        public boolean tick() {
            if ((System.currentTimeMillis() - this.startTime) > this.lifetimeMillis) return true;
            this.prevPos = this.pos;
            this.motion = this.motion.scale(this.friction);
            this.pos = this.pos.add(this.motion);
            return false;
        }

    }

    interface Particle {
        boolean tick();

        boolean canRender(Vec3 arg0, float arg1, long arg2);

        int getCircleCount();

        void render(PoseStack arg0, BufferBuilder arg1, Vec3 arg2, float arg3, long arg4, Quaternionf arg5, Quaternionf arg6);

    }

    abstract class BaseParticle implements ParticleManager.Particle {
        protected final int color;
        protected final int shadowColor;
        protected final boolean glow;
        protected final long startTime;
        protected final double lifetime;
        protected final long lifetimeMillis;
        protected Vec3 pos;
        protected Vec3 prevPos;

        protected BaseParticle(Vec3 arg0, double arg1, int arg2, int arg3, boolean arg4) {
            this.pos = arg0;
            this.prevPos = arg0;
            this.color = arg2;
            this.shadowColor = arg3;
            this.glow = arg4;
            this.startTime = System.currentTimeMillis();
            this.lifetime = arg1;
            this.lifetimeMillis = ((long) (arg1 * 1000.0));
        }

        public int getCircleCount() {
            return (!(!this.glow) ? 4 : 2);
        }

        public boolean canRender(Vec3 arg0, float arg1, long arg2) {
            long var9 = (arg2 - this.startTime);
            if (var9 > this.lifetimeMillis) return false;
            double var11 = ((((double) var9) / 1000.0) / this.lifetime);
            float var13 = ParticleManager.calcAlpha(Mth.clamp(var11, 0.0, 1.0));
            if (var13 <= 0.0f) return false;
            double var14 = (Mth.lerp(((double) arg1), this.prevPos.x, this.pos.x) - arg0.x);
            double var7 = (Mth.lerp(((double) arg1), this.prevPos.y, this.pos.y) - arg0.y);
            double var5 = (Mth.lerp(((double) arg1), this.prevPos.z, this.pos.z) - arg0.z);
            return (((var14 * var14) + ((Mth.lerp(((double) arg1), this.prevPos.y, this.pos.y) - arg0.y) * var7)) + ((Mth.lerp(((double) arg1), this.prevPos.z, this.pos.z) - arg0.z) * var5)) <= 4096.0;
        }

        public void render(PoseStack arg0, BufferBuilder arg1, Vec3 arg2, float arg3, long arg4, Quaternionf arg5, Quaternionf arg6) {
            long var9 = (arg4 - this.startTime);
            double var11 = ((((double) var9) / 1000.0) / this.lifetime);
            float var13 = ParticleManager.calcAlpha(Mth.clamp(var11, 0.0, 1.0));
            double var14 = (Mth.lerp(((double) arg3), this.prevPos.x, this.pos.x) - arg2.x);
            double var16 = (Mth.lerp(((double) arg3), this.prevPos.y, this.pos.y) - arg2.y);
            double var18 = (Mth.lerp(((double) arg3), this.prevPos.z, this.pos.z) - arg2.z);
            arg0.pushPose();
            arg0.translate(var14, var16, var18);
            arg0.scale(0.02f, 0.02f, 0.02f);
            arg0.mulPose(arg5);
            arg0.mulPose(arg6);
            Object var20 = arg0.last().pose();
            int var21 = ParticleManager.alphaMulti(this.shadowColor, var13);
            int var22 = ParticleManager.alphaMulti(this.color, var13);
            if (!(!this.glow)) {
                int var23 = ParticleManager.alphaMulti(this.color, (var13 * 0.4f));
                ParticleManager.renderCircle(var20, arg1, 4.0f, -0.02f, var23);
                ParticleManager.renderCircle(var20, arg1, 2.5f, -0.015f, var23);
            }
            ParticleManager.renderCircle(var20, arg1, 2.0f, -0.01f, var21);
            ParticleManager.renderCircle(var20, arg1, 1.0f, 0.0f, var22);
            arg0.popPose();
        }

    }
}
