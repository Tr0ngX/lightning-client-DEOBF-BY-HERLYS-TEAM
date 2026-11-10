package lightning.utils.minecraft;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lightning.LightningClient;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.impl.combat.CrystalPlacementHelper;
import lightning.modules.impl.core.RotationsModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.S2CBlockRenderPacket;
import lightning.utils.IMinecraft;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WorldUtils implements IMinecraft {
    public static Set RIGHT_CLICKABLE_BLOCKS;
    private static final ItemStack NETHERITE_PICKAXE;
    private static final Map pendingAirPlaces;
    private static final Map predictedPlacements;
    private static final long PREDICTION_TIMEOUT_MS = 1000L;
    public static volatile long lastInteractablePlaceTime;
    private static final String[] FACING_NAMES;
    private static final String[] FACING_AXES;

    public static boolean isInteractable(BlockState arg0) {
        if ((arg0 == null) || (arg0.isAir())) {
            return false;
        }
        Object var1 = arg0.getBlock();
        if (RIGHT_CLICKABLE_BLOCKS.contains(var1)) return true;
        if (!(var1 instanceof BaseEntityBlock) && !(var1 instanceof ShulkerBoxBlock) && !(var1 instanceof ChestBlock) && !(var1 instanceof EnderChestBlock) && !(var1 instanceof AnvilBlock) && !(var1 instanceof CraftingTableBlock) && !(var1 instanceof DoorBlock) && !(var1 instanceof TrapDoorBlock) && !(var1 instanceof FenceGateBlock) && !(var1 instanceof ButtonBlock) && !(var1 instanceof LeverBlock) && !(var1 instanceof AbstractFurnaceBlock) && !(var1 instanceof BedBlock) && !(var1 instanceof SignBlock) && !(var1 instanceof WallSignBlock) && !(var1 instanceof CrafterBlock) && !(var1 instanceof RespawnAnchorBlock)) {
            if ((var1 instanceof BarrelBlock)) {
            }
        } else {
        }
        return false;
    }

    public static boolean placeBlock(BlockPos arg0, Direction arg1, InteractionHand arg2, boolean arg3, boolean arg4) {
        return placeBlock(arg0, arg1, arg2, (!(!arg3) ? "Normal" : "None"), arg4, false);
    }

    public static boolean placeBlock(BlockPos arg0, Direction arg1, InteractionHand arg2, boolean arg3, boolean arg4, boolean arg5) {
        return placeBlock(arg0, arg1, arg2, null, (!(!arg3) ? "Normal" : "None"), arg4, arg5);
    }

    public static boolean placeBlock(BlockPos arg0, Direction arg1, InteractionHand arg2, Runnable arg3, boolean arg4, boolean arg5, boolean arg6) {
        return placeBlock(arg0, arg1, arg2, arg3, (!(!arg4) ? "Normal" : "None"), arg5, arg6);
    }

    public static boolean placeBlock(BlockPos arg0, Direction arg1, InteractionHand arg2, String arg3, boolean arg4) {
        return placeBlock(arg0, arg1, arg2, arg3, arg4, false);
    }

    public static boolean placeBlock(BlockPos arg0, Direction arg1, InteractionHand arg2, String arg3, boolean arg4, boolean arg5) {
        return placeBlock(arg0, arg1, arg2, null, arg3, arg4, arg5);
    }

    public static void predictPlacement(BlockPos arg0, Block arg1) {
        if ((arg0 == null) || (arg1 == null)) {
            return;
        }
        long var2 = System.currentTimeMillis();
        if (predictedPlacements.size() > 256) {
            predictedPlacements.entrySet().removeIf((arg1) -> (arg0 - ((WorldUtils.PredictedPlacement) arg1.getValue()).time()) >= 1000L);
        }
        predictedPlacements.put(arg0.immutable(), new WorldUtils.PredictedPlacement(arg1, var2));
    }

    public static void clearPredictedPlacement(BlockPos arg0) {
        if (arg0 != null) {
            predictedPlacements.remove(arg0);
        }
    }

    public static void clearPredictedPlacements() {
        predictedPlacements.clear();
    }

    public static BlockState getPredictedCollisionState(BlockPos arg0) {
        if (predictedPlacements.isEmpty()) return null;
        Object var1 = ((WorldUtils.PredictedPlacement) predictedPlacements.get(arg0));
        if (var1 == null) return null;
        if ((System.currentTimeMillis() - var1.time()) >= 1000L) {
            predictedPlacements.remove(arg0);
            return null;
        }
        return var1.block().defaultBlockState();
    }

    public static void airPlaceBlock(BlockPos arg0, InteractionHand arg1, String arg2, boolean arg3) {
        if ((((arg0 == null) || (mc.player == null)) || (mc.level == null)) || (mc.getConnection() == null)) {
            return;
        }
        long var6 = System.currentTimeMillis();
        long var8 = Math.max(50L, (((long) LightningClient.SERVER_MANAGER.getPing()) * 2L));
        Object var10 = ((Long) pendingAirPlaces.get(arg0));
        if (var10 != null) {
            if ((var6 - var10.longValue()) < var8) return;
            pendingAirPlaces.remove(arg0);
        }
        if (pendingAirPlaces.size() > 256) {
            pendingAirPlaces.entrySet().removeIf((arg2) -> (arg0 - ((Long) arg2.getValue()).longValue()) >= arg1);
        }
        pendingAirPlaces.put(arg0, var6);
        Object var11 = Vec3.atCenterOf(arg0).add(0.0, 0.5, 0.0);
        if ((arg2 == null) || (arg2.equalsIgnoreCase("None"))) break label_5;
        var tmp0 = (!(arg2.equalsIgnoreCase("false")) ? 1 : 0);
        int var5 = tmp0;
        int var12 = tmp0;
        if (!var5) break label_8;
        Object var13 = RotationUtils.getRotations(var11);
        if (LightningClient.ROTATION_MANAGER.isBatchingRotations()) {
            LightningClient.ROTATION_MANAGER.batchRotate(var13[0], var13[1]);
            break label_8;
        }
        LightningClient.ROTATION_MANAGER.silentRotate(var13[0], var13[1]);
        label_8:
        if (!arg3) break label_9;
        var tmp1 = new RenderPosition(arg0);
        Object var4 = tmp1;
        if (LightningClient.RENDER_MANAGER.renderPositions.contains(tmp1)) break label_9;
        LightningClient.RENDER_MANAGER.renderPositions.add(var4);
        label_9:
        var13 = new BlockHitResult(var11, Direction.UP, arg0, false);
        NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemOnPacket(arg0, arg1, arg2));
        mc.getConnection().send(new ServerboundSwingPacket(arg1));
    }

    public static boolean placeBlock(BlockPos arg0, Direction arg1, InteractionHand arg2, Runnable arg3, String arg4, boolean arg5, boolean arg6) {
        if ((arg0 == null) || (arg1 == null)) {
            return false;
        }
        Object var13 = arg0.relative(arg1);
        if (mc.level == null) break label_2;
        if (mc.player == null) {
            label_2:
            return false;
        }
        if (!(isInteractable(mc.level.getBlockState(var13)))) {
            if (isInteractable(mc.level.getBlockState(arg0))) {
            }
        } else {
        }
        int var12 = 0;
        int var14 = 0;
        if (!(!var12)) {
            lastInteractablePlaceTime = System.currentTimeMillis();
        }
        Object var15 = Vec3.atCenterOf(var13).add((((double) -(arg1.getStepX())) * 0.45), (((double) -(arg1.getStepY())) * 0.45), (((double) -(arg1.getStepZ())) * 0.45));
        LightningClient.WORLD_MANAGER.reservePlacement(arg0);
        float var16 = LightningClient.ROTATION_MANAGER.getServerYaw();
        float var17 = LightningClient.ROTATION_MANAGER.getServerPitch();
        float var18 = ((float) RotationUtils.getYRotToVec(mc.player, var15));
        float var19 = ((float) RotationUtils.getXRotToVec(mc.player, var15));
        if ((arg4 == null) || (arg4.equalsIgnoreCase("None"))) break label_8;
        var tmp0 = (!(arg4.equalsIgnoreCase("false")) ? 1 : 0);
        int var11 = tmp0;
        int var20 = tmp0;
        if (!var11) break label_11;
        if (LightningClient.ROTATION_MANAGER.isBatchingRotations()) {
            LightningClient.ROTATION_MANAGER.batchRotate(var18, var19);
            break label_11;
        }
        LightningClient.ROTATION_MANAGER.silentRotate(var18, var19);
        label_11:
        if (!(!arg5)) {
            Object var21 = arg1;
            Object var22 = () -> placeBlock(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
            if (destroyCrystals(arg0, mc.player.getEyePosition(), var15, var22, arg4)) return false;
        }
        if (arg3 != null) {
            arg3.run();
        }
        if (!(isInteractable(mc.level.getBlockState(var13)))) {
            if (!(isInteractable(mc.level.getBlockState(arg0)))) break label_15;
        }
        var tmp1 = (!(mc.player.isShiftKeyDown()) ? 1 : 0);
        int var10 = tmp1;
        var21 = tmp1;
        if (!var10) break label_19;
        if (mc.player.input != null) {
        } else {
        }
        var22 = new Input(false, false, false, false, false, false, false);
        Object var23 = new Input(var22.forward(), var22.backward(), var22.left(), var22.right(), var22.jump(), true, var22.sprint());
        mc.getConnection().send(new ServerboundPlayerInputPacket(var23));
        Object var9 = mc.player;
        if (!((var9 instanceof ClientPlayerEntityAccessor))) break label_19;
        Object var8 = ((ClientPlayerEntityAccessor) var9);
        var8.setLastSentInput(var23);
        label_19:
        var22 = new BlockHitResult(var15, arg1.getOpposite(), var13, false);
        NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemOnPacket(arg0, arg1, arg2));
        mc.getConnection().send(new ServerboundSwingPacket(arg2));
        if (!(!var11) && !(LightningClient.ROTATION_MANAGER.isBatchingRotations()) && ((RotationsModule) LightningClient.MODULE_MANAGER.getModule(RotationsModule.class)).snapBack.getValue()) {
            LightningClient.ROTATION_MANAGER.packetRotate(var16, var17);
        }
        LightningClient.WORLD_MANAGER.getPlaceTimer().reset();
        if (!var10) break label_23;
        if (mc.player.input != null) {
        } else {
        }
        var23 = new Input(false, false, false, false, false, false, false);
        mc.getConnection().send(new ServerboundPlayerInputPacket(var23));
        var9 = mc.player;
        if (!((var9 instanceof ClientPlayerEntityAccessor))) break label_23;
        var8 = ((ClientPlayerEntityAccessor) var9);
        var8.setLastSentInput(var23);
        label_23:
        if (!arg6) break label_26;
        var tmp2 = new RenderPosition(arg0);
        Object var7 = tmp2;
        if (LightningClient.RENDER_MANAGER.renderPositions.contains(tmp2)) break label_26;
        LightningClient.RENDER_MANAGER.renderPositions.add(var7);
        if ((((!PingBypassFlags.proxyForwardingActive) || (LightningClient.PINGBYPASS_CONFIG == null)) || (!(LightningClient.PINGBYPASS_CONFIG.isServer()))) || (LightningClient.PROXY_SERVER == null)) break label_26;
        var23 = new ClientboundCustomPayloadPacket(PbCustomPayload.fromPacket(new S2CBlockRenderPacket(arg0)));
        Object var24 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var24.hasNext()) {
            Object var25 = ((Connection) var24.next());
            if (!(var25.isConnected())) {
                break label_24;
            }
            var25.send(var23);
        }
        label_26:
        return true;
    }

    public static boolean isPlaceable(BlockPos arg0) {
        return isPlaceable(arg0, false);
    }

    public static boolean isPlaceable(BlockPos arg0, boolean arg1) {
        if ((mc.level == null) || (mc.player == null)) {
            return false;
        }
        if (mc.level.isOutsideBuildHeight(arg0)) return false;
        if (!(mc.level.getBlockState(arg0).canBeReplaced())) return false;
        int var3 = (((double) arg0.getY()) < Math.floor((mc.player.getBoundingBox().minY + 0.01)) ? 1 : 0);
        int var4 = new AABB(arg0).contains(mc.player.position().x, (mc.player.getBoundingBox().minY + 0.1), mc.player.position().z);
        if (var3) break label_6;
        int var5 = (arg0.getY() == ((int) Math.floor(mc.player.getBoundingBox().minY)) ? 1 : 0);
        double var6 = (!(!var5) ? 0.05 : 0.25);
        if ((arg1) || (var3)) break label_11;
        if (!var4) {
            if ((EntityUtils.isPhased(mc.player)) || (!(mc.player.getBoundingBox().deflate(var6).intersects(new AABB(arg0))))) break label_11;
        }
        break label_12;
        label_11:
        label_12:
        int var2 = 0;
        int var8 = 0;
        if (!(!var2)) return false;
        return mc.level.getEntities(((Entity) null), new AABB(arg0), (arg0) -> true).stream().noneMatch((arg3) -> {
    if (((((arg3 instanceof EndCrystal)) || ((arg3 instanceof ExperienceOrb))) || ((arg3 instanceof ItemEntity))) || ((arg3 instanceof AbstractArrow))) break label_1;
    if (arg3.equals(mc.player)) {
        if (((arg0) || (arg1)) || (!arg2)) break label_1;
    }
    return !(EntityUtils.isGhost(arg3));
});
    }

    public static boolean isCrystalPlaceable(BlockPos arg0) {
        if (mc.level == null) return false;
        if (!(mc.level.getBlockState(arg0).canBeReplaced())) return false;
        return mc.level.getEntities(((Entity) null), new AABB(arg0), (arg0) -> true).stream().noneMatch((arg0) -> {
    if (((((arg0 instanceof EndCrystal)) || ((arg0 instanceof ExperienceOrb))) || ((arg0 instanceof ItemEntity))) || ((arg0 instanceof AbstractArrow))) break label_0;
    return !(EntityUtils.isGhost(arg0));
});
    }

    public static boolean isInstantBreakable(BlockPos arg0) {
        if (mc.level == null) return false;
        Object var1 = mc.level.getBlockState(arg0);
        if ((var1.isAir()) || (var1.canBeReplaced())) {
            return false;
        }
        return var1.getDestroySpeed(mc.level, arg0) == 0.0f;
    }

    public static void instantBreak(BlockPos arg0, Direction arg1) {
        if ((mc.level == null) || (mc.player == null)) {
            return;
        }
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, arg0, (arg1 != null ? arg1 : Direction.UP)));
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, arg0, (arg1 != null ? arg1 : Direction.UP)));
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
    }

    public static Direction getDirection(BlockPos arg0) {
        return getDirection(arg0, null, false);
    }

    public static Direction getDirection(BlockPos arg0, boolean arg1) {
        return getDirection(arg0, null, arg1);
    }

    public static BlockPos findSupportBlock(BlockPos arg0, List arg1, boolean arg2, int arg3) {
        if (((mc.level == null) || (mc.player == null)) || (arg0 == null)) {
            return null;
        }
        int var4 = 1;
        while (var4 <= arg3) {
            Object var5 = Direction.values();
            int var6 = var5.length;
            int var7 = 0;
            while (var7 < var6) {
                Object var8 = var5[var7];
                Object var10 = arg0.relative(var8, var4);
                if ((!(mc.player.isWithinBlockInteractionRange(var10, 0.0))) || (!(isPlaceable(var10, false)))) break label_5;
                var tmp0 = getDirection(var10, arg1, arg2);
                Object var9 = tmp0;
                if (tmp0 == null) {
                    break label_5;
                }
                return var10;
                label_5:
                var7++;
            }
            var4++;
        }
        return null;
    }

    private static boolean isVisibleBlock(BlockPos arg0, BlockPos arg1, BlockPos arg2) {
        if (arg0 == null) return false;
        if (!(arg0.equals(arg1))) {
            if (arg0.equals(arg2)) {
            }
        } else {
        }
        return false;
    }

    public static List getDirections(BlockPos arg0, List arg1, boolean arg2) {
        if ((mc.level == null) || (mc.player == null)) {
            return Collections.emptyList();
        }
        Object var3 = mc.player.getEyePosition();
        Object var4 = new ArrayList();
        Object var5 = new ArrayList();
        Object var6 = Direction.values();
        int var7 = var6.length;
        int var8 = 0;
        while (var8 < var7) {
            Object var9 = var6[var8];
            Object var10 = arg0.relative(var9);
            Object var11 = mc.level.getBlockState(var10);
            if (var11.canBeReplaced()) {
                if ((arg1 == null) || (!(arg1.contains(var10)))) break label_19;
            }
            if (!(var11.getFluidState().isEmpty())) break label_19;
            if (!(mc.player.isWithinBlockInteractionRange(var10, 0.0))) {
                break label_19;
            }
            Object var12 = Vec3.atCenterOf(arg0).relative(var9, 0.5);
            if (!arg2) break label_16;
            if (var9 == Direction.DOWN) {
                if (var3.y >= (((double) var10.getY()) + 1.0)) break label_7;
                break label_19;
            }
            if (var9 == Direction.UP) {
                if (var3.y <= ((double) var10.getY())) break label_7;
                break label_19;
            }
            var tmp0 = getStrictDirections(var3, Vec3.atCenterOf(var10));
            Object var13 = tmp0;
            if (tmp0.contains(var9.getOpposite())) break label_7;
            break label_19;
            label_7:
            Object var14 = var9.getOpposite();
            var tmp1 = new int[3];
            tmp1[0] = var14.getStepX();
            tmp1[1] = var14.getStepY();
            tmp1[2] = var14.getStepZ();
            Object var15 = tmp1;
            var tmp2 = new double[][5];
            var tmp3 = new double[2];
            tmp3[0] = 0.5;
            tmp3[1] = 0.5;
            tmp2[0] = tmp3;
            var tmp4 = new double[2];
            tmp4[0] = 0.25;
            tmp4[1] = 0.5;
            tmp2[1] = tmp4;
            var tmp5 = new double[2];
            tmp5[0] = 0.75;
            tmp5[1] = 0.5;
            tmp2[2] = tmp5;
            var tmp6 = new double[2];
            tmp6[0] = 0.5;
            tmp6[1] = 0.25;
            tmp2[3] = tmp6;
            var tmp7 = new double[2];
            tmp7[0] = 0.5;
            tmp7[1] = 0.75;
            tmp2[4] = tmp7;
            Object var16 = tmp2;
            int var17 = 0;
            Object var18 = var16;
            int var19 = var18.length;
            int var20 = 0;
            while (var20 < var19) {
                Object var21 = var18[var20];
                var tmp8 = new double[3];
                tmp8[0] = ((double) var10.getX());
                tmp8[1] = ((double) var10.getY());
                tmp8[2] = ((double) var10.getZ());
                Object var23 = tmp8;
                int var24 = 0;
                int var25 = 0;
                while (var25 < 3) {
                    int var26 = var25;
                    if (var15[var25]) {
                    } else {
                        var24++;
                    }
                    var26[var23[var26]] = ((0.5 + (((double) var15[var25]) * 0.48)) + var21[var24]);
                    var25++;
                }
                var25 = mc.level.clip(new ClipContext(var3, new Vec3(var23[0], var23[1], var23[2]), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
                if ((var25.getType() != HitResult.Type.BLOCK) || (!((var25 instanceof BlockHitResult)))) break label_14;
                Object var22 = var25;
                if (!(var25.getBlockPos().equals(var10))) break label_14;
                if (var22.getDirection() != var14) {
                    break label_14;
                }
                var17 = 1;
                break label_15;
                label_14:
                var20++;
            }
            label_15:
            if (var17) break label_16;
            break label_19;
            label_16:
            var13 = var3.distanceToSqr(var12);
            if (RIGHT_CLICKABLE_BLOCKS.contains(var11.getBlock())) {
                var13 = (var13 + 20.0);
            }
            if (var9 == Direction.DOWN) {
                var13 = (var13 - 50.0);
            }
            var4.add(new WorldUtils.DirectionCandidate(var9, var13));
            label_19:
            var8++;
        }
        var4.sort(Comparator.comparingDouble((arg0) -> arg0.distSq));
        var6 = new ArrayList();
        var7 = var4.iterator();
        while (var7.hasNext()) {
            var8 = ((WorldUtils.DirectionCandidate) var7.next());
            var6.add(var8.direction);
        }
        return var6;
    }

    public static Direction getDirection(BlockPos arg0, List arg1, boolean arg2) {
        Object var3 = getDirections(arg0, arg1, arg2);
        if (var3.isEmpty()) {
        } else {
        }
        return ((Direction) var3.get(0));
    }

    public static boolean destroyCrystals(BlockPos arg0) {
        return destroyCrystals(arg0, null, null, null, "None");
    }

    public static boolean destroyCrystals(BlockPos arg0, Vec3 arg1, Vec3 arg2, Runnable arg3, String arg4) {
        if (arg1 != null && arg2 != null) {
            break label_1;
        }
        label_1:
        Object var5 = new AABB(arg0);
        try {
            Object var6 = mc.level.getEntities(((Entity) null), var5, (arg3) -> {
    if (!((arg3 instanceof EndCrystal))) break label_1;
    if (arg0 != null && arg1 != null && !(new AABB(arg2).intersects(arg3.getBoundingBox()))) {
        if (!(arg3.getBoundingBox().clip(arg0, arg1).isPresent())) break label_1;
    }
    break label_2;
    label_1:
    label_2:
    return false;
});
        } catch (ConcurrentModificationException ex4) {
            Object var7 = ex4;
            return false;
        }
        if (var6.isEmpty()) return false;
        if ((arg4 == null) || (arg4.equalsIgnoreCase("None"))) break label_7;
        var7 = (!(arg4.equalsIgnoreCase("false")) ? 1 : 0);
        Object var8 = var6.iterator();
        while (var8.hasNext()) {
            Object var9 = ((Entity) var8.next());
            if (((!(var9.isAlive())) || (var9.isRemoved())) || (mc.player.getEyePosition().distanceToSqr(var9.position()) > 36.0)) break label_11;
            if (!(!var7)) {
                Object var10 = RotationUtils.getRotations(var9.getBoundingBox().getCenter());
                LightningClient.ROTATION_MANAGER.silentRotate(var10[0], var10[1]);
            }
            mc.player.connection.send(new ServerboundAttackPacket(var9.getId()));
            mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            if (arg3 == null) break label_11;
            LightningClient.WORLD_MANAGER.onCrystalAttacked(var9.getId(), arg3);
            label_11:
        }
        return true;
    }

    public static Vec3 getHitVector(BlockPos arg0, Direction arg1) {
        return Vec3.atCenterOf(arg0).add((((double) arg1.getStepX()) / 2.0), (((double) arg1.getStepY()) / 2.0), (((double) arg1.getStepZ()) / 2.0));
    }

    public static Direction getClosestDirection(BlockPos arg0, boolean arg1) {
        if (!(!arg1)) {
            if (mc.player.getY() >= ((double) arg0.getY())) return Direction.UP;
            Object var2 = mc.level.clip(new ClipContext(mc.player.getEyePosition(), Vec3.atCenterOf(arg0), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
            if (((var2 == null) || (var2.getType() != HitResult.Type.BLOCK)) || (var2.getDirection() == null)) {
                return getClosestDirection(arg0);
            }
            return var2.getDirection();
        }
        return getClosestDirection(arg0);
    }

    private static Direction getClosestDirection(BlockPos arg0) {
        Object var1 = null;
        Object var2 = null;
        Object var3 = Direction.values();
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Object var6 = var3[var5];
            Object var7 = getHitVector(arg0, var6);
            if (var1 == null) {
                var1 = var6;
                var2 = var7;
                break label_3;
            }
            if (mc.player.distanceToSqr(var7) >= mc.player.distanceToSqr(var2)) {
                break label_3;
            }
            var1 = var6;
            var2 = var7;
            label_3:
            var5++;
        }
        return var1;
    }

    public static List getStrictDirections(Vec3 arg0, Vec3 arg1) {
        Object var2 = new ArrayList();
        double var3 = (arg0.x - arg1.x);
        double var5 = (arg0.y - arg1.y);
        double var7 = (arg0.z - arg1.z);
        if (var5 > 0.5) {
            var2.add(Direction.UP);
            break label_1;
        }
        if (var5 >= -0.5) break label_1;
        var2.add(Direction.DOWN);
        label_1:
        if (var3 > 0.5) {
            var2.add(Direction.EAST);
            break label_3;
        }
        if (var3 >= -0.5) break label_3;
        var2.add(Direction.WEST);
        label_3:
        if (var7 > 0.5) {
            var2.add(Direction.SOUTH);
            break label_5;
        }
        if (var7 >= -0.5) break label_5;
        var2.add(Direction.NORTH);
        label_5:
        return var2;
    }

    public static HitResult getRaytraceTarget(float arg0, float arg1, double arg2, double arg3, double arg4) {
        Object var11 = new Vec3(((double) (Mth.sin(((double) (-(arg0) * 0.017453292f))) * Mth.cos(((double) (arg1 * 0.017453292f))))), ((double) -(Mth.sin(((double) (arg1 * 0.017453292f))))), ((double) (Mth.cos(((double) (-(arg0) * 0.017453292f))) * Mth.cos(((double) (arg1 * 0.017453292f))))));
        Object var12 = mc.level.clip(new ClipContext(new Vec3(arg2, arg3, arg4), new Vec3((arg2 + (var11.x * 5.0)), (arg3 + (var11.y * 5.0)), (arg4 + (var11.z * 5.0))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
        Object var13 = new Vec3(arg2, (arg3 + ((double) mc.player.getEyeHeight(mc.player.getPose()))), arg4);
        double var14 = 25.0;
        if (var12 != null) {
            var14 = var12.getLocation().distanceToSqr(var13);
        }
        var tmp0 = var13.add((var11.x * 5.0), (var11.y * 5.0), (var11.z * 5.0));
        Object var9 = tmp0;
        var tmp1 = new AABB((arg2 - 0.3), arg3, (arg4 - 0.3), (arg2 + 0.3), (arg3 + 1.8), (arg4 + 0.3)).expandTowards(var11.scale(5.0)).inflate(1.0, 1.0, 1.0);
        Object var8 = tmp1;
        var tmp2 = ProjectileUtil.getEntityHitResult(mc.player, var13, tmp0, tmp1, (arg0) -> {
    if (arg0.isSpectator()) break label_0;
    return arg0.isPickable();
}, var14);
        Object var10 = tmp2;
        if (tmp2 == null) break label_2;
        if (var13.distanceToSqr(var10.getLocation()) >= var14) {
            if (var12 != null) break label_2;
        }
        if ((var10.getEntity() instanceof LivingEntity)) return var10;
        label_2:
        return var12;
    }

    public static boolean canSee(Entity arg0) {
        if (((arg0 == null) || (mc.player == null)) || (mc.level == null)) {
            return false;
        }
        Object var1 = arg0.getBoundingBox();
        Object var2 = new Vec3(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
        if (canSee(var2, var1.getCenter())) return true;
        if (canSee(var2, new Vec3(var1.getCenter().x, (var1.maxY - 0.1), var1.getCenter().z))) return true;
        if (canSee(var2, new Vec3(var1.getCenter().x, (var1.minY + 0.1), var1.getCenter().z))) return true;
        double var3 = (var1.minX + 0.1);
        double var5 = (var1.maxX - 0.1);
        double var7 = (var1.minY + 0.1);
        double var9 = (var1.maxY - 0.1);
        double var11 = (var1.minZ + 0.1);
        double var13 = (var1.maxZ - 0.1);
        if (canSee(var2, new Vec3(var3, var9, var11))) return true;
        if (canSee(var2, new Vec3(var5, var9, var11))) return true;
        if (canSee(var2, new Vec3(var3, var9, var13))) return true;
        if (canSee(var2, new Vec3(var5, var9, var13))) return true;
        if (canSee(var2, new Vec3(var3, var7, var11))) return true;
        if (canSee(var2, new Vec3(var5, var7, var11))) return true;
        if (canSee(var2, new Vec3(var3, var7, var13))) return true;
        if (canSee(var2, new Vec3(var5, var7, var13))) return true;
        return canSeeBlock(BlockPos.containing(var1.getCenter().x, (var1.minY - 0.5), var1.getCenter().z));
    }

    public static boolean canSee(Vec3 arg0, Vec3 arg1) {
        return mc.level.clip(new ClipContext(arg0, arg1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean canSee(BlockPos arg0) {
        return canSee((((double) arg0.getX()) + 0.5), (((double) arg0.getY()) + 0.5), (((double) arg0.getZ()) + 0.5));
    }

    public static boolean canSee(Vec3 arg0) {
        return canSee(arg0.x, arg0.y, arg0.z);
    }

    public static boolean canSee(double arg0, double arg1, double arg2) {
        return mc.level.clip(new ClipContext(new Vec3(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ()), new Vec3(arg0, arg1, arg2), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean canSeeBlock(BlockPos arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return false;
        }
        Object var3 = new Vec3(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
        Object var4 = mc.level.clip(new ClipContext(var3, Vec3.atCenterOf(arg0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var4.getType() != HitResult.Type.MISS) {
            if (!((var4 instanceof BlockHitResult))) break label_3;
            Object var2 = var4;
            if (!(var4.getBlockPos().equals(arg0))) break label_3;
        }
        return true;
        label_3:
        Object var5 = new Vec3((((double) arg0.getX()) + 0.5), (((double) arg0.getY()) + 1.0), (((double) arg0.getZ()) + 0.5));
        Object var6 = mc.level.clip(new ClipContext(var3, var5, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var6.getType() != HitResult.Type.MISS) {
            if (!((var6 instanceof BlockHitResult))) break label_5;
            Object var1 = var6;
            if (!(var6.getBlockPos().equals(arg0))) break label_5;
        }
        return true;
        label_5:
        return CrystalPlacementHelper.isPlacementVisible(arg0);
    }

    public static boolean canBreak(BlockPos... arg0) {
        return Arrays.stream(arg0).allMatch((arg0) -> mc.level.getBlockState(arg0).getBlock().defaultDestroyTime() != -1.0f);
    }

    public static boolean isReplaceable(BlockPos... arg0) {
        return Arrays.stream(arg0).allMatch((arg0) -> mc.level.getBlockState(arg0).canBeReplaced());
    }

    public static Block getBlock(BlockPos arg0) {
        return mc.level.getBlockState(arg0).getBlock();
    }

    public static int getNetherPosition(int arg0) {
        if (mc.player.level().dimension() == Level.NETHER) {
        } else {
        }
        return (arg0 / 8);
    }

    public static String getMovementDirection(Direction arg0) {
        if (arg0.getName().equalsIgnoreCase("North")) return "-Z";
        if (arg0.getName().equalsIgnoreCase("East")) return "+X";
        if (arg0.getName().equalsIgnoreCase("South")) return "+Z";
        if (arg0.getName().equalsIgnoreCase("West")) return "-X";
        return "N/A";
    }

    public static String getFacingName(float arg0) {
        return FACING_NAMES[getFacingOctant(arg0)];
    }

    public static String getFacingAxes(float arg0) {
        return FACING_AXES[getFacingOctant(arg0)];
    }

    private static int getFacingOctant(float arg0) {
        float var1 = (arg0 % 360.0f);
        if (var1 < 0.0f) {
            var1 = (var1 + 360.0f);
        }
        return (Math.round((var1 / 45.0f)) % 8);
    }

    public static float getBreakTime(Player arg0, BlockState arg1) {
        if (arg0 == null) return 0.0f;
        float var2 = (NETHERITE_PICKAXE.getItem().getDestroySpeed(NETHERITE_PICKAXE, arg1) + 26.0f);
        return ((1.0f / ((var2 / arg1.getBlock().defaultDestroyTime()) / 30.0f)) * 50.0f);
    }

    public static double getBreakDelta(BlockState arg0, int arg1) {
        if (arg1 == -1) return 0.0;
        float var3 = mc.player.getInventory().getItem(arg1).getItem().getDestroySpeed(mc.player.getInventory().getItem(arg1), arg0);
        if (var3 <= 1.0f) break label_1;
        Object var4 = mc.player.getInventory().getItem(arg1);
        int var5 = EnchantmentHelper.getItemEnchantmentLevel(mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), var4);
        if ((var5 <= 0) || (var4.isEmpty())) break label_1;
        var3 = (var3 + ((float) ((var5 * var5) + 1)));
        label_1:
        if (MobEffectUtil.hasDigSpeed(mc.player)) {
            var3 = (var3 * (1.0f + (((float) (MobEffectUtil.getDigSpeedAmplification(mc.player) + 1)) * 0.2f)));
        }
        if (mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
            switch (mc.player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> { break label_3; }
                case 1 -> { break label_4; }
                case 2 -> { break label_5; }
                default -> { break label_6; }
            }
            label_3:
            break label_7;
            label_4:
            break label_7;
            label_5:
            break label_7;
            label_6:
            label_7:
            var3 = (0.0027f * 8.1E-4f);
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        var4 = null;
        if ((var4 == null) || (!(var4.isToggled()))) break label_11;
        var5 = (var4.liquidCheck.getValue() ? 1 : 0);
        if ((var4 == null) || (!(var4.isToggled()))) break label_13;
        var tmp0 = (var4.airCheck.getValue() ? 1 : 0);
        int var2 = tmp0;
        int var6 = tmp0;
        if (!var5 && mc.player.isEyeInFluid(FluidTags.WATER) && EnchantmentHelper.getEnchantmentLevel(mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.AQUA_AFFINITY), mc.player) <= 0) {
            var3 = (var3 / 5.0f);
        }
        if (!var2 && !(mc.player.onGround())) {
            var3 = (var3 / 5.0f);
        }
        if (arg0.requiresCorrectToolForDrops()) {
            if (mc.player.getInventory().getItem(arg1).isCorrectToolForDrops(arg0)) {
            }
        } else {
        }
        return ((double) (30 / ((float) 100)));
    }

    public static float getMineSpeed(BlockState arg0, int arg1) {
        if (mc.player == null) return 0.0f;
        float var3 = mc.player.getInventory().getItem(arg1).getItem().getDestroySpeed(mc.player.getInventory().getItem(arg1), arg0);
        if (var3 <= 1.0f) break label_1;
        Object var4 = mc.player.getInventory().getItem(arg1);
        int var5 = EnchantmentHelper.getItemEnchantmentLevel(mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), var4);
        if ((var5 <= 0) || (var4.isEmpty())) break label_1;
        var3 = (var3 + ((float) (StrictMath.pow(((double) var5), 2.0) + 1.0)));
        label_1:
        if (mc.player.hasEffect(MobEffects.HASTE)) {
            var3 = (var3 * (1.0f + (((float) (mc.player.getEffect(MobEffects.HASTE).getAmplifier() + 1)) * 0.2f)));
        }
        if (mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
            var3 = (var3 * ((float) Math.pow(0.30000001192092896, ((double) (mc.player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier() + 1)))));
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        var4 = null;
        if ((var4 == null) || (!(var4.isToggled()))) break label_6;
        var5 = (var4.liquidCheck.getValue() ? 1 : 0);
        if ((var4 == null) || (!(var4.isToggled()))) break label_8;
        var tmp0 = (var4.airCheck.getValue() ? 1 : 0);
        int var2 = tmp0;
        int var6 = tmp0;
        if (!var5 && mc.player.isEyeInFluid(FluidTags.WATER)) {
            var3 = (var3 * ((float) mc.player.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue()));
        }
        if (!var2 && !(mc.player.onGround())) {
            var3 = (var3 / 5.0f);
        }
        var3 = (var3 < 0.0f ? 0.0f : var3);
        if (arg0.requiresCorrectToolForDrops()) {
            if (mc.player.getInventory().getItem(arg1).isCorrectToolForDrops(arg0)) {
            }
        } else {
        }
        return (30 / ((float) 100));
    }

    public static boolean blocksMovement(BlockState arg0) {
        if ((arg0.getBlock() == Blocks.COBWEB) || (arg0.getBlock() == Blocks.BAMBOO_SAPLING)) break label_0;
        return !(arg0.canBeReplaced());
    }

    public static boolean equals(BlockPos arg0, BlockPos arg1) {
        if (arg0 == null && arg1 == null) return true;
        if ((arg0 == null) || (arg1 == null)) {
            return false;
        }
        return arg0.equals(arg1);
    }

    public static String getDimension() {
        return mc.player.level().dimension().identifier().toString().replace("minecraft:", "");
    }

    public static List getCollisions(BlockPos arg0) {
        Object var1 = new ArrayList();
        Object var2 = mc.level.players().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Player) var2.next());
            if (((var3 == null) || (var3.isRemoved())) || (EntityUtils.isGhost(var3))) break label_0;
            if (!(var3.getBoundingBox().intersects(new AABB(arg0)))) {
                break label_0;
            }
            var1.add(var3);
        }
        return var1;
    }

    static {
        var tmp0 = new Block[111];
        tmp0[0] = Blocks.CHEST;
        tmp0[1] = Blocks.TRAPPED_CHEST;
        tmp0[2] = Blocks.ENDER_CHEST;
        tmp0[3] = Blocks.SHULKER_BOX;
        tmp0[4] = Blocks.BARREL;
        tmp0[5] = Blocks.RESPAWN_ANCHOR;
        tmp0[6] = Blocks.CRAFTER;
        tmp0[7] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.WHITE));
        tmp0[8] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.ORANGE));
        tmp0[9] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.MAGENTA));
        tmp0[10] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_BLUE));
        tmp0[11] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.YELLOW));
        tmp0[12] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIME));
        tmp0[13] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.PINK));
        tmp0[14] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.GRAY));
        tmp0[15] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_GRAY));
        tmp0[16] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.CYAN));
        tmp0[17] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.PURPLE));
        tmp0[18] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.BLUE));
        tmp0[19] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.BROWN));
        tmp0[20] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.GREEN));
        tmp0[21] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.RED));
        tmp0[22] = ((Block) Blocks.DYED_SHULKER_BOX.pick(DyeColor.BLACK));
        tmp0[23] = Blocks.ANVIL;
        tmp0[24] = Blocks.CHIPPED_ANVIL;
        tmp0[25] = Blocks.DAMAGED_ANVIL;
        tmp0[26] = Blocks.BELL;
        tmp0[27] = Blocks.OAK_BUTTON;
        tmp0[28] = Blocks.ACACIA_BUTTON;
        tmp0[29] = Blocks.BIRCH_BUTTON;
        tmp0[30] = Blocks.DARK_OAK_BUTTON;
        tmp0[31] = Blocks.JUNGLE_BUTTON;
        tmp0[32] = Blocks.SPRUCE_BUTTON;
        tmp0[33] = Blocks.STONE_BUTTON;
        tmp0[34] = Blocks.COMPARATOR;
        tmp0[35] = Blocks.REPEATER;
        tmp0[36] = Blocks.OAK_FENCE_GATE;
        tmp0[37] = Blocks.SPRUCE_FENCE_GATE;
        tmp0[38] = Blocks.BIRCH_FENCE_GATE;
        tmp0[39] = Blocks.JUNGLE_FENCE_GATE;
        tmp0[40] = Blocks.DARK_OAK_FENCE_GATE;
        tmp0[41] = Blocks.ACACIA_FENCE_GATE;
        tmp0[42] = Blocks.BREWING_STAND;
        tmp0[43] = Blocks.DISPENSER;
        tmp0[44] = Blocks.DROPPER;
        tmp0[45] = Blocks.LEVER;
        tmp0[46] = Blocks.NOTE_BLOCK;
        tmp0[47] = Blocks.JUKEBOX;
        tmp0[48] = Blocks.BEACON;
        tmp0[49] = ((Block) Blocks.BED.pick(DyeColor.BLACK));
        tmp0[50] = ((Block) Blocks.BED.pick(DyeColor.BLUE));
        tmp0[51] = ((Block) Blocks.BED.pick(DyeColor.BROWN));
        tmp0[52] = ((Block) Blocks.BED.pick(DyeColor.CYAN));
        tmp0[53] = ((Block) Blocks.BED.pick(DyeColor.GRAY));
        tmp0[54] = ((Block) Blocks.BED.pick(DyeColor.GREEN));
        tmp0[55] = ((Block) Blocks.BED.pick(DyeColor.LIGHT_BLUE));
        tmp0[56] = ((Block) Blocks.BED.pick(DyeColor.LIGHT_GRAY));
        tmp0[57] = ((Block) Blocks.BED.pick(DyeColor.LIME));
        tmp0[58] = ((Block) Blocks.BED.pick(DyeColor.MAGENTA));
        tmp0[59] = ((Block) Blocks.BED.pick(DyeColor.ORANGE));
        tmp0[60] = ((Block) Blocks.BED.pick(DyeColor.PINK));
        tmp0[61] = ((Block) Blocks.BED.pick(DyeColor.PURPLE));
        tmp0[62] = ((Block) Blocks.BED.pick(DyeColor.RED));
        tmp0[63] = ((Block) Blocks.BED.pick(DyeColor.WHITE));
        tmp0[64] = ((Block) Blocks.BED.pick(DyeColor.YELLOW));
        tmp0[65] = Blocks.FURNACE;
        tmp0[66] = Blocks.OAK_DOOR;
        tmp0[67] = Blocks.SPRUCE_DOOR;
        tmp0[68] = Blocks.BIRCH_DOOR;
        tmp0[69] = Blocks.JUNGLE_DOOR;
        tmp0[70] = Blocks.ACACIA_DOOR;
        tmp0[71] = Blocks.DARK_OAK_DOOR;
        tmp0[72] = Blocks.CAKE;
        tmp0[73] = Blocks.ENCHANTING_TABLE;
        tmp0[74] = Blocks.DRAGON_EGG;
        tmp0[75] = Blocks.HOPPER;
        tmp0[76] = Blocks.REPEATING_COMMAND_BLOCK;
        tmp0[77] = Blocks.COMMAND_BLOCK;
        tmp0[78] = Blocks.CHAIN_COMMAND_BLOCK;
        tmp0[79] = Blocks.CRAFTING_TABLE;
        tmp0[80] = Blocks.ACACIA_TRAPDOOR;
        tmp0[81] = Blocks.BIRCH_TRAPDOOR;
        tmp0[82] = Blocks.DARK_OAK_TRAPDOOR;
        tmp0[83] = Blocks.JUNGLE_TRAPDOOR;
        tmp0[84] = Blocks.OAK_TRAPDOOR;
        tmp0[85] = Blocks.SPRUCE_TRAPDOOR;
        tmp0[86] = Blocks.CAKE;
        tmp0[87] = Blocks.ACACIA_SIGN;
        tmp0[88] = Blocks.ACACIA_WALL_SIGN;
        tmp0[89] = Blocks.BIRCH_SIGN;
        tmp0[90] = Blocks.BIRCH_WALL_SIGN;
        tmp0[91] = Blocks.DARK_OAK_SIGN;
        tmp0[92] = Blocks.DARK_OAK_WALL_SIGN;
        tmp0[93] = Blocks.JUNGLE_SIGN;
        tmp0[94] = Blocks.JUNGLE_WALL_SIGN;
        tmp0[95] = Blocks.OAK_SIGN;
        tmp0[96] = Blocks.OAK_WALL_SIGN;
        tmp0[97] = Blocks.SPRUCE_SIGN;
        tmp0[98] = Blocks.SPRUCE_WALL_SIGN;
        tmp0[99] = Blocks.CRIMSON_SIGN;
        tmp0[100] = Blocks.CRIMSON_WALL_SIGN;
        tmp0[101] = Blocks.WARPED_SIGN;
        tmp0[102] = Blocks.WARPED_WALL_SIGN;
        tmp0[103] = Blocks.BLAST_FURNACE;
        tmp0[104] = Blocks.SMOKER;
        tmp0[105] = Blocks.CARTOGRAPHY_TABLE;
        tmp0[106] = Blocks.GRINDSTONE;
        tmp0[107] = Blocks.LECTERN;
        tmp0[108] = Blocks.LOOM;
        tmp0[109] = Blocks.STONECUTTER;
        tmp0[110] = Blocks.SMITHING_TABLE;
        RIGHT_CLICKABLE_BLOCKS = Sets.newHashSet(tmp0);
        NETHERITE_PICKAXE = new ItemStack(Items.NETHERITE_PICKAXE);
        pendingAirPlaces = new ConcurrentHashMap();
        predictedPlacements = new ConcurrentHashMap();
        lastInteractablePlaceTime = 0L;
        var tmp1 = new String[8];
        tmp1[0] = "South";
        tmp1[1] = "South West";
        tmp1[2] = "West";
        tmp1[3] = "North West";
        tmp1[4] = "North";
        tmp1[5] = "North East";
        tmp1[6] = "East";
        tmp1[7] = "South East";
        FACING_NAMES = tmp1;
        var tmp2 = new String[8];
        tmp2[0] = "+Z";
        tmp2[1] = "-X +Z";
        tmp2[2] = "-X";
        tmp2[3] = "-X -Z";
        tmp2[4] = "-Z";
        tmp2[5] = "+X -Z";
        tmp2[6] = "+X";
        tmp2[7] = "+X +Z";
        FACING_AXES = tmp2;
    }


    record PredictedPlacement(Block block, long time) {

        private PredictedPlacement(Block arg0, long arg1) {
            this.block = arg0;
            this.time = arg1;
        }

        public Block block() {
            return this.block;
        }

        public long time() {
            return this.time;
        }

    }

    record DirectionCandidate(Direction direction, double distSq) {

        private DirectionCandidate(Direction arg0, double arg1) {
            this.direction = arg0;
            this.distSq = arg1;
        }

        public Direction direction() {
            return this.direction;
        }

        public double distSq() {
            return this.distSq;
        }

    }
}
