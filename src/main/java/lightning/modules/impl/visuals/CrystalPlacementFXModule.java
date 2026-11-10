package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.CrystalPlacementMath;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@RegisterModule(name = "CrystalPlacementFX", description = "Smooth rings and ground glow for your own crystal placements.", category = Module.Category.VISUALS)
public class CrystalPlacementFXModule extends Module {
    public ModeSetting style;
    public ColorSetting color;
    public ColorSetting secondaryColor;
    public BooleanSetting dualColor;
    public NumberSetting duration;
    public NumberSetting radius;
    public NumberSetting expansion;
    public NumberSetting thickness;
    public NumberSetting rotationSpeed;
    public NumberSetting heightOffset;
    public BooleanSetting glow;
    public NumberSetting glowStrength;
    public NumberSetting quality;
    public NumberSetting range;
    public NumberSetting maxEffects;
    public BooleanSetting risingRing;
    public NumberSetting riseHeight;
    public NumberSetting riseSpeed;
    private final ArrayDeque placements;
    private final Map bursts;
    private ClientLevel effectLevel;

    public CrystalPlacementFXModule() {
        var tmp0 = new String[8];
        tmp0[0] = "Pulse";
        tmp0[1] = "Double";
        tmp0[2] = "Segmented";
        tmp0[3] = "Hexagon";
        tmp0[4] = "Rune";
        tmp0[5] = "GroundGlow";
        tmp0[6] = "Petal";
        tmp0[7] = "Orbit";
        this.style = new ModeSetting("Style", "Placement effect preset.", "Pulse", tmp0);
        this.color = new ColorSetting("Color", "Primary effect color.", new ColorSetting.Color(new Color(30, 240, 205, 210), false, false));
        this.secondaryColor = new ColorSetting("SecondaryColor", "Secondary effect color.", new ColorSetting.Color(new Color(115, 140, 255, 180), false, false));
        this.dualColor = new BooleanSetting("DualColor", "Blend both colors around the effect.", true);
        this.duration = new NumberSetting("Duration", "Effect lifetime in milliseconds.", 300, 100, 1500);
        this.radius = new NumberSetting("Radius", "Starting ring radius in blocks.", 0.55f, 0.15f, 2.0f);
        this.expansion = new NumberSetting("Expansion", "Additional radial expansion in blocks.", 0.25f, 0.0f, 2.0f);
        this.thickness = new NumberSetting("Thickness", "Ring band width in blocks.", 0.035f, 0.01f, 0.2f);
        this.rotationSpeed = new NumberSetting("RotationSpeed", "Effect rotation in degrees per second.", 90.0f, -360.0f, 360.0f);
        this.heightOffset = new NumberSetting("HeightOffset", "Height above the crystal base to avoid surface flicker.", 0.025f, 0.005f, 0.5f);
        this.glow = new BooleanSetting("Glow", "Add soft additive glow; no post-processing shader.", true);
        this.glowStrength = new NumberSetting("GlowStrength", "Soft glow intensity.", 0.5f, 0.0f, 1.0f);
        this.quality = new NumberSetting("Quality", "Segments per circular ring.", 48, 24, 96);
        this.range = new NumberSetting("Range", "Maximum distance for placement effects.", 32, 4, 96);
        this.maxEffects = new NumberSetting("MaxEffects", "Maximum simultaneous effects; oldest expire first.", 16, 1, 48);
        this.risingRing = new BooleanSetting("RisingRing", "An extra luminous ring rises from your crystal placement.", true);
        this.riseHeight = new NumberSetting("RiseHeight", "Maximum rising ring height in blocks.", 1.2f, 0.1f, 3.0f);
        this.riseSpeed = new NumberSetting("RiseSpeed", "Rising animation speed multiplier.", 1.0f, 0.25f, 3.0f);
        this.placements = new ArrayDeque();
        this.bursts = new LinkedHashMap();
    }

    public void onEnable() {
        clear();
        this.effectLevel = mc.level;
    }

    public void onDisable() {
        clear();
    }

    private void clear() {
        this.bursts.clear();
        this.placements.clear();
        this.effectLevel = null;
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent arg0) {
        mc.execute(this::clear);
    }

