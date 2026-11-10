package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.ThreadExecutor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "AutoTrap", description = "Automatically places blocks around you to prevent other people from getting inside your hole.", category = Module.Category.COMBAT)
public class AutoTrapModule extends Module {
    public ModeSetting autoSwitch;
    public ModeSetting mode;
    public BooleanSetting head;
    public BooleanSetting noFeet;
    public BooleanSetting disableAfterFeet;
    public BooleanSetting asynchronous;
    public NumberSetting bpt;
    public NumberSetting range;
    public NumberSetting enemyRange;
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
    public BooleanSetting holeCheck;
    public BooleanSetting antiStep;
    public BooleanSetting whileEating;
    public BooleanSetting ignoreNaked;
    public BooleanSetting selfDisable;
    public BooleanSetting itemDisable;
    public BooleanSetting render;
    private List positions;
    private int blocksPlaced;
    private boolean feetTrappedOnce;

    public AutoTrapModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        var tmp0 = new String[2];
        tmp0[0] = "Partial";
        tmp0[1] = "Full";
        this.mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Full", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Full";
        this.head = new BooleanSetting("Head", "Whether or not to cover the block on the players head.", new ModeSetting.Visibility(this.mode, tmp1), true);
        this.noFeet = new BooleanSetting("NoFeet", "Skips the new floor-level ring around the target's feet entirely -- walls and head (if enabled) still trap as before.", false);
        this.disableAfterFeet = new BooleanSetting("DisableAfterFeet", "Stops re-trapping the floor-level feet ring once it has successfully placed there at least once since the module was enabled -- walls/head keep trapping normally.", new BooleanSetting.Visibility(this.noFeet, false), false);
        this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
        this.bpt = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", 4, 1, 8);
        this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
        this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", 8.0f, 0.0f, 16.0f);
        this.await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
        var tmp2 = new String[3];
        tmp2[0] = "None";
        tmp2[1] = "Normal";
        tmp2[2] = "Grim";
        this.rotate = new ModeSetting("Rotate", "The rotation mode when placing blocks.", "Grim", tmp2);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.airPlace = new BooleanSetting("AirPlace", "Places blocks directly in the air with no real neighbor needed, skipping the support-scaffold bootstrap entirely instead of only using it as a last resort.", false);
        this.attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with your trap.", false);
        this.attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), 3.0f, 1.0f, 6.0f);
        this.attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
        this.attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
        this.holeCheck = new BooleanSetting("HoleCheck", "Checks if the target is in a hole or not before placing.", true);
        this.antiStep = new BooleanSetting("AntiStep", "Adds additional blocks that prevent the player from stepping out of the hole.", false);
        this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
        this.ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", false);
        this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
        this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.positions = new ArrayList();
        this.blocksPlaced = 0;
        this.feetTrappedOnce = false;
    }

    public void onEnable() {
        if (mc.player != null) {
            if (mc.level == null) {
            }
            setToggled(false);
        }
        this.feetTrappedOnce = false;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) return;
        Object var2 = mc.level.players();
        Object var3 = () -> {
    this.blocksPlaced = 0;
    if ((!(this.autoSwitch.getValue().equalsIgnoreCase("None"))) || ((mc.player.getMainHandItem().getItem() instanceof BlockItem))) break label_1;
    if (this.itemDisable.getValue()) {
        LightningClient.CHAT_MANAGER.tagged("You are currently not holding any blocks.", getName());
        setToggled(false);
    }
    this.positions = new ArrayList();
    return;
    label_1:
    if (this.autoSwitch.getValue().equalsIgnoreCase("AltSwap")) break label_2;
    if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
        label_2:
    } else {
    }
    int var2 = InventoryUtils.findHardestBlock(35, 8);
    int var3 = mc.player.getInventory().getSelectedSlot();
    if (var2 == -1) {
        if (this.itemDisable.getValue()) {
            LightningClient.CHAT_MANAGER.tagged("No blocks could be found in your hotbar.", getName());
            setToggled(false);
        }
        this.positions = new ArrayList();
        return;
    }
    Object var4 = getTarget(arg0);
    if (var4 == null) {
        if (this.selfDisable.getValue()) {
            setToggled(false);
        }
        this.positions = new ArrayList();
        return;
    }
    this.positions = var4.positions();
    if (this.attack.getValue()) {
        attackTrapCrystals();
    }
    int var5 = var4.player().getBlockY();
    LightningClient.ROTATION_MANAGER.beginBatchRotation();
    InventoryUtils.switchSlot(this.autoSwitch.getValue(), var2, var3);
    if (LightningClient.MODULE_MANAGER != null) {
    } else {
    }
    Object var6 = null;
    int var7 = (Mth.floor(var4.player().getY()) + 2);
    Object var8 = new ArrayList();
    Object var9 = this.positions.iterator();
    while (var9.hasNext()) {
        Object var10 = ((BlockPos) var9.next());
        if (this.blocksPlaced >= this.bpt.getValue().intValue()) {
            break label_25;
        }
        if (var10.getY() == var7 && var6 != null && var6.isToggled() && var6.hasLiveCrystalNear(var10.above())) {
            break label_13;
        }
        if (!(this.attack.getValue())) break label_19;
        Object var12 = mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(var10).inflate(0.5)).iterator();
        while (var12.hasNext()) {
            Object var13 = ((EndCrystal) var12.next());
            if (!(var13.isAlive())) {
                break label_16;
            }
            mc.getConnection().send(new ServerboundAttackPacket(var13.getId()));
            if (!(this.attackSwing.getValue())) {
                break label_16;
            }
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        label_19:
        var tmp0 = WorldUtils.getDirection(var10, var8, this.strictDirection.getValue());
        Object var11 = tmp0;
        if (tmp0 == null) {
            if (this.airPlace.getValue()) {
                WorldUtils.airPlaceBlock(var10, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
                var8.add(var10);
                this.blocksPlaced = (this.blocksPlaced + 1);
                if (var10.getY() != var5) {
                    break label_13;
                }
                this.feetTrappedOnce = true;
                break label_13;
            }
            var13 = WorldUtils.findSupportBlock(var10, var8, this.strictDirection.getValue(), 2);
            if (var13 == null) break label_13;
            var tmp1 = WorldUtils.getDirection(var13, var8, this.strictDirection.getValue());
            var12 = tmp1;
            if (tmp1 == null) break label_13;
            if (!(WorldUtils.placeBlock(var13, var12, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue()))) {
                break label_13;
            }
            var8.add(var13);
            this.blocksPlaced = (this.blocksPlaced + 1);
            break label_13;
        }
        WorldUtils.placeBlock(var10, var11, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue());
        var8.add(var10);
        this.blocksPlaced = (this.blocksPlaced + 1);
        if (var10.getY() != var5) {
            break label_13;
        }
        this.feetTrappedOnce = true;
    }
    label_25:
    InventoryUtils.switchBack(this.autoSwitch.getValue(), var2, var3);
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    break label_29;
    Object var14 = ((AutoCrystalModule) LightningClient.MODULE_MANAGER.getModule(AutoCrystalModule.class));
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    throw var14;
    label_29:
};
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(var3);
        } else {
            var3.run();
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
        Object var7 = this.positions.iterator();
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
            Object var5 = this.positions.iterator();
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

    public void onDisable() {
        this.positions = new ArrayList();
    }

    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }

    private AutoTrapModule.Target getTarget(List arg0) {
        Object var2 = null;
        if (LightningClient.MODULE_MANAGER != null) {
            break label_1;
        }
        label_1:
        Object var3 = null;
        if (LightningClient.MODULE_MANAGER == null) break label_2;
        break label_3;
        label_2:
        label_3:
        Object var4 = null;
        Object var5 = new ArrayList(arg0);
        if ((var3 == null) || (!(var3.isToggled()))) break label_6;
        Object var6 = var3.getGhosts().iterator();
        while (var6.hasNext()) {
            Object var7 = ((Player) var6.next());
            if (var7 == null) break label_4;
            if (var5.contains(var7)) {
                break label_4;
            }
            var5.add(((AbstractClientPlayer) var7));
        }
        label_6:
        var6 = var5.iterator();
        while (var6.hasNext()) {
            var7 = ((Player) var6.next());
            if (var7 == mc.player) break label_7;
            if (var4 != null) {
                if (var4.isGhost(var7)) break label_7;
            }
            var tmp0 = EntityUtils.isGhost(var7);
            int var10 = tmp0;
            if (!tmp0) {
                if ((!(var7.isAlive())) || (var7.getHealth() <= 0.0f)) break label_7;
            }
            if (mc.player.distanceToSqr(var7) > Mth.square(this.enemyRange.getValue().doubleValue())) break label_7;
            if (this.ignoreNaked.getValue()) {
                if (EntityUtils.isNaked(var7)) break label_7;
            }
            if (var3 != null) {
                if (!(var3.isGhost(var7))) {
                }
                if (!(LightningClient.FRIEND_MANAGER.contains(var7.getName().getString()))) break label_13;
                break label_7;
            }
            var tmp1 = var3.getSpot(((RemotePlayer) var7));
            Object var9 = tmp1;
            if ((tmp1 == null) || (!(LightningClient.FRIEND_MANAGER.contains(var9.data.name)))) break label_13;
            break label_7;
            label_13:
            if (this.holeCheck.getValue() && !(HoleUtils.isPlayerInHole(var7))) {
                break label_7;
            }
            if (this.noFeet.getValue()) break label_16;
            if (this.disableAfterFeet.getValue()) {
                if (this.feetTrappedOnce) break label_16;
            }
            break label_17;
            label_16:
            label_17:
            int var8 = 0;
            int var11 = 0;
            Object var12 = HoleUtils.getTrapPositions(var7, this.mode.getValue().equalsIgnoreCase("Partial"), this.head.getValue(), this.antiStep.getValue(), false, this.strictDirection.getValue(), var8, this.airPlace.getValue()).stream().filter(() -> mc.player.distanceToSqr(Vec3.atCenterOf(arg0)) <= Mth.square(this.range.getValue().doubleValue())).filter(WorldUtils::isPlaceable).toList();
            if (var12.isEmpty()) {
                break label_7;
            }
            if (var2 == null) {
                var2 = new AutoTrapModule.Target(var7, var12);
                break label_7;
            }
            int var13 = EntityUtils.isGhost(var2.player());
            if (var13 != var10) {
                if (!var13) {
                    break label_7;
                }
                var2 = new AutoTrapModule.Target(var7, var12);
                break label_7;
            }
            if (mc.player.distanceToSqr(var7) >= mc.player.distanceToSqr(var2.player())) {
                break label_7;
            }
            var2 = new AutoTrapModule.Target(var7, var12);
        }
        return var2;
    }


    record Target(Player player, List positions) {

        private Target(Player arg0, List arg1) {
            this.player = arg0;
            this.positions = arg1;
        }

        public Player player() {
            return this.player;
        }

        public List positions() {
            return this.positions;
        }

    }
}
