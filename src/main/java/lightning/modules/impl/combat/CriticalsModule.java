package lightning.modules.impl.combat;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

@RegisterModule(name = "Criticals", description = "Changes player movement for always landing critical hits.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class CriticalsModule extends Module {
    public ModeSetting mode;
    public BooleanSetting onlyPhased;
    public BooleanSetting onlyStandingStill;
    public BooleanSetting onlyWhenHeadCovered;
    private long lastCritTime;

    public CriticalsModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Packet";
        tmp0[1] = "Grim";
        tmp0[2] = "GrimNew";
        this.mode = new ModeSetting("Mode", "The method that will be used to achieve critical hits.", "Packet", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Grim";
        tmp1[1] = "GrimNew";
        this.onlyPhased = new BooleanSetting("OnlyPhased", "Only crits when you are phased into blocks.", new ModeSetting.Visibility(this.mode, tmp1), true);
        var tmp2 = new String[2];
        tmp2[0] = "Grim";
        tmp2[1] = "GrimNew";
        this.onlyStandingStill = new BooleanSetting("OnlyStandingStill", "Only crits when you are standing still.", new ModeSetting.Visibility(this.mode, tmp2), true);
        var tmp3 = new String[2];
        tmp3[0] = "Grim";
        tmp3[1] = "GrimNew";
        this.onlyWhenHeadCovered = new BooleanSetting("HeadCovered", "Only crits when there is a block covering your head.", new ModeSetting.Visibility(this.mode, tmp3), true);
        this.lastCritTime = 0L;
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent arg0) {
        if (shouldRunOnProxy()) return;
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (((((mc.player.isFallFlying()) || (mc.player.isInWater())) || (mc.player.isInLava())) || (mc.player.isSuppressingSlidingDownLadder())) || (mc.player.hasEffect(MobEffects.BLINDNESS))) {
            return;
        }
        Object var3 = arg0.getTarget();
        if (!((var3 instanceof LivingEntity))) break label_5;
        var tmp0 = ((LivingEntity) var3);
        Object var2 = tmp0;
        if ((!(tmp0.isAlive())) || ((var3 instanceof EndCrystal))) break label_5;
        if ((var3 instanceof ItemFrame)) {
            label_5:
            return;
        }
        if (mc.player.isHandsBusy()) {
            ridingAttack(var3);
            return;
        }
        onGroundAttack();
        mc.player.crit(var3);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if (shouldRunOnProxy()) return;
        if ((arg0.getPacket() instanceof ServerboundAttackPacket)) {
            long var2 = System.currentTimeMillis();
            if ((var2 - this.lastCritTime) < 50L) return;
            if ((mc.player == null) || (mc.level == null)) {
                return;
            }
            if (((((mc.player.isFallFlying()) || (mc.player.isInWater())) || (mc.player.isInLava())) || (mc.player.isSuppressingSlidingDownLadder())) || (mc.player.hasEffect(MobEffects.BLINDNESS))) {
                return;
            }
            onGroundAttack();
        }
    }

    private void ridingAttack(Entity arg0) {
        if (!(this.mode.getValue().equalsIgnoreCase("Packet"))) break label_1;
        int var2 = 0;
        while (var2 < 5) {
            mc.getConnection().send(new ServerboundAttackPacket(arg0.getId()));
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            var2++;
        }
        label_1:
    }

    private void onGroundAttack() {
        this.lastCritTime = System.currentTimeMillis();
        double var1 = mc.player.getX();
        double var3 = mc.player.getY();
        double var5 = mc.player.getZ();
        ProxyServerTickListener.allowSend(() -> {
    Object var7 = this.mode.getValue();
    int var8 = -1;
    switch (var7.hashCode()) {
        case -1911998296 -> { break label_0; }
        case 2228079 -> { break label_1; }
        case 1952270257 -> { break label_2; }
        default -> { break label_3; }
    }
    label_0:
    if (!(var7.equals("Packet"))) break label_3;
    var8 = 0;
    break label_3;
    label_1:
    if (!(var7.equals("Grim"))) break label_3;
    var8 = 1;
    break label_3;
    label_2:
    if (!(var7.equals("GrimNew"))) break label_3;
    var8 = 2;
    label_3:
    switch (var8) {
        case 0 -> { break label_4; }
        case 1 -> { break label_5; }
        case 2 -> { break label_6; }
        default -> { break label_7; }
    }
    label_4:
    packetCrit(arg0, arg1, arg2);
    break label_7;
    label_5:
    grimCrit(arg0, arg1, arg2);
    break label_7;
    label_6:
    grimNewCrit(arg0, arg1, arg2);
    label_7:
});
        ((ClientPlayerEntityAccessor) mc.player).setLastOnGround(false);
    }

    private void packetCrit(double arg0, double arg1, double arg2) {
        if (!(mc.player.onGround())) return;
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(arg0, (arg1 + 0.0625), arg2, false, mc.player.horizontalCollision));
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(arg0, arg1, arg2, false, mc.player.horizontalCollision));
    }

    private void grimCrit(double arg0, double arg1, double arg2) {
        if (!(mc.player.onGround())) return;
        if (this.onlyPhased.getValue() && !(isPhased(mc.player))) return;
        if (this.onlyStandingStill.getValue()) {
            if (((Math.abs(mc.player.getDeltaMovement().x) > 0.01) || (Math.abs(mc.player.getDeltaMovement().y) > 0.01)) || (Math.abs(mc.player.getDeltaMovement().z) > 0.01)) {
                return;
            }
        }
        if (this.onlyWhenHeadCovered.getValue()) {
            Object var8 = mc.player.blockPosition();
            if (mc.player.isVisuallyCrawling()) {
            } else {
            }
            var tmp0 = var8.above(2);
            Object var7 = tmp0;
            Object var9 = tmp0;
            if (mc.level.getBlockState(var7).isAir()) return;
        }
        var7 = LightningClient.ROTATION_MANAGER.getServerYaw();
        var8 = LightningClient.ROTATION_MANAGER.getServerPitch();
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(arg0, (arg1 + 0.0625), arg2, var7, var8, false, mc.player.horizontalCollision));
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(arg0, (arg1 + 0.0625013579), arg2, var7, var8, false, mc.player.horizontalCollision));
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(arg0, (arg1 + 1.3579E-6), arg2, var7, var8, false, mc.player.horizontalCollision));
    }

    private void grimNewCrit(double arg0, double arg1, double arg2) {
        if (!(mc.player.onGround())) return;
        if (this.onlyPhased.getValue()) {
            if ((!(isPhased(mc.player))) || (!(eyesPhased(mc.player)))) {
                return;
            }
        }
        if (this.onlyStandingStill.getValue()) {
            if (((Math.abs(mc.player.getDeltaMovement().x) > 0.01) || (Math.abs(mc.player.getDeltaMovement().y) > 0.01)) || (Math.abs(mc.player.getDeltaMovement().z) > 0.01)) {
                return;
            }
        }
        if (this.onlyWhenHeadCovered.getValue()) {
            Object var8 = mc.player.blockPosition();
            if (mc.player.isVisuallyCrawling()) {
            } else {
            }
            var tmp0 = var8.above(2);
            Object var7 = tmp0;
            Object var9 = tmp0;
            if (mc.level.getBlockState(var7).isAir()) return;
        }
        var7 = LightningClient.ROTATION_MANAGER.getServerYaw();
        var8 = LightningClient.ROTATION_MANAGER.getServerPitch();
        var9 = ((float) (((Math.random() * 2.0) - 1.0) * 0.0010000000474974513));
        float var10 = Mth.clamp((var8 + var9), -90.0f, 90.0f);
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(arg0, (arg1 + 0.0626), arg2, var7, var10, false, mc.player.horizontalCollision));
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(arg0, (arg1 + 0.0455), arg2, var7, var10, false, mc.player.horizontalCollision));
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

    private boolean eyesPhased(Player arg0) {
        Object var2 = arg0.getEyePosition();
        Object var3 = BlockPos.containing(var2);
        Object var4 = mc.level.getBlockState(var3);
        if (var4.isAir()) return false;
        Object var5 = var4.getCollisionShape(mc.level, var3);
        if (var5.isEmpty()) return false;
        Object var6 = var5.toAabbs().iterator();
        label_2:
        if (var6.hasNext()) {
            Object var7 = ((AABB) var6.next());
            if (!(var7.move(var3).contains(var2))) {
                break label_2;
            }
            return true;
        }
        return false;
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

}
