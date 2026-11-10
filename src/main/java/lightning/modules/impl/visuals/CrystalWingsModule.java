package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.CrystalWingsMath;
import lightning.utils.graphics.CrystalWingsState;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@RegisterModule(name = "CrystalWings", description = "One animated wing pair after repeated own crystal placements on one block in combat.", category = Module.Category.VISUALS)
public class CrystalWingsModule extends Module {
    public ModeSetting style;
    public NumberSetting crystalCount;
    public NumberSetting resetDelay;
    public NumberSetting holdTime;
    public NumberSetting targetGrace;
    public NumberSetting scale;
    public NumberSetting width;
    public NumberSetting height;
    public NumberSetting gap;
    public NumberSetting heightOffset;
    public NumberSetting backOffset;
    public NumberSetting opacity;
    public ColorSetting color;
    public BooleanSetting animate;
    public NumberSetting flapSpeed;
    public NumberSetting flapAngle;
    public BooleanSetting glow;
    public NumberSetting glowStrength;
    public NumberSetting lineWidth;
    public ModeSetting appearStyle;
    public NumberSetting appearDuration;
    public NumberSetting startScale;
    public NumberSetting overshoot;
    public NumberSetting range;
    private final ArrayDeque pending;
    private final LinkedHashMap seen;
    private final CrystalWingsState state;
    private ClientLevel world;
    private Object player;
    private Vec3 anchor;
    private long targetSeen;
    private long animationOrigin;
    private String cachedStyle;
    private CrystalWingsMath.Mesh mesh;

    public CrystalWingsModule() {
        this.style = new ModeSetting("Style", "Simple wing silhouette.", "Curl", CrystalWingsMath.STYLES);
        this.crystalCount = new NumberSetting("CrystalCount", "Successful own placements on the same block before wings appear.", 50, 1, 500);
        this.resetDelay = new NumberSetting("ResetDelay", "Idle milliseconds before the block counter resets.", 3000, 500, 15000);
        this.holdTime = new NumberSetting("HoldTime", "Keep wings after the last placement, in milliseconds.", 1000, 100, 5000);
        this.targetGrace = new NumberSetting("TargetGrace", "Grace for brief AutoCrystal target flicker, in milliseconds.", 350, 0, 1500);
        this.scale = new NumberSetting("Scale", "Overall wing size.", 0.75f, 0.15f, 2.0f);
        this.width = new NumberSetting("Width", "Horizontal size multiplier.", 1.0f, 0.4f, 2.0f);
        this.height = new NumberSetting("Height", "Vertical size multiplier.", 1.0f, 0.4f, 2.0f);
        this.gap = new NumberSetting("Gap", "Distance between the two wing roots, in blocks.", 0.35f, 0.0f, 1.5f);
        this.heightOffset = new NumberSetting("HeightOffset", "Attachment height above the crystal base.", 1.1f, 0.1f, 2.5f);
        this.backOffset = new NumberSetting("BackOffset", "Move wings behind the crystal relative to the camera.", 0.16f, 0.0f, 0.75f);
        this.opacity = new NumberSetting("Opacity", "Wing opacity percent; 0 hides wings.", 55, 0, 100);
        this.color = new ColorSetting("Color", "Wing color.", new ColorSetting.Color(Color.WHITE, false, false));
        this.animate = new BooleanSetting("Animate", "Gently flap the wings.", true);
        this.flapSpeed = new NumberSetting("FlapSpeed", "Flaps per second.", 0.65f, 0.0f, 3.0f);
        this.flapAngle = new NumberSetting("FlapAngle", "Maximum flap angle in degrees.", 12.0f, 0.0f, 40.0f);
        this.glow = new BooleanSetting("Glow", "One soft edge layer; no postprocessing shader.", false);
        this.glowStrength = new NumberSetting("GlowStrength", "Edge glow intensity.", 0.2f, 0.0f, 0.6f);
        this.lineWidth = new NumberSetting("LineWidth", "Contour thickness in local wing units.", 0.018f, 0.005f, 0.06f);
        var tmp0 = new String[5];
        tmp0[0] = "Grow";
        tmp0[1] = "Fade";
        tmp0[2] = "Unfold";
        tmp0[3] = "Pop";
        tmp0[4] = "None";
        this.appearStyle = new ModeSetting("AppearStyle", "Appearance runs once when the block reaches CrystalCount.", "Grow", tmp0);
        this.appearDuration = new NumberSetting("AppearDuration", "Appearance duration in milliseconds.", 250, 50, 1500);
        this.startScale = new NumberSetting("StartScale", "Initial size for Grow and Pop.", 0.2f, 0.05f, 1.0f);
        this.overshoot = new NumberSetting("Overshoot", "Small size bounce for Pop.", 0.08f, 0.0f, 0.15f);
        this.range = new NumberSetting("Range", "Maximum visible distance in blocks.", 32, 4, 96);
        this.pending = new ArrayDeque();
        this.seen = new LinkedHashMap();
        this.state = new CrystalWingsState();
    }

