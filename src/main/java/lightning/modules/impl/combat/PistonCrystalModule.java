package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.minecraft.PistonHelper;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "PistonCrystal", description = "Places piston + redstone to push a crystal into targets.", category = Module.Category.COMBAT)
public class PistonCrystalModule extends Module {
    public NumberSetting minDamage;
    public NumberSetting maxLocalDamage;
    public NumberSetting delay;
    public ModeSetting sort;
    public NumberSetting targetRange;
    public BooleanSetting inAirTarget;
    public BooleanSetting breakCrystal;
    public BooleanSetting autoBreak;
    public NumberSetting placeRange;
    public NumberSetting breakRange;
    public ModeSetting rotate;
    public BooleanSetting grim;
    public ModeSetting swapAction;
    public BooleanSetting airPlace;
    public BooleanSetting safety;
    public BooleanSetting blockDestruction;
    public BooleanSetting selfExtrapolate;
    public NumberSetting extrapolationTicks;
    public BooleanSetting assumeBestArmor;
    public BooleanSetting autoSupport;
    public BooleanSetting pauseOnEat;
    public BooleanSetting strictDirection;
    private BlockPos pistonPos;
    private BlockPos redstonePos;
    private BlockPos crystalPos;
    private boolean crystalForcedPush;
    private BlockPos crystalSupportPos;
    private BlockPos pistonSupportPos;
    private BlockPos lastPiston;
    private BlockPos lastRedstone;
    private final Set placedRedstone;
    private final Set placedPistons;
    private int preferredHeight;
    private Direction face;
    private boolean usingTorch;
    private Player target;
    private long lastActionMs;
    private long breakTimerMs;
    private int preSwapSlot;

    public PistonCrystalModule() {
        this.minDamage = new NumberSetting("MinDamage", "Minimum damage to attempt a sequence", 6.0f, 1.0f, 20.0f);
        this.maxLocalDamage = new NumberSetting("MaxLocalDamage", "Max self-damage allowed", 12.0f, 1.0f, 20.0f);
        this.delay = new NumberSetting("Delay", "Delay between place actions (ticks)", 0, 0, 10);
        var tmp0 = new String[3];
        tmp0[0] = "LowestDistance";
        tmp0[1] = "LowestHealth";
        tmp0[2] = "CloseAngle";
        this.sort = new ModeSetting("Sort", "How to pick target", "LowestDistance", tmp0);
        this.targetRange = new NumberSetting("TargetRange", "Max target range", 6.0f, 1.0f, 20.0f);
        this.inAirTarget = new BooleanSetting("InAirTarget", "Target players in the air", true);
        this.breakCrystal = new BooleanSetting("BreakCrystal", "Attack crystal after placing it", true);
        this.autoBreak = new BooleanSetting("AutoBreak", "Automatically breaks leftover redstone/piston with pickaxe after crystal explosion.", true);
        this.placeRange = new NumberSetting("PlaceRange", "Range to place blocks", 4.5f, 0.1f, 6.0f);
        this.breakRange = new NumberSetting("BreakRange", "Range to break crystals", 4.5f, 0.1f, 6.0f);
        var tmp1 = new String[3];
        tmp1[0] = "None";
        tmp1[1] = "Normal";
        tmp1[2] = "Silent";
        this.rotate = new ModeSetting("Rotate", "Silently rotate to placed blocks", "Silent", tmp1);
        this.grim = new BooleanSetting("Grim", "YawDeceive - filter dir by player facing & override piston yaw", true);
        this.swapAction = new ModeSetting("Swap", "How to swap to required items", "Silent", InventoryUtils.SWITCH_MODES);
        this.airPlace = new BooleanSetting("AirPlace", "Allow placing when not on ground", false);
        this.safety = new BooleanSetting("Safety", "Skip if crystal would kill self", true);
        this.blockDestruction = new BooleanSetting("BlockDestruction", "Account for explosion block destruction", false);
        this.selfExtrapolate = new BooleanSetting("SelfExtrapolate", "Extrapolate self position for self-damage calc", false);
        this.extrapolationTicks = new NumberSetting("ExtrapolationTicks", "Ticks to extrapolate enemy position", 0, 0, 10);
        this.assumeBestArmor = new BooleanSetting("AssumeBestArmor", "Assume target has Prot 4 netherite", false);
        this.autoSupport = new BooleanSetting("AutoSupport", "Automatically places obsidian support blocks when no platform exists.", true);
        this.pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses the module while eating or using an item.", true);
        this.strictDirection = new BooleanSetting("StrictDirection", "Use raytrace to verify line-of-sight before placing piston/redstone (required on servers that check raytrace).", false);
        this.placedRedstone = new LinkedHashSet();
        this.placedPistons = new LinkedHashSet();
        this.preferredHeight = -1;
        this.lastActionMs = 0L;
        this.breakTimerMs = 0L;
        this.preSwapSlot = -1;
    }

    public void onEnable() {
        resetState();
        this.placedRedstone.clear();
        this.placedPistons.clear();
        this.breakTimerMs = (System.currentTimeMillis() + 99999999L);
    }

