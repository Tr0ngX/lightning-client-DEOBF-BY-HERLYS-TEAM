package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.events.impl.PlayerMineEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.HitboxDesyncModule;
import lightning.modules.impl.movement.SpeedModule;
import lightning.modules.impl.movement.StepModule;
import lightning.modules.impl.movement.TickShiftModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.modules.impl.player.KeyActionModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Surround", description = "Automatically places blocks at your feet to prevent crystal damage.", category = Module.Category.COMBAT)
public class SurroundModule extends Module {
    public ModeSetting autoSwitch;
    public ModeSetting timing;
    public NumberSetting bpt;
    public NumberSetting range;
    public BooleanSetting await;
    public ModeSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting airPlace;
    public BooleanSetting attack;
    public BooleanSetting attackRotate;
    public NumberSetting attackRange;
    public NumberSetting attackAge;
    public BooleanSetting attackMultiTask;
    public BooleanSetting attackSwing;
    public BooleanSetting center;
    public BooleanSetting floor;
    public BooleanSetting underFeet;
    public BooleanSetting extension;
    public BooleanSetting blocker;
    public BooleanSetting blockerUp;
    public BooleanSetting blockerCorner;
    public BooleanSetting blockerEdge;
    public BooleanSetting advoidHelp;
    public NumberSetting advoidHelpHealth;
    public BooleanSetting whileEating;
    public BooleanSetting advanced;
    public BooleanSetting chorusCenter;
    public BooleanSetting predict;
    private int packetsSent;
    private boolean isWorking;
    public BooleanSetting selfDisable;
    public BooleanSetting itemDisable;
    public BooleanSetting jumpDisable;
    public BooleanSetting pearlDisable;
    public BooleanSetting chorusDisable;
    public BooleanSetting rubberbandDisable;
    public BooleanSetting stepToggle;
    public BooleanSetting speedToggle;
    public BooleanSetting tickShiftToggle;
    public BooleanSetting render;
    private Set targetPositions;
    private BlockPos lastPosition;
    private int blocksPlaced;
    private boolean awaitChorusCenter;
    private final Map activeMines;
    private static final Direction[] HORIZONTALS;
    private long lastPlacementTime;