    private void clear() {
        this.pending.clear();
        this.seen.clear();
        this.state.clear();
        this.anchor = null;
        this.targetSeen = 0L;
        this.animationOrigin = System.nanoTime();
    }

    public void onEnable() {
        clear();
        this.world = mc.level;
        this.player = mc.player;
    }

    public void onDisable() {
        clear();
        this.world = null;
        this.player = null;
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent arg0) {
        Object var2 = mc.level;
        Object var3 = mc.player;
        mc.execute(() -> {
    if (mc.level == arg0 && mc.player == arg1) {
        onDisable();
    }
});
    }

    private void context() {
        if (this.world == mc.level) {
            if (this.player != mc.player) {
            }
            clear();
            this.world = mc.level;
            this.player = mc.player;
        }
    }

    private boolean combat(long arg0) {
        Object var3 = ((AutoCrystalModule) LightningClient.MODULE_MANAGER.getModule(AutoCrystalModule.class));
        if (var3 != null && var3.isToggled() && var3.getTarget() != null && var3.getTarget().isAlive()) {
            this.targetSeen = arg0;
        }
        if (this.targetSeen == 0L) break label_1;
        return (arg0 - this.targetSeen) <= (this.targetGrace.getValue().longValue() * 1000000L);
    }

    private void expire(long arg0) {
        this.pending.removeIf((arg1) -> (arg0 - arg1.time) > 2000000000L);
        this.seen.entrySet().removeIf((arg1) -> (arg0 - ((Long) arg1.getValue()).longValue()) > 3000000000L);
        this.state.expire(arg0, this.resetDelay.getValue().intValue());
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (!(isToggled())) return;
        context();
        if (mc.player != null && mc.level != null) {
            long var2 = System.nanoTime();
            combat(var2);
            expire(var2);
        }
    }

