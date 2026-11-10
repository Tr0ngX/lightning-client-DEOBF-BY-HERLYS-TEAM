package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FeetTrapModule extends Module {
    public ModeSetting autoSwitch;
    public NumberSetting limit;
    public NumberSetting delay;
    public NumberSetting range;
    public BooleanSetting extension;
    public BooleanSetting floor;
    public BooleanSetting corners;
    public BooleanSetting jumpDisable;
    public ModeSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting airPlace;
    public BooleanSetting grim;
    public BooleanSetting multiTask;
    public BooleanSetting swing;
    public BooleanSetting antiBreak;
    public BooleanSetting selfDisable;
    public BooleanSetting render;
    public BooleanSetting attack;
    public BooleanSetting attackRotate;
    public NumberSetting attackRange;
    public NumberSetting attackAge;
    public BooleanSetting attackMultiTask;
    public BooleanSetting attackSwing;
    private int ticks;
    private int targetCount;
    private Set targetPositions;
    private final Map breakStates;

    public FeetTrapModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        this.limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", 4, 1, 20);
        this.delay = new NumberSetting("Delay", "The amount of ticks waited between each group of placements.", 0, 0, 20);
        this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
        this.extension = new BooleanSetting("Extension", "Extends the trap if there are entities obstructing block placement.", false);
        this.floor = new BooleanSetting("Floor", "Places blocks under your feet as well.", true);
        this.corners = new BooleanSetting("Corners", "Also covers the four diagonals at feet level.", false);
        this.jumpDisable = new BooleanSetting("JumpDisable", "Toggles off the module whenever you leave the ground.", false);
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Grim";
        this.rotate = new ModeSetting("Rotate", "The rotation mode when placing blocks.", "Grim", tmp0);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", true);
        this.airPlace = new BooleanSetting("AirPlace", "Places blocks in the air using the block below as support, facing UP, when no direct neighbor is available.", false);
        this.grim = new BooleanSetting("Grim", "AirPlace only: places from the offhand via a swap-and-swapback trick instead of the mainhand.", new BooleanSetting.Visibility(this.airPlace, true), false);
        this.multiTask = new BooleanSetting("MultiTask", "Allows placing while an item is already in use (eating, blocking, drawing a bow).", false);
        this.swing = new BooleanSetting("Swing", "Sends a swing packet whenever placing a block.", true);
        this.antiBreak = new BooleanSetting("AntiBreak", "Pre-places extra blocks around a spot an enemy is actively digging through toward your trap.", false);
        this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.attack = new BooleanSetting("Attack", "Attacks any crystal that lands on one of your trap positions.", false);
        this.attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), 3.0f, 1.0f, 6.0f);
        this.attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
        this.attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
        this.ticks = 0;
        this.targetCount = 0;
        this.targetPositions = new LinkedHashSet();
        this.breakStates = new HashMap();
    }

    private float getMiningSpeed(BlockState arg0, Player arg1) {
        Object var3 = new ItemStack(Items.NETHERITE_PICKAXE);
        float var4 = var3.getDestroySpeed(arg0);
        if (var4 > 1.0f) {
            int var5 = 5;
            var4 = (var4 + ((float) ((var5 * var5) + 1)));
        }
        if (!(arg1.onGround())) {
            var4 = (var4 / 5.0f);
        }
        return var4;
    }

    public String getMetaData() {
        return String.valueOf(this.targetCount);
    }

    public void onDisable() {
        this.ticks = 0;
        this.targetCount = 0;
        this.targetPositions.clear();
        this.breakStates.clear();
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent arg0) {
        if (this.jumpDisable.getValue()) {
            setToggled(false);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((!(this.antiBreak.getValue())) || (mc.level == null)) {
            return;
        }
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundBlockDestructionPacket))) return;
        Object var3 = ((ClientboundBlockDestructionPacket) var2);
        if (!((mc.level.getEntity(var3.getId()) instanceof Player))) return;
        ((FeetTrapModule.BreakState) this.breakStates.computeIfAbsent(var3.getId(), () -> new FeetTrapModule.BreakState(this, this, arg0.intValue()))).startBreak(var3.getPos(), 0.7f);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if ((!(this.jumpDisable.getValue())) || (mc.player.onGround())) break label_2;
        setToggled(false);
        return;
        label_2:
        if (this.ticks < this.delay.getValue().intValue()) {
            this.ticks = (this.ticks + 1);
            return;
        }
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("None"))) break label_5;
        var tmp0 = mc.player.getMainHandItem().getItem();
        Object var3 = tmp0;
        if ((tmp0 instanceof BlockItem)) {
            var tmp1 = ((BlockItem) var3);
            Object var2 = tmp1;
            if (isBlastProof(tmp1.getBlock())) break label_5;
        }
        LightningClient.CHAT_MANAGER.tagged("You are currently not holding any blast-proof blocks.", getName());
        setToggled(false);
        return;
        label_5:
        int var4 = findBlastProofBlock();
        int var5 = mc.player.getInventory().getSelectedSlot();
        if (var4 == -1) {
            LightningClient.CHAT_MANAGER.tagged("No blast-proof blocks could be found in your hotbar.", getName());
            setToggled(false);
            return;
        }
        this.targetPositions = getTargets();
        if (this.antiBreak.getValue()) {
            applyAntiBreak();
        }
        Object var6 = this.targetPositions.stream().filter(() -> mc.player.distanceToSqr(Vec3.atCenterOf(arg0)) <= Mth.square(this.range.getValue().doubleValue())).filter(WorldUtils::isPlaceable).toList();
        this.targetCount = var6.size();
        if (this.attack.getValue()) {
            attackTrapCrystals();
        }
        if (var6.isEmpty()) {
            if (this.selfDisable.getValue()) {
                setToggled(false);
            }
            return;
        }
        if (!(this.multiTask.getValue()) && mc.player.isUsingItem()) return;
        LightningClient.ROTATION_MANAGER.beginBatchRotation();
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), var4, var5);
        int var7 = 0;
        Object var8 = new ArrayList();
        Object var9 = var6.iterator();
        while (var9.hasNext()) {
            Object var10 = ((BlockPos) var9.next());
            if (var7 >= this.limit.getValue().intValue()) {
                break label_18;
            }
            Object var11 = WorldUtils.getDirection(var10, var8, this.strictDirection.getValue());
            if (var11 == null) {
                if (!(this.airPlace.getValue())) {
                    break label_13;
                }
                placeAir(var10);
                var8.add(var10);
                var7++;
                break label_13;
            }
            if (!(WorldUtils.placeBlock(var10, var11, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue()))) {
                break label_13;
            }
            var8.add(var10);
            var7++;
        }
        label_18:
        InventoryUtils.switchBack(this.autoSwitch.getValue(), var4, var5);
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        break label_22;
        Object var12 = null;
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        throw var12;
        label_22:
        this.ticks = 0;
    }

    private void placeAir(BlockPos arg0) {
        if (!(this.grim.getValue())) {
            WorldUtils.airPlaceBlock(arg0, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
            return;
        }
        Object var3 = Vec3.atCenterOf(arg0).add(0.0, 0.5, 0.0);
        if (this.rotate.getValue().equalsIgnoreCase("None")) break label_2;
        Object var4 = RotationUtils.getRotations(var3);
        if (LightningClient.ROTATION_MANAGER.isBatchingRotations()) {
            LightningClient.ROTATION_MANAGER.batchRotate(var4[0], var4[1]);
            break label_2;
        }
        LightningClient.ROTATION_MANAGER.silentRotate(var4[0], var4[1]);
        label_2:
        if (!(this.render.getValue())) break label_3;
        var tmp0 = new RenderPosition(arg0);
        Object var2 = tmp0;
        if (LightningClient.RENDER_MANAGER.renderPositions.contains(tmp0)) break label_3;
        LightningClient.RENDER_MANAGER.renderPositions.add(var2);
        label_3:
        var4 = new BlockHitResult(var3, Direction.UP, arg0, false);
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        NetworkUtils.sendSequencedPacket((arg1) -> new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND, arg0, arg1));
        if (this.swing.getValue()) {
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.OFF_HAND));
        }
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
    }

    private void attackTrapCrystals() {
        LightningClient.ROTATION_MANAGER.beginBatchRotation();
        Object var1 = mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(mc.player.blockPosition()).inflate((this.range.getValue().doubleValue() + 3.0))).iterator();
        while (var1.hasNext()) {
            Object var2 = ((EndCrystal) var1.next());
            if ((!(var2.isAlive())) || (var2.isRemoved())) break label_0;
            if (var2.tickCount < this.attackAge.getValue().intValue()) {
                break label_0;
            }
            Object var3 = var2.getBoundingBox();
            int var4 = 0;
            Object var5 = this.targetPositions.iterator();
            label_2:
            if (!(var5.hasNext())) break label_4;
            Object var6 = ((BlockPos) var5.next());
            if (!(mc.level.getBlockState(var6).canBeReplaced())) break label_2;
            if (!(new AABB(var6).intersects(var3))) {
                break label_2;
            }
            var4 = 1;
            break label_4;
            label_4:
            if ((!var4) || (var3.distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue()))) break label_0;
            if (!(this.attackMultiTask.getValue()) && mc.player.isUsingItem()) {
                break label_0;
            }
            if (!(this.attackRotate.getValue())) break label_6;
            var5 = RotationUtils.getRotations(var2.getBoundingBox().getCenter());
            LightningClient.ROTATION_MANAGER.silentRotate(var5[0], var5[1]);
            var6 = var3.contains(mc.player.getEyePosition());
            if ((var6) || (WorldUtils.canSee(var2.getBoundingBox().getCenter()))) break label_6;
            break label_0;
            label_6:
            mc.getConnection().send(new ServerboundAttackPacket(var2.getId()));
            if (!(this.attackSwing.getValue())) {
                break label_0;
            }
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        LightningClient.ROTATION_MANAGER.endBatchRotation();
    }

    private void applyAntiBreak() {
        Object var1 = this.breakStates.values().iterator();
        while (var1.hasNext()) {
            Object var2 = ((FeetTrapModule.BreakState) var1.next());
            var2.onTick();
        }
        label_1:
        if ((!(this.antiBreak.getValue())) || (this.targetPositions.isEmpty())) {
            return;
        }
        var1 = new ArrayList();
        var2 = this.targetPositions.iterator();
        while (var2.hasNext()) {
            Object var3 = ((BlockPos) var2.next());
            int var4 = 0;
            Object var5 = this.breakStates.values().iterator();
            label_5:
            if (!(var5.hasNext())) break label_7;
            Object var6 = ((FeetTrapModule.BreakState) var5.next());
            if (!(var6.isBreaking(var3))) {
                break label_5;
            }
            var4 = 1;
            break label_7;
            label_7:
            if (!var4) {
                break label_4;
            }
            var5 = Direction.values();
            var6 = var5.length;
            int var7 = 0;
            while (var7 < var6) {
                Object var8 = var5[var7];
                if (var8 == Direction.DOWN) break label_11;
                var tmp0 = var3.relative(var8);
                Object var9 = tmp0;
                if (((this.targetPositions.contains(tmp0)) || (var1.contains(var9))) || (!(mc.level.getBlockState(var9).canBeReplaced()))) break label_11;
                if (isEntityBlocking(var9)) {
                    break label_11;
                }
                var1.add(var9);
                label_11:
                var7++;
            }
        }
        this.targetPositions.addAll(var1);
    }

    private boolean isEntityBlocking(BlockPos arg0) {
        Object var2 = new AABB(arg0);
        Object var3 = mc.level.entitiesForRendering().iterator();
        label_0:
        if (var3.hasNext()) {
            Object var4 = ((Entity) var3.next());
            if ((((var4.distanceToSqr(mc.player) > 10.0) || ((var4 instanceof EndCrystal))) || ((var4 instanceof ItemEntity))) || ((var4 instanceof Arrow))) break label_0;
            if (!(var4.getBoundingBox().intersects(var2))) {
                break label_0;
            }
            return true;
        }
        return false;
    }

    private Set getTargets() {
        Object var1 = new LinkedHashSet(HoleUtils.getFeetPositions(mc.player, this.extension.getValue(), this.floor.getValue(), false));
        if (!(this.corners.getValue())) return var1;
        Object var2 = mc.player.getBoundingBox();
        int var3 = Mth.floor(mc.player.getY());
        int var4 = Mth.floor(var2.minX);
        while (((double) var4) < Math.ceil(var2.maxX)) {
            int var5 = Mth.floor(var2.minZ);
            while (((double) var5) < Math.ceil(var2.maxZ)) {
                Object var6 = new BlockPos(var4, var3, var5);
                var tmp0 = new BlockPos[4];
                tmp0[0] = var6.north().east();
                tmp0[1] = var6.north().west();
                tmp0[2] = var6.south().east();
                tmp0[3] = var6.south().west();
                Object var7 = tmp0;
                int var8 = var7.length;
                int var9 = 0;
                while (var9 < var8) {
                    Object var10 = var7[var9];
                    if (isEntityBlocking(var10)) {
                    } else {
                        var1.add(var10);
                    }
                    var9++;
                }
                var5++;
            }
            var4++;
        }
        return var1;
    }

    private int findBlastProofBlock() {
        float var1 = -1.0f;
        int var2 = -1;
        int var3 = 0;
        while (var3 <= 8) {
            Object var6 = mc.player.getInventory().getItem(var3).getItem();
            if (!((var6 instanceof BlockItem))) break label_4;
            var tmp0 = ((BlockItem) var6);
            Object var4 = tmp0;
            var tmp1 = tmp0.getBlock();
            Object var5 = tmp1;
            if (!(isBlastProof(tmp1))) {
                break label_4;
            }
            float var7 = var5.defaultDestroyTime();
            if (var7 == -1.0f) return var3;
            if (var7 <= var1) {
                break label_4;
            }
            var1 = var7;
            var2 = var3;
            label_4:
            var3++;
        }
        return var2;
    }

    private boolean isBlastProof(Block arg0) {
        if (arg0 != Blocks.OBSIDIAN && arg0 != Blocks.ENDER_CHEST && arg0 != Blocks.CRYING_OBSIDIAN && arg0 != Blocks.NETHERITE_BLOCK && arg0 != Blocks.RESPAWN_ANCHOR && arg0 != Blocks.ANCIENT_DEBRIS && arg0 != Blocks.ENCHANTING_TABLE && arg0 != Blocks.ANVIL && arg0 != Blocks.CHIPPED_ANVIL) {
            if (arg0 == Blocks.DAMAGED_ANVIL) {
            }
        } else {
        }
        return false;
    }


    class BreakState {
        final int entityId;
        final FeetTrapModule.BreakTask current;
        final FeetTrapModule.BreakTask doubleMine;
        final FeetTrapModule this$0;

        BreakState(FeetTrapModule arg0, FeetTrapModule arg1, int arg2) {
            Objects.requireNonNull(arg0);
            Object var4 = arg1;
            Objects.requireNonNull(var4);
            this.this$0 = var4;
            this.current = new FeetTrapModule.BreakTask();
            this.doubleMine = new FeetTrapModule.BreakTask();
            this.entityId = arg2;
        }

        void startBreak(BlockPos arg0, float arg1) {
            if (!(!this.current.active) && !(this.current.pos.equals(arg0))) {
                if (!this.doubleMine.active) {
                    this.doubleMine.copyFrom(this.current);
                    this.doubleMine.targetSpeed = 1.0f;
                }
                this.current.reset();
            }
            if (!(!this.current.active)) {
                if (!(this.current.pos.equals(arg0))) {
                }
                this.current.start(arg0, arg1);
            }
        }

        void onTick() {
            tickTask(this.current);
            tickTask(this.doubleMine);
        }

        private void tickTask(FeetTrapModule.BreakTask arg0) {
            if (!arg0.active) return;
            Object var2 = IMinecraft.mc.level.getEntity(this.entityId);
            if (!((var2 instanceof Player))) {
                arg0.reset();
                return;
            }
            Object var3 = ((Player) var2);
            Object var4 = IMinecraft.mc.level.getBlockState(arg0.pos);
            if (var4.isAir()) {
                arg0.active = false;
                return;
            }
            float var5 = var4.getDestroySpeed(IMinecraft.mc.level, arg0.pos);
            if (var5 == -1.0f) return;
            float var6 = this.this$0.getMiningSpeed(var4, var3);
            arg0.progress = (arg0.progress + ((var6 / var5) / 30.0f));
            if (arg0.progress >= arg0.targetSpeed) {
                arg0.active = false;
            }
        }

        boolean isBreaking(BlockPos arg0) {
            if (this.current.pos != null && this.current.pos.equals(arg0) && !(!this.current.active)) return true;
            if ((this.doubleMine.pos == null) || (!(this.doubleMine.pos.equals(arg0)))) break label_1;
            return !(!this.doubleMine.active);
        }

    }

    class BreakTask {
        BlockPos pos;
        float targetSpeed;
        float progress;
        boolean active;

        void start(BlockPos arg0, float arg1) {
            this.pos = arg0;
            this.targetSpeed = arg1;
            this.progress = 0.0f;
            this.active = true;
        }

        void copyFrom(FeetTrapModule.BreakTask arg0) {
            this.pos = arg0.pos;
            this.targetSpeed = arg0.targetSpeed;
            this.progress = arg0.progress;
            this.active = arg0.active;
        }

        void reset() {
            this.active = false;
            this.progress = 0.0f;
        }

    }
}