    public SurroundModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        var tmp0 = new String[2];
        tmp0[0] = "Vanilla";
        tmp0[1] = "Sequential";
        this.timing = new ModeSetting("Timing", "The timing that will be used in replacing broken surround blocks.", "Sequential", tmp0);
        this.bpt = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", 4, 1, 8);
        this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
        this.await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
        var tmp1 = new String[3];
        tmp1[0] = "None";
        tmp1[1] = "Normal";
        tmp1[2] = "Grim";
        this.rotate = new ModeSetting("Rotate", "The rotation mode when placing surround blocks.", "Grim", tmp1);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
        this.attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with your surround.", false);
        this.attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), 3.0f, 1.0f, 6.0f);
        this.attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
        this.attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
        this.center = new BooleanSetting("Center", "Puts you in the center of the block when you surround.", false);
        this.floor = new BooleanSetting("Floor", "Places blocks under your feet as well.", true);
        this.underFeet = new BooleanSetting("UnderFeet", "Replaces the block directly under your feet when it breaks.", new BooleanSetting.Visibility(this.floor, true), true);
        this.extension = new BooleanSetting("Extension", "Extends the surround if there are entities obstructing block placement.", true);
        this.blocker = new BooleanSetting("Blocker", "Pre-places extra blocks where an enemy is digging toward you.", true);
        this.blockerUp = new BooleanSetting("Up", "Blocker: places above/below the edge being mined through.", new BooleanSetting.Visibility(this.blocker, true), true);
        this.blockerCorner = new BooleanSetting("Corner", "Blocker: places the 2 corner cells left/right of the edge being mined through.", new BooleanSetting.Visibility(this.blocker, true), true);
        this.blockerEdge = new BooleanSetting("Edge", "Blocker: places the cell straight out in front of the edge being mined through.", new BooleanSetting.Visibility(this.blocker, true), true);
        this.advoidHelp = new BooleanSetting("AdvoidHelp", "Skips placements that would also wall in a nearby enemy.", true);
        this.advoidHelpHealth = new NumberSetting("AdvoidHelpHealth", "Below this health, place every block regardless -- ignores AdvoidHelp entirely.", new BooleanSetting.Visibility(this.advoidHelp, true), 10.0f, 0.0f, 20.0f);
        this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
        this.advanced = new BooleanSetting("Advanced", "While WhileEating is off, keeps placing through an eat pause as long as your surround still has an open gap (stops once sealed, or if you're stuck inside a block).", new BooleanSetting.Visibility(this.whileEating, false), false);
        this.chorusCenter = new BooleanSetting("CenterOnTP", "Centers you if you have just teleported to surround against crystals easier.", true);
        this.predict = new BooleanSetting("Predict", "Replaces a surround block instantly the moment we see the server's own break packet for it, instead of waiting for the next normal placement cycle.", true);
        this.packetsSent = 0;
        this.isWorking = false;
        this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
        this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module when no valid items are found in your hotbar/inventory.", true);
        this.jumpDisable = new BooleanSetting("JumpDisable", "Toggles off the module whenever your Y level changes.", true);
        this.pearlDisable = new BooleanSetting("PearlDisable", "Toggles off surround whenever you throw or land an ender pearl.", false);
        this.chorusDisable = new BooleanSetting("ChorusDisable", "Toggles off surround whenever you eat a chorus fruit.", false);
        this.rubberbandDisable = new BooleanSetting("RubberbandDisable", "Toggles off surround whenever you get rubberbanded/setback by the server.", false);
        this.stepToggle = new BooleanSetting("StepToggle", "Toggles the step module when you surround.", false);
        this.speedToggle = new BooleanSetting("SpeedToggle", "Toggles the speed module when you surround.", false);
        this.tickShiftToggle = new BooleanSetting("TickShiftToggle", "Toggles the TickShift module when you surround.", false);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.targetPositions = new HashSet();
        this.lastPosition = null;
        this.blocksPlaced = 0;
        this.awaitChorusCenter = false;
        this.activeMines = new HashMap();
        this.lastPlacementTime = 0L;
    }

    public String getMetaData() {
        return ("Packet: " + this.packetsSent);
    }

    public void onEnable() {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        this.packetsSent = 0;
        this.lastPosition = PositionUtils.getFlooredPosition(mc.player);
        this.awaitChorusCenter = false;
        this.activeMines.clear();
        if (this.stepToggle.getValue() && ((StepModule) LightningClient.MODULE_MANAGER.getModule(StepModule.class)).isToggled()) {
            ((StepModule) LightningClient.MODULE_MANAGER.getModule(StepModule.class)).setToggled(false);
        }
        if (this.speedToggle.getValue() && ((SpeedModule) LightningClient.MODULE_MANAGER.getModule(SpeedModule.class)).isToggled()) {
            ((SpeedModule) LightningClient.MODULE_MANAGER.getModule(SpeedModule.class)).setToggled(false);
        }
        if (this.tickShiftToggle.getValue() && ((TickShiftModule) LightningClient.MODULE_MANAGER.getModule(TickShiftModule.class)).isToggled()) {
            ((TickShiftModule) LightningClient.MODULE_MANAGER.getModule(TickShiftModule.class)).setToggled(false);
        }
        if (this.center.getValue()) {
            mc.player.setPos((((double) this.lastPosition.getX()) + 0.5), ((double) this.lastPosition.getY()), (((double) this.lastPosition.getZ()) + 0.5));
        }
        if (this.rotate.getValue().equalsIgnoreCase("Grim")) {
            LightningClient.WORLD_MANAGER.setTimerMultiplier(1.08f);
        }
    }

    public void onDisable() {
        LightningClient.WORLD_MANAGER.setTimerMultiplier(1.0f);
        this.targetPositions.clear();
        this.lastPosition = null;
        this.isWorking = false;
        this.awaitChorusCenter = false;
        this.activeMines.clear();
        this.blocksPlaced = 0;
    }

    @SubscribeEvent
    public void onPlayerMine(PlayerMineEvent arg0) {
        if ((!(this.blocker.getValue())) || (mc.player == null)) {
            return;
        }
        if (arg0.getActorID() == mc.player.getId()) return;
        int var2 = 0;
        Object var3 = ownFootCells().iterator();
        label_3:
        if (!(var3.hasNext())) break label_6;
        Object var4 = ((BlockPos) var3.next());
        if (arg0.getPosition().getY() != var4.getY()) {
            break label_3;
        }
        int var5 = (arg0.getPosition().getX() - var4.getX());
        int var6 = (arg0.getPosition().getZ() - var4.getZ());
        if ((Math.abs(var5) + Math.abs(var6)) != 1) {
            break label_3;
        }
        var2 = 1;
        break label_6;
        label_6:
        if (!var2) return;
        var3 = ((SurroundModule.MineData) this.activeMines.get(arg0.getActorID()));
        if (var3 != null) {
            if (!(var3.pos.equals(arg0.getPosition()))) {
            }
            this.activeMines.put(arg0.getActorID(), new SurroundModule.MineData(arg0.getPosition()));
        } else {
            var3.lastSeenTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent arg0) {
        if (this.jumpDisable.getValue()) {
            setToggled(false);
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) break label_45;
        if (!(!this.awaitChorusCenter)) {
            Object var2 = PositionUtils.getFlooredPosition(mc.player);
            mc.player.setPos((((double) var2.getX()) + 0.5), mc.player.getY(), (((double) var2.getZ()) + 0.5));
            this.lastPosition = var2;
            this.awaitChorusCenter = false;
        }
        if (!(this.jumpDisable.getValue())) break label_2;
        if (mc.player.fallDistance > 2.0) break label_44;
        if (((StepModule) LightningClient.MODULE_MANAGER.getModule(StepModule.class)).isToggled()) break label_1;
        if (!(((SpeedModule) LightningClient.MODULE_MANAGER.getModule(SpeedModule.class)).isToggled())) break label_2;
        label_1:
        if ((this.lastPosition == null) || (this.lastPosition.getY() != PositionUtils.getFlooredPosition(mc.player).getY())) break label_44;
        label_2:
        if ((this.whileEating.getValue()) || (!(EntityUtils.isEating()))) break label_3;
        if (shouldPauseForEating()) break label_45;
        label_3:
        this.blocksPlaced = 0;
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("None"))) break label_4;
        Object var3 = mc.player.getMainHandItem().getItem();
        if (!((var3 instanceof BlockItem))) break label_42;
        var2 = ((BlockItem) var3);
        if (!(isBlastProof(var2.getBlock()))) break label_42;
        label_4:
        if (this.autoSwitch.getValue().equalsIgnoreCase("AltSwap")) break label_5;
        var3 = findBlastProofBlock(0, (!(this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) ? 8 : 35));
        int var4 = mc.player.getInventory().getSelectedSlot();
        if (var3 == -1) {
            if (this.itemDisable.getValue()) {
                LightningClient.CHAT_MANAGER.tagged("No blast-proof blocks could be found in your inventory.", getName());
                setToggled(false);
            }
            this.targetPositions.clear();
            break label_41;
        }
        this.targetPositions = HoleUtils.getFeetPositions(mc.player, this.extension.getValue(), this.floor.getValue(), this.underFeet.getValue(), false);
        if (this.blocker.getValue()) {
            applyBlocker();
        }
        if (this.attack.getValue()) {
            attackTrapCrystals();
        }
        if (this.advoidHelp.getValue()) {
        } else {
        }
        Object var5 = Set.of();
        this.targetPositions.stream().filter(() -> mc.player.distanceToSqr(Vec3.atCenterOf(arg0)) <= Mth.square(this.range.getValue().doubleValue())).filter((arg0) -> {
    if (WorldUtils.isPlaceable(arg0)) break label_0;
    return WorldUtils.isInstantBreakable(arg0);
}).forEach((arg0) -> WorldUtils.instantBreak(arg0, Direction.UP));
        Object var6 = ((HitboxDesyncModule) LightningClient.MODULE_MANAGER.getModule(HitboxDesyncModule.class));
        Object var7 = new ArrayList(this.targetPositions.stream().filter(() -> mc.player.distanceToSqr(Vec3.atCenterOf(arg0)) <= Mth.square(this.range.getValue().doubleValue())).filter((arg1) -> {
    if ((arg0 == null) || (!(arg0.isToggled()))) break label_0;
    return WorldUtils.isPlaceable(arg1, !(arg0.close.getValue()));
}).filter((arg1) -> !(arg0.contains(arg1))).toList());
        Object var8 = mc.player.position().add(mc.player.getDeltaMovement().scale(0.5));
        var7.sort((arg1, arg2) -> {
    int var3 = Integer.compare(arg1.getY(), arg2.getY());
    if (!(!var3)) return var3;
    int var4 = (WorldUtils.getDirection(arg1, false) != null ? 1 : 0);
    int var5 = (WorldUtils.getDirection(arg2, false) != null ? 1 : 0);
    if (var4 != var5) return (!(!var4) ? -1 : 1);
    return Double.compare(Vec3.atCenterOf(arg1).distanceToSqr(arg0), Vec3.atCenterOf(arg2).distanceToSqr(arg0));
});
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var9 = null;
        if (!(this.pearlDisable.getValue())) break label_17;
        if (var9 != null) {
            if (!(var9.isPearlActive())) {
            }
            if ((!(mc.player.isUsingItem())) || (mc.player.getUseItem().getItem() != Items.ENDER_PEARL)) break label_17;
        }
        setToggled(false);
        break label_41;
        label_17:
        if (this.chorusDisable.getValue() && mc.player.isUsingItem() && mc.player.getUseItem().getItem() == Items.CHORUS_FRUIT) {
            setToggled(false);
            break label_41;
        }
        if (var9 != null) {
            if (var9.isPearlActive()) break label_41;
        }
        if (var7.isEmpty()) {
            if (!(this.selfDisable.getValue())) break label_41;
            setToggled(false);
            break label_41;
        }
        this.isWorking = true;
        int var10 = this.rotate.getValue().equalsIgnoreCase("Grim");
        if (!(!var10)) {
            LightningClient.ROTATION_MANAGER.beginBatchRotation();
        }
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), var3, var4);
        Object var11 = new ArrayList();
        Object var12 = var7.iterator();
        while (var12.hasNext()) {
            Object var13 = ((BlockPos) var12.next());
            if (this.blocksPlaced >= this.bpt.getValue().intValue()) {
                break label_35;
            }
            if (!(this.attack.getValue())) break label_26;
            Object var14 = mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(var13).inflate(0.5)).iterator();
            while (var14.hasNext()) {
                Object var15 = ((EndCrystal) var14.next());
                if (!(var15.isAlive())) break label_25;
                mc.getConnection().send(new ServerboundAttackPacket(var15.getId()));
                if (!(this.attackSwing.getValue())) break label_25;
                mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                label_25:
            }
            label_26:
            var14 = WorldUtils.getDirections(var13, var11, this.strictDirection.getValue());
            if (var14.isEmpty()) {
                if (this.airPlace.getValue() && !(this.strictDirection.getValue())) {
                    placeAirBlock(var13);
                    var11.add(var13);
                    this.lastPlacementTime = System.currentTimeMillis();
                    this.blocksPlaced = (this.blocksPlaced + 1);
                    break label_34;
                }
                var15 = WorldUtils.findSupportBlock(var13, var11, this.strictDirection.getValue(), 2);
                if (var15 == null) break label_28;
                Object var16 = WorldUtils.getDirection(var15, var11, this.strictDirection.getValue());
                if ((var16 == null) || (!(WorldUtils.placeBlock(var15, var16, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())))) break label_28;
                var11.add(var15);
                this.lastPlacementTime = System.currentTimeMillis();
                this.blocksPlaced = (this.blocksPlaced + 1);
                label_28:
                break label_34;
            }
            var15 = 0;
            var16 = var14.iterator();
            while (var16.hasNext()) {
                Object var17 = ((Direction) var16.next());
                if (WorldUtils.placeBlock(var13, var17, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) {
                    var11.add(var13);
                    this.lastPlacementTime = System.currentTimeMillis();
                    this.blocksPlaced = (this.blocksPlaced + 1);
                    var15 = 1;
                    break label_32;
                }
            }
            label_32:
            if (var15) break label_34;
            if (this.airPlace.getValue() && !(this.strictDirection.getValue())) {
                placeAirBlock(var13);
                var11.add(var13);
                this.lastPlacementTime = System.currentTimeMillis();
                this.blocksPlaced = (this.blocksPlaced + 1);
                break label_34;
            }
            var16 = WorldUtils.findSupportBlock(var13, var11, this.strictDirection.getValue(), 2);
            if (var16 == null) break label_34;
            var17 = WorldUtils.getDirection(var16, var11, this.strictDirection.getValue());
            if ((var17 == null) || (!(WorldUtils.placeBlock(var16, var17, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())))) break label_34;
            var11.add(var16);
            this.lastPlacementTime = System.currentTimeMillis();
            this.blocksPlaced = (this.blocksPlaced + 1);
            label_34:
        }
        label_35:
        InventoryUtils.switchBack(this.autoSwitch.getValue(), var3, var4);
        if (!(!var10)) {
            LightningClient.ROTATION_MANAGER.endBatchRotation();
        }
        this.isWorking = false;
        break label_41;
        Object var18 = ((KeyActionModule) LightningClient.MODULE_MANAGER.getModule(KeyActionModule.class));
        if (!(!var10)) {
            LightningClient.ROTATION_MANAGER.endBatchRotation();
        }
        this.isWorking = false;
        throw var18;
        label_41:
        break label_45;
        label_42:
        if (this.itemDisable.getValue()) {
            LightningClient.CHAT_MANAGER.tagged("You are currently not holding any blast-proof blocks.", getName());
            setToggled(false);
        }
        this.targetPositions.clear();
        break label_45;
        label_44:
        setToggled(false);
        label_45:
    }

    private void placeAirBlock(BlockPos arg0) {
        WorldUtils.airPlaceBlock(arg0, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) break label_29;
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ClientboundOpenScreenPacket))) break label_2;
        Object var2 = ((ClientboundOpenScreenPacket) var3);
        var3 = ((AutoShopModule) LightningClient.MODULE_MANAGER.getModule(AutoShopModule.class));
        if (var3 != null && var3.isRunning()) return;
        if ((this.isWorking) || ((System.currentTimeMillis() - this.lastPlacementTime) < 1000L)) break label_1;
        if ((System.currentTimeMillis() - WorldUtils.lastInteractablePlaceTime) >= 1000L) break label_2;
        label_1:
        arg0.setCancelled(true);
        mc.getConnection().send(new ServerboundContainerClosePacket(var2.getContainerId()));
        return;
        label_2:
        var3 = arg0.getPacket();
        if (!((var3 instanceof ClientboundPlayerPositionPacket))) break label_5;
        var2 = ((ClientboundPlayerPositionPacket) var3);
        if (!(this.rubberbandDisable.getValue())) break label_3;
        var3 = mc.player.position();
        double var4 = var2.change().position().x;
        double var6 = var2.change().position().y;
        double var8 = var2.change().position().z;
        if (var3.distanceToSqr(var4, var6, var8) < 1.0) break label_3;
        setToggled(false);
        return;
        label_3:
        if (((!(this.chorusCenter.getValue())) || (!(mc.player.isUsingItem()))) || (mc.player.getUseItem().getItem() != Items.CHORUS_FRUIT)) break label_5;
        if (this.chorusDisable.getValue()) {
            setToggled(false);
            return;
        }
        this.awaitChorusCenter = true;
        label_5:
        if (!(this.attack.getValue())) break label_11;
        var3 = arg0.getPacket();
        if (!((var3 instanceof ClientboundAddEntityPacket))) break label_11;
        var2 = ((ClientboundAddEntityPacket) var3);
        if (var2.getType() != EntityTypes.END_CRYSTAL) break label_11;
        var3 = new Vec3(var2.getX(), var2.getY(), var2.getZ());
        var4 = new AABB((var3.x - 1.0), var3.y, (var3.z - 1.0), (var3.x + 1.0), (var3.y + 2.0), (var3.z + 1.0));
        int var5 = 0;
        var6 = this.targetPositions.iterator();
        while (var6.hasNext()) {
            Object var7 = ((BlockPos) var6.next());
            if (!(var4.intersects(new AABB(var7)))) {
                if (var3.distanceToSqr(Vec3.atCenterOf(var7)) <= 4.0) {
                }
                var5 = 1;
                break label_9;
            }
        }
        label_9:
        if ((!var5) || (mc.player.getEyePosition().distanceToSqr(var3) > Mth.square(this.attackRange.getValue().doubleValue()))) break label_11;
        if (!(this.attackMultiTask.getValue())) {
            if (mc.player.isUsingItem()) break label_11;
        }
        mc.getConnection().send(new ServerboundAttackPacket(var2.getId()));
        if (!(this.attackSwing.getValue())) break label_11;
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        label_11:
        if ((!(this.timing.getValue().equalsIgnoreCase("Sequential"))) || (!(this.predict.getValue()))) break label_29;
        var3 = arg0.getPacket();
        if (!((var3 instanceof ClientboundBlockUpdatePacket))) break label_29;
        var2 = ((ClientboundBlockUpdatePacket) var3);
        if (((!(var2.getBlockState().isAir())) || (!(this.targetPositions.contains(var2.getPos())))) || (this.blocksPlaced >= this.bpt.getValue().intValue())) break label_29;
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) {
            if (shouldPauseForEating()) break label_29;
        }
        var3 = ((HitboxDesyncModule) LightningClient.MODULE_MANAGER.getModule(HitboxDesyncModule.class));
        if ((var3 == null) || (!(var3.isToggled()))) break label_13;
        var4 = (!(var3.close.getValue()) ? 1 : 0);
        if (!(WorldUtils.isPlaceable(var2.getPos(), var4))) break label_29;
        if (this.autoSwitch.getValue().equalsIgnoreCase("AltSwap")) break label_15;
        var5 = findBlastProofBlock(0, (!(this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) ? 8 : 35));
        var6 = mc.player.getInventory().getSelectedSlot();
        if (var5 == -1) break label_29;
        var7 = WorldUtils.getDirection(var2.getPos(), this.strictDirection.getValue());
        if (var7 != null) {
            LightningClient.ROTATION_MANAGER.beginBatchRotation();
            if (InventoryUtils.switchSlot(this.autoSwitch.getValue(), var5, var6)) {
                WorldUtils.placeBlock(var2.getPos(), var7, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue());
                this.blocksPlaced = (this.blocksPlaced + 1);
                this.lastPlacementTime = System.currentTimeMillis();
                InventoryUtils.switchBack(this.autoSwitch.getValue(), var5, var6);
                LightningClient.ROTATION_MANAGER.endBatchRotation();
                return;
            }
            LightningClient.ROTATION_MANAGER.endBatchRotation();
            break label_22;
            Object var10 = null;
            LightningClient.ROTATION_MANAGER.endBatchRotation();
            throw var10;
            label_22:
            break label_29;
        }
        if ((!(this.airPlace.getValue())) || (this.strictDirection.getValue())) break label_29;
        LightningClient.ROTATION_MANAGER.beginBatchRotation();
        if (InventoryUtils.switchSlot(this.autoSwitch.getValue(), var5, var6)) {
            placeAirBlock(var2.getPos());
            this.blocksPlaced = (this.blocksPlaced + 1);
            this.lastPlacementTime = System.currentTimeMillis();
            InventoryUtils.switchBack(this.autoSwitch.getValue(), var5, var6);
            LightningClient.ROTATION_MANAGER.endBatchRotation();
            return;
        }
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        break label_29;
        Object var11 = null;
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        throw var11;
        label_29:
    }

    private void attackTrapCrystals() {
        LightningClient.ROTATION_MANAGER.beginBatchRotation();
        Object var1 = mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(mc.player.blockPosition()).inflate((this.attackRange.getValue().doubleValue() + 3.0))).iterator();
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
            if ((!var4) || (var3.distanceToSqr(mc.player.getEyePosition()) > (this.attackRange.getValue().doubleValue() * this.attackRange.getValue().doubleValue()))) break label_0;
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

    private void applyBlocker() {
        this.activeMines.entrySet().removeIf((arg0) -> {
    Object var1 = ((SurroundModule.MineData) arg0.getValue());
    if (mc.level.getEntity(((Integer) arg0.getKey()).intValue()) != null && !(mc.level.getBlockState(var1.pos).canBeReplaced())) {
        if ((System.currentTimeMillis() - var1.lastSeenTime) > 1000L) {
        }
    } else {
    }
    return false;
});
        Object var1 = this.activeMines.values().iterator();
        while (var1.hasNext()) {
            Object var2 = ((SurroundModule.MineData) var1.next());
            var2.ticks = (var2.ticks + 1);
            if ((var2.ticks >= 2) || ((System.currentTimeMillis() - var2.startTime) >= 100L)) break label_1;
            break label_0;
            label_1:
            Object var3 = var2.pos;
            Object var4 = ownFootCells().iterator();
            while (var4.hasNext()) {
                Object var5 = ((BlockPos) var4.next());
                if (var3.getY() != var5.getY()) {
                    break label_2;
                }
                int var6 = (var3.getX() - var5.getX());
                int var7 = (var3.getZ() - var5.getZ());
                if ((Math.abs(var6) + Math.abs(var7)) != 1) {
                    break label_2;
                }
                if (this.blockerUp.getValue()) {
                    this.targetPositions.add(var3.above());
                    this.targetPositions.add(var3.below());
                }
                if (!(this.blockerCorner.getValue())) break label_7;
                if (!(!var6)) {
                    this.targetPositions.add(var5.offset(var6, 0, 1));
                    this.targetPositions.add(var5.offset(var6, 0, -1));
                    break label_7;
                }
                this.targetPositions.add(var5.offset(1, 0, var7));
                this.targetPositions.add(var5.offset(-1, 0, var7));
                label_7:
                if (!(this.blockerEdge.getValue())) {
                    break label_2;
                }
                if (!(!var6)) {
                    this.targetPositions.add(var5.offset((var6 * 2), 0, 0));
                    break label_2;
                }
                this.targetPositions.add(var5.offset(0, 0, (var7 * 2)));
            }
        }
    }

    private Set computeHelpBlocked() {
        Object var1 = ownFootCells();
        if (mc.player.getHealth() < this.advoidHelpHealth.getValue().floatValue()) return Set.of();
        Object var2 = new HashSet();
        Object var3 = mc.level.players().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Player) var3.next());
            if (((var4 == mc.player) || (LightningClient.FRIEND_MANAGER.contains(var4.getName().getString()))) || (mc.player.distanceToSqr(var4) > 25.0)) break label_1;
            if (!(mc.level.getBlockState(var4.blockPosition()).canBeReplaced())) {
                break label_1;
            }
            Object var5 = ownFootCells(var4).iterator();
            while (var5.hasNext()) {
                Object var6 = ((BlockPos) var5.next());
                Object var7 = HORIZONTALS;
                int var8 = var7.length;
                int var9 = 0;
                while (var9 < var8) {
                    Object var10 = var7[var9];
                    Object var11 = var6.relative(var10);
                    if (ownFootCells(var4).contains(var11)) {
                    } else {
                        var2.add(var11);
                    }
                    var9++;
                }
                var2.add(var6.below());
                var2.add(var6);
            }
        }
        if (var2.isEmpty()) return Set.of();
        var3 = new HashSet();
        var4 = var2.iterator();
        while (var4.hasNext()) {
            var5 = ((BlockPos) var4.next());
            var6 = 0;
            var7 = var1.iterator();
            label_12:
            if (!(var7.hasNext())) break label_14;
            var8 = ((BlockPos) var7.next());
            if (!(var5.equals(var8)) && !(var5.equals(var8.above())) && !(var5.equals(var8.below())) && !(var5.equals(var8.north())) && !(var5.equals(var8.south())) && !(var5.equals(var8.east())) && !(var5.equals(var8.west()))) {
                break label_12;
            }
            var6 = 1;
            break label_14;
            label_14:
            if (!(!var6)) {
                break label_11;
            }
            var3.add(var5);
        }
        return var3;
    }

    private Set ownFootCells() {
        return ownFootCells(mc.player);
    }

    private Set ownFootCells(Player arg0) {
        Object var2 = new HashSet();
        Object var3 = arg0.getBoundingBox();
        int var4 = arg0.blockPosition().getY();
        int var5 = ((int) Math.floor(var3.minX));
        while (((double) var5) < Math.ceil(var3.maxX)) {
            int var6 = ((int) Math.floor(var3.minZ));
            while (((double) var6) < Math.ceil(var3.maxZ)) {
                var2.add(new BlockPos(var5, var4, var6));
                var6++;
            }
            var5++;
        }
        return var2;
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent arg0) {
        setToggled(false);
    }

    private boolean shouldPauseForEating() {
        if (!(this.advanced.getValue())) return true;
        if (EntityUtils.isPhased(mc.player)) return true;
        Object var1 = HoleUtils.getFeetPositions(mc.player, this.extension.getValue(), this.floor.getValue(), this.underFeet.getValue(), false);
        int var2 = var1.stream().filter(WorldUtils::isPlaceable).anyMatch(() -> {
    if (WorldUtils.getDirection(arg0, this.strictDirection.getValue()) == null) {
        if (this.airPlace.getValue()) {
        }
    } else {
    }
    return false;
});
        return !var2;
    }

    private boolean isBlastProof(Block arg0) {
        if (arg0 != Blocks.OBSIDIAN && arg0 != Blocks.ENDER_CHEST && arg0 != Blocks.CRYING_OBSIDIAN && arg0 != Blocks.NETHERITE_BLOCK && arg0 != Blocks.RESPAWN_ANCHOR && arg0 != Blocks.ANCIENT_DEBRIS && arg0 != Blocks.ENCHANTING_TABLE && arg0 != Blocks.ANVIL && arg0 != Blocks.CHIPPED_ANVIL) {
            if (arg0 == Blocks.DAMAGED_ANVIL) {
            }
        } else {
        }
        return false;
    }

    private int findBlastProofBlock(int arg0, int arg1) {
        float var3 = -1.0f;
        int var4 = -1;
        int var5 = arg0;
        while (var5 <= arg1) {
            Object var8 = mc.player.getInventory().getItem(var5).getItem();
            if (!((var8 instanceof BlockItem))) break label_4;
            var tmp0 = ((BlockItem) var8);
            Object var6 = tmp0;
            var tmp1 = tmp0.getBlock();
            Object var7 = tmp1;
            if (!(isBlastProof(tmp1))) {
                break label_4;
            }
            float var9 = var7.defaultDestroyTime();
            if (var9 == -1.0f) return var5;
            if (var9 <= var3) {
                break label_4;
            }
            var3 = var9;
            var4 = var5;
            label_4:
            var5++;
        }
        return var4;
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent.Post arg0) {
        if (!(!this.isWorking)) {
            this.packetsSent = (this.packetsSent + 1);
        }
    }

    static {
        var tmp0 = new Direction[4];
        tmp0[0] = Direction.NORTH;
        tmp0[1] = Direction.SOUTH;
        tmp0[2] = Direction.EAST;
        tmp0[3] = Direction.WEST;
        HORIZONTALS = tmp0;
    }


    class MineData {
        final BlockPos pos;
        final long startTime;
        int ticks;
        long lastSeenTime;

        MineData(BlockPos arg0) {
            this.pos = arg0;
            this.startTime = System.currentTimeMillis();
            this.ticks = 0;
            this.lastSeenTime = System.currentTimeMillis();
        }

    }
}