    @SubscribeEvent
    public void onUse(PacketSendEvent.Post arg0) {
        if (((!(isToggled())) || (mc.player == null)) || (mc.level == null)) break label_0;
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundUseItemOnPacket))) break label_0;
        Object var2 = ((ServerboundUseItemOnPacket) var3);
        break label_1;
        label_0:
        return;
        label_1:
        var3 = LightningClient.POSITION_MANAGER.getServerSlot();
        if (var2.getHand() == InteractionHand.OFF_HAND) {
            break label_4;
        }
        if ((var3 < 0) || (var3 >= 9)) break label_3;
        int var4 = (mc.player.getInventory().getItem(var3).is(Items.END_CRYSTAL) ? 1 : 0);
        if (!var4) return;
        Object var5 = Vec3.atBottomCenterOf(var2.getHitResult().getBlockPos().above());
        Object var6 = mc.level;
        Object var7 = mc.player;
        mc.execute(() -> {
    if (((!(isToggled())) || (mc.level != arg0)) || (mc.player != arg1)) {
        return;
    }
    context();
    long var4 = System.nanoTime();
    if (!(combat(var4))) return;
    expire(var4);
    while (this.pending.size() >= 64) {
        this.pending.removeFirst();
    }
    this.pending.addLast(new CrystalWingsModule.Pending(arg2, var4));
});
    }

    @SubscribeEvent
    public void onSpawn(EntitySpawnEvent arg0) {
        if (((!(isToggled())) || (mc.player == null)) || (mc.level == null)) break label_0;
        Object var3 = arg0.getEntity();
        if (!((var3 instanceof EndCrystal))) break label_0;
        Object var2 = ((EndCrystal) var3);
        break label_1;
        label_0:
        return;
        label_1:
        var3 = var2.position();
        Object var4 = var2.getUUID();
        Object var5 = mc.level;
        Object var6 = mc.player;
        mc.execute(() -> {
    if (((!(isToggled())) || (mc.level != arg0)) || (mc.player != arg1)) {
        return;
    }
    context();
    long var5 = System.nanoTime();
    expire(var5);
    if ((this.seen.containsKey(arg2)) || (!(combat(var5)))) {
        return;
    }
    Object var7 = null;
    Object var8 = this.pending.iterator();
    while (var8.hasNext()) {
        Object var9 = ((CrystalWingsModule.Pending) var8.next());
        if (var9.position.distanceToSqr(arg3) < 0.04) {
            var7 = var9;
            break label_6;
        }
    }
    label_6:
    if (var7 == null) return;
    this.pending.remove(var7);
    if (mc.player.position().distanceToSqr(arg3) > Math.pow(this.range.getValue().doubleValue(), 2.0)) return;
    while (this.seen.size() >= 128) {
        this.seen.remove(this.seen.keySet().iterator().next());
    }
    this.seen.put(arg2, var5);
    this.state.record(BlockPos.containing(arg3).asLong(), var5, this.crystalCount.getValue().intValue(), this.resetDelay.getValue().intValue());
    this.anchor = arg3;
});
    }

    @SubscribeEvent
    public void onRender(RenderWorldEvent arg0) {
        if (!(isToggled())) return;
        context();
        if ((mc.player == null) || (mc.level == null)) break label_1;
        if (Renderer3D.RENDERING) break label_2;
        label_1:
        return;
        label_2:
        long var2 = System.nanoTime();
        combat(var2);
        this.state.expire(var2, this.resetDelay.getValue().intValue());
        if (((this.anchor == null) || (!(this.state.visible(var2, this.holdTime.getValue().intValue(), this.targetSeen, this.targetGrace.getValue().intValue())))) || (!(this.opacity.getValue().intValue()))) {
            return;
        }
        double var4 = this.scale.getValue().doubleValue();
        double var6 = (((((var4 * (1.0 + this.overshoot.getValue().doubleValue())) * (Math.max((this.width.getValue().doubleValue() * 2.0), (this.height.getValue().doubleValue() * 1.5)) + this.gap.getValue().doubleValue())) + this.heightOffset.getValue().doubleValue()) + this.backOffset.getValue().doubleValue()) + 1.0);
        if ((mc.player.position().distanceToSqr(this.anchor) > Math.pow(this.range.getValue().doubleValue(), 2.0)) || (!(Renderer3D.isFrustumVisible(new AABB((this.anchor.x - var6), (this.anchor.y - var6), (this.anchor.z - var6), (this.anchor.x + var6), ((this.anchor.y + var6) + 3.0), (this.anchor.z + var6)))))) {
            return;
        }
        Object var8 = this.style.getValue();
        if (this.mesh != null) {
            if (!(var8.equals(this.cachedStyle))) {
            }
            this.mesh = CrystalWingsMath.model(var8);
            this.cachedStyle = var8;
        }
        double var9 = CrystalWingsMath.progress((var2 - this.state.appearedAt()), this.appearDuration.getValue().intValue());
        Object var11 = this.appearStyle.getValue();
        if (var11.equals("None")) {
        } else {
        }
        double var12 = (1.0 * CrystalWingsMath.ease(var9));
        if (var12 < 0.001) return;
        var4 = (var4 * CrystalWingsMath.appearScale(var11, var9, this.startScale.getValue().doubleValue(), this.overshoot.getValue().doubleValue()));
        if (this.animate.getValue()) {
        } else {
        }
        double var14 = 0.0;
        if (var11.equals("Unfold")) {
            var14 = (var14 + (Math.toRadians(65.0) * (1.0 - CrystalWingsMath.ease(var9))));
        }
        Object var16 = mc.gameRenderer.mainCamera().position();
        Object var17 = this.anchor.subtract(var16).multiply(1.0, 0.0, 1.0);
        if (var17.lengthSqr() < 1.0E-8) {
            var17 = new Vec3(0.0, 0.0, 1.0);
        } else {
            var17 = var17.normalize();
        }
        Object var18 = new Vec3(var17.z, 0.0, -(var17.x));
        Object var19 = this.anchor.add(0.0, this.heightOffset.getValue().doubleValue(), 0.0).add(var17.scale(this.backOffset.getValue().doubleValue())).subtract(var16);
        Object var20 = arg0.getMatrices().last().pose();
        Object var21 = this.color.getColor();
        var tmp0 = new int[2];
        tmp0[0] = -1;
        tmp0[1] = 1;
        Object var22 = tmp0;
        int var23 = var22.length;
        int var24 = 0;
        while (var24 < var23) {
            int var25 = var22[var24];
            if (var8.equals("Outline")) break label_19;
            Object var26 = this.mesh.triangles().iterator();
            while (var26.hasNext()) {
                Object var27 = ((double[][]) var26.next());
                Object var28 = vertex(var20, var19, var18, var17, var27[0], var25, var4, var14, var21, var12);
                Object var29 = vertex(var20, var19, var18, var17, var27[1], var25, var4, var14, var21, var12);
                Object var30 = vertex(var20, var19, var18, var17, var27[2], var25, var4, var14, var21, var12);
                var tmp1 = new Renderer3D.Vertex[4];
                tmp1[0] = var28;
                tmp1[1] = var29;
                tmp1[2] = var30;
                tmp1[3] = var30;
                Renderer3D.QUADS.add(new Renderer3D.VertexCollection(tmp1));
            }
            label_19:
            lines(var20, var19, var18, var17, this.mesh.outline(), true, var25, var4, var14, var21, var12);
            if (var8.equals("Outline")) break label_21;
            var26 = this.mesh.seams().iterator();
            while (var26.hasNext()) {
                var27 = ((List) var26.next());
                lines(var20, var19, var18, var17, var27, false, var25, var4, var14, new Color((var21.getRed() / 4), (var21.getGreen() / 4), (var21.getBlue() / 4), var21.getAlpha()), (var12 * 0.8));
            }
            label_21:
            var24++;
        }
    }

    private Renderer3D.Vertex vertex(Matrix4f arg0, Vec3 arg1, Vec3 arg2, Vec3 arg3, double[] arg4, int arg5, double arg6, double arg7, Color arg8, double arg9) {
        double var14 = (((((double) arg5) * ((this.gap.getValue().doubleValue() / 2.0) + (arg4[0] * Math.cos(arg7)))) * arg6) * this.width.getValue().doubleValue());
        double var16 = ((arg4[1] * arg6) * this.height.getValue().doubleValue());
        double var18 = (((arg4[0] * Math.sin(arg7)) * arg6) * this.width.getValue().doubleValue());
        Object var20 = arg1.add(arg2.scale(var14)).add(0.0, var16, 0.0).add(arg3.scale(var18));
        int var21 = new Color(arg8.getRed(), arg8.getGreen(), arg8.getBlue(), ((int) Math.clamp((((double) arg8.getAlpha()) * arg9), 0.0, 255.0))).getRGB();
        return new Renderer3D.Vertex(arg0, ((float) var20.x), ((float) var20.y), ((float) var20.z), var21);
    }

    private void lines(Matrix4f arg0, Vec3 arg1, Vec3 arg2, Vec3 arg3, List arg4, boolean arg5, int arg6, double arg7, double arg8, Color arg9, double arg10) {
        int var15 = 0;
        label_0:
        if (!(!arg5)) {
        } else {
        }
        if (arg4.size() < (arg4.size() - 1)) {
            Object var16 = ((double[]) arg4.get(var15));
            Object var17 = ((double[]) arg4.get(((var15 + 1) % arg4.size())));
            double var18 = (var17[0] - var16[0]);
            double var20 = (var17[1] - var16[1]);
            double var22 = Math.hypot(var18, var20);
            if (var22 < 1.0E-8) {
                break label_4;
            }
            double var24 = (this.lineWidth.getValue().doubleValue() / 2.0);
            band(arg0, arg1, arg2, arg3, var16, var17, ((-(var20) / var22) * var24), ((var18 / var22) * var24), arg6, arg7, arg8, arg9, arg10, false);
            if ((!(this.glow.getValue())) || (!arg5)) break label_4;
            band(arg0, arg1, arg2, arg3, var16, var17, (((-(var20) / var22) * var24) * 3.0), (((var18 / var22) * var24) * 3.0), arg6, arg7, arg8, arg9, (arg10 * this.glowStrength.getValue().doubleValue()), true);
            label_4:
            var15++;
            break label_0;
        }
    }

    private void band(Matrix4f arg0, Vec3 arg1, Vec3 arg2, Vec3 arg3, double[] arg4, double[] arg5, double arg6, double arg7, int arg8, double arg9, double arg10, Color arg11, double arg12, boolean arg13) {
        Object var20 = (!(!arg13) ? Renderer3D.SHINE_QUADS : Renderer3D.QUADS);
        var tmp0 = new Renderer3D.Vertex[4];
        var tmp1 = new double[2];
        tmp1[0] = (arg4[0] - arg6);
        tmp1[1] = (arg4[1] - arg7);
        tmp0[0] = vertex(arg0, arg1, arg2, arg3, tmp1, arg8, arg9, arg10, arg11, arg12);
        var tmp2 = new double[2];
        tmp2[0] = (arg5[0] - arg6);
        tmp2[1] = (arg5[1] - arg7);
        tmp0[1] = vertex(arg0, arg1, arg2, arg3, tmp2, arg8, arg9, arg10, arg11, arg12);
        var tmp3 = new double[2];
        tmp3[0] = (arg5[0] + arg6);
        tmp3[1] = (arg5[1] + arg7);
        tmp0[2] = vertex(arg0, arg1, arg2, arg3, tmp3, arg8, arg9, arg10, arg11, arg12);
        var tmp4 = new double[2];
        tmp4[0] = (arg4[0] + arg6);
        tmp4[1] = (arg4[1] + arg7);
        tmp0[3] = vertex(arg0, arg1, arg2, arg3, tmp4, arg8, arg9, arg10, arg11, arg12);
        var20.add(new Renderer3D.VertexCollection(tmp0));
    }

    public String getMetaData() {
        return (this.state.count() + "/" + this.crystalCount.getValue().intValue());
    }


    record Pending(Vec3 position, long time) {

        private Pending(Vec3 arg0, long arg1) {
            this.position = arg0;
            this.time = arg1;
        }

        public Vec3 position() {
            return this.position;
        }

        public long time() {
            return this.time;
        }

    }
}