    @SubscribeEvent
    public void onPlaced(PacketSendEvent.Post arg0) {
        if (((!(isToggled())) || (mc.player == null)) || (mc.level == null)) break label_0;
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundUseItemOnPacket))) break label_0;
        Object var2 = ((ServerboundUseItemOnPacket) var3);
        break label_1;
        label_0:
        return;
        label_1:
        if (var2.getHand() == InteractionHand.OFF_HAND) {
        } else {
        }
        var3 = mc.player.getInventory().getItem(LightningClient.POSITION_MANAGER.getServerSlot()).is(Items.END_CRYSTAL);
        if (!var3) return;
        Object var4 = Vec3.atBottomCenterOf(var2.getHitResult().getBlockPos().above());
        Object var5 = mc.level;
        long var6 = System.nanoTime();
        mc.execute(() -> {
    if ((!(isToggled())) || (mc.level != arg0)) {
        return;
    }
    if (this.effectLevel != arg0) {
        clear();
        this.effectLevel = arg0;
    }
    this.placements.removeIf((arg1) -> (arg0 - arg1.start) > 2000000000L);
    while (this.placements.size() >= 128) {
        this.placements.removeFirst();
    }
    this.placements.addLast(new CrystalPlacementFXModule.Pending(arg2, arg1));
});
    }

    @SubscribeEvent
    public void onSpawn(EntitySpawnEvent arg0) {
        if (!(isToggled())) break label_0;
        Object var3 = arg0.getEntity();
        if (!((var3 instanceof EndCrystal))) break label_0;
        Object var2 = ((EndCrystal) var3);
        if (mc.player == null) break label_0;
        if (mc.level == null) {
            label_0:
            return;
        }
        var3 = var2.position();
        Object var4 = var2.getUUID();
        Object var5 = mc.level;
        long var6 = System.nanoTime();
        mc.execute(() -> {
    if (((!(isToggled())) || (mc.level != arg0)) || (mc.player == null)) {
        return;
    }
    if (this.effectLevel != arg0) {
        clear();
        this.effectLevel = arg0;
    }
    this.placements.removeIf((arg1) -> (arg0 - arg1.start) > 2000000000L);
    Object var6 = null;
    Object var7 = this.placements.iterator();
    while (var7.hasNext()) {
        Object var8 = ((CrystalPlacementFXModule.Pending) var7.next());
        if (var8.center.distanceToSqr(arg2) < 0.04) {
            var6 = var8;
            break label_5;
        }
    }
    label_5:
    if (var6 == null) return;
    this.placements.remove(var6);
    var7 = this.range.getValue().doubleValue();
    if ((mc.player.position().distanceToSqr(arg2) > (var7 * var7)) || (this.bursts.containsKey(arg3))) {
        return;
    }
    while (this.bursts.size() >= this.maxEffects.getValue().intValue()) {
        this.bursts.remove(this.bursts.keySet().iterator().next());
    }
    this.bursts.put(arg3, new CrystalPlacementFXModule.Burst(arg2, arg1));
});
    }

    @SubscribeEvent
    public void onRender(RenderWorldEvent arg0) {
        if (!(isToggled())) return;
        if ((mc.level == null) || (mc.player == null)) break label_1;
        if (this.effectLevel != mc.level) {
            label_1:
            clear();
            return;
        }
        long var2 = System.nanoTime();
        int var4 = this.duration.getValue().intValue();
        this.bursts.values().removeIf((arg2) -> CrystalPlacementMath.progress((arg0 - arg2.start), arg1) >= 1.0);
        while (this.bursts.size() > this.maxEffects.getValue().intValue()) {
            this.bursts.remove(this.bursts.keySet().iterator().next());
        }
        if (!Renderer3D.RENDERING) return;
        double var5 = this.range.getValue().doubleValue();
        Object var7 = this.bursts.values().iterator();
        while (var7.hasNext()) {
            Object var8 = ((CrystalPlacementFXModule.Burst) var7.next());
            if (mc.player.position().distanceToSqr(var8.center) > (var5 * var5)) {
                break label_6;
            }
            double var9 = ((this.radius.getValue().doubleValue() + this.expansion.getValue().doubleValue()) + 0.8);
            if (!(Renderer3D.isFrustumVisible(new AABB((var8.center.x - var9), var8.center.y, (var8.center.z - var9), (var8.center.x + var9), ((var8.center.y + this.riseHeight.getValue().doubleValue()) + 1.0), (var8.center.z + var9))))) {
                break label_6;
            }
            double var11 = CrystalPlacementMath.progress((var2 - var8.start), var4);
            double var13 = CrystalPlacementMath.opacity(var11);
            if (var13 < 0.001) {
                break label_6;
            }
            double var15 = CrystalPlacementMath.radius(this.radius.getValue().doubleValue(), this.expansion.getValue().doubleValue(), var11);
            double var17 = ((((double) (var2 - var8.start)) / 1.0E9) * Math.toRadians(this.rotationSpeed.getValue().doubleValue()));
            Object var19 = arg0.getMatrices().last().pose();
            Object var20 = var8.center.add(0.0, this.heightOffset.getValue().doubleValue(), 0.0).subtract(mc.gameRenderer.mainCamera().position());
            if (this.risingRing.getValue()) {
                double var21 = CrystalPlacementMath.ease(Math.min(1.0, (var11 * this.riseSpeed.getValue().doubleValue())));
                ring(var19, var20.add(0.0, (this.riseHeight.getValue().doubleValue() * var21), 0.0), (var15 * 0.9), var17, var13, "Pulse");
            }
            var21 = this.style.getValue();
            if (var21.equals("GroundGlow")) {
                disk(var19, var20, var15, var13);
                break label_6;
            }
            ring(var19, var20, var15, var17, var13, var21);
            if (var21.equals("Double")) {
                double var22 = Math.max(0.0, ((var11 - 0.12) / 0.88));
                if (var11 > 0.12) {
                    ring(var19, var20, (var15 * 0.72), -(var17), CrystalPlacementMath.opacity(var22), "Pulse");
                }
                break label_18;
            }
            if (var21.equals("Rune")) {
                ring(var19, var20, (var15 * 0.72), -(var17), (var13 * 0.7), "Segmented");
                var22 = 0;
                while (var22 < 12) {
                    double var23 = (var17 + ((((double) var22) * 3.141592653589793) / 6.0));
                    stroke(var19, var20, (var15 * 1.08), (var15 * 1.2), var23, (var23 + 0.035), var13);
                    var22++;
                }
                break label_18;
            }
            if (!(var21.equals("Orbit"))) break label_18;
            var22 = 0;
            while (var22 < 3) {
                arc(var19, var20, (var15 * 1.2), ((var17 * 1.8) + (((((double) var22) * 3.141592653589793) * 2.0) / 3.0)), 0.42, var13);
                var22++;
            }
            label_18:
        }
    }

    private Color tint(double arg0, double arg1) {
        Object var5 = this.color.getColor();
        if (this.dualColor.getValue()) {
        } else {
        }
        Object var6 = var5;
        double var7 = ((Math.sin(arg0) + 1.0) / 2.0);
        int var9 = ((int) Math.max(0.0, Math.min(255.0, (((((double) var5.getAlpha()) * (1.0 - var7)) + (((double) var6.getAlpha()) * var7)) * arg1))));
        return new Color(((int) ((((double) var5.getRed()) * (1.0 - var7)) + (((double) var6.getRed()) * var7))), ((int) ((((double) var5.getGreen()) * (1.0 - var7)) + (((double) var6.getGreen()) * var7))), ((int) ((((double) var5.getBlue()) * (1.0 - var7)) + (((double) var6.getBlue()) * var7))), var9);
    }

    private void ring(Matrix4f arg0, Vec3 arg1, double arg2, double arg3, double arg4, String arg5) {
        if (arg5.equals("Hexagon")) {
        } else {
        }
        int var10 = this.quality.getValue().intValue();
        double var11 = this.thickness.getValue().doubleValue();
        int var13 = 0;
        while (var13 < var10) {
            if (arg5.equals("Segmented") && (var13 % 6) >= 4) {
                break label_8;
            }
            double var14 = (arg3 + (((((double) var13) * 3.141592653589793) * 2.0) / ((double) var10)));
            double var16 = (arg3 + (((((double) (var13 + 1)) * 3.141592653589793) * 2.0) / ((double) var10)));
            if (arg5.equals("Petal")) {
            } else {
            }
            double var18 = (CrystalPlacementMath.petal((var14 - arg3)) * 1.0);
            if (arg5.equals("Petal")) {
            } else {
            }
            double var20 = (CrystalPlacementMath.petal((var16 - arg3)) * 1.0);
            band(arg0, arg1, (var18 - (var11 / 2.0)), (var20 - (var11 / 2.0)), (var18 + (var11 / 2.0)), (var20 + (var11 / 2.0)), var14, var16, arg4, arg4, false);
            if (!(this.glow.getValue())) break label_8;
            double var22 = ((arg4 * this.glowStrength.getValue().doubleValue()) * 0.45);
            band(arg0, arg1, (var18 - (var11 * 3.0)), (var20 - (var11 * 3.0)), (var18 - (var11 / 2.0)), (var20 - (var11 / 2.0)), var14, var16, 0.0, var22, true);
            band(arg0, arg1, (var18 + (var11 / 2.0)), (var20 + (var11 / 2.0)), (var18 + (var11 * 3.0)), (var20 + (var11 * 3.0)), var14, var16, var22, 0.0, true);
            label_8:
            var13++;
        }
    }

    private void arc(Matrix4f arg0, Vec3 arg1, double arg2, double arg3, double arg4, double arg5) {
        int var11 = 0;
        while (var11 < 8) {
            stroke(arg0, arg1, (arg2 - 0.025), (arg2 + 0.025), (arg3 + ((arg4 * ((double) var11)) / 8.0)), (arg3 + ((arg4 * ((double) (var11 + 1))) / 8.0)), (arg5 * (0.35 + ((0.65 * ((double) var11)) / 8.0))));
            var11++;
        }
    }

    private void stroke(Matrix4f arg0, Vec3 arg1, double arg2, double arg3, double arg4, double arg5, double arg6) {
        band(arg0, arg1, arg2, arg2, arg3, arg3, arg4, arg5, arg6, arg6, false);
    }

    private void disk(Matrix4f arg0, Vec3 arg1, double arg2, double arg3) {
        int var7 = this.quality.getValue().intValue();
        int var8 = 0;
        while (var8 < var7) {
            double var9 = (((((double) var8) * 3.141592653589793) * 2.0) / ((double) var7));
            double var11 = (((((double) (var8 + 1)) * 3.141592653589793) * 2.0) / ((double) var7));
            band(arg0, arg1, 0.0, 0.0, arg2, arg2, var9, var11, (arg3 * 0.65), 0.0, this.glow.getValue());
            var8++;
        }
    }

    private Renderer3D.Vertex vertex(Matrix4f arg0, Vec3 arg1, double arg2, double arg3, double arg4) {
        return new Renderer3D.Vertex(arg0, ((float) (arg1.x + (Math.cos(arg3) * Math.max(0.0, arg2)))), ((float) arg1.y), ((float) (arg1.z + (Math.sin(arg3) * Math.max(0.0, arg2)))), tint(arg3, arg4).getRGB());
    }

    private void band(Matrix4f arg0, Vec3 arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7, double arg8, double arg9, boolean arg10) {
        Object var20 = (!(!arg10) ? Renderer3D.SHINE_QUADS : Renderer3D.QUADS);
        var tmp0 = new Renderer3D.Vertex[4];
        tmp0[0] = vertex(arg0, arg1, arg2, arg6, arg8);
        tmp0[1] = vertex(arg0, arg1, arg3, arg7, arg8);
        tmp0[2] = vertex(arg0, arg1, arg5, arg7, arg9);
        tmp0[3] = vertex(arg0, arg1, arg4, arg6, arg9);
        var20.add(new Renderer3D.VertexCollection(tmp0));
    }


    record Pending(Vec3 center, long start) {

        private Pending(Vec3 arg0, long arg1) {
            this.center = arg0;
            this.start = arg1;
        }

        public Vec3 center() {
            return this.center;
        }

        public long start() {
            return this.start;
        }

    }

    record Burst(Vec3 center, long start) {

        private Burst(Vec3 arg0, long arg1) {
            this.center = arg0;
            this.start = arg1;
        }

        public Vec3 center() {
            return this.center;
        }

        public long start() {
            return this.start;
        }

    }
}
