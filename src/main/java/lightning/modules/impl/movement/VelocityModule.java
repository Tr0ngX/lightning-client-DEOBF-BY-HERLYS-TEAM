package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.Vec3dAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Velocity", description = "Modifies the amount of knockback that you receive.", category = Module.Category.MOVEMENT)
public class VelocityModule extends Module {
    public ModeSetting mode;
    public NumberSetting horizontal;
    public NumberSetting vertical;
    public BooleanSetting onlyOnGround;
    public BooleanSetting explosions;
    public BooleanSetting pause;
    public BooleanSetting onlyPhased;
    public BooleanSetting onlyWhenHeadCovered;
    public NumberSetting fallDistance;
    public BooleanSetting conceal;
    public CategorySetting antiPushCategory;
    public BooleanSetting antiPush;
    public BooleanSetting antiLiquidPush;
    public BooleanSetting antiBlockPush;
    public BooleanSetting antiFishingRod;
    private boolean cancel;
    private boolean pendingConcealment;
    private boolean pendingVelocity;

    public VelocityModule() {
        var tmp0 = new String[4];
        tmp0[0] = "Normal";
        tmp0[1] = "Walls";
        tmp0[2] = "Cancel";
        tmp0[3] = "Grim";
        this.mode = new ModeSetting("Mode", "The method that will be used to achieve the knockback modification.", "Normal", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Normal";
        tmp1[1] = "Walls";
        this.horizontal = new NumberSetting("Horizontal", "The amount of horizontal knockback that you will receive.", new ModeSetting.Visibility(this.mode, tmp1), 0, 0, 100);
        var tmp2 = new String[2];
        tmp2[0] = "Normal";
        tmp2[1] = "Walls";
        this.vertical = new NumberSetting("Vertical", "The amount of vertical knockback that you will receive.", new ModeSetting.Visibility(this.mode, tmp2), 0, 0, 100);
        var tmp3 = new String[1];
        tmp3[0] = "Walls";
        this.onlyOnGround = new BooleanSetting("OnlyOnGround", "Only reduces knockback while phased AND on the ground.", new ModeSetting.Visibility(this.mode, tmp3), false);
        this.explosions = new BooleanSetting("Explosions", "Modifies knockback received from explosions.", true);
        var tmp4 = new String[2];
        tmp4[0] = "Cancel";
        tmp4[1] = "Grim";
        this.pause = new BooleanSetting("Pause", "Pauses the velocity for a certain duration whenever you get rubberbanded.", new ModeSetting.Visibility(this.mode, tmp4), true);
        var tmp5 = new String[1];
        tmp5[0] = "Grim";
        this.onlyPhased = new BooleanSetting("OnlyPhased", "Only reduces knockback while phased (Grim mode).", new ModeSetting.Visibility(this.mode, tmp5), false);
        var tmp6 = new String[1];
        tmp6[0] = "Grim";
        this.onlyWhenHeadCovered = new BooleanSetting("OnlyCoveredHead", "Only reduces knockback when a block is above your head (Grim mode).", new ModeSetting.Visibility(this.mode, tmp6), false);
        this.fallDistance = new NumberSetting("FallDistance", "Pause velocity if falling from more than this distance in blocks (0 to disable).", 10.0, 0.0, 50.0);
        this.conceal = new BooleanSetting("Conceal", "Ignores fake 0-0-0 velocity packets sent after a setback.", false);
        this.antiPushCategory = new CategorySetting("AntiPush", "Prevents certain things from pushing you.");
        this.antiPush = new BooleanSetting("AntiPush", "Entities", "Prevents other entities from pushing you.", new CategorySetting.Visibility(this.antiPushCategory), true);
        this.antiLiquidPush = new BooleanSetting("AntiLiquidPush", "Liquids", "Prevents liquids from pushing you.", new CategorySetting.Visibility(this.antiPushCategory), false);
        this.antiBlockPush = new BooleanSetting("AntiBlockPush", "Blocks", "Prevents you from being pushed outside of blocks.", new CategorySetting.Visibility(this.antiPushCategory), true);
        this.antiFishingRod = new BooleanSetting("AntiFishingRod", "FishingRods", "Prevents fishing rods from pushing you.", new CategorySetting.Visibility(this.antiPushCategory), false);
        this.pendingConcealment = false;
        this.pendingVelocity = false;
    }

    public void onEnable() {
        this.pendingVelocity = false;
        this.pendingConcealment = false;
    }

    public void onDisable() {
        if (!this.pendingVelocity) return;
        if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            sendRotationFix();
        }
        this.pendingVelocity = false;
        this.pendingConcealment = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.player == null) return;
        if (!(!this.pendingVelocity) && this.mode.getValue().equalsIgnoreCase("Grim")) {
            sendRotationFix();
        }
        this.pendingVelocity = false;
        this.pendingConcealment = false;
        this.cancel = false;
    }

    private void sendRotationFix() {
        float var1 = LightningClient.ROTATION_MANAGER.getServerYaw();
        float var2 = LightningClient.ROTATION_MANAGER.getServerPitch();
        float var3 = ((float) (((Math.random() * 2.0) - 1.0) * 0.0010000000474974513));
        float var4 = Mth.clamp((var2 + var3), -90.0f, 90.0f);
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(), var1, var4, mc.player.onGround(), mc.player.horizontalCollision));
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (mc.player == null) break label_36;
        if (this.fallDistance.getValue().doubleValue() > 0.0) {
            Double.compare(mc.player.fallDistance, ((double) this.fallDistance.getValue().floatValue()));
        }
        if ((!((arg0.getPacket() instanceof ClientboundPlayerPositionPacket))) || (!(this.conceal.getValue()))) break label_1;
        this.pendingConcealment = true;
        label_1:
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ClientboundSetEntityMotionPacket))) break label_20;
        Object var2 = ((ClientboundSetEntityMotionPacket) var3);
        if (var2.id() != mc.player.getId()) return;
        if ((((!this.pendingConcealment) || (var2.movement().x != 0.0)) || (var2.movement().y != 0.0)) || (var2.movement().z != 0.0)) break label_3;
        this.pendingConcealment = false;
        return;
        label_3:
        var3 = this.mode.getValue();
        int var4 = -1;
        switch (var3.hashCode()) {
            case -1955878649 -> { break label_4; }
            case 2228079 -> { break label_7; }
            case 83343305 -> { break label_5; }
            case 2011110042 -> { break label_6; }
            default -> { break label_8; }
        }
        label_4:
        if (!(var3.equals("Normal"))) break label_8;
        var4 = 0;
        break label_8;
        label_5:
        if (!(var3.equals("Walls"))) break label_8;
        var4 = 1;
        break label_8;
        label_6:
        if (!(var3.equals("Cancel"))) break label_8;
        var4 = 2;
        break label_8;
        label_7:
        if (!(var3.equals("Grim"))) break label_8;
        var4 = 3;
        label_8:
        switch (var4) {
            case 0 -> { break label_9; }
            case 1 -> { break label_10; }
            case 2 -> { break label_13; }
            case 3 -> { break label_15; }
            default -> { break label_20; }
        }
        label_9:
        scaleVelocity(arg0, var2);
        break label_20;
        label_10:
        if ((!(isPhased(mc.player))) || (this.onlyOnGround.getValue() && !(mc.player.onGround()))) {
            return;
        }
        scaleVelocity(arg0, var2);
        break label_20;
        label_13:
        if (this.pause.getValue() && !(LightningClient.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L))) return;
        arg0.setCancelled(true);
        break label_20;
        label_15:
        if (this.pause.getValue() && !(LightningClient.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L))) return;
        if (this.onlyPhased.getValue() && !(isPhased(mc.player))) return;
        if (this.onlyWhenHeadCovered.getValue()) {
            Object var5 = mc.player.blockPosition().above(2);
            if (mc.player.isVisuallyCrawling()) {
                var5 = mc.player.blockPosition().above(1);
            }
            if (mc.level.getBlockState(var5).isAir()) return;
        }
        scaleVelocity(arg0, var2);
        this.pendingVelocity = true;
        label_20:
        var3 = arg0.getPacket();
        if (!((var3 instanceof ClientboundExplodePacket))) break label_36;
        var2 = ((ClientboundExplodePacket) var3);
        if (!(this.explosions.getValue())) break label_36;
        var3 = this.mode.getValue();
        var4 = -1;
        switch (var3.hashCode()) {
            case -1955878649 -> { break label_21; }
            case 2228079 -> { break label_24; }
            case 83343305 -> { break label_22; }
            case 2011110042 -> { break label_23; }
            default -> { break label_25; }
        }
        label_21:
        if (!(var3.equals("Normal"))) break label_25;
        var4 = 0;
        break label_25;
        label_22:
        if (!(var3.equals("Walls"))) break label_25;
        var4 = 1;
        break label_25;
        label_23:
        if (!(var3.equals("Cancel"))) break label_25;
        var4 = 2;
        break label_25;
        label_24:
        if (!(var3.equals("Grim"))) break label_25;
        var4 = 3;
        label_25:
        switch (var4) {
            case 0 -> { break label_26; }
            case 1 -> { break label_27; }
            case 2 -> { break label_28; }
            case 3 -> { break label_30; }
            default -> { break label_35; }
        }
        label_26:
        scaleExplosion(var2);
        break label_35;
        label_27:
        if (!(isPhased(mc.player))) break label_35;
        scaleExplosion(var2);
        break label_35;
        label_28:
        if (this.pause.getValue() && !(LightningClient.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L))) return;
        arg0.setCancelled(true);
        break label_35;
        label_30:
        if (this.pause.getValue() && !(LightningClient.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L))) return;
        if (this.onlyPhased.getValue() && !(isPhased(mc.player))) return;
        if (this.onlyWhenHeadCovered.getValue()) {
            var5 = mc.player.blockPosition().above(2);
            if (mc.player.isVisuallyCrawling()) {
                var5 = mc.player.blockPosition().above(1);
            }
            if (mc.level.getBlockState(var5).isAir()) return;
        }
        scaleExplosion(var2);
        this.pendingVelocity = true;
        label_35:
        if (!(arg0.isCancelled())) break label_36;
        mc.executeBlocking(() -> {
    Object var1 = arg0.center();
    mc.getSoundManager().play(new SimpleSoundInstance(((SoundEvent) arg0.explosionSound().value()), SoundSource.BLOCKS, 4.0f, ((1.0f + ((mc.level.getRandom().nextFloat() - mc.level.getRandom().nextFloat()) * 0.2f)) * 0.7f), mc.level.getRandom(), var1.x, var1.y, var1.z));
    mc.level.addParticle(arg0.explosionParticle(), var1.x, var1.y, var1.z, 1.0, 0.0, 0.0);
});
        label_36:
    }

    private void scaleVelocity(PacketReceiveEvent arg0, ClientboundSetEntityMotionPacket arg1) {
        Object var3 = mc.player.getDeltaMovement();
        Object var4 = arg1.movement();
        double var5 = (((var4.x - var3.x) * (this.horizontal.getValue().doubleValue() / 100.0)) + var3.x);
        double var7 = (((var4.y - var3.y) * (this.vertical.getValue().doubleValue() / 100.0)) + var3.y);
        double var9 = (((var4.z - var3.z) * (this.horizontal.getValue().doubleValue() / 100.0)) + var3.z);
        arg0.setCancelled(true);
        mc.player.lerpMotion(new Vec3(var5, var7, var9));
    }

    private void scaleExplosion(ClientboundExplodePacket arg0) {
        if (arg0.playerKnockback().isPresent()) {
            ((Vec3dAccessor) arg0.playerKnockback().get()).setX(((double) ((float) (((Vec3) arg0.playerKnockback().get()).x * (this.horizontal.getValue().doubleValue() / 100.0)))));
        }
        if (arg0.playerKnockback().isPresent()) {
            ((Vec3dAccessor) arg0.playerKnockback().get()).setY(((double) ((float) (((Vec3) arg0.playerKnockback().get()).y * (this.vertical.getValue().doubleValue() / 100.0)))));
        }
        if (arg0.playerKnockback().isPresent()) {
            ((Vec3dAccessor) arg0.playerKnockback().get()).setZ(((double) ((float) (((Vec3) arg0.playerKnockback().get()).z * (this.horizontal.getValue().doubleValue() / 100.0)))));
        }
    }

    private boolean isPhased(Entity arg0) {
        if ((arg0 == null) || (mc.level == null)) {
            return false;
        }
        Object var2 = arg0.getBoundingBox();
        int var3 = Mth.floor(var2.minX);
        int var4 = Mth.ceil(var2.maxX);
        int var5 = Mth.floor(var2.minY);
        int var6 = Mth.ceil(var2.maxY);
        int var7 = Mth.floor(var2.minZ);
        int var8 = Mth.ceil(var2.maxZ);
        int var9 = var3;
        while (var9 < var4) {
            int var10 = var5;
            while (var10 < var6) {
                int var11 = var7;
                while (var11 < var8) {
                    Object var12 = new BlockPos(var9, var10, var11);
                    Object var13 = mc.level.getBlockState(var12).getCollisionShape(mc.level, var12);
                    if (var13.isEmpty()) break label_6;
                    if (!(var13.bounds().move(var12).intersects(var2))) {
                        break label_6;
                    }
                    return true;
                    label_6:
                    var11++;
                }
                var10++;
            }
            var9++;
        }
        return false;
    }

    public String getMetaData() {
        if (this.mode.getValue().equalsIgnoreCase("Cancel")) return "0%, 0%";
        if (this.mode.getValue().equalsIgnoreCase("Grim")) return "Grim";
        return (this.horizontal.getValue().intValue() + "%, " + this.vertical.getValue().intValue() + "%");
    }

}
