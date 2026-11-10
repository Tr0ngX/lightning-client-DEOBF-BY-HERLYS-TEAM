package lightning.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@RegisterModule(name = "CrystalSplash", description = "Water ripple effect on the ground when an end crystal explodes.", category = Module.Category.VISUALS)
public class CrystalSplashModule extends Module {
    private static final double RING_LIFE = 1.6;
    private static final double RING_GAP = 0.22;
    private static final int MAX_SPLASHES = 24;
    public CategorySetting splashCategory;
    public NumberSetting rings;
    public NumberSetting size;
    public NumberSetting speed;
    public NumberSetting range;
    public ColorSetting colorA;
    public ColorSetting colorB;
    public BooleanSetting droplets;
    public BooleanSetting crystalOnly;
    private final Queue pending;
    private final List splashes;
    private final List knownNow;
    private final List knownPrev;

    public CrystalSplashModule() {
        this.splashCategory = new CategorySetting("CrystalSplash", "Settings for the crystal explosion water effect.");
        this.rings = new NumberSetting("Rings", "Rings", "Number of ripple rings.", new CategorySetting.Visibility(this.splashCategory), 3, 1, 5);
        this.size = new NumberSetting("Size", "Size", "Maximum ripple radius in blocks.", new CategorySetting.Visibility(this.splashCategory), 3.0, 1.0, 6.0);
        this.speed = new NumberSetting("Speed", "Speed", "Animation speed multiplier.", new CategorySetting.Visibility(this.splashCategory), 1.0, 0.5, 2.0);
        this.range = new NumberSetting("Range", "Range", "Maximum distance at which the effect is shown.", new CategorySetting.Visibility(this.splashCategory), 64.0, 8.0, 128.0);
        this.colorA = new ColorSetting("ColorA", "Water color", "Main color of the water ripples.", new CategorySetting.Visibility(this.splashCategory), new ColorSetting.Color(new Color(55, 138, 221, 255), false, false));
        this.colorB = new ColorSetting("ColorB", "Highlight color", "Color of the ripple crest and sparkles.", new CategorySetting.Visibility(this.splashCategory), new ColorSetting.Color(new Color(230, 241, 251, 255), false, false));
        this.droplets = new BooleanSetting("Droplets", "Droplets", "Small low water droplets in the middle.", new CategorySetting.Visibility(this.splashCategory), true);
        this.crystalOnly = new BooleanSetting("CrystalOnly", "Crystal only", "Only show the effect for end crystal explosions.", new CategorySetting.Visibility(this.splashCategory), true);
        this.pending = new ConcurrentLinkedQueue();
        this.splashes = new ArrayList();
        this.knownNow = new ArrayList();
        this.knownPrev = new ArrayList();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (!((arg0.getPacket() instanceof ClientboundExplodePacket))) break label_0;
        Object var2 = ((ClientboundExplodePacket) arg0.getPacket());
        Object var3 = var2.center();
        if (var3 == null) break label_0;
        this.pending.add(var3);
        label_0:
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.level != null) {
            if (mc.player == null) {
            }
            this.pending.clear();
            this.splashes.clear();
            this.knownNow.clear();
            this.knownPrev.clear();
            return;
        }
        long var2 = System.currentTimeMillis();
        label_2:
        var tmp0 = ((Vec3) this.pending.poll());
        Object var4 = tmp0;
        if (tmp0 == null) break label_6;
        if (this.splashes.size() >= 24) {
            break label_6;
        }
        if (isDuplicate(var4, var2)) {
            break label_2;
        }
        if (this.crystalOnly.getValue() && !(isCrystalAt(var4))) {
            break label_2;
        }
        this.splashes.add(new CrystalSplashModule.Splash(var4, var2));
        break label_2;
        label_6:
        this.knownPrev.clear();
        this.knownPrev.addAll(this.knownNow);
        this.knownNow.clear();
        Object var5 = mc.player.getBoundingBox().inflate(128.0);
        Object var6 = mc.level.getEntitiesOfClass(EndCrystal.class, var5).iterator();
        while (var6.hasNext()) {
            Object var7 = ((EndCrystal) var6.next());
            this.knownNow.add(var7.position());
        }
        var6 = lifeEnd(this.rings.getValue().intValue());
        double var8 = this.speed.getValue().doubleValue();
        this.splashes.removeIf((arg3) -> ((((double) (arg0 - arg3.start)) / 1000.0) * arg1) > arg2);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((this.splashes.isEmpty()) || (!Renderer3D.RENDERING)) {
            return;
        }
        if ((mc.gameRenderer == null) || (mc.gameRenderer.mainCamera() == null)) {
            return;
        }
        Object var2 = arg0.getMatrices();
        Object var3 = var2.last().pose();
        Object var4 = mc.gameRenderer.mainCamera().position();
        long var5 = System.currentTimeMillis();
        double var7 = this.range.getValue().doubleValue();
        Object var9 = this.splashes.iterator();
        while (var9.hasNext()) {
            Object var10 = ((CrystalSplashModule.Splash) var9.next());
            if (var10.pos.distanceToSqr(var4) > (var7 * var7)) {
                break label_4;
            }
            drawSplash(var2, var3, var4, var10, var5);
        }
    }

    private double lifeEnd(int arg0) {
        return ((1.6 + (0.22 * ((double) (arg0 - 1)))) + 1.0);
    }

    private boolean isDuplicate(Vec3 arg0, long arg1) {
        Object var4 = this.splashes.iterator();
        while (var4.hasNext()) {
            Object var5 = ((CrystalSplashModule.Splash) var4.next());
            if ((arg1 - var5.start) < 300L && var5.pos.distanceToSqr(arg0) < 0.25) return true;
        }
        return false;
    }

    private boolean isCrystalAt(Vec3 arg0) {
        Object var2 = new AABB((arg0.x - 1.5), (arg0.y - 1.5), (arg0.z - 1.5), (arg0.x + 1.5), (arg0.y + 1.5), (arg0.z + 1.5));
        if (!(mc.level.getEntitiesOfClass(EndCrystal.class, var2).isEmpty())) return true;
        Object var3 = this.knownNow.iterator();
        while (var3.hasNext()) {
            Object var4 = ((Vec3) var3.next());
            if (var4.distanceToSqr(arg0) < 2.25) return true;
        }
        var3 = this.knownPrev.iterator();
        while (var3.hasNext()) {
            var4 = ((Vec3) var3.next());
            if (var4.distanceToSqr(arg0) < 2.25) return true;
        }
        return false;
    }

    private static double hash(int arg0, int arg1) {
        double var2 = (Math.sin(((((double) arg0) * 127.1) + (((double) arg1) * 311.7))) * 43758.5453);
        return (var2 - Math.floor(var2));
    }

    private static int argb(int arg0, double arg1) {
        int var3 = ((int) (Math.max(0.0, Math.min(1.0, arg1)) * 255.0));
        return ((var3 << 24) | (arg0 & 16777215));
    }

    private static Color color(int arg0, double arg1) {
        return new Color(((arg0 >> 16) & 255), ((arg0 >> 8) & 255), (arg0 & 255), ((int) (Math.max(0.0, Math.min(1.0, arg1)) * 255.0)));
    }

    private void drawSplash(PoseStack arg0, Matrix4f arg1, Vec3 arg2, CrystalSplashModule.Splash arg3, long arg4) {
        int var7 = this.rings.getValue().intValue();
        double var8 = this.size.getValue().doubleValue();
        double var10 = ((((double) (arg4 - arg3.start)) / 1000.0) * this.speed.getValue().doubleValue());
        int var12 = (this.colorA.getColor().getRGB() & 16777215);
        int var13 = (this.colorB.getColor().getRGB() & 16777215);
        int var14 = 533594;
        double var15 = arg3.pos.x;
        double var17 = (arg3.pos.y + 0.02);
        double var19 = arg3.pos.z;
        double var21 = var8;
        double var23 = Math.min(1.0, (var10 / 0.9));
        if (var10 < 0.4) {
        } else {
        }
        double var25 = Math.max(0.0, (1.0 - ((var10 - 0.4) / 2.2)));
        if (var25 > 0.0) {
            double var27 = (var21 * (0.25 + (0.7 * var23)));
            var tmp0 = new double[3];
            tmp0[0] = 0.0;
            tmp0[1] = (var27 * 0.7);
            tmp0[2] = var27;
            Object var29 = tmp0;
            var tmp1 = new int[3];
            tmp1[0] = argb(var12, (0.34 * var25));
            tmp1[1] = argb(var12, (0.2 * var25));
            tmp1[2] = argb(var12, 0.0);
            Object var30 = tmp1;
            bands(arg1, arg2, var15, var17, var19, var29, var30, 0.0, 0.0);
        }
        if (var10 < 0.22) {
            var27 = (var10 / 0.22);
            var29 = (0.8 + (2.2 * var27));
            var tmp2 = new double[3];
            tmp2[0] = 0.0;
            tmp2[1] = (var29 * 0.5);
            tmp2[2] = var29;
            Object var31 = tmp2;
            var tmp3 = new int[3];
            tmp3[0] = argb(16777215, (0.7 * (1.0 - var27)));
            tmp3[1] = argb(var13, (0.35 * (1.0 - var27)));
            tmp3[2] = argb(var13, 0.0);
            Object var32 = tmp3;
            bands(arg1, arg2, var15, var17, var19, var31, var32, 0.0, 0.0);
        }
        if (var10 < 0.28) {
            var27 = (var10 / 0.28);
            var29 = ((var21 * 0.55) * var27);
            var31 = ((0.12 * (1.0 - var27)) + 0.03);
            var tmp4 = new double[3];
            tmp4[0] = Math.max(0.02, (var29 - var31));
            tmp4[1] = var29;
            tmp4[2] = (var29 + (var31 * 0.4));
            Object var33 = tmp4;
            var tmp5 = new int[3];
            tmp5[0] = argb(16777215, 0.0);
            tmp5[1] = argb(16777215, (0.7 * (1.0 - var27)));
            tmp5[2] = argb(16777215, 0.0);
            Object var34 = tmp5;
            bands(arg1, arg2, var15, var17, var19, var33, var34, 0.0, 0.0);
        }
        var27 = 0;
        while (var27 < var7) {
            double var28 = (var10 - (((double) var27) * 0.22));
            if (var28 < 0.0) break label_7;
            if (var28 > 1.6) {
                break label_7;
            }
            var30 = (var28 / 1.6);
            var32 = (1.0 - Math.pow((1.0 - var30), 2.2));
            var34 = (var21 * (0.08 + (0.92 * var32)));
            double var36 = (((9.0 * (1.0 - var30)) + 3.5) / 36.0);
            double var38 = (Math.min(1.0, (var28 / 0.06)) * Math.pow((1.0 - var30), 1.15));
            double var40 = ((var10 * 6.0) + (((double) var27) * 1.7));
            var tmp6 = new double[5];
            tmp6[0] = Math.max(0.02, (var34 - var36));
            tmp6[1] = (var34 - (var36 * 0.4));
            tmp6[2] = (var34 + (var36 * 0.1));
            tmp6[3] = (var34 + (var36 * 0.56));
            tmp6[4] = (var34 + var36);
            Object var42 = tmp6;
            var tmp7 = new int[5];
            tmp7[0] = argb(var12, 0.0);
            tmp7[1] = argb(var14, (0.32 * var38));
            tmp7[2] = argb(var13, (0.9 * var38));
            tmp7[3] = argb(var12, (0.42 * var38));
            tmp7[4] = argb(var12, 0.0);
            Object var43 = tmp7;
            bands(arg1, arg2, var15, var17, var19, var42, var43, var40, 0.025);
            crestLine(arg0, var15, var17, var19, var34, var38, var40, var13);
            if (var27) break label_7;
            sparkles(arg0, var15, var17, var19, var34, var38, var10, var13);
            label_7:
            var27++;
        }
        if (this.droplets.getValue()) {
            drawDroplets(arg0, var15, var17, var19, var8, var10, var12, var13);
        }
    }

    private void bands(Matrix4f arg0, Vec3 arg1, double arg2, double arg3, double arg4, double[] arg5, int[] arg6, double arg7, double arg8) {
        int var15 = 48;
        int var16 = arg5.length;
        Object var17 = new float[var16][49];
        Object var18 = new float[var16][49];
        float var19 = ((float) (arg3 - arg1.y));
        int var20 = 0;
        while (var20 <= 48) {
            double var21 = (((((double) var20) * 3.141592653589793) * 2.0) / 48.0);
            double var23 = (1.0 + (arg8 * (Math.sin(((var21 * 5.0) + arg7)) + (0.6 * Math.sin(((var21 * 9.0) - (arg7 * 1.3)))))));
            double var25 = Math.cos(var21);
            double var27 = Math.sin(var21);
            int var29 = 0;
            while (var29 < var16) {
                double var30 = (arg5[var29] * var23);
                var17[var29][var20] = ((float) ((arg2 + (var25 * var30)) - arg1.x));
                var18[var29][var20] = ((float) ((arg4 + (var27 * var30)) - arg1.z));
                var29++;
            }
            var20++;
        }
        var20 = 0;
        while (var20 < (var16 - 1)) {
            var21 = 0;
            while (var21 < 48) {
                var tmp0 = new Renderer3D.Vertex[4];
                tmp0[0] = new Renderer3D.Vertex(arg0, var17[var20][var21], var19, var18[var20][var21], arg6[var20]);
                tmp0[1] = new Renderer3D.Vertex(arg0, var17[(var20 + 1)][var21], var19, var18[(var20 + 1)][var21], arg6[(var20 + 1)]);
                tmp0[2] = new Renderer3D.Vertex(arg0, var17[(var20 + 1)][(var21 + 1)], var19, var18[(var20 + 1)][(var21 + 1)], arg6[(var20 + 1)]);
                tmp0[3] = new Renderer3D.Vertex(arg0, var17[var20][(var21 + 1)], var19, var18[var20][(var21 + 1)], arg6[var20]);
                Renderer3D.QUADS.add(new Renderer3D.VertexCollection(tmp0));
                var21++;
            }
            var20++;
        }
    }

    private void crestLine(PoseStack arg0, double arg1, double arg2, double arg3, double arg4, double arg5, double arg6, int arg7) {
        int var15 = 40;
        Object var16 = color(16777215, (0.6 * arg5));
        Object var17 = null;
        int var18 = 0;
        while (var18 <= 40) {
            double var19 = (((((double) var18) * 3.141592653589793) * 2.0) / 40.0);
            double var21 = (1.0 + (0.025 * (Math.sin(((var19 * 5.0) + arg6)) + (0.6 * Math.sin(((var19 * 9.0) - (arg6 * 1.3)))))));
            Object var23 = new Vec3((arg1 + ((Math.cos(var19) * arg4) * var21)), (arg2 + 0.005), (arg3 + ((Math.sin(var19) * arg4) * var21)));
            if (var17 != null) {
                Renderer3D.renderLine(arg0, var17, var23, var16);
            }
            var17 = var23;
            var18++;
        }
    }

    private void sparkles(PoseStack arg0, double arg1, double arg2, double arg3, double arg4, double arg5, double arg6, int arg7) {
        int var15 = 0;
        while (var15 < 14) {
            double var16 = ((hash(var15, 3) * 3.141592653589793) * 2.0);
            double var18 = (0.5 + (0.5 * Math.sin(((arg6 * 16.0) + (((double) var15) * 2.3)))));
            double var20 = (arg1 + (Math.cos(var16) * arg4));
            double var22 = (arg3 + (Math.sin(var16) * arg4));
            Object var24 = new AABB((var20 - 0.03), arg2, (var22 - 0.03), (var20 + 0.03), (arg2 + 0.01), (var22 + 0.03));
            Renderer3D.renderBox(arg0, var24, color(16777215, ((arg5 * var18) * 0.9)));
            var15++;
        }
    }

    private void drawDroplets(PoseStack arg0, double arg1, double arg2, double arg3, double arg4, double arg5, int arg6, int arg7) {
        if (arg5 >= 0.5) break label_1;
        double var14 = (arg5 / 0.5);
        int var16 = 0;
        while (var16 < 12) {
            double var17 = (((((double) var16) * 3.141592653589793) / 6.0) + 0.2);
            double var19 = (0.6 + (0.4 * hash(var16, 7)));
            double var21 = ((arg4 * 0.1) * (0.4 + var14));
            double var23 = (arg1 + (Math.cos(var17) * var21));
            double var25 = (arg3 + (Math.sin(var17) * var21));
            double var27 = ((0.42 * Math.sin((3.141592653589793 * Math.min(1.0, var14)))) * var19);
            Object var29 = color(arg7, ((1.0 - var14) * 0.85));
            Renderer3D.renderLine(arg0, new Vec3(var23, arg2, var25), new Vec3(var23, (arg2 + var27), var25), var29);
            var16++;
        }
        label_1:
        var14 = (arg5 - 0.02);
        if (var14 < 0.0) return;
        var16 = 0;
        while (var16 < 10) {
            var17 = (((((double) var16) * 0.628) + 0.5) + (hash(var16, 1) * 0.4));
            var19 = (arg4 * (0.35 + (0.25 * hash(var16, 2))));
            var21 = (1.94 + (0.83 * hash(var16, 4)));
            var23 = 8.33;
            var25 = ((var21 * var14) - (((0.5 * var23) * var14) * var14));
            if (var25 < 0.0) {
            } else {
                var27 = (arg1 + ((Math.cos(var17) * var19) * var14));
                var29 = (arg3 + ((Math.sin(var17) * var19) * var14));
                Object var31 = new AABB((var27 - 0.03), ((arg2 + var25) - 0.03), (var29 - 0.03), (var27 + 0.03), ((arg2 + var25) + 0.03), (var29 + 0.03));
                Renderer3D.renderBox(arg0, var31, color(arg6, 0.95));
            }
            var16++;
        }
    }


    final class Splash {
        final Vec3 pos;
        final long start;

        Splash(Vec3 arg0, long arg1) {
            this.pos = arg0;
            this.start = arg1;
        }

    }
}
