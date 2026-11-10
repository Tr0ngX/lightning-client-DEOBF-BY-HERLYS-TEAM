package lightning.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lightning.events.SubscribeEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.IMinecraft;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.Renderer2D;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Icons", description = "Renders icons for specific events.", category = Module.Category.VISUALS)
public class IconsModule extends Module {
    public CategorySetting chorusCategory;
    public BooleanSetting chorus;
    public ColorSetting chorusColor;
    public NumberSetting duration;
    public CategorySetting pearlsCategory;
    public BooleanSetting pearls;
    public ColorSetting pearlsColor;
    public NumberSetting scale;
    private final ArrayList iconList;
    private final Map pearlMap;

    public IconsModule() {
        this.chorusCategory = new CategorySetting("Chorus", "The category for chorus icons.");
        this.chorus = new BooleanSetting("Chorus", "Enabled", "Renders an icon for chorus positions.", new CategorySetting.Visibility(this.chorusCategory), true);
        this.chorusColor = new ColorSetting("ChorusColor", "Color", "The color for the chorus icons.", new CategorySetting.Visibility(this.chorusCategory), new ColorSetting.Color(new Color(192, 147, 212), false, false));
        this.duration = new NumberSetting("Duration", "The duration of icon renders.", new CategorySetting.Visibility(this.chorusCategory), 1500, 0, 5000);
        this.pearlsCategory = new CategorySetting("Pearls", "The category for pearl icons.");
        this.pearls = new BooleanSetting("Pearls", "Enabled", "Renders an icon for pearl positions.", new CategorySetting.Visibility(this.pearlsCategory), true);
        this.pearlsColor = new ColorSetting("PearlsColor", "Color", "The color for the pearl icons.", new CategorySetting.Visibility(this.pearlsCategory), new ColorSetting.Color(new Color(30, 131, 89), false, false));
        this.scale = new NumberSetting("Scale", "The scaling that will be applied to the text ESP rendering.", 40, 10, 50);
        this.iconList = new ArrayList();
        this.pearlMap = new HashMap();
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent arg0) {
        if (getNull()) return;
        Object var2 = this.iconList;
        Object var3 = var2;
        // synchronized (var2)
        Object var4 = arg0.getEntity();
        if ((var4 instanceof ThrownEnderpearl)) {
            Object var5 = ((ThrownEnderpearl) var4);
            mc.level.players().stream().min(Comparator.comparingDouble((arg1) -> ((double) arg1.distanceTo(arg0)))).ifPresent(() -> {
    Object var3 = projectPearl(arg0.position(), arg1.getYRot(), arg1.getXRot(), arg1.getDeltaMovement());
    if (var3 != null) {
        Object var4 = new IconsModule.Icon(this, this, new Vec3(var3.x, var3.y, var3.z), IconsModule.Icon.Type.PEARL);
        this.pearlMap.put(var4, arg0.getId());
        this.iconList.add(var4);
    }
});
        }
        break label_6;
        Object var6 = null;
        throw var6;
        label_6:
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (getNull()) return;
        Object var2 = this.iconList;
        Object var3 = var2;
        // synchronized (var2)
        Object var6 = arg0.getPacket();
        if (!((var6 instanceof ClientboundSoundPacket))) break label_3;
        var tmp0 = ((ClientboundSoundPacket) var6);
        Object var4 = tmp0;
        var tmp1 = ((SoundEvent) tmp0.getSound().value());
        Object var5 = tmp1;
        if (tmp1 != SoundEvents.CHORUS_FRUIT_TELEPORT) {
            if (var5 != SoundEvents.ENDERMAN_TELEPORT) break label_3;
        }
        this.iconList.add(new IconsModule.Icon(this, this, new Vec3(var4.getX(), var4.getY(), var4.getZ()), IconsModule.Icon.Type.CHORUS));
        label_3:
        break label_7;
        Object var7 = null;
        throw var7;
        label_7:
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (getNull()) return;
        Object var2 = arg0.getMatrices();
        Object var3 = this.iconList;
        Object var4 = var3;
        // synchronized (var3)
        this.iconList.removeIf(() -> {
    if (arg0.type == IconsModule.Icon.Type.CHORUS) {
        if ((System.currentTimeMillis() - arg0.time) < ((long) (this.duration.getValue().intValue() + 1200))) {
        }
        if (arg0.type == IconsModule.Icon.Type.PEARL && !(mc.level.getEntity(((Integer) this.pearlMap.get(arg0)).intValue()) instanceof ThrownEnderpearl) && arg0.animation.value() == 0.0f) {
        }
    } else {
    }
    return false;
});
        if (!(this.chorus.getValue())) break label_5;
        Object var5 = this.iconList.stream().filter((arg0) -> arg0.type == IconsModule.Icon.Type.CHORUS).toList().iterator();
        while (var5.hasNext()) {
            Object var6 = ((IconsModule.Icon) var5.next());
            var6.render(var2, ((System.currentTimeMillis() - var6.time) >= ((long) (600 + this.duration.getValue().intValue())) ? 0.0f : 1.0f), () -> Renderer2D.renderTexture(arg0, -6.0f, -6.5f, 6.0f, 6.5f, Identifier.fromNamespaceAndPath("lightning", "textures/chorus.png"), Color.WHITE), this.chorusColor.getColor());
        }
        label_5:
        if (!(this.pearls.getValue())) break label_9;
        var5 = this.iconList.stream().filter((arg0) -> arg0.type == IconsModule.Icon.Type.PEARL).toList().iterator();
        while (var5.hasNext()) {
            var6 = ((IconsModule.Icon) var5.next());
            var6.render(var2, (!((mc.level.getEntity(((Integer) this.pearlMap.get(var6)).intValue()) instanceof ThrownEnderpearl)) ? 0.0f : 1.0f), () -> Renderer2D.renderTexture(arg0, -6.5f, -6.5f, 6.5f, 6.5f, Identifier.fromNamespaceAndPath("lightning", "textures/pearl.png"), Color.WHITE), this.pearlsColor.getColor());
        }
        label_9:
        break label_13;
        Object var7 = null;
        throw var7;
        label_13:
    }

