package lightning.modules.impl.combat;

import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientRotationEvent;
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
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "AutoWeb", description = "Automatically places webs on other people's feet.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class AutoWebModule extends Module {
    public ModeSetting autoSwitch;
    public BooleanSetting asynchronous;
    public NumberSetting delay;
    public NumberSetting range;
    public NumberSetting enemyRange;
    public NumberSetting extrapolation;
    public BooleanSetting rotate;
    public BooleanSetting airPlace;
    public BooleanSetting strictDirection;
    public BooleanSetting holeCheck;
    public BooleanSetting whileEating;
    public BooleanSetting selfDisable;
    public BooleanSetting itemDisable;
    public BooleanSetting render;
    private Player target;
    private BlockPos rotatePosition;
    private int ticks;

    public AutoWebModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
        this.delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", 0, 0, 20);
        this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
        this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", 8.0f, 0.0f, 16.0f);
        this.extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", 0, 0, 20);
        this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
        this.airPlace = new BooleanSetting("AirPlace", "Lets you place webs on air.", false);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.holeCheck = new BooleanSetting("HoleCheck", "Checks if the target is in a hole or not before placing.", true);
        this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
        this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
        this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.target = null;
        this.rotatePosition = null;
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) return;
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) return;
        Object var2 = mc.level.players();
        Object var3 = () -> {
    this.rotatePosition = null;
    if (this.ticks < this.delay.getValue().intValue()) {
        this.ticks = (this.ticks + 1);
        return;
    }
    if (this.autoSwitch.getValue().equalsIgnoreCase("None") && mc.player.getMainHandItem().getItem() != Items.COBWEB) {
        if (!(this.itemDisable.getValue())) break label_1;
        LightningClient.CHAT_MANAGER.tagged("You are currently not holding any cobwebs.", getName());
        setToggled(false);
        label_1:
        this.target = null;
        return;
    }
    if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
        if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
        }
    } else {
    }
    int var2 = InventoryUtils.find(0, 35, 8);
    int var3 = mc.player.getInventory().getSelectedSlot();
    if (var2 == -1) {
        if (this.itemDisable.getValue()) {
            LightningClient.CHAT_MANAGER.tagged("No cobwebs could be found in your hotbar.", getName());
            setToggled(false);
        }
        this.target = null;
        return;
    }
    this.target = getTarget(arg0);
    if (this.target == null) {
        if (this.selfDisable.getValue()) {
            setToggled(false);
        }
        return;
    }
    Object var4 = PositionUtils.extrapolate(this.target, this.extrapolation.getValue().intValue()).getCenter();
    Object var5 = new BlockPos(((int) Math.floor(var4.x)), ((int) var4.y), ((int) Math.floor(var4.z)));
    if (this.target.getItemBySlot(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA) && mc.level.getBlockState(var5).getBlock().equals(Blocks.COBWEB)) {
        var5 = var5.above();
    }
    if (!(mc.level.getBlockState(var5).canBeReplaced())) return;
    if (mc.player.distanceToSqr(Vec3.atCenterOf(var5)) > Mth.square(this.range.getValue().doubleValue())) return;
    if (PositionUtils.getFlooredPosition(mc.player).equals(var5) && HoleUtils.isPlayerInHole(mc.player)) return;
    Object var6 = WorldUtils.getDirection(var5, this.strictDirection.getValue());
    if (var6 == null && !(this.airPlace.getValue())) return;
    if (this.rotate.getValue() && this.airPlace.getValue()) {
        this.rotatePosition = var5;
    }
    InventoryUtils.switchSlot(this.autoSwitch.getValue(), var2, var3);
    WorldUtils.placeBlock(var5, var6, InteractionHand.MAIN_HAND, this.rotate.getValue(), false, this.render.getValue());
    InventoryUtils.switchBack(this.autoSwitch.getValue(), var2, var3);
    this.ticks = 0;
};
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(var3);
        } else {
            var3.run();
        }
    }

    @SubscribeEvent
    public void onClientRotation(ClientRotationEvent arg0) {
        if ((this.rotatePosition == null) || (arg0.isCancelled())) {
            return;
        }
        Object var2 = RotationUtils.getRotations(Vec3.atCenterOf(this.rotatePosition));
        arg0.setYaw(var2[0]);
        arg0.setPitch(var2[1]);
    }

    public void onEnable() {
        if (mc.player != null) {
            if (mc.level == null) {
            }
            setToggled(false);
        }
    }

    public String getMetaData() {
        if (this.target == null) return "None";
        return this.target.getName().getString();
    }

    private Player getTarget(List arg0) {
        Object var2 = null;
        Object var3 = arg0.iterator();
        while (var3.hasNext()) {
            Object var4 = ((Player) var3.next());
            if (((((var4 == mc.player) || (!(var4.isAlive()))) || (var4.getHealth() <= 0.0f)) || (mc.player.distanceToSqr(var4) > Mth.square(this.enemyRange.getValue().doubleValue()))) || (LightningClient.FRIEND_MANAGER.contains(var4.getName().getString()))) break label_0;
            if (this.holeCheck.getValue() && !(HoleUtils.isPlayerInHole(var4))) {
                break label_0;
            }
            if (var2 == null) {
                var2 = var4;
                break label_0;
            }
            if (mc.player.distanceToSqr(var4) >= mc.player.distanceToSqr(var2)) {
                break label_0;
            }
            var2 = var4;
        }
        return var2;
    }

}
