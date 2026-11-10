package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.ISprintModule;
import lightning.modules.impl.player.MultiTaskModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Phase", description = "Throws an ender pearl to phase into blocks. KingMC Crawl mode requires crawling away from the target block before throwing.", category = Module.Category.MOVEMENT)
public class PhaseModule extends Module {
    public ModeSetting logic;
    public ModeSetting swap;
    public BooleanSetting crawl;
    public BooleanSetting antiVoid;
    public BooleanSetting debug;
    public ModeSetting ncpMode;
    public ModeSetting bypassSwitch;
    public BooleanSetting autoRemove;
    private static final double CORNER_THRESHOLD = 0.5;
    private static final double CORNER_OFFSET = 0.5;
    private int attempt;

    public PhaseModule() {
        var tmp0 = new String[1];
        tmp0[0] = "PIP";
        this.logic = new ModeSetting("Logic", "Phase bypass targeting logic. PIP = one combined logic: KingMC standing/crawl targeting, automatic Grim/NCP fallback when the throw would land in the void, and the NCP bypass item step when the item is in your hotbar.", "PIP", tmp0);
        this.swap = new ModeSetting("Swap", "The mode that will be used for switching to the pearl before throwing it.", "Silent", InventoryUtils.SWITCH_MODES);
        this.crawl = new BooleanSetting("Crawl", "Crawl phase mode. In KingMC mode, crawl away from the target block before throwing.", false);
        this.antiVoid = new BooleanSetting("AntiVoid", "Prevents phasing into or through blocks that lead directly into the void.", true);
        this.debug = new BooleanSetting("Debug", "Chat-prints pos/yaw/pitch/target for every throw, so a failed (popped-up) attempt can be matched back to its exact numbers.", false);
        var tmp1 = new String[1];
        tmp1[0] = "PIP";
        var tmp2 = new String[3];
        tmp2[0] = "FireCharge";
        tmp2[1] = "FlintAndSteel";
        tmp2[2] = "Cobweb";
        this.ncpMode = new ModeSetting("NCP-Mode", "Item used to bypass pearl distance checks on NCP.", new ModeSetting.Visibility(this.logic, tmp1), "FireCharge", tmp2);
        var tmp3 = new String[1];
        tmp3[0] = "PIP";
        var tmp4 = new String[4];
        tmp4[0] = "Normal";
        tmp4[1] = "Silent";
        tmp4[2] = "AltPickup";
        tmp4[3] = "AltSwap";
        this.bypassSwitch = new ModeSetting("Switch", "The mode that will be used for switching to the bypass item.", new ModeSetting.Visibility(this.logic, tmp3), "Silent", tmp4);
        var tmp5 = new String[1];
        tmp5[0] = "PIP";
        this.autoRemove = new BooleanSetting("AutoRemove", "Automatically removes the bypass block/fire once you have phased.", new ModeSetting.Visibility(this.logic, tmp5), true);
        this.attempt = 0;
    }

    private boolean isInWeb(Entity arg0) {
        if (arg0.getInBlockState().is(Blocks.COBWEB)) return true;
        Object var2 = BlockPos.betweenClosed(BlockPos.containing(arg0.getBoundingBox().minX, arg0.getBoundingBox().minY, arg0.getBoundingBox().minZ), BlockPos.containing(arg0.getBoundingBox().maxX, arg0.getBoundingBox().maxY, arg0.getBoundingBox().maxZ)).iterator();
        label_1:
        if (var2.hasNext()) {
            Object var3 = ((BlockPos) var2.next());
            if (!(arg0.level().getBlockState(var3).is(Blocks.COBWEB))) {
                break label_1;
            }
            return true;
        }
        return false;
    }

    private boolean isInLadder(Entity arg0) {
        if (arg0.getInBlockState().is(Blocks.LADDER)) return true;
        Object var2 = BlockPos.betweenClosed(BlockPos.containing(arg0.getBoundingBox().minX, arg0.getBoundingBox().minY, arg0.getBoundingBox().minZ), BlockPos.containing(arg0.getBoundingBox().maxX, arg0.getBoundingBox().maxY, arg0.getBoundingBox().maxZ)).iterator();
        label_1:
        if (var2.hasNext()) {
            Object var3 = ((BlockPos) var2.next());
            if (!(arg0.level().getBlockState(var3).is(Blocks.LADDER))) {
                break label_1;
            }
            return true;
        }
        return false;
    }

