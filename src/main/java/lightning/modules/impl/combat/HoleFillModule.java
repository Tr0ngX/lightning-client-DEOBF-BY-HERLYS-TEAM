package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.ThreadExecutor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "HoleFill", description = "Automatically places blocks inside of holes to prevent others from getting inside of them.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class HoleFillModule extends Module {
    public ModeSetting autoSwitch;
    public ModeSetting mode;
    public BooleanSetting webs;
    public BooleanSetting selfWeb;
    public BooleanSetting refill;
    public BooleanSetting waitUntilExited;
    public BooleanSetting asynchronous;
    public NumberSetting bpt;
    public NumberSetting range;
    public NumberSetting enemyRange;
    public NumberSetting smartRange;
    public BooleanSetting safety;
    public NumberSetting safetyRange;
    public BooleanSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting attack;
    public BooleanSetting attackRotate;
    public NumberSetting attackRange;
    public NumberSetting attackAge;
    public BooleanSetting attackMultiTask;
    public BooleanSetting attackSwing;
    public BooleanSetting doubleHoles;
    public BooleanSetting holeCheck;
    public BooleanSetting whileEating;
    public BooleanSetting selfDisable;
    public BooleanSetting itemDisable;
    public BooleanSetting render;
    private List positions;
    private int blocksPlaced;
    private BlockPos lastHolePos;
    private BlockPos refillPos;
    private boolean wasInHole;

    public HoleFillModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        var tmp0 = new String[2];
        tmp0[0] = "Normal";
        tmp0[1] = "Smart";
        this.mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Normal", tmp0);
        this.webs = new BooleanSetting("Webs", "Use webs to holefill instead of blocks.", false);
        this.selfWeb = new BooleanSetting("SelfWeb", "Includes your own hole when holefilling with webs.", false);
        this.refill = new BooleanSetting("Refill", "Fills the hole you just exited.", false);
        this.waitUntilExited = new BooleanSetting("WaitUntilExited", "Waits until your hitbox completely leaves the hole before refilling.", new BooleanSetting.Visibility(this.refill, true), true);
        this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
        this.bpt = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", 4, 1, 8);
        this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
        var tmp1 = new String[1];
        tmp1[0] = "Smart";
        this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", new ModeSetting.Visibility(this.mode, tmp1), 8.0f, 0.0f, 16.0f);
        var tmp2 = new String[1];
        tmp2[0] = "Smart";
        this.smartRange = new NumberSetting("SmartRange", "The distance at which the holes will have to be away from the target.", new ModeSetting.Visibility(this.mode, tmp2), 3.0f, 0.0f, 6.0f);
        this.safety = new BooleanSetting("Safety", "Prevents holes close to you from getting filled.", true);
        this.safetyRange = new NumberSetting("SafetyRange", "The maximum distance the holes can be at to be prevented from getting filled.", new BooleanSetting.Visibility(this.safety, true), 2.0f, 0.0f, 6.0f);
        this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with hole filling.", false);
        this.attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), 3.0f, 1.0f, 6.0f);
        this.attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
        this.attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
        this.attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
        this.doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to fill double holes.", false);
        this.holeCheck = new BooleanSetting("HoleCheck", "Checks if the target isn't in a hole before placing.", true);
        this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
        this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
        this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.positions = new ArrayList();
        this.blocksPlaced = 0;
        this.lastHolePos = null;
        this.refillPos = null;
        this.wasInHole = false;
    }

    public void onEnable() {
        this.lastHolePos = null;
        this.refillPos = null;
        this.wasInHole = false;
        if ((getNull()) || (mc.player == null)) break label_0;
        var tmp0 = PositionUtils.getFlooredPosition(mc.player);
        Object var1 = tmp0;
        if (!(isHole(tmp0))) break label_0;
        this.lastHolePos = var1.immutable();
        this.wasInHole = true;
        label_0:
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) return;
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) return;
        Object var2 = mc.level.players();
        Object var3 = () -> {
    this.blocksPlaced = 0;
    if (!(this.webs.getValue())) break label_1;
    if (!(mc.player.getMainHandItem().getItem().equals(Items.COBWEB))) {
        break label_3;
    }
    break label_3;
    label_1:
    int var3 = (!((mc.player.getMainHandItem().getItem() instanceof BlockItem)) ? 1 : 0);
    if (this.autoSwitch.getValue().equalsIgnoreCase("None") && !(!var3)) {
        if (this.itemDisable.getValue()) {
            LightningClient.CHAT_MANAGER.tagged(("You are currently not holding any " + (this.webs.getValue() ? "cobwebs." : "blocks.")), getName());
            setToggled(false);
        }
        this.positions = new ArrayList();
        return;
    }
    if (this.webs.getValue()) {
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
    } else {
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
    }
    int var4 = InventoryUtils.findHardestBlock(35, 8);
    int var5 = mc.player.getInventory().getSelectedSlot();
    if (var4 == -1) {
        if (this.itemDisable.getValue()) {
            LightningClient.CHAT_MANAGER.tagged(("No " + (this.webs.getValue() ? "cobwebs" : "blocks") + " could be found in your hotbar."), getName());
            setToggled(false);
        }
        this.positions = new ArrayList();
        return;
    }
    if (this.mode.getValue().equalsIgnoreCase("Smart")) {
        var tmp0 = getTarget(arg0);
        Object var2 = tmp0;
        if (tmp0 == null) {
            break label_22;
        }
        break label_22;
    }
    label_22:
    var2.positions().positions = getPositions(null);
    if (this.refill.getValue()) {
        updateExitedHole(mc.player);
        if (this.refillPos == null) break label_27;
        if (!(WorldUtils.isPlaceable(this.refillPos))) {
            this.refillPos = null;
            break label_27;
        }
        if (this.waitUntilExited.getValue()) {
            if (playerIntersects(this.refillPos, mc.player)) break label_27;
        }
        if (mc.player.distanceToSqr(Vec3.atCenterOf(this.refillPos)) <= Mth.square(this.range.getValue().doubleValue())) {
            if (this.positions.contains(this.refillPos)) break label_27;
            this.positions.add(0, this.refillPos);
            break label_27;
        }
        this.refillPos = null;
        break label_27;
    }
    this.refillPos = null;
    this.wasInHole = false;
    this.lastHolePos = null;
    label_27:
    if (this.attack.getValue()) {
        attackHoleCrystals();
    }
    if (this.positions.isEmpty()) {
        if (this.selfDisable.getValue() && !(this.mode.getValue().equalsIgnoreCase("Smart"))) {
            setToggled(false);
        }
        return;
    }
    LightningClient.ROTATION_MANAGER.beginBatchRotation();
    InventoryUtils.switchSlot(this.autoSwitch.getValue(), var4, var5);
    Object var6 = this.positions.iterator();
    while (var6.hasNext()) {
        Object var7 = ((BlockPos) var6.next());
        if (this.blocksPlaced >= this.bpt.getValue().intValue()) {
            break label_36;
        }
        Object var8 = WorldUtils.getDirection(var7, this.strictDirection.getValue());
        if (var8 == null) break label_32;
        if (!(WorldUtils.placeBlock(var7, var8, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue()))) {
            break label_32;
        }
        this.blocksPlaced = (this.blocksPlaced + 1);
        if (!(var7.equals(this.refillPos))) {
            break label_32;
        }
        this.refillPos = null;
    }
    label_36:
    InventoryUtils.switchBack(this.autoSwitch.getValue(), var4, var5);
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    break label_40;
    Object var9 = new ArrayList();
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    throw var9;
    label_40:
};
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(var3);
        } else {
            var3.run();
        }
    }

    private void attackHoleCrystals() {
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

    private void updateExitedHole(LocalPlayer arg0) {
        Object var2 = PositionUtils.getFlooredPosition(arg0);
        int var3 = isHole(var2);
        if (!(!var3)) {
            this.lastHolePos = var2.immutable();
            this.wasInHole = true;
            return;
        }
        if (!(!this.wasInHole)) {
            if (this.lastHolePos == null) {
            }
            this.wasInHole = false;
            return;
        }
        if (!(var2.equals(this.lastHolePos))) {
            this.refillPos = this.lastHolePos.immutable();
        }
        this.wasInHole = false;
    }

    private boolean isHole(BlockPos arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        if (HoleUtils.getSingleHole(arg0, 1.0, false) != null) return true;
        if (!(this.doubleHoles.getValue())) break label_3;
        return HoleUtils.getDoubleHole(arg0, 1.0) != null;
    }

    private boolean playerIntersects(BlockPos arg0, Player arg1) {
        return arg1.getBoundingBox().intersects(new AABB(arg0));
    }

    public void onDisable() {
        this.positions = new ArrayList();
        this.lastHolePos = null;
        this.refillPos = null;
        this.wasInHole = false;
    }

    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }

    private HoleFillModule.Target getTarget(List arg0) {
        Object var2 = null;
        Object var3 = arg0.iterator();
        while (var3.hasNext()) {
            Object var4 = ((Player) var3.next());
            if (((((var4 == mc.player) || (!(var4.isAlive()))) || (var4.getHealth() <= 0.0f)) || (mc.player.distanceToSqr(var4) > Mth.square(this.enemyRange.getValue().doubleValue()))) || (LightningClient.FRIEND_MANAGER.contains(var4.getName().getString()))) break label_0;
            if (this.holeCheck.getValue()) {
                if (HoleUtils.isPlayerInHole(var4)) break label_0;
            }
            var tmp0 = getPositions(var4);
            Object var5 = tmp0;
            if (tmp0.isEmpty()) {
                break label_0;
            }
            if (var2 == null) {
                var2 = new HoleFillModule.Target(var4, var5);
                break label_0;
            }
            if (mc.player.distanceToSqr(var4) >= mc.player.distanceToSqr(var2.player())) {
                break label_0;
            }
            var2 = new HoleFillModule.Target(var4, var5);
        }
        return var2;
    }

    private List getPositions(Player arg0) {
        Object var2 = new ArrayList();
        int var3 = this.range.getValue().intValue();
        Object var4 = mc.player.blockPosition();
        Object var5 = BlockPos.betweenClosed(var4.offset(-(var3), -(var3), -(var3)), var4.offset(var3, var3, var3)).iterator();
        while (var5.hasNext()) {
            Object var6 = ((BlockPos) var5.next());
            if ((mc.player.distanceToSqr(Vec3.atCenterOf(var6)) > Mth.square(this.range.getValue().doubleValue())) || (!(WorldUtils.isPlaceable(var6)))) break label_0;
            if (this.safety.getValue() && mc.player.distanceToSqr(Vec3.atCenterOf(var6)) <= Mth.square(this.safetyRange.getValue().doubleValue())) {
                break label_0;
            }
            if (this.selfWeb.getValue() && HoleUtils.isPlayerInHole(mc.player) && mc.player.blockPosition().equals(var6)) {
                var2.add(var6.immutable());
                break label_0;
            }
            if (HoleUtils.getSingleHole(var6, 1.0) != null) {
                if (arg0 != null && arg0.distanceToSqr(Vec3.atCenterOf(var6)) > Mth.square(this.smartRange.getValue().doubleValue())) {
                    break label_0;
                }
                var2.add(var6.immutable());
                break label_0;
            }
            if ((!(this.doubleHoles.getValue())) || (HoleUtils.getDoubleHole(var6, 1.0) == null)) break label_0;
            if (arg0 != null && arg0.distanceToSqr(Vec3.atCenterOf(var6)) > Mth.square(this.smartRange.getValue().doubleValue())) {
                break label_0;
            }
            var2.add(var6.immutable());
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
