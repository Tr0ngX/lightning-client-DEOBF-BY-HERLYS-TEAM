package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.HitboxDesyncModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.system.ThreadExecutor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "OpponentScaffold", description = "Automatically places blocks under non-friended players to prevent them from falling.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class OpponentScaffold extends Module {
    public ModeSetting autoSwitch;
    public BooleanSetting asynchronous;
    public NumberSetting limit;
    public NumberSetting delay;
    public NumberSetting range;
    public NumberSetting enemyRange;
    public NumberSetting extrapolation;
    public BooleanSetting await;
    public BooleanSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting crystalDestruction;
    public BooleanSetting whileEating;
    public BooleanSetting selfDisable;
    public BooleanSetting itemDisable;
    public BooleanSetting render;
    private List positions;
    private int ticks;
    private int blocksPlaced;

    public OpponentScaffold() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
        this.limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", 4, 1, 20);
        this.delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", 0, 0, 20);
        this.range = new NumberSetting("Range", "The maximum range at which blocks will be placed at.", 5.0, 0.0, 12.0);
        this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", 10.0f, 0.0f, 20.0f);
        this.extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to predict where they will move.", 0, 0, 20);
        this.await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
        this.rotate = new BooleanSetting("Rotate", "Whether or not you should rotate when you place blocks.", true);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
        this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
        this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
        this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.positions = new ArrayList();
        this.ticks = 0;
        this.blocksPlaced = 0;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) return;
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) return;
        Object var2 = mc.level.players();
        if ((var2 == null) || (var2.isEmpty())) {
            return;
        }
        Object var3 = () -> {
    if ((mc.player == null) || (mc.level == null)) {
        return;
    }
    this.blocksPlaced = 0;
    if (this.ticks >= this.delay.getValue().intValue()) break label_2;
    this.ticks = (this.ticks + 1);
    return;
    label_2:
    if (!(this.autoSwitch.getValue().equalsIgnoreCase("None"))) break label_5;
    if (mc.player.getMainHandItem() != null) {
        if ((mc.player.getMainHandItem().getItem() instanceof BlockItem)) break label_5;
    }
    if (this.itemDisable.getValue()) {
        LightningClient.CHAT_MANAGER.tagged("You are currently not holding any blocks.", getName());
        setToggled(false);
    }
    this.positions = new ArrayList();
    return;
    label_5:
    if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
        if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
        }
    } else {
    }
    int var3 = InventoryUtils.findHardestBlock(35, 8);
    if (mc.player.getInventory() != null) {
    } else {
    }
    int var2 = 0;
    int var4 = 0;
    if (var3 == -1) {
        if (this.itemDisable.getValue()) {
            LightningClient.CHAT_MANAGER.tagged("No blocks could be found in your hotbar.", getName());
            setToggled(false);
        }
        this.positions = new ArrayList();
        return;
    }
    Object var5 = getTarget(arg0);
    if (var5 == null) {
        if (this.selfDisable.getValue()) {
            setToggled(false);
        }
        this.positions = new ArrayList();
        return;
    }
    this.positions = var5.positions();
    LightningClient.ROTATION_MANAGER.beginBatchRotation();
    InventoryUtils.switchSlot(this.autoSwitch.getValue(), var3, var2);
    Object var6 = new ArrayList();
    Object var7 = this.positions.iterator();
    while (var7.hasNext()) {
        Object var8 = ((BlockPos) var7.next());
        if (this.blocksPlaced >= this.limit.getValue().intValue()) {
            break label_21;
        }
        Object var9 = WorldUtils.getDirection(var8, var6, this.strictDirection.getValue());
        if (var9 != null) break label_20;
        Object var11 = var8.offset(0, -1, 0);
        if (!(WorldUtils.isPlaceable(var11))) break label_16;
        var tmp0 = WorldUtils.getDirection(var11, var6, this.strictDirection.getValue());
        Object var10 = tmp0;
        if (tmp0 == null) {
            break label_16;
        }
        WorldUtils.placeBlock(var11, var10, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        var6.add(var11);
        this.blocksPlaced = (this.blocksPlaced + 1);
        if (this.blocksPlaced >= this.limit.getValue().intValue()) {
            break label_21;
        }
        if (this.await.getValue()) break label_16;
        var tmp1 = WorldUtils.getDirection(var8, var6, this.strictDirection.getValue());
        var9 = tmp1;
        if (tmp1 != null) break label_20;
        break label_16;
        label_20:
        WorldUtils.placeBlock(var8, var9, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        var6.add(var8);
        this.blocksPlaced = (this.blocksPlaced + 1);
    }
    label_21:
    InventoryUtils.switchBack(this.autoSwitch.getValue(), var3, var2);
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    break label_25;
    Object var12 = mc.player.getInventory().getSelectedSlot();
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    throw var12;
    label_25:
    this.ticks = 0;
};
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(var3);
        } else {
            var3.run();
        }
    }

    public void onEnable() {
        if (mc.player != null) {
            if (mc.level == null) {
            }
            setToggled(false);
        }
    }

    public void onDisable() {
        this.positions = new ArrayList();
    }

    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }

    private OpponentScaffold.Target getTarget(List arg0) {
        if (mc.player == null) return null;
        Object var5 = null;
        double var6 = 1.7976931348623157E308;
        Object var8 = arg0.iterator();
        while (var8.hasNext()) {
            Object var9 = ((AbstractClientPlayer) var8.next());
            if ((((var9 == mc.player) || (var9.isInvisible())) || (LightningClient.FRIEND_MANAGER.contains(var9.getName().getString()))) || (var9.getHealth() <= 0.0f)) break label_1;
            double var10 = mc.player.distanceToSqr(var9);
            if (mc.player.distanceToSqr(var9) > ((double) Mth.square(this.enemyRange.getValue().floatValue()))) break label_1;
            if (var10 >= var6) {
                break label_1;
            }
            var6 = var10;
            var5 = var9;
        }
        if (var5 == null) return null;
        var8 = getTargetPosition(var5);
        if (var8 == null) return null;
        var9 = ((HitboxDesyncModule) LightningClient.MODULE_MANAGER.getModule(HitboxDesyncModule.class));
        var10 = new ArrayList();
        Object var11 = var8.below();
        if ((var9 == null) || (!(var9.isToggled()))) break label_6;
        var tmp0 = (!(Boolean.TRUE.equals(var9.close.getValue())) ? 1 : 0);
        int var4 = tmp0;
        int var12 = tmp0;
        if (!(WorldUtils.isPlaceable(var11, var4))) break label_8;
        double var2 = mc.player.distanceToSqr(Vec3.atCenterOf(var11));
        if (mc.player.distanceToSqr(Vec3.atCenterOf(var11)) > Mth.square(this.range.getValue().doubleValue())) break label_8;
        var10.add(var11);
        label_8:
        if (var10.isEmpty()) return null;
        return new OpponentScaffold.Target(var5, var10);
    }

    private BlockPos getTargetPosition(Player arg0) {
        if (arg0 == null) return null;
        int var2 = this.extrapolation.getValue().intValue();
        if (var2 > 0) {
            double var3 = (arg0.getX() - arg0.xo);
            double var5 = (arg0.getZ() - arg0.zo);
            double var7 = (arg0.getX() + (var3 * ((double) var2)));
            double var9 = (arg0.getZ() + (var5 * ((double) var2)));
            return new BlockPos(((int) Math.floor(var7)), ((int) Math.floor(arg0.getY())), ((int) Math.floor(var9)));
        }
        return PositionUtils.getFlooredPosition(arg0);
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