    private boolean isPlayerCrawling() {
        if (mc.player == null) return false;
        if (mc.player.getPose() != Pose.SWIMMING && !(mc.player.isVisuallyCrawling())) {
            if (mc.player.getEyeHeight() <= 1.0f) {
            }
        } else {
        }
        return false;
    }

    public void onEnable() {
        if (mc.player != null) {
            if (mc.level != null) break label_1;
        }
        setToggled(false);
        return;
        label_1:
        var tmp0 = new BlockPos[2];
        tmp0[0] = mc.player.blockPosition();
        tmp0[1] = mc.player.blockPosition().below();
        Object var2 = tmp0;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            if (mc.level.getBlockState(var5).is(Blocks.SCAFFOLDING)) break label_3;
            break label_4;
            label_3:
            NetworkUtils.sendSequencedPacket((arg1) -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, arg0, Direction.UP, arg1));
            NetworkUtils.sendSequencedPacket((arg1) -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, arg0, Direction.UP, arg1));
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            mc.level.removeBlock(var5, false);
            label_4:
            var4++;
        }
        var2 = 1;
        var3 = isPlayerCrawling();
        if (!(this.crawl.getValue()) && !(!var3) && !var2) {
            setToggled(false);
            return;
        }
        if (!(this.crawl.getValue())) break label_7;
        var4 = (!(!var3) ? 1 : 0);
        if (!var4) break label_9;
        var5 = (!(!var3) ? 1 : 0);
        int var6 = isInWeb(mc.player);
        int var7 = isInLadder(mc.player);
        if (!var5 && !var6 && !var7 && !(mc.level.getBlockState(mc.player.blockPosition()).canBeReplaced())) {
            setToggled(false);
            return;
        }
        if (mc.player.isCrouching()) {
            setToggled(false);
            return;
        }
        if (mc.player.getCooldowns().isOnCooldown(new ItemStack(Items.ENDER_PEARL))) {
            setToggled(false);
            return;
        }
        Object var8 = ((MultiTaskModule) LightningClient.MODULE_MANAGER.getModule(MultiTaskModule.class));
        if (((var8 == null) || (!(var8.isToggled()))) || (!(var8.pearl.getValue()))) break label_14;
        int var9 = (mc.player.isUsingItem() ? 1 : 0);
        if (!(!var9)) {
        } else {
        }
        Object var10 = this.swap.getValue();
        if (!(var10.equalsIgnoreCase("AltSwap"))) {
            if (var10.equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var11 = InventoryUtils.find(0, 35, 8);
        int var12 = mc.player.getInventory().getSelectedSlot();
        int var13 = 0;
        if (var11 == -1) {
            LightningClient.CHAT_MANAGER.tagged("No pearls could be found in your hotbar.", getName());
            setToggled(false);
            return;
        }
        float var14 = mc.player.getYRot();
        float var15 = mc.player.getXRot();
        Object var16 = mc.player.blockPosition().below();
        float var17 = Mth.wrapDegrees(getPhaseYaw());
        float var18 = (((var17 % 90.0f) + 90.0f) % 90.0f);
        int var19 = (Math.min(var18, (90.0f - var18)) < 22.5f ? 1 : 0);
        int var20 = isOnBedrock();
        int var21 = this.logic.getValue().equalsIgnoreCase("Grim");
        Object var1 = pipTarget(var17, var4, var19, var20);
        if (this.antiVoid.getValue() && isVoidHazard(var1, var17)) {
            LightningClient.CHAT_MANAGER.tagged("Phase cancelled to prevent falling into the void.", getName());
            if (mc.getSoundManager() != null) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.5f));
            }
            setToggled(false);
            return;
        }
        float var22 = calcYaw(var1);
        float var23 = solvePitch(var1);
        if (this.debug.getValue()) {
            this.attempt = (this.attempt + 1);
            var tmp1 = new Object[12];
            tmp1[0] = this.attempt;
            tmp1[1] = this.logic.getValue();
            if (!(!var2) && !(!var20)) {
                break label_30;
            }
            if (!(!var2) && !(!var4)) {
                break label_30;
            }
            "/Bedrock"["/Crawl"] = (!(!var2) ? "/Standing" : "");
            2[3] = mc.player.getX();
            2[4] = mc.player.getY();
            2[5] = mc.player.getZ();
            2[6] = var22;
            2[7] = var23;
            2[8] = var1.x;
            2[9] = var1.y;
            2[10] = var1.z;
            2[11] = (!(!var19) ? "straight" : "corner");
            tmp1.tagged(String.format(tmp1, 2), getName());
        }
        if (!(mc.level.getBlockState(mc.player.blockPosition()).isAir()) && !var6) {
            if (!var7) break label_45;
        }
        if (mc.level.getBlockState(var16).canBeReplaced()) break label_45;
        Object var25 = this.ncpMode.getValue();
        int var26 = -1;
        switch (var25.hashCode()) {
            case 2023701054 -> { break label_36; }
            case 2041192093 -> { break label_35; }
            default -> { break label_37; }
        }
        label_35:
        if (!(var25.equals("FlintAndSteel"))) break label_37;
        var26 = 0;
        break label_37;
        label_36:
        if (!(var25.equals("Cobweb"))) break label_37;
        var26 = 1;
        label_37:
        switch (var26) {
            case 0 -> { break label_38; }
            case 1 -> { break label_39; }
            default -> { break label_40; }
        }
        label_38:
        break label_41;
        label_39:
        break label_41;
        label_40:
        label_41:
        Object var24 = Items.FIRE_CHARGE;
        if (!(this.bypassSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.bypassSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        var25 = InventoryUtils.find(InventoryUtils.HOTBAR_START, 35, 8);
        if (var25 == -1) break label_45;
        LightningClient.ROTATION_MANAGER.packetRotate(var22, 90.0f, true);
        InventoryUtils.switchSlot(this.bypassSwitch.getValue(), var25, var12);
        NetworkUtils.sendSequencedPacket((arg1) -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(arg0).add(0.0, 1.0, 0.0), Direction.UP, arg0, false), arg1));
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(this.bypassSwitch.getValue(), var25, var12);
        var13 = 1;
        label_45:
        LightningClient.ROTATION_MANAGER.packetRotate(var22, var23, true);
        if (InventoryUtils.switchSlot(var10, var11, var12)) {
            NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, arg2, arg0, arg1));
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            InventoryUtils.switchBack(var10, var11, var12);
        }
        if (!(!var13) && this.autoRemove.getValue()) {
            NetworkUtils.sendSequencedPacket((arg0) -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, mc.player.blockPosition(), Direction.UP, arg0));
            NetworkUtils.sendSequencedPacket((arg0) -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, mc.player.blockPosition(), Direction.UP, arg0));
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        setToggled(false);
    }

    private float getPhaseYaw() {
        if (mc.player == null) return 0.0f;
        float var2 = mc.player.getYRot();
        if (LightningClient.MODULE_MANAGER == null) break label_1;
        var tmp0 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
        Object var1 = tmp0;
        if (tmp0 != null && var1.isGrimCompensating()) return var1.getGrimYaw();
        label_1:
        int var3 = ((mc.options.keyRight.isDown() ? 1 : 0) - (mc.options.keyLeft.isDown() ? 1 : 0));
        int var4 = ((mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0));
        if (!var3) {
            if (!(!var4)) {
            }
            float var5 = ((float) Math.toDegrees(Math.atan2(((double) var3), ((double) var4))));
            return Mth.wrapDegrees((var2 + var5));
        }
        return var2;
    }

    private boolean isOnBedrock() {
        if ((mc.player == null) || (mc.level == null)) {
            return false;
        }
        double var1 = mc.player.getBoundingBox().minY;
        Object var3 = BlockPos.containing(mc.player.getX(), (var1 - 0.1), mc.player.getZ());
        if (mc.level.getBlockState(var3).is(Blocks.BEDROCK)) return true;
        if (mc.level.getBlockState(mc.player.blockPosition().below()).is(Blocks.BEDROCK)) return true;
        return mc.level.getBlockState(mc.player.blockPosition()).is(Blocks.BEDROCK);
    }

    private Vec3 calculateKingMCStandingTarget(float arg0) {
        double var2 = mc.player.getX();
        double var4 = mc.player.getY();
        double var6 = mc.player.getZ();
        double var8 = Math.toRadians(((double) arg0));
        Object var10 = new Vec3(-(Math.sin(var8)), 0.0, Math.cos(var8)).normalize();
        float var11 = (((arg0 % 90.0f) + 90.0f) % 90.0f);
        int var12 = (Math.min(var11, (90.0f - var11)) < 22.5f ? 1 : 0);
        double var13 = 0.165;
        if (!(!var12)) {
            var tmp0 = (Math.abs(var10.z) > Math.abs(var10.x) ? 1 : 0);
            int var15 = tmp0;
            int var16 = tmp0;
            if (!(!var15)) {
                if (var10.z > 0.0) {
                } else {
                }
                double var17 = Math.floor(var6);
                if (var10.z > 0.0) {
                } else {
                }
                double var19 = (var17 + 0.165);
                return new Vec3(var2, var4, var19);
            }
            if (var10.x > 0.0) {
            } else {
            }
            var17 = Math.floor(var2);
            if (var10.x > 0.0) {
            } else {
            }
            var19 = (var17 + 0.165);
            return new Vec3(var19, var4, var6);
        }
        if (var10.x > 0.0) {
        } else {
        }
        var15 = Math.floor(var2);
        if (var10.z > 0.0) {
        } else {
        }
        var17 = Math.floor(var6);
        if (var10.x > 0.0) {
        } else {
        }
        var19 = (var15 + 0.165);
        if (var10.z > 0.0) {
        } else {
        }
        double var21 = (var17 + 0.165);
        return new Vec3(var19, var4, var21);
    }

    private Vec3 calculateKingMCCrawlTarget(float arg0) {
        Object var12 = getEffectiveEyePos();
        var tmp0 = mc.player.getYRot();
        float var8 = tmp0;
        double var9 = Math.toRadians(((double) tmp0));
        var tmp1 = new Vec3(-(Math.sin(Math.toRadians(((double) tmp0)))), 0.0, Math.cos(var9)).normalize();
        Object var11 = tmp1;
        Object var13 = mc.level.clip(new ClipContext(var12, var12.add(tmp1.scale(4.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var13 != null && var13.getType() == HitResult.Type.BLOCK) {
            arg0 = var8;
            Object var7 = var11;
            break label_1;
        }
        double var14 = Math.toRadians(((double) arg0));
        var7 = new Vec3(-(Math.sin(var14)), 0.0, Math.cos(var14)).normalize();
        var13 = mc.level.clip(new ClipContext(var12, var12.add(var7.scale(4.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        label_1:
        var14 = null;
        Object var15 = null;
        Object var16 = null;
        if (var13 != null && var13.getType() == HitResult.Type.BLOCK) {
            Object var17 = var13;
            var14 = var17.getBlockPos();
            var15 = var17.getDirection();
            var16 = var17.getLocation();
        } else {
            var17 = Direction.fromYRot(((double) arg0));
            var14 = mc.player.blockPosition().relative(var17, 1);
            var15 = var17.getOpposite();
        }
        var17 = (((double) var14.getY()) + 0.52);
        float var19 = (((arg0 % 90.0f) + 90.0f) % 90.0f);
        var tmp2 = (Math.min(var19, (90.0f - var19)) < 22.5f ? 1 : 0);
        int var6 = tmp2;
        int var20 = tmp2;
        if (!(!var6) && var15 != null && var15.getAxis().isHorizontal()) {
            if (var15.getAxis() == Direction.Axis.Z) {
                if (var15 == Direction.NORTH) {
                } else {
                }
                double var23 = (((double) var14.getZ()) + 1.0);
                double var25 = (((double) var14.getZ()) + 1.0);
                if (var16 != null && Math.abs((var16.x - ((double) Math.round(var16.x)))) < 0.25) {
                } else {
                }
                double var21 = (((double) var14.getX()) + 0.5);
            } else {
                if (var15 == Direction.WEST) {
                } else {
                }
                var21 = (((double) var14.getX()) + 1.0);
                if (var16 != null && Math.abs((var16.z - ((double) Math.round(var16.z)))) < 0.25) {
                } else {
                }
                var23 = (((double) var14.getZ()) + 0.5);
            }
            return new Vec3(var21, var17, var23);
        }
        if (var16 != null) {
            double var4 = ((double) Math.round(var16.x));
            double var2 = ((double) Math.round(var16.z));
        } else {
            var4 = ((double) Math.round((mc.player.getX() + var7.x)));
            var2 = ((double) Math.round((mc.player.getZ() + var7.z)));
        }
        return new Vec3(var4, var17, var2);
    }

    private Vec3 calculateTargetPos() {
        double var1 = mc.player.getX();
        double var3 = mc.player.getZ();
        if (this.crawl.getValue() && isPlayerCrawling()) {
        } else {
        }
        double var5 = (mc.player.getY() - 0.5);
        double var7 = ((double) Math.round(var1));
        double var9 = ((double) Math.round(var3));
        double var11 = (var7 - var1);
        double var13 = (var9 - var3);
        if (Math.abs(var11) <= 0.5 && Math.abs(var13) <= 0.5) return new Vec3((var1 + Mth.clamp(var11, -0.5, 0.5)), var5, (var3 + Mth.clamp(var13, -0.5, 0.5)));
        double var15 = 0.241660973353061;
        double var17 = 0.7853981633974483;
        double var19 = (var1 + Mth.clamp((toClosest(var1, (Math.floor(var1) + 0.241660973353061), (Math.floor(var1) + 0.7853981633974483)) - var1), -0.2, 0.2));
        double var21 = (var3 + Mth.clamp((toClosest(var3, (Math.floor(var3) + 0.241660973353061), (Math.floor(var3) + 0.7853981633974483)) - var3), -0.2, 0.2));
        return new Vec3(var19, var5, var21);
    }

    private double toClosest(double arg0, double arg1, double arg2) {
        return ((arg0 - arg1) > (arg2 - arg0) ? arg2 : arg1);
    }

    private Vec3 calculateGrimSectorTarget() {
        double var3 = mc.player.getX();
        double var5 = mc.player.getZ();
        if (this.crawl.getValue() && isPlayerCrawling()) {
            break label_1;
        }
        label_1:
        double var7 = (mc.player.getY() - 0.5);
        double var9 = Math.floor(var3);
        double var11 = Math.floor(var5);
        double var13 = (var11 + 0.5);
        double var1 = (var9 + 0.5);
        double var15 = Math.toDegrees(Math.atan2((var5 - var13), (var3 - (var9 + 0.5))));
        if (var15 < 0.0) {
            var15 = (var15 + 360.0);
        }
        int var17 = (((int) Math.floor(((var15 + 22.5) / 45.0))) & 7);
        double var18 = (var9 + 1.0);
        double var20 = (var11 + 1.0);
        switch (var17) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            case 2 -> { break label_5; }
            case 3 -> { break label_6; }
            case 4 -> { break label_7; }
            case 5 -> { break label_8; }
            case 6 -> { break label_9; }
            default -> { break label_10; }
        }
        label_3:
        break label_11;
        label_4:
        break label_11;
        label_5:
        break label_11;
        label_6:
        break label_11;
        label_7:
        break label_11;
        label_8:
        break label_11;
        label_9:
        break label_11;
        label_10:
        label_11:
        return new Vec3(var18, var7, var11);
    }

    private Vec3 boundaryTarget(float arg0) {
        double var3 = mc.player.getX();
        double var5 = mc.player.getZ();
        if (this.crawl.getValue() && isPlayerCrawling()) {
        } else {
        }
        double var7 = (mc.player.getY() - 0.5);
        double var9 = Math.toRadians(((double) arg0));
        Object var11 = new Vec3(-(Math.sin(var9)), 0.0, Math.cos(var9)).normalize();
        var tmp0 = (Math.abs(var11.z) > Math.abs(var11.x) ? 1 : 0);
        int var2 = tmp0;
        int var12 = tmp0;
        if (!(!var2)) {
            if (var11.z > 0.0) {
            } else {
            }
            double var13 = Math.floor(var5);
            return new Vec3(var3, var7, var13);
        }
        if (var11.x > 0.0) {
        } else {
        }
        var13 = Math.floor(var3);
        return new Vec3(var13, var7, var5);
    }

    private Vec3 getEffectiveEyePos() {
        if (mc.player == null) return Vec3.ZERO;
        double var1 = (mc.player.getEyeY() - 0.1);
        return new Vec3(mc.player.getX(), var1, mc.player.getZ());
    }

    private float calcYaw(Vec3 arg0) {
        Object var2 = getEffectiveEyePos();
        Object var3 = arg0.subtract(var2);
        return ((float) Math.toDegrees(Math.atan2(-(var3.x), var3.z)));
    }

    private float solvePitch(Vec3 arg0) {
        Object var2 = getEffectiveEyePos();
        double var3 = Math.hypot((arg0.x - var2.x), (arg0.z - var2.z));
        double var5 = (arg0.y - var2.y);
        if (var3 < 1.0E-4) return (var5 < 0.0 ? 85.0f : -85.0f);
        double var7 = -85.0;
        double var9 = 89.9;
        int var11 = 0;
        while (var11 < 30) {
            double var12 = ((var7 + var9) * 0.5);
            double var14 = simulatePearlYAtDist(var12, var3);
            if (var14 < var5) {
                var9 = var12;
            } else {
                var7 = var12;
            }
            var11++;
        }
        return ((float) ((var7 + var9) * 0.5));
    }

    private static double simulatePearlYAtDist(double arg0, double arg1) {
        double var4 = Math.toRadians(arg0);
        double var6 = (1.5 * Math.cos(var4));
        double var8 = (-1.5 * Math.sin(var4));
        double var10 = 0.0;
        double var12 = 0.0;
        int var14 = 0;
        while (var14 < 100) {
            double var15 = var10;
            double var17 = var12;
            var10 = (var10 + var6);
            var12 = (var12 + var8);
            if (var10 >= arg1) {
                double var19 = ((arg1 - var15) / (var10 - var15));
                return (var17 + ((var12 - var17) * var19));
            }
            var6 = (var6 * 0.99);
            var8 = ((var8 * 0.99) - 0.03);
            var14++;
        }
        return var12;
    }

    private boolean isFloorVoid(double arg0, double arg1, double arg2) {
        if (mc.level == null) return false;
        int var7 = mc.level.getMinY();
        int var8 = ((int) Math.floor(arg1));
        if (var8 < var7) return true;
        int var9 = var8;
        while (var9 >= var7) {
            Object var10 = BlockPos.containing(arg0, ((double) var9), arg2);
            Object var11 = mc.level.getBlockState(var10);
            if (var11.isAir()) break label_4;
            if (var11.getCollisionShape(mc.level, var10).isEmpty()) {
                break label_4;
            }
            return false;
            label_4:
            var9--;
        }
        return true;
    }

    private boolean isVoidHazard(Vec3 arg0, float arg1) {
        if ((mc.player == null) || (mc.level == null)) {
            return false;
        }
        double var3 = mc.player.getX();
        double var5 = mc.player.getY();
        double var7 = mc.player.getZ();
        int var9 = mc.level.getMinY();
        if (var5 < ((double) var9)) return true;
        double var10 = Math.toRadians(((double) arg1));
        Object var12 = new Vec3(-(Math.sin(var10)), 0.0, Math.cos(var10)).normalize();
        Object var13 = BlockPos.containing(arg0.x, arg0.y, arg0.z);
        Object var14 = BlockPos.containing((var3 + (var12.x * 0.5)), var5, (var7 + (var12.z * 0.5)));
        int var15 = (!(isFloorVoid((((double) var13.getX()) + 0.5), arg0.y, (((double) var13.getZ()) + 0.5))) ? 1 : 0);
        int var16 = (!(isFloorVoid((((double) var14.getX()) + 0.5), var5, (((double) var14.getZ()) + 0.5))) ? 1 : 0);
        if (var15) break label_7;
        return !var16;
    }

    private Vec3 pipTarget(float arg0, boolean arg1, boolean arg2, boolean arg3) {
        if (!arg3) {
            if (!(!arg1)) {
                Object var5 = calculateKingMCCrawlTarget(arg0);
                break label_3;
            }
            var5 = calculateKingMCStandingTarget(arg0);
            break label_3;
        }
        if (!(!arg2)) {
            var5 = boundaryTarget(arg0);
            break label_3;
        }
        var5 = calculateTargetPos();
        label_3:
        if (this.antiVoid.getValue() && isVoidHazard(var5, arg0)) {
            Object var6 = calculateGrimSectorTarget();
            if (!(isVoidHazard(var6, arg0))) return var6;
            if (!(!arg2)) {
                Object var7 = boundaryTarget(arg0);
            } else {
                var7 = calculateTargetPos();
            }
            if (!(isVoidHazard(var7, arg0))) return var7;
        }
        return var5;
    }

}
