package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMineEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.system.ThreadExecutor;
import lightning.utils.system.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Blocker", description = "Places blocks to stop enemies from placing crystals.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class BlockerModule extends Module {
    public ModeSetting autoSwitch;
    public BooleanSetting asynchronous;
    public NumberSetting limit;
    public NumberSetting delay;
    public NumberSetting range;
    public BooleanSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting crystalDestruction;
    public BooleanSetting whileEating;
    public BooleanSetting feet;
    public BooleanSetting head;
    public BooleanSetting render;
    private final CopyOnWriteArrayList targetPositions;
    private BlockerModule.Mine mine;
    private int ticks;

    public BlockerModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
        this.limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", 4, 1, 20);
        this.delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", 0, 0, 20);
        this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
        this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
        this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
        this.feet = new BooleanSetting("Feet", "Places on feet level blocks.", true);
        this.head = new BooleanSetting("Head", "Places on head level blocks.", true);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.targetPositions = new CopyOnWriteArrayList();
        this.mine = null;
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) return;
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) return;
        Object var2 = () -> {
    if ((mc.player == null) || (mc.level == null)) {
        return;
    }
    Object var1 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
    if ((this.mine == null) || (var1.getPrimary() == null)) break label_2;
    if (!(this.mine.position().equals(var1.getPrimary().getPosition()))) {
        label_2:
        if (var1.getSecondary() != null && this.mine.position().equals(var1.getSecondary().getPosition())) {
        }
        this.mine = null;
        return;
    }
    int var2 = 0;
    if (this.ticks < this.delay.getValue().intValue()) {
        this.ticks = (this.ticks + 1);
        return;
    }
    Object var3 = HoleUtils.getFeetPositions(mc.player, true, false, false);
    Object var4 = HoleUtils.getInsidePositions(mc.player);
    if ((this.mine == null) || (!(this.mine.timer().hasTimeElapsed(Math.max((this.mine.breakTime() - 200.0f), 0.0f))))) break label_15;
    Object var5 = this.mine.position();
    if (this.mine.type() == BlockerModule.MineType.FEET && this.feet.getValue()) {
        if (!(var3.contains(this.mine.position()))) break label_9;
        this.targetPositions.add(new BlockerModule.Position(var5, var5.above()));
        Object var6 = Direction.values();
        int var7 = var6.length;
        int var8 = 0;
        while (var8 < var7) {
            Object var9 = var6[var8];
            if (!(var9.getAxis().isHorizontal())) {
            } else {
                this.targetPositions.add(new BlockerModule.Position(var5, var5.relative(var9)));
            }
            var8++;
        }
        label_9:
        this.mine = null;
        break label_15;
    }
    if (this.mine.type() != BlockerModule.MineType.HEAD) {
        if (this.mine.type() != BlockerModule.MineType.SIDE) break label_15;
    }
    if (!(this.head.getValue())) break label_15;
    if (this.mine.type() == BlockerModule.MineType.HEAD) {
        if (!(var4.contains(this.mine.position().below().below()))) {
        }
        if (this.mine.type() == BlockerModule.MineType.SIDE && var3.contains(this.mine.position().below())) {
        }
        this.targetPositions.add(new BlockerModule.Position(var5, var5.above()));
    }
    this.mine = null;
    label_15:
    this.targetPositions.removeIf((arg0) -> !(WorldUtils.isPlaceable(arg0.position())));
    this.targetPositions.removeIf(() -> mc.player.distanceToSqr(Vec3.atCenterOf(arg0.position())) > Mth.square(this.range.getValue().doubleValue()));
    this.targetPositions.removeIf((arg2) -> {
    if ((arg0.contains(arg2.original())) || (arg0.contains(arg2.original().below()))) break label_0;
    return !(arg1.contains(arg2.original().below().below()));
});
    if (this.targetPositions.isEmpty()) return;
    if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
        if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
        }
    } else {
    }
    var5 = InventoryUtils.findHardestBlock(35, 8);
    var6 = mc.player.getInventory().getSelectedSlot();
    if (var5 == -1) {
        this.targetPositions.clear();
        return;
    }
    LightningClient.ROTATION_MANAGER.beginBatchRotation();
    InventoryUtils.switchSlot(this.autoSwitch.getValue(), var5, var6);
    var7 = new ArrayList(this.targetPositions).iterator();
    while (var7.hasNext()) {
        var8 = ((BlockerModule.Position) var7.next());
        if (var2 >= this.limit.getValue().intValue()) {
            break label_25;
        }
        var9 = WorldUtils.getDirection(var8.position(), null, this.strictDirection.getValue());
        if (var9 == null) {
            break label_22;
        }
        WorldUtils.placeBlock(var8.position(), var9, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        this.targetPositions.remove(var8);
        var2++;
    }
    label_25:
    InventoryUtils.switchBack(this.autoSwitch.getValue(), var5, var6);
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    break label_29;
    Object var10 = 0;
    LightningClient.ROTATION_MANAGER.endBatchRotation();
    throw var10;
    label_29:
    this.ticks = 0;
};
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(var2);
        } else {
            var2.run();
        }
    }

    @SubscribeEvent
    public void onPlayerMine(PlayerMineEvent arg0) {
        if (shouldRunOnProxy()) return;
        if (mc.player == null) break label_1;
        if (mc.level == null) {
            label_1:
            return;
        }
        if (this.mine != null && this.mine.position().equals(arg0.getPosition())) return;
        Object var2 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        if (var2.getPrimary() != null) {
            if (!(arg0.getPosition().equals(var2.getPrimary().getPosition()))) {
            }
            if (var2.getSecondary() != null && arg0.getPosition().equals(var2.getSecondary().getPosition())) {
            }
            return;
        }
        Object var3 = mc.level.getEntity(arg0.getActorID());
        if (var3 == mc.player) return;
        if (!((var3 instanceof Player))) return;
        Object var4 = ((Player) var3);
        if (LightningClient.FRIEND_MANAGER.contains(var4.getName().getString())) return;
        Object var5 = HoleUtils.getFeetPositions(mc.player, true, false, false);
        Object var6 = HoleUtils.getInsidePositions(mc.player);
        if (this.feet.getValue() && var5.contains(arg0.getPosition())) {
            this.mine = new BlockerModule.Mine(arg0.getPosition(), new Timer(), WorldUtils.getBreakTime(var4, mc.level.getBlockState(arg0.getPosition())), BlockerModule.MineType.FEET);
            return;
        }
        if (!(this.head.getValue())) break label_12;
        if (var5.contains(arg0.getPosition().below())) {
            this.mine = new BlockerModule.Mine(arg0.getPosition(), new Timer(), WorldUtils.getBreakTime(var4, mc.level.getBlockState(arg0.getPosition())), BlockerModule.MineType.SIDE);
        }
        if (!(var6.contains(arg0.getPosition().below().below()))) break label_12;
        this.mine = new BlockerModule.Mine(arg0.getPosition(), new Timer(), WorldUtils.getBreakTime(var4, mc.level.getBlockState(arg0.getPosition())), BlockerModule.MineType.HEAD);
        label_12:
    }

    public void onDisable() {
        this.targetPositions.clear();
    }

    public String getMetaData() {
        if (this.targetPositions == null) return "0";
        return String.valueOf(this.targetPositions.size());
    }


    record Mine(BlockPos position, Timer timer, float breakTime, BlockerModule.MineType type) {

        private Mine(BlockPos arg0, Timer arg1, float arg2, BlockerModule.MineType arg3) {
            this.position = arg0;
            this.timer = arg1;
            this.breakTime = arg2;
            this.type = arg3;
        }

        public BlockPos position() {
            return this.position;
        }

        public Timer timer() {
            return this.timer;
        }

        public float breakTime() {
            return this.breakTime;
        }

        public BlockerModule.MineType type() {
            return this.type;
        }

    }

    record Position(BlockPos original, BlockPos position) {

        private Position(BlockPos arg0, BlockPos arg1) {
            this.original = arg0;
            this.position = arg1;
        }

        public BlockPos original() {
            return this.original;
        }

        public BlockPos position() {
            return this.position;
        }

    }

    enum MineType {
        FEET,
        HEAD,
        SIDE;


        private MineType() {
        }

    }
}