    private Vec3 projectPearl(Vec3 arg0, float arg1, float arg2, Vec3 arg3) {
        double var5 = arg0.x;
        double var7 = arg0.y;
        double var9 = arg0.z;
        var7 = ((var7 + ((double) mc.player.getEyeHeight(mc.player.getPose()))) - 0.1000000014901161);
        float var11 = 0.4f;
        double var12 = ((double) ((-(Mth.sin(((double) ((arg1 / 180.0f) * 3.1415927f)))) * Mth.cos(((double) ((arg2 / 180.0f) * 3.1415927f)))) * var11));
        double var14 = ((double) (-(Mth.sin(((double) ((arg2 / 180.0f) * 3.141593f)))) * var11));
        double var16 = ((double) ((Mth.cos(((double) ((arg1 / 180.0f) * 3.1415927f))) * Mth.cos(((double) ((arg2 / 180.0f) * 3.1415927f)))) * var11));
        float var18 = Mth.sqrt(((float) (((var12 * var12) + (var14 * var14)) + (var16 * var16))));
        var12 = (var12 / ((double) var18));
        var14 = (var14 / ((double) var18));
        var16 = (var16 / ((double) var18));
        float var19 = 1.5f;
        var12 = (var12 * ((double) var19));
        var14 = (var14 * ((double) var19));
        var16 = (var16 * ((double) var19));
        var12 = (var12 + arg3.x);
        var14 = (var14 + arg3.y);
        var16 = (var16 + arg3.z);
        label_0:
        if (var7 > -65.0) {
            Object var20 = new Vec3(var5, var7, var9);
            var5 = (var5 + var12);
            var7 = (var7 + var14);
            var9 = (var9 + var16);
            if (mc.level.getBlockState(new BlockPos(((int) (var5 + var12)), ((int) (var7 + var14)), ((int) (var9 + var16)))).getBlock() == Blocks.WATER) {
                var12 = (var12 * 0.8);
                var14 = (var14 * 0.8);
                var16 = (var16 * 0.8);
            } else {
                var12 = (var12 * 0.99);
                var14 = (var14 * 0.99);
                var16 = (var16 * 0.99);
            }
            var14 = (var14 - 0.029999999329447746);
            Object var21 = new Vec3(var5, var7, var9);
            Object var22 = mc.level.entitiesForRendering().iterator();
            label_3:
            if (var22.hasNext()) {
                Object var23 = ((Entity) var22.next());
                if (((var23 == mc.player) || ((var23 instanceof Arrow))) || ((var23 instanceof ThrownEnderpearl))) break label_3;
                if (!(var23.getBoundingBox().intersects(new AABB((var5 - 1.0), (var7 - 1.0), (var9 - 1.0), (var5 + 1.0), (var7 + 1.0), (var9 + 1.0))))) {
                    break label_3;
                }
                return var23.getBoundingBox().getCenter();
            }
            var22 = mc.level.clip(new ClipContext(var20, var21, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
            if (var22 == null) break label_0;
            if (var22.getType() == HitResult.Type.MISS) {
                break label_0;
            }
            return var22.getLocation();
        }
        return null;
    }


    class Icon {
        private final Vec3 pos;
        private final Animation animation;
        private final IconsModule.Icon.Type type;
        private final long time;
        final IconsModule this$0;

        public Icon(IconsModule arg0, IconsModule arg1, Vec3 arg2, IconsModule.Icon.Type arg3) {
            Objects.requireNonNull(arg0);
            Object var5 = arg1;
            Objects.requireNonNull(var5);
            this.this$0 = var5;
            this.pos = arg2;
            this.animation = new Animation(0.0f, 1.0f, 600, Easing.Method.EASE_IN_OUT_ELASTIC);
            this.type = arg3;
            this.time = System.currentTimeMillis();
        }

        private void render(PoseStack arg0, float arg1, Runnable arg2, Color arg3) {
            float var5 = this.animation.get(arg1);
            float var6 = ((float) Math.sqrt(IMinecraft.mc.gameRenderer.mainCamera().position().distanceToSqr(this.pos.x, this.pos.y, this.pos.z)));
            float var7 = (0.0018f + ((this.this$0.scale.getValue().floatValue() / 10000.0f) * var6));
            if (((double) var6) <= 4.0) {
                var7 = 0.0245f;
            }
            Object var8 = new Vec3((this.pos.x - IMinecraft.mc.gameRenderer.mainCamera().position().x), (this.pos.y - IMinecraft.mc.gameRenderer.mainCamera().position().y), (this.pos.z - IMinecraft.mc.gameRenderer.mainCamera().position().z));
            arg0.pushPose();
            arg0.translate(var8.x, var8.y, var8.z);
            arg0.mulPose(IMinecraft.mc.gameRenderer.mainCamera().rotation());
            arg0.scale(var7, -(var7), var7);
            arg0.scale(var5, var5, 1.0f);
            Renderer2D.renderCircle(arg0, 0.0f, 0.0f, 12.0f, new Color(0, 0, 0, 100));
            Renderer2D.renderCircle(arg0, 0.0f, 0.0f, 10.0f, arg3);
            arg2.run();
            arg0.popPose();
        }

        @Generated
        public Vec3 getPos() {
            return this.pos;
        }

        @Generated
        public Animation getAnimation() {
            return this.animation;
        }

        @Generated
        public IconsModule.Icon.Type getType() {
            return this.type;
        }

        @Generated
        public long getTime() {
            return this.time;
        }


        public enum Type {
            CHORUS,
            PEARL;


            private Type() {
            }

        }
    }
}