    public void onDisable() {
        resetState();
        this.placedRedstone.clear();
        this.placedPistons.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) break label_60;
        if (this.pauseOnEat.getValue()) {
            if (EntityUtils.isEating()) break label_60;
        }
        long var2 = System.currentTimeMillis();
        if ((var2 - this.lastActionMs) < (this.delay.getValue().longValue() * 50L)) break label_60;
        int var4 = findItem(Items.END_CRYSTAL);
        int var5 = findPiston();
        int var6 = findRedstone();
        if (var4 != -1 && var5 != -1 && var6 != -1) {
            this.target = pickTarget();
            if (this.target != null) break label_1;
            resetState();
            break label_60;
            label_1:
            if (((this.pistonPos == null) || (this.redstonePos == null)) || (this.crystalPos == null)) break label_19;
            if (!((mc.level.getBlockState(this.pistonPos).getBlock() instanceof PistonBaseBlock))) break label_3;
            if (mc.level.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_BLOCK) break label_2;
            if (mc.level.getBlockState(this.redstonePos).getBlock() != Blocks.REDSTONE_TORCH) break label_3;
            label_2:
            break label_4;
            label_3:
            label_4:
            int var7 = 0;
            if (var7) break label_19;
            int var8 = this.airPlace.getValue();
            if ((PistonHelper.canPlace(this.pistonPos, null, var8, true)) || ((mc.level.getBlockState(this.pistonPos).getBlock() instanceof PistonBaseBlock))) break label_5;
            if ((var8) || (this.pistonSupportPos == null)) break label_6;
            if (PistonHelper.canPlace(this.pistonSupportPos, null, var8, true)) break label_5;
            if (!(isSolidBlock(this.pistonSupportPos))) break label_6;
            label_5:
            break label_7;
            label_6:
            label_7:
            int var9 = 0;
            if (!(PistonHelper.canPlace(this.redstonePos, null, var8, true)) && mc.level.getBlockState(this.redstonePos).getBlock() != Blocks.REDSTONE_BLOCK && mc.level.getBlockState(this.redstonePos).getBlock() != Blocks.REDSTONE_TORCH && !(this.redstonePos.equals(this.pistonPos.above()))) {
                if (this.redstonePos.equals(this.pistonPos.relative(this.face))) {
                }
            } else {
            }
            int var10 = 0;
            if (!(!this.crystalForcedPush)) {
                if (PistonHelper.canPlaceCrystalIgnoreHeadroom(this.crystalPos, var8)) {
                    break label_13;
                }
                if (PistonHelper.canPlaceCrystal(this.crystalPos, var8)) break label_13;
            }
            if (this.crystalSupportPos == null) break label_14;
            if (PistonHelper.canPlace(this.crystalSupportPos, null, false, true)) break label_13;
            if (!(isObsidian(this.crystalSupportPos))) break label_14;
            label_13:
            break label_15;
            label_14:
            label_15:
            int var11 = 0;
            if ((((var9) || (var8)) || (!(this.autoSupport.getValue()))) || (this.pistonSupportPos != null)) break label_16;
            Object var12 = findPistonSupportRepair(this.pistonPos, this.face);
            if (var12 == null) break label_16;
            this.pistonSupportPos = var12;
            var9 = 1;
            label_16:
            if (((var11) || (!(this.autoSupport.getValue()))) || (this.crystalSupportPos != null)) break label_17;
            var12 = this.crystalPos.below();
            if (!(canSupportCrystal(this.crystalPos, var12))) break label_17;
            this.crystalSupportPos = var12;
            var11 = 1;
            label_17:
            if (!(!var9) && !(!var10)) {
                if (var11) break label_19;
            }
            resetState();
            label_19:
            if (this.pistonPos == null && this.crystalPos == null && this.redstonePos == null) {
                doPistonCrystal(this.target);
            }
            if ((((!(this.autoSupport.getValue())) || (this.crystalSupportPos == null)) || (isObsidian(this.crystalSupportPos))) || (!(PistonHelper.canPlace(this.crystalSupportPos, null, false, true)))) break label_22;
            var7 = findSupportBlock();
            if (var7 == -1) break label_22;
            if (!(placeBlock(var7, this.crystalSupportPos, null))) {
                resetState();
                return;
            }
            this.lastActionMs = var2;
            return;
            label_22:
            if (((((this.airPlace.getValue()) || (!(this.autoSupport.getValue()))) || (this.pistonSupportPos == null)) || (isSolidBlock(this.pistonSupportPos))) || (!(PistonHelper.canPlace(this.pistonSupportPos, null, false, true)))) break label_24;
            var7 = findSupportBlock();
            if (var7 == -1) break label_24;
            if (!(placeBlock(var7, this.pistonSupportPos, null))) {
                resetState();
                return;
            }
            this.lastActionMs = var2;
            return;
            label_24:
            if (this.pistonPos != null && PistonHelper.canPlace(this.pistonPos, null, this.airPlace.getValue(), true)) {
                var7 = placeBlock(var5, this.pistonPos, this.face);
                if (!var7 && this.strictDirection.getValue()) {
                    resetState();
                    return;
                }
                this.lastPiston = this.pistonPos;
                this.placedPistons.add(this.pistonPos);
                this.lastActionMs = var2;
            }
            if (this.redstonePos != null && PistonHelper.canPlace(this.redstonePos, null, this.airPlace.getValue(), true)) {
                if (!(!this.usingTorch)) {
                    placeTorch(var6, this.redstonePos);
                } else {
                    placeBlock(var6, this.redstonePos, null);
                }
                this.lastRedstone = this.redstonePos;
                this.placedRedstone.add(this.redstonePos);
                this.lastActionMs = var2;
            }
            if (this.crystalPos == null) break label_31;
            if (!(!this.crystalForcedPush)) {
                if (PistonHelper.canPlaceCrystalIgnoreHeadroom(this.crystalPos, this.airPlace.getValue())) break label_58;
                break label_31;
            }
            if (PistonHelper.canPlaceCrystal(this.crystalPos, this.airPlace.getValue())) break label_58;
            label_31:
            if ((!(this.breakCrystal.getValue())) || ((var2 - this.breakTimerMs) < (this.delay.getValue().longValue() * 50L))) break label_35;
            var7 = -1;
            while (var7 <= 4) {
                var8 = this.target.blockPosition().above(var7);
                if (!(PistonHelper.hasCrystal(var8))) break label_34;
                if (this.crystalPos != null) {
                    if (this.crystalPos.equals(var8)) break label_34;
                }
                attackCrystalAt(var8);
                this.lastActionMs = var2;
                this.breakTimerMs = var2;
                resetState();
                return;
                label_34:
                var7++;
            }
            label_35:
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            var7 = null;
            if ((var7 == null) || (!(var7.isToggled()))) break label_38;
            var8 = (var7.primaryDigging() ? 1 : 0);
            var9 = (this.lastActionMs == var2 ? 1 : 0);
            if (((!(this.autoBreak.getValue())) || (var8)) || (var9)) break label_49;
            var10 = this.placedRedstone.iterator();
            while (var10.hasNext()) {
                var11 = ((BlockPos) var10.next());
                if (!(isRedstone(var11))) {
                    var10.remove();
                    break label_44;
                }
                if ((var11.equals(this.redstonePos)) || (!(inRange(var11, ((double) this.placeRange.getValue().floatValue()))))) break label_44;
                breakBlock(var11);
                var10.remove();
                this.lastActionMs = var2;
                return;
                label_44:
            }
            var10 = this.placedPistons.iterator();
            while (var10.hasNext()) {
                var11 = ((BlockPos) var10.next());
                if (!((mc.level.getBlockState(var11).getBlock() instanceof PistonBaseBlock))) {
                    var10.remove();
                    break label_48;
                }
                if ((var11.equals(this.pistonPos)) || (!(inRange(var11, ((double) this.placeRange.getValue().floatValue()))))) break label_48;
                breakBlock(var11);
                var10.remove();
                this.lastActionMs = var2;
                return;
                label_48:
            }
            label_49:
            var10 = ((2L + this.delay.getValue().longValue()) * 50L);
            if (((!(this.autoBreak.getValue())) || (var8)) || ((var2 - this.lastActionMs) < var10)) break label_57;
            var12 = this.placedRedstone.iterator();
            while (var12.hasNext()) {
                Object var13 = ((BlockPos) var12.next());
                if (isRedstone(var13)) {
                    if (inRange(var13, ((double) this.placeRange.getValue().floatValue()))) {
                        breakBlock(var13);
                        var12.remove();
                        this.lastActionMs = var2;
                        resetState();
                        return;
                    }
                    var12.remove();
                }
            }
            var12 = this.placedPistons.iterator();
            while (var12.hasNext()) {
                var13 = ((BlockPos) var12.next());
                if ((mc.level.getBlockState(var13).getBlock() instanceof PistonBaseBlock)) {
                    if (inRange(var13, ((double) this.placeRange.getValue().floatValue()))) {
                        breakBlock(var13);
                        var12.remove();
                        this.lastActionMs = var2;
                        resetState();
                        return;
                    }
                    var12.remove();
                }
            }
            label_57:
            break label_60;
            label_58:
            placeCrystal(var4, this.crystalPos);
            this.lastActionMs = var2;
            this.breakTimerMs = var2;
            break label_60;
        }
        resetState();
        label_60:
    }

    private void doPistonCrystal(Player arg0) {
        if (isTargetPhased(arg0)) return;
        int var4 = this.extrapolationTicks.getValue().intValue();
        double var5 = ((arg0.getX() - arg0.xOld) * ((double) var4));
        double var7 = (((arg0.getY() - arg0.yOld) * ((double) var4)) * 0.3);
        double var9 = ((arg0.getZ() - arg0.zOld) * ((double) var4));
        double var11 = (arg0.getX() + var5);
        double var13 = (arg0.getY() + var7);
        double var15 = (arg0.getZ() + var9);
        Object var17 = getEntityFeetPos(arg0, var11, var13, var15);
        Object var18 = arg0.getBoundingBox().move(var5, var7, var9);
        if (!(this.selfExtrapolate.getValue())) break label_1;
        break label_2;
        label_1:
        label_2:
        Object var19 = mc.player.getBoundingBox();
        if (((arg0.getPose() == Pose.SWIMMING) || (arg0.isVisuallyCrawling())) || (arg0.getEyeHeight() <= 1.0f)) break label_3;
        if (arg0.getBoundingBox().getYsize() > 1.0) break label_4;
        label_3:
        break label_5;
        label_4:
        label_5:
        int var20 = 0;
        int var3 = 0;
        while (var3 <= 4 && !(hasHorizontalOpening(var17.above(var3)))) {
            var3++;
        }
        if (((!var20) || (mc.level == null)) || (mc.level.getBlockState(var17.above(1)).isAir())) break label_8;
        int var21 = (!(hasHorizontalOpening(var17.above(0))) ? 1 : 0);
        if (((var20) || (mc.level == null)) || (mc.level.getBlockState(var17.above(2)).isAir())) break label_10;
        int var22 = (!(hasHorizontalOpening(var17.above(1))) ? 1 : 0);
        if (var20) break label_12;
        int var23 = (var3 >= 2 ? 1 : 0);
        int var24 = (!(mc.level.getBlockState(var17.above(2)).isAir()) ? 1 : 0);
        int var25 = (!(hasHorizontalOpening(var17.above(2))) ? 1 : 0);
        if ((var20) || (mc.level == null)) break label_19;
        if (!var24) {
            if (!var25) break label_19;
        }
        var tmp0 = (hasHorizontalOpening(var17.above(1)) ? 1 : 0);
        int var2 = tmp0;
        int var26 = tmp0;
        if (!(!var21)) {
            var tmp1 = new int[3];
            tmp1[0] = 1;
            tmp1[1] = 0;
            tmp1[2] = 2;
            break label_26;
        }
        if (var3 >= 3) {
            var tmp2 = new int[5];
            tmp2[0] = var3;
            tmp2[1] = (var3 - 1);
            tmp2[2] = 2;
            tmp2[3] = 1;
            tmp2[4] = 0;
            break label_26;
        }
        if (!var22) {
            if (var3 == 2) {
            }
            var tmp3 = new int[4];
            tmp3[0] = 2;
            tmp3[1] = 1;
            tmp3[2] = 0;
            tmp3[3] = 3;
            break label_26;
        }
        if (!(!var2)) {
            var tmp4 = new int[4];
            tmp4[0] = 1;
            tmp4[1] = 0;
            tmp4[2] = 2;
            tmp4[3] = 3;
            break label_26;
        }
        var tmp5 = new int[4];
        tmp5[0] = 0;
        tmp5[1] = 1;
        tmp5[2] = 2;
        tmp5[3] = 3;
        label_26:
        Object var27 = tmp5;
        int var28 = 0;
        Object var29 = var27;
        int var30 = var29.length;
        int var31 = 0;
        while (var31 < var30) {
            int var32 = var29[var31];
            if (var32 != this.preferredHeight) {
            } else {
                var28 = 1;
                break label_30;
            }
            var31++;
        }
        label_30:
        if (this.preferredHeight != -1 && !(!var28) && this.preferredHeight != var27[0]) {
            var29 = new int[var27.length];
            var29[0] = this.preferredHeight;
            var30 = 1;
            var31 = var27;
            var32 = var31.length;
            int var33 = 0;
            while (var33 < var32) {
                int var34 = var31[var33];
                if (var34 == this.preferredHeight) {
                } else {
                    var30++;
                    var29[var30] = var34;
                }
                var33++;
            }
            var27 = var29;
        }
        var29 = var27;
        var30 = var29.length;
        var31 = 0;
        while (var31 < var30) {
            var32 = var29[var31];
            var34 = var17.above(var32);
            if (!(!var21)) {
                if (var32 != 1) {
                }
                if (!(!var2) && var32 == 1) {
                }
                break label_43;
            }
            if (!var22) {
                if (!var23) break label_42;
            }
            if (var32 != 2) {
                if (var32 != var3) break label_42;
            }
            break label_43;
            label_42:
            label_43:
            Object var35 = null;
            float var36 = DamageUtils.getCrystalDamage(arg0, var18, var34, var35, this.blockDestruction.getValue());
            float var37 = DamageUtils.getCrystalDamage(mc.player, var19, var34, null, this.blockDestruction.getValue());
            if (var32 == 1) {
                if (var21) break label_47;
            }
            if (var32 == 1) {
                if (var2) break label_47;
            }
            if (var32 == 2) {
                if (var22) break label_47;
            }
            if (var32 == var3 && !(!var23)) {
                label_47:
            } else {
            }
            var33 = 0;
            int var38 = 0;
            if (!(!var33)) {
                if ((var36 <= this.minDamage.getValue().floatValue()) || (var37 > this.maxLocalDamage.getValue().floatValue())) break label_59;
                if (this.safety.getValue() && var37 >= (mc.player.getHealth() + mc.player.getAbsorptionAmount())) {
                    break label_59;
                }
                if (!(trySearch(var17, var32, true))) {
                    tryPreBuildCrystalSupport(var17, var32);
                    break label_59;
                }
                this.preferredHeight = var32;
                return;
            }
            if ((var36 <= this.minDamage.getValue().floatValue()) || (var37 > this.maxLocalDamage.getValue().floatValue())) break label_59;
            if (this.safety.getValue() && var37 >= (mc.player.getHealth() + mc.player.getAbsorptionAmount())) {
                break label_59;
            }
            if (!var22 && !var21) {
                if (!(!var23)) {
                }
            } else {
            }
            if (!(var17.trySearch(var32, 1, false))) {
                if (!(tryPreBuildCrystalSupport(var17, var32))) {
                    break label_59;
                }
                return;
            }
            this.preferredHeight = var32;
            return;
            label_59:
            var31++;
        }
    }

    private boolean tryPreBuildCrystalSupport(BlockPos arg0, int arg1) {
        if (!(this.autoSupport.getValue())) return false;
        Object var3 = Direction.Plane.HORIZONTAL.iterator();
        label_1:
        if (var3.hasNext()) {
            Object var4 = ((Direction) var3.next());
            Object var6 = arg0.relative(var4).above(arg1);
            if (!(inRange(var6, ((double) this.placeRange.getValue().floatValue())))) break label_1;
            if (!(PistonHelper.canPlaceCrystal(var6, false))) {
                var tmp0 = var6.below();
                Object var5 = tmp0;
                if (isObsidian(tmp0)) break label_1;
                if (canSupportCrystal(var6, var5)) break label_3;
                break label_1;
            }
            var tmp1 = var6.relative(var4).below();
            var5 = tmp1;
            if (!(inRange(tmp1, ((double) this.placeRange.getValue().floatValue())))) break label_1;
            if (PistonHelper.canPlace(var5, null, false, true)) break label_3;
            break label_1;
            label_3:
            int var7 = findSupportBlock();
            if (var7 == -1) return false;
            placeBlock(var7, var5, null);
            this.lastActionMs = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static BlockPos getEntityFeetPos(Entity arg0, double arg1, double arg2, double arg3) {
        if (mc.level == null) return new BlockPos(Mth.floor(arg1), Mth.floor(arg2), Mth.floor(arg3));
        int var10 = Mth.floor(arg2);
        Object var11 = new BlockPos(Mth.floor(arg1), var10, Mth.floor(arg3));
        Object var12 = mc.level.getBlockState(var11);
        if ((var12.isAir()) || (var12.canBeReplaced())) break label_1;
        var tmp0 = var12.getCollisionShape(mc.level, var11);
        Object var9 = tmp0;
        if (tmp0.isEmpty()) break label_1;
        double var7 = (((double) var11.getY()) + var9.max(Direction.Axis.Y));
        if (arg2 >= ((((double) var11.getY()) + var9.max(Direction.Axis.Y)) - 0.05)) return var11.above();
        label_1:
        return var11;
    }

    private boolean isTargetPhased(Player arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        Object var2 = getEntityFeetPos(arg0, arg0.getX(), arg0.getY(), arg0.getZ());
        Object var3 = mc.level.getBlockState(var2);
        return isHardPhasedBlock(var3);
    }

    private boolean hasHorizontalOpening(BlockPos arg0) {
        if (mc.level == null) return true;
        Object var2 = Direction.Plane.HORIZONTAL.iterator();
        label_1:
        if (var2.hasNext()) {
            Object var3 = ((Direction) var2.next());
            Object var4 = mc.level.getBlockState(arg0.relative(var3));
            if (!(var4.isAir()) && !(var4.canBeReplaced())) {
                break label_1;
            }
            return true;
        }
        return false;
    }

    private boolean isDecoyOrPassableBlock(BlockState arg0) {
        if (((arg0 == null) || (arg0.isAir())) || (arg0.canBeReplaced())) {
            return true;
        }
        Object var2 = arg0.getBlock();
        if (!(var2 instanceof WebBlock) && !(var2 instanceof ButtonBlock) && !(var2 instanceof MushroomBlock) && !(var2 instanceof LadderBlock) && !(var2 instanceof ScaffoldingBlock) && !(var2 instanceof FlowerBlock) && !(var2 instanceof FlowerPotBlock) && !(var2 instanceof TallFlowerBlock) && !(var2 instanceof VineBlock) && !(var2 instanceof TorchBlock) && !(var2 instanceof LeverBlock) && !(var2 instanceof PressurePlateBlock) && !(var2 instanceof SignBlock) && !(var2 instanceof BannerBlock) && !(var2 instanceof CarpetBlock) && !(var2 instanceof TripWireBlock) && !(var2 instanceof TripWireHookBlock) && !(var2 instanceof BaseRailBlock) && var2 != Blocks.COBWEB && var2 != Blocks.LADDER && var2 != Blocks.SCAFFOLDING && var2 != Blocks.VINE && var2 != Blocks.BROWN_MUSHROOM && var2 != Blocks.RED_MUSHROOM && var2 != Blocks.TORCH && var2 != Blocks.SOUL_TORCH) {
            if (var2 == Blocks.REDSTONE_TORCH) {
            }
        } else {
        }
        return false;
    }

    private boolean isHardPhasedBlock(BlockState arg0) {
        if (isDecoyOrPassableBlock(arg0)) return false;
        Object var2 = arg0.getBlock();
        if (((((((((((var2 == Blocks.OBSIDIAN) || (var2 == Blocks.BEDROCK)) || (var2 == Blocks.ENDER_CHEST)) || (var2 == Blocks.RESPAWN_ANCHOR)) || (var2 == Blocks.CRYING_OBSIDIAN)) || (var2 == Blocks.ANVIL)) || (var2 == Blocks.CHIPPED_ANVIL)) || (var2 == Blocks.DAMAGED_ANVIL)) || (var2 == Blocks.REINFORCED_DEEPSLATE)) || (var2 == Blocks.NETHERITE_BLOCK)) || (var2 == Blocks.ANCIENT_DEBRIS)) {
            return true;
        }
        if (!(arg0.isSolid())) break label_3;
        return arg0.blocksMotion();
    }

    private boolean trySearch(BlockPos arg0, int arg1, boolean arg2) {
        Object var4 = new ArrayList();
        Object var5 = mc.player.getDirection();
        var4.add(var5);
        Object var6 = Direction.Plane.HORIZONTAL.iterator();
        while (var6.hasNext()) {
            Object var7 = ((Direction) var6.next());
            if (var7 != var5) break label_1;
            break label_0;
            label_1:
            var4.add(var7);
        }
        label_2:
        var6 = this.airPlace.getValue();
        var7 = var4.iterator();
        label_3:
        if (var7.hasNext()) {
            Object var8 = ((Direction) var7.next());
            Object var10 = arg0.relative(var8).above(arg1);
            if (!(inRange(var10, ((double) this.placeRange.getValue().floatValue())))) break label_3;
            if (this.strictDirection.getValue() && !(WorldUtils.canSeeBlock(var10))) {
                break label_3;
            }
            Object var11 = null;
            if (PistonHelper.canPlaceCrystal(var10, var6)) break label_7;
            if (!(!arg2)) {
                if (PistonHelper.canPlaceCrystalIgnoreHeadroom(var10, var6)) break label_7;
            }
            if ((var6) || (!(this.autoSupport.getValue()))) break label_3;
            var tmp0 = var10.below();
            Object var12 = tmp0;
            if (!(canSupportCrystal(var10, tmp0))) {
                break label_3;
            }
            var11 = var12;
            label_7:
            var12 = null;
            Object var13 = null;
            Object var14 = null;
            if (!(!arg2)) {
                Object var9 = new ArrayList(4);
                var9.add(var8);
                var9.add(var8.getClockWise());
                var9.add(var8.getCounterClockWise());
                Object var15 = Direction.Plane.HORIZONTAL.iterator();
                while (var15.hasNext()) {
                    Object var16 = ((Direction) var15.next());
                    if (var16 == var8.getOpposite()) break label_8;
                    if (var9.contains(var16)) {
                        break label_8;
                    }
                    var9.add(var16);
                }
                break label_14;
            }
            var9 = new ArrayList(3);
            var15 = Direction.Plane.HORIZONTAL.iterator();
            while (var15.hasNext()) {
                var16 = ((Direction) var15.next());
                if (var16 == var8.getOpposite()) {
                    break label_12;
                }
                var9.add(var16);
            }
            label_14:
            var15 = var9.iterator();
            while (var15.hasNext()) {
                var16 = ((Direction) var15.next());
                Object var20 = var10.relative(var8).relative(var16);
                if (!(!arg2)) {
                    if (var16 != var8) break label_17;
                }
                var tmp1 = var20.relative(var8.getOpposite());
                Object var19 = tmp1;
                if (mc.level.getBlockState(tmp1).isAir()) break label_17;
                if (!(mc.level.getBlockState(var19).canBeReplaced())) break label_15;
                label_17:
                var tmp2 = (mc.level.getBlockState(var20).getBlock() instanceof PistonBaseBlock);
                int var18 = tmp2;
                if (!tmp2 && this.strictDirection.getValue() && !(WorldUtils.canSeeBlock(var20))) {
                    break label_15;
                }
                var14 = null;
                if (!(PistonHelper.canPlace(var20, null, var6, true))) {
                    if (!(!var18)) {
                    }
                } else {
                }
                int var21 = 0;
                if ((var6) || (var11 == null)) break label_22;
                var tmp3 = (var20.distManhattan(var11) == 1 ? 1 : 0);
                int var17 = tmp3;
                int var22 = tmp3;
                if (!var21 && !var17) {
                    if ((var6) || (!(this.autoSupport.getValue()))) break label_15;
                    var tmp4 = findSupportForPiston(var20, var8, var16, var11);
                    Object var23 = tmp4;
                    if (tmp4 == null) {
                        break label_15;
                    }
                    var14 = var23;
                }
                if (!(inRange(var20, ((double) this.placeRange.getValue().floatValue())))) {
                    break label_15;
                }
                var12 = var20;
                var23 = Direction.values();
                int var24 = var23.length;
                int var25 = 0;
                while (var25 < var24) {
                    Object var26 = var23[var25];
                    if (var26 == var8.getOpposite()) {
                        break label_34;
                    }
                    Object var27 = var20.relative(var26);
                    if (var14 != null) {
                        if (var27.equals(var14)) break label_34;
                    }
                    if (var11 != null && var27.equals(var11)) {
                        break label_34;
                    }
                    if (mc.level.getBlockState(var27).getBlock() != Blocks.REDSTONE_BLOCK) {
                        if (mc.level.getBlockState(var27).getBlock() == Blocks.REDSTONE_TORCH) {
                        }
                        var13 = var27;
                        break label_36;
                    }
                    if ((!(PistonHelper.canPlace(var27, null, var6, true))) || (!(inRange(var27, ((double) this.placeRange.getValue().floatValue()))))) break label_34;
                    if (this.strictDirection.getValue() && !(WorldUtils.canSeeBlock(var27))) {
                        break label_34;
                    }
                    var13 = var27;
                    break label_36;
                    label_34:
                    var25++;
                }
            }
            label_36:
            if (var12 == null) break label_3;
            if (var13 == null) {
                break label_3;
            }
            this.face = var8;
            this.crystalPos = var10;
            this.crystalForcedPush = arg2;
            this.pistonPos = var12;
            this.redstonePos = var13;
            this.crystalSupportPos = var11;
            this.pistonSupportPos = var14;
            return true;
        }
        return false;
    }

    private boolean placeBlock(int arg0, BlockPos arg1, Direction arg2) {
        if (arg0 == -1) return false;
        if (arg2 != null) {
            if (this.strictDirection.getValue()) {
                Object var5 = WorldUtils.getDirections(arg1, null, true);
                if (!(var5.contains(arg2))) break label_1;
                break label_3;
                label_1:
                if (var5.isEmpty()) {
                    break label_3;
                }
                label_3:
                Object var4 = ((Direction) var5.get(0));
                if (var4 == null && !(this.airPlace.getValue())) return false;
                break label_9;
            }
            var4 = PistonHelper.getPlaceSide(arg1, (arg0) -> true);
            if ((var4 != null) || (this.airPlace.getValue())) break label_9;
            return false;
        }
        if (this.strictDirection.getValue()) {
            var tmp0 = WorldUtils.getDirection(arg1, true);
            var4 = tmp0;
            if (tmp0 != null) break label_9;
        } else {
            var tmp1 = PistonHelper.getPlaceSide(arg1, (arg0) -> true);
            var4 = tmp1;
            if ((tmp1 != null) || (this.airPlace.getValue())) break label_9;
        }
        return false;
        label_9:
        var5 = mc.player.getInventory().getSelectedSlot();
        doSwap(arg0, var5);
        if (!(shouldRotate())) break label_13;
        if (arg2 != null) {
            pistonFacingSnap(arg2);
            break label_13;
        }
        Object var6 = (var4 != null ? var4 : Direction.UP);
        doRotate(arg1, var6);
        label_13:
        if (var4 != null) {
            PistonHelper.placeBlock(arg1, var4, false);
        } else {
            PistonHelper.clickBlock(arg1, Direction.UP, false);
        }
        restoreSlot(arg0, var5);
        LightningClient.RENDER_MANAGER.getRenderPositions().removeIf((arg1) -> arg1.getPos().equals(arg0));
        LightningClient.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(arg1));
        return true;
    }

    private void placeCrystal(int arg0, BlockPos arg1) {
        Object var3 = arg1.below();
        Object var4 = PistonHelper.getClickSide(var3);
        if (var4 != null) break label_1;
        if (this.airPlace.getValue()) {
            var3 = arg1;
            var4 = Direction.UP;
            break label_1;
        }
        var4 = Direction.UP;
        label_1:
        if (arg0 == -1) return;
        int var5 = mc.player.getInventory().getSelectedSlot();
        doSwap(arg0, var5);
        if (shouldRotate()) {
            doRotate(var3, var4);
        }
        PistonHelper.clickBlock(var3, var4, false);
        restoreSlot(arg0, var5);
        LightningClient.RENDER_MANAGER.getRenderPositions().removeIf((arg1) -> arg1.getPos().equals(arg0));
        LightningClient.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(arg1));
    }

    private void placeTorch(int arg0, BlockPos arg1) {
        if ((!(PistonHelper.canPlace(arg1, null, this.airPlace.getValue(), true))) || (arg0 == -1)) {
            return;
        }
        Object var3 = PistonHelper.getPlaceSides(arg1, null);
        if (var3.isEmpty()) {
            if (this.airPlace.getValue()) {
                int var4 = mc.player.getInventory().getSelectedSlot();
                doSwap(arg0, var4);
                if (shouldRotate()) {
                    doRotate(arg1, Direction.UP);
                }
                PistonHelper.clickBlock(arg1, Direction.UP, false);
                restoreSlot(arg0, var4);
                LightningClient.RENDER_MANAGER.getRenderPositions().removeIf((arg1) -> arg1.getPos().equals(arg0));
                LightningClient.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(arg1));
                return;
            }
            return;
        }
        var4 = mc.player.getInventory().getSelectedSlot();
        Object var5 = var3.iterator();
        label_5:
        if (var5.hasNext()) {
            Object var6 = ((Direction) var5.next());
            if ((mc.level.getBlockState(arg1.relative(var6)).getBlock() instanceof PistonBaseBlock)) break label_5;
            if (var6 == Direction.UP) {
                break label_5;
            }
            doSwap(arg0, var4);
            if (shouldRotate()) {
                doRotate(arg1, var6);
            }
            PistonHelper.placeBlock(arg1, var6, false);
            restoreSlot(arg0, var4);
            LightningClient.RENDER_MANAGER.getRenderPositions().removeIf((arg1) -> arg1.getPos().equals(arg0));
            LightningClient.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(arg1));
            return;
        }
    }

    private void attackCrystalAt(BlockPos arg0) {
        Object var2 = mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(arg0).inflate(1.0), (arg0) -> arg0.isAlive());
        if (var2.isEmpty()) break label_1;
        double var3 = ((double) (this.breakRange.getValue().floatValue() * this.breakRange.getValue().floatValue()));
        Object var5 = ((EndCrystal) var2.stream().filter((arg1) -> mc.player.distanceToSqr(arg1) <= arg0).min(Comparator.comparingDouble((arg0) -> mc.player.distanceToSqr(arg0))).orElse(null));
        if (var5 == null) break label_1;
        if (shouldRotate()) {
            Object var6 = var5.position().add(0.0, 0.5, 0.0);
            Object var7 = RotationUtils.getRotations(var6);
            LightningClient.ROTATION_MANAGER.silentRotate(var7[0], var7[1]);
        }
        mc.getConnection().send(new ServerboundAttackPacket(var5.getId()));
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        label_1:
    }

    private void breakBlock(BlockPos arg0) {
        if ((((mc.player == null) || (mc.getConnection() == null)) || (mc.level == null)) || (arg0 == null)) {
            return;
        }
        Object var2 = PistonHelper.getClickSide(arg0);
        if (var2 == null) {
            var2 = Direction.UP;
        }
        int var3 = findPickaxe();
        int var4 = mc.player.getInventory().getSelectedSlot();
        if (var3 != -1) {
            doSwap(var3, var4);
        }
        if (shouldRotate()) {
            doRotate(arg0, var2);
        }
        Object var5 = var2;
        NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, arg0, arg1, arg2));
        NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, arg0, arg1, arg2));
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        if (var3 != -1) {
            restoreSlot(var3, var4);
        }
    }

    private int findPickaxe() {
        int var1 = findItem(Items.NETHERITE_PICKAXE);
        if (var1 != -1) return var1;
        var1 = findItem(Items.DIAMOND_PICKAXE);
        if (var1 != -1) return var1;
        var1 = findItem(Items.IRON_PICKAXE);
        if (var1 != -1) return var1;
        return -1;
    }

    private boolean shouldRotate() {
        return !(this.rotate.getValue().equalsIgnoreCase("None"));
    }

    private void doRotate(BlockPos arg0, Direction arg1) {
        Object var3 = Vec3.atCenterOf(arg0).add((((double) arg1.getStepX()) * 0.5), (((double) arg1.getStepY()) * 0.5), (((double) arg1.getStepZ()) * 0.5));
        Object var4 = RotationUtils.getRotations(var3);
        LightningClient.ROTATION_MANAGER.silentRotate(var4[0], var4[1]);
    }

    private void pistonFacingSnap(Direction arg0) {
        if (arg0 == Direction.EAST) {
            LightningClient.ROTATION_MANAGER.silentRotate(-90.0f, 0.0f);
            break label_3;
        }
        if (arg0 == Direction.WEST) {
            LightningClient.ROTATION_MANAGER.silentRotate(90.0f, 0.0f);
            break label_3;
        }
        if (arg0 == Direction.NORTH) {
            LightningClient.ROTATION_MANAGER.silentRotate(180.0f, 0.0f);
            break label_3;
        }
        LightningClient.ROTATION_MANAGER.silentRotate(0.0f, 0.0f);
        label_3:
    }

    private void doSwap(int arg0, int arg1) {
        InventoryUtils.switchSlot(this.swapAction.getValue(), arg0, arg1);
    }

    private void restoreSlot(int arg0, int arg1) {
        InventoryUtils.switchBack(this.swapAction.getValue(), arg0, arg1);
    }

    private int findItem(Item arg0) {
        if (this.swapAction.getValue().equalsIgnoreCase("None")) {
            Object var2 = mc.player.getMainHandItem();
            if (!(var2.isEmpty()) && var2.getItem() == arg0) {
            } else {
            }
            return -1;
        }
        if (!(this.swapAction.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.swapAction.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        var2 = InventoryUtils.HOTBAR_END;
        int var3 = 0;
        while (var3 <= var2) {
            Object var4 = mc.player.getInventory().getItem(var3);
            if (var4.isEmpty()) break label_8;
            if (var4.getItem() != arg0) {
                break label_8;
            }
            return var3;
            label_8:
            var3++;
        }
        return -1;
    }

    private int findPiston() {
        int var1 = findBlock(Blocks.PISTON);
        if (var1 != -1) return var1;
        return findBlock(Blocks.STICKY_PISTON);
    }

    private int findBlock(Block arg0) {
        if (this.swapAction.getValue().equalsIgnoreCase("None")) {
            Object var4 = mc.player.getMainHandItem();
            if (var4.isEmpty()) break label_0;
            var tmp0 = var4.getItem();
            Object var3 = tmp0;
            if (!((tmp0 instanceof BlockItem))) break label_0;
            var tmp1 = ((BlockItem) var3);
            Object var2 = tmp1;
            if (tmp1.getBlock() == arg0) return mc.player.getInventory().getSelectedSlot();
            label_0:
            return -1;
        }
        if (!(this.swapAction.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.swapAction.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        var2 = InventoryUtils.HOTBAR_END;
        var3 = 0;
        while (var3 <= var2) {
            Object var6 = mc.player.getInventory().getItem(var3);
            if (var6.isEmpty()) break label_7;
            var tmp2 = var6.getItem();
            Object var5 = tmp2;
            if (!((tmp2 instanceof BlockItem))) break label_7;
            var tmp3 = ((BlockItem) var5);
            var4 = tmp3;
            if (tmp3.getBlock() != arg0) {
                break label_7;
            }
            return var3;
            label_7:
            var3++;
        }
        return -1;
    }

    private int findRedstone() {
        int var1 = findItem(Items.REDSTONE_BLOCK);
        if (var1 != -1) {
            this.usingTorch = false;
            return var1;
        }
        var1 = findItem(Items.REDSTONE_TORCH);
        if (var1 != -1) {
            this.usingTorch = true;
            return var1;
        }
        return -1;
    }

    private Player pickTarget() {
        double var1 = ((double) this.targetRange.getValue().floatValue());
        return ((Player) mc.level.players().stream().filter((arg0) -> arg0 != mc.player).filter((arg0) -> !(EntityUtils.isGhost(arg0))).filter((arg0) -> {
    if (arg0.isSpectator()) break label_0;
    return arg0.isAlive();
}).filter((arg1) -> mc.player.distanceToSqr(arg1) <= (arg0 * arg0)).filter((arg0) -> !(LightningClient.FRIEND_MANAGER.contains(arg0.getName().getString()))).filter(() -> {
    if (!(this.inAirTarget.getValue())) {
        if (arg0.onGround()) {
        }
    } else {
    }
    return false;
}).min(getComparator()).orElse(null));
    }

    private Comparator getComparator() {
        Object var1 = this.sort.getValue();
        int var2 = -1;
        switch (var1.hashCode()) {
            case 136178382 -> { break label_0; }
            default -> { break label_1; }
        }
        label_0:
        if (!(var1.equals("LowestHealth"))) break label_1;
        var2 = 0;
        label_1:
        switch (var2) {
            case 0 -> { break label_2; }
            default -> { break label_3; }
        }
        label_2:
        break label_4;
        label_3:
        label_4:
        return Comparator.comparingDouble((arg0) -> mc.player.distanceToSqr(arg0));
    }

    private boolean inRange(BlockPos arg0, double arg1) {
        return mc.player.distanceToSqr(Vec3.atCenterOf(arg0)) <= (arg1 * arg1);
    }

    private boolean canSupportCrystal(BlockPos arg0, BlockPos arg1) {
        if (((mc.level == null) || (arg1 == null)) || (arg0 == null)) {
            return false;
        }
        if (!(inRange(arg1, ((double) this.placeRange.getValue().floatValue())))) return false;
        if (!(mc.level.getBlockState(arg0).isAir()) && !(mc.level.getBlockState(arg0).canBeReplaced())) return false;
        if (!(mc.level.getBlockState(arg0.above()).isAir()) && !(mc.level.getBlockState(arg0.above()).canBeReplaced())) return false;
        if ((PistonHelper.hasEntityBlockCrystal(arg0, false)) || (PistonHelper.hasEntityBlockCrystal(arg0.above(), false))) {
            return false;
        }
        if (!(mc.level.getBlockState(arg1).isAir()) && !(mc.level.getBlockState(arg1).canBeReplaced())) return false;
        if (!(PistonHelper.canPlace(arg1, null, false, true))) return false;
        if (this.strictDirection.getValue()) {
            if (WorldUtils.getDirection(arg1, true) != null) {
            }
        } else {
        }
        return false;
    }

    private BlockPos findSupportForPiston(BlockPos arg0, Direction arg1, Direction arg2, BlockPos arg3) {
        if ((mc.level == null) || (arg0 == null)) {
            return null;
        }
        if (arg3 != null && arg0.distManhattan(arg3) == 1) return null;
        var tmp0 = new Direction[3];
        tmp0[0] = Direction.DOWN;
        tmp0[1] = arg2;
        tmp0[2] = arg2.getOpposite();
        Object var5 = tmp0;
        Object var6 = tmp0;
        int var7 = var6.length;
        int var8 = 0;
        while (var8 < var7) {
            Object var9 = var6[var8];
            Object var10 = arg0.relative(var9);
            if (!(inRange(var10, ((double) this.placeRange.getValue().floatValue())))) break label_6;
            if (!(mc.level.getBlockState(var10).isAir())) {
                if (!(mc.level.getBlockState(var10).canBeReplaced())) break label_6;
            }
            if (!(PistonHelper.canPlace(var10, null, false, true))) break label_6;
            if (this.strictDirection.getValue() && WorldUtils.getDirection(var10, true) == null) {
                break label_6;
            }
            return var10;
            label_6:
            var8++;
        }
        return null;
    }

    private BlockPos findPistonSupportRepair(BlockPos arg0, Direction arg1) {
        if ((mc.level == null) || (arg0 == null)) {
            return null;
        }
        Object var3 = arg0.below();
        if ((isReservedForSetup(var3)) || (!(inRange(var3, ((double) this.placeRange.getValue().floatValue()))))) break label_3;
        if (!(mc.level.getBlockState(var3).isAir())) {
            if (!(mc.level.getBlockState(var3).canBeReplaced())) break label_3;
        }
        if (PistonHelper.canPlace(var3, null, false, true)) return var3;
        label_3:
        Object var4 = Direction.Plane.HORIZONTAL.iterator();
        label_4:
        if (var4.hasNext()) {
            Object var5 = ((Direction) var4.next());
            if (arg1 != null) {
                if (var5 == arg1.getOpposite()) break label_4;
            }
            var tmp0 = arg0.relative(var5);
            Object var6 = tmp0;
            if ((isReservedForSetup(tmp0)) || (!(inRange(var6, ((double) this.placeRange.getValue().floatValue()))))) break label_4;
            if (!(mc.level.getBlockState(var6).isAir())) {
                if (!(mc.level.getBlockState(var6).canBeReplaced())) break label_4;
            }
            if (!(PistonHelper.canPlace(var6, null, false, true))) {
                break label_4;
            }
            return var6;
        }
        return null;
    }

    private boolean isReservedForSetup(BlockPos arg0) {
        if (!(arg0.equals(this.redstonePos)) && !(arg0.equals(this.crystalPos))) {
            if (arg0.equals(this.crystalSupportPos)) {
            }
        } else {
        }
        return false;
    }

    public int findSupportBlock() {
        int var1 = findBlock(Blocks.OBSIDIAN);
        if (var1 == -1) {
            var1 = findBlock(Blocks.CRYING_OBSIDIAN);
        }
        if (var1 == -1) {
            var1 = findBlock(Blocks.ENDER_CHEST);
        }
        if (var1 == -1) {
            var1 = findBlock(Blocks.NETHERITE_BLOCK);
        }
        if (var1 == -1) {
            var1 = findBlock(Blocks.COBBLESTONE);
        }
        if (var1 == -1) {
            var1 = findBlock(Blocks.STONE);
        }
        return var1;
    }

    private boolean isObsidian(BlockPos arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        Object var2 = mc.level.getBlockState(arg0).getBlock();
        if (var2 != Blocks.OBSIDIAN && var2 != Blocks.CRYING_OBSIDIAN && var2 != Blocks.BEDROCK) {
            if (var2 == Blocks.ENDER_CHEST) {
            }
        } else {
        }
        return false;
    }

    private boolean isSolidBlock(BlockPos arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        Object var2 = mc.level.getBlockState(arg0);
        if (var2.isAir()) break label_2;
        return var2.isSolid();
    }

    private boolean isRedstone(BlockPos arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        Object var2 = mc.level.getBlockState(arg0).getBlock();
        if (var2 != Blocks.REDSTONE_BLOCK) {
            if (var2 == Blocks.REDSTONE_TORCH) {
            }
        } else {
        }
        return false;
    }

    private void resetState() {
        this.pistonPos = null;
        this.redstonePos = null;
        this.crystalPos = null;
        this.crystalForcedPush = false;
        this.crystalSupportPos = null;
        this.pistonSupportPos = null;
        this.lastPiston = null;
        this.face = null;
        this.usingTorch = false;
    }

}
