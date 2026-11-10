package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.ThreadExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "SelfTrap", description = "Automatically places blocks around you to prevent other people from getting inside your hole.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class SelfTrapModule extends Module {
    public ModeSetting autoSwitch;
    public ModeSetting mode;
    public BooleanSetting head;
    public BooleanSetting asynchronous;
    public NumberSetting bpt;
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
    public BooleanSetting antiStep;
    public BooleanSetting antiBomb;
    public BooleanSetting holeCheck;
    public ModeSetting autoJump;
    public BooleanSetting whileEating;
    public BooleanSetting selfDisable;
    public BooleanSetting render;
    private int packetsSent;
    private boolean isWorking;
    private List targetPositions;
    private BlockPos lastGroundPos;
    private boolean jumpingForTrap;
    private int blocksPlaced;

    public SelfTrapModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        var tmp0 = new String[2];
        tmp0[0] = "Partial";
        tmp0[1] = "Full";
        this.mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Partial", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Full";
        this.head = new BooleanSetting("Head", "Whether or not to cover the block on the players head.", new ModeSetting.Visibility(this.mode, tmp1), true);
        this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
        this.bpt = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", 4, 1, 8);
        this.await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
        var tmp2 = new String[3];
        tmp2[0] = "None";
        tmp2[1] = "Normal";
        tmp2[2] = "Grim";
        this.rotate = new ModeSetting("Rotate", "The rotation mode when placing blocks.", "Grim", tmp2);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
        this.attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with your trap.", false);
        this.attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), 3.0f, 1.0f, 6.0f);
        this.attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
        this.attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
        this.antiStep = new BooleanSetting("AntiStep", "Adds additional blocks that prevent anyone from stepping out of the hole.", false);
        this.antiBomb = new BooleanSetting("AntiBomb", "Places an extra block above your head to prevent you from getting bombed.", false);
        this.holeCheck = new BooleanSetting("HoleCheck", "Only self traps whenever you are in a hole.", false);
        var tmp3 = new String[3];
        tmp3[0] = "None";
        tmp3[1] = "NCP";
        tmp3[2] = "Grim";
        this.autoJump = new ModeSetting("AutoJump", "Automatically jumps to place high blocks when on-ground eye height is too low for StrictDirection.", "Grim", tmp3);
        this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
        this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.packetsSent = 0;
        this.isWorking = false;
        this.targetPositions = new ArrayList();
        this.lastGroundPos = null;
        this.jumpingForTrap = false;
        this.blocksPlaced = 0;
    }

    public String getMetaData() {
        return ("Packet: " + this.packetsSent);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) return;
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) return;
        Object var2 = () -> {
    this.blocksPlaced = 0;
    if (!(this.autoSwitch.getValue().equalsIgnoreCase("None"))) break label_1;
    var tmp0 = mc.player.getMainHandItem().getItem();
    Object var2 = tmp0;
    if ((tmp0 instanceof BlockItem)) {
        var tmp1 = ((BlockItem) var2);
        Object var1 = tmp1;
        if (isBlastProof(tmp1.getBlock())) break label_1;
    }
    LightningClient.CHAT_MANAGER.tagged("You are currently not holding any blast-proof blocks.", getName());
    setToggled(false);
    this.targetPositions = new ArrayList();
    return;
    label_1:
    if (!(mc.player.onGround())) break label_2;
    this.lastGroundPos = mc.player.blockPosition();
    this.jumpingForTrap = false;
    label_2:
    if (isTrapComplete(this.lastGroundPos)) {
        if (((!(this.selfDisable.getValue())) || (!(mc.player.onGround()))) || (this.jumpingForTrap)) break label_3;
        setToggled(false);
        label_3:
        return;
    }
    if (this.holeCheck.getValue() && !(HoleUtils.isPlayerInHole(mc.player))) {
        if ((this.autoJump.getValue().equalsIgnoreCase("None")) || (mc.player.onGround())) {
            return;
        }
    }
    if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
        if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
        }
    } else {
    }
    int var3 = 0.findBlastProofBlock(35, 8);
    int var4 = mc.player.getInventory().getSelectedSlot();
    if (var3 == -1) {
        LightningClient.CHAT_MANAGER.tagged("No blast-proof blocks could be found in your hotbar.", getName());
        setToggled(false);
        this.targetPositions = new ArrayList();
        return;
    }
    this.targetPositions = HoleUtils.getTrapPositions(mc.player, this.lastGroundPos, this.mode.getValue().equalsIgnoreCase("Partial"), this.head.getValue(), this.antiStep.getValue(), this.antiBomb.getValue(), this.strictDirection.getValue(), this.airPlace.getValue()).stream().filter((arg0) -> WorldUtils.isPlaceable(arg0, true)).toList();
    if (this.attack.getValue()) {
        attackTrapCrystals();
    }
    if (this.targetPositions.isEmpty()) {
        if (this.selfDisable.getValue() && mc.player.onGround() && !this.jumpingForTrap && isTrapComplete(this.lastGroundPos)) {
            setToggled(false);
        }
        return;
    }
    if ((((this.autoJump.getValue().equalsIgnoreCase("None")) || (!(this.strictDirection.getValue()))) || (!(mc.player.onGround()))) || (this.airPlace.getValue())) break label_17;
    int var5 = 0;
    Object var6 = this.targetPositions.iterator();
    label_14:
    if (!(var6.hasNext())) break label_16;
    Object var7 = ((BlockPos) var6.next());
    if (var7.getY() < (mc.player.getBlockY() + 2)) break label_14;
    var tmp2 = WorldUtils.getDirection(var7, this.strictDirection.getValue());
    Object var8 = tmp2;
    if (tmp2 != null) {
        break label_14;
    }
    var5 = 1;
    break label_16;
    label_16:
    if (!var5) break label_17;
    this.jumpingForTrap = true;
    mc.execute(() -> {
    if (mc.player != null && mc.player.onGround()) {
        mc.player.jumpFromGround();
    }
});
    label_17:
    this.isWorking = true;
    LightningClient.ROTATION_MANAGER.beginBatchRotation();
    if (this.blocksPlaced >= this.bpt.getValue().intValue()) {
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        this.isWorking = false;
        return;
    }
    InventoryUtils.switchSlot(this.autoSwitch.getValue(), var3, var4);
    var5 = new ArrayList();
    var6 = this.targetPositions.iterator();
    while (var6.hasNext()) {
        var7 = ((BlockPos) var6.next());
        if (this.blocksPlaced >= this.bpt.getValue().intValue()) {
            break label_31;
        }
        if (!(this.attack.getValue())) break label_26;
        Object var9 = mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(var7).inflate(0.5)).iterator();
        while (var9.hasNext()) {
            Object var10 = ((EndCrystal) var9.next());
            if (!(var10.isAlive())) {
                break label_23;
            }
            mc.getConnection().send(new ServerboundAttackPacket(var10.getId()));
            if (!(this.attackSwing.getValue())) {
                break label_23;
            }
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        label_26:
        var tmp3 = WorldUtils.getDirection(var7, var5, this.strictDirection.getValue());
        var8 = tmp3;
        if (tmp3 == null) {
            if (this.airPlace.getValue()) {
                WorldUtils.airPlaceBlock(var7, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
                var5.add(var7);
                this.blocksPlaced = (this.blocksPlaced + 1);
                break label_21;
            }
            var10 = WorldUtils.findSupportBlock(var7, var5, this.strictDirection.getValue(), 2);
            if (var10 == null) break label_21;
            var tmp4 = WorldUtils.getDirection(var10, var5, this.strictDirection.getValue());
            var9 = tmp4;
            if (tmp4 == null) break label_21;
            if (!(WorldUtils.placeBlock(var10, var9, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue()))) {
                break label_21;
            }
            var5.add(var10);
            this.blocksPlaced = (this.blocksPlaced + 1);
            break label_21;
        }
        if (!(WorldUtils.placeBlock(var7, var8, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue()))) {
            break label_21;
        }
        var5.add(var7);
        this.blocksPlaced = (this.blocksPlaced + 1);
    }
    label_31:
    if (!(mc.player.onGround()) && this.autoJump.getValue().equalsIgnoreCase("NCP")) {
        mc.execute(() -> {
    if (mc.player != null && !(mc.player.onGround())) {
        mc.player.setDeltaMovement(new Vec3(mc.player.getDeltaMovement().x, -0.42, mc.player.getDeltaMovement().z));
    }
});
    }
    InventoryUtils.switchBack(this.autoSwitch.getValue(), var3, var4);
    if (this.selfDisable.getValue() && mc.player.onGround() && !this.jumpingForTrap && isTrapComplete(this.lastGroundPos)) {
        setToggled(false);
    }
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    this.isWorking = false;
    break label_36;
    Object var11 = this;
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    this.isWorking = false;
    throw var11;
    label_36:
};
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(var2);
        } else {
            var2.run();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.attack.getValue())) break label_6;
        var tmp0 = arg0.getPacket();
        Object var3 = tmp0;
        if (!((tmp0 instanceof ClientboundAddEntityPacket))) break label_6;
        var tmp1 = ((ClientboundAddEntityPacket) var3);
        Object var2 = tmp1;
        if (tmp1.getType() != EntityTypes.END_CRYSTAL) break label_6;
        Object var4 = new Vec3(var2.getX(), var2.getY(), var2.getZ());
        Object var5 = new AABB((var4.x - 1.0), var4.y, (var4.z - 1.0), (var4.x + 1.0), (var4.y + 2.0), (var4.z + 1.0));
        int var6 = 0;
        Object var7 = this.targetPositions.iterator();
        label_2:
        if (!(var7.hasNext())) break label_4;
        Object var8 = ((BlockPos) var7.next());
        if (!(var5.intersects(new AABB(var8))) && var4.distanceToSqr(Vec3.atCenterOf(var8)) > 4.0) {
            break label_2;
        }
        var6 = 1;
        break label_4;
        label_4:
        if ((!var6) || (mc.player.getEyePosition().distanceToSqr(var4) > Mth.square(this.attackRange.getValue().doubleValue()))) break label_6;
        if (!(this.attackMultiTask.getValue())) {
            if (mc.player.isUsingItem()) break label_6;
        }
        mc.getConnection().send(new ServerboundAttackPacket(var2.getId()));
        if (!(this.attackSwing.getValue())) break label_6;
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        label_6:
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

    public void onEnable() {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        this.packetsSent = 0;
        this.targetPositions.clear();
        this.lastGroundPos = mc.player.blockPosition();
        this.jumpingForTrap = false;
    }

    public void onDisable() {
        this.targetPositions = new ArrayList();
        this.lastGroundPos = null;
        this.jumpingForTrap = false;
    }

    private boolean isTrapComplete(BlockPos arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        if (this.mode.getValue().equalsIgnoreCase("Partial")) {
            Object var2 = arg0.offset(0, 2, 0);
            return !(mc.level.getBlockState(var2).canBeReplaced());
        }
        var2 = HoleUtils.getTrapPositions(mc.player, arg0, false, this.head.getValue(), this.antiStep.getValue(), this.antiBomb.getValue(), this.strictDirection.getValue(), this.airPlace.getValue());
        Object var3 = var2.iterator();
        label_5:
        if (var3.hasNext()) {
            Object var4 = ((BlockPos) var3.next());
            if (!(mc.level.getBlockState(var4).canBeReplaced())) {
                break label_5;
            }
            return false;
        }
        return true;
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

}
