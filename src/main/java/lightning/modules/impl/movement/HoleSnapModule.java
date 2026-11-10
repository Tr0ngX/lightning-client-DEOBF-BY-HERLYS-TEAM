package lightning.modules.impl.movement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.mixins.accessors.Vec3dAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IHoleSnapModule;
import lightning.modules.impl.combat.SurroundModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.MovementUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "HoleSnap", description = "Pulls you toward your nearest hole.", category = Module.Category.MOVEMENT)
public class HoleSnapModule extends Module implements IHoleSnapModule {
    public NumberSetting range;
    public BooleanSetting doubleHoles;
    public BooleanSetting quadHoles;
    public BooleanSetting step;
    public BooleanSetting fillHole;
    public ModeSetting autoSwitch;
    public BooleanSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting crystalDestruction;
    public BooleanSetting render;
    public NumberSetting timer;
    public BooleanSetting renderTarget;
    public ColorSetting renderTargetColor;
    public AABB hole;
    private HoleUtils.Hole startingHole;
    private AABB lastUsedHole;
    private boolean jumpPressed;
    private int jumpAttempts;
    private AABB unreachableHole;
    private int giveUpCount;
    private static final int MAX_CONSECUTIVE_GIVE_UPS = 3;
    private int jumpWaitTicks;
    private final Deque fillQueue;
    private AABB pendingFillHole;
    private int fillWaitTicks;
    private static final int MAX_FILL_WAIT_TICKS = 20;
    private final List filledPositions;

    public HoleSnapModule() {
        this.range = new NumberSetting("Range", "Range for the holes.", 5, 1, 8);
        this.doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to snap you to double holes.", true);
        this.quadHoles = new BooleanSetting("QuadHoles", "Whether or not to snap you to quad holes.", true);
        this.step = new BooleanSetting("Step", "Keeps snapping to the next nearest hole instead of stopping once you've reached one.", false);
        this.fillHole = new BooleanSetting("FillHole", "Fills every cell of the hole you just stepped out of, once you've fully left its footprint (needs Step).", new BooleanSetting.Visibility(this.step, true), false);
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.timer = new NumberSetting("Timer", "The tick-speed multiplier to run at while HoleSnap is active (1.0 = normal speed).", 1.0f, 0.5f, 10.0f);
        this.renderTarget = new BooleanSetting("RenderTarget", "Draws an outline around the hole HoleSnap is currently walking toward.", true);
        this.renderTargetColor = new ColorSetting("RenderTargetColor", "The color of the target hole outline.", new BooleanSetting.Visibility(this.renderTarget, true), ColorUtils.getDefaultOutlineColor());
        this.hole = null;
        this.startingHole = null;
        this.lastUsedHole = null;
        this.jumpPressed = false;
        this.jumpAttempts = 0;
        this.unreachableHole = null;
        this.giveUpCount = 0;
        this.jumpWaitTicks = 0;
        this.fillQueue = new ArrayDeque();
        this.pendingFillHole = null;
        this.fillWaitTicks = 0;
        this.filledPositions = new ArrayList();
    }

    public boolean isStepEnabled() {
        return this.step.getValue();
    }

    public static boolean isInHole(BlockPos arg0) {
        if (HoleUtils.getSingleHole(arg0, 1.0, false) == null && HoleUtils.getDoubleHole(arg0, 1.0) == null) {
            if (HoleUtils.getQuadHole(arg0, 1.0) != null) {
            }
        } else {
        }
        return false;
    }

    public void onEnable() {
        this.hole = null;
        this.startingHole = null;
        this.fillQueue.clear();
        this.pendingFillHole = null;
        this.filledPositions.clear();
        this.fillWaitTicks = 0;
        this.jumpAttempts = 0;
        this.jumpWaitTicks = 0;
        this.unreachableHole = null;
        this.giveUpCount = 0;
        LightningClient.WORLD_MANAGER.setTimerMultiplier(this.timer.getValue().floatValue());
        if (!(this.step.getValue())) break label_0;
        var tmp0 = ((SurroundModule) LightningClient.MODULE_MANAGER.getModule(SurroundModule.class));
        Object var1 = tmp0;
        if (!(tmp0.isToggled())) break label_0;
        var1.setToggled(false);
        label_0:
    }

    public void onDisable() {
        LightningClient.WORLD_MANAGER.setTimerMultiplier(1.0f);
        if (!(!this.jumpPressed)) {
            mc.options.keyJump.setDown(false);
            this.jumpPressed = false;
        }
        this.jumpWaitTicks = 0;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((!(this.renderTarget.getValue())) || (this.hole == null)) {
            return;
        }
        Renderer3D.renderBoxOutline(arg0.getMatrices(), this.hole, this.renderTargetColor.getColor());
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent arg0) {
        LightningClient.WORLD_MANAGER.setTimerMultiplier(this.timer.getValue().floatValue());
        if (getNull()) return;
        if (this.pendingFillHole != null) {
            if (!(this.pendingFillHole.contains(mc.player.getX(), (this.pendingFillHole.minY + 0.5), mc.player.getZ()))) break label_1;
            this.fillWaitTicks = (this.fillWaitTicks + 1);
            if (this.fillWaitTicks < 20) return;
            label_1:
            if (this.fillQueue.isEmpty()) break label_2;
            fillNext();
            return;
            label_2:
            this.pendingFillHole = null;
            this.filledPositions.clear();
            setToggled(false);
            return;
        }
        if (mc.player.fallDistance >= 5.0 && this.hole == null) return;
        if (this.hole == null && !(pickTarget())) return;
        if (this.hole != null && !(isHoleReachable(this.hole))) {
            this.unreachableHole = this.hole;
            this.hole = null;
            this.startingHole = null;
            this.jumpAttempts = 0;
            this.giveUpCount = (this.giveUpCount + 1);
            if ((this.giveUpCount + 1) >= 3) {
                LightningClient.CHAT_MANAGER.tagged(("Gave up reaching a hole " + this.giveUpCount + " times in a row, disabling."), getName());
                setToggled(false);
                return;
            }
            if (!(pickTarget())) return;
        }
        if (this.hole != null && !(isHoleStillValid(this.hole))) {
            this.unreachableHole = this.hole;
            this.hole = null;
            this.startingHole = null;
            this.jumpAttempts = 0;
            if (!(pickTarget())) return;
        }
        if ((Math.abs((mc.player.getX() - this.hole.getCenter().x)) >= 0.03) || (Math.abs((mc.player.getY() - this.hole.minY)) >= 0.05)) break label_9;
        var tmp0 = (Math.abs((mc.player.getZ() - this.hole.getCenter().z)) < 0.03 ? 1 : 0);
        int var2 = tmp0;
        int var3 = tmp0;
        if (!var2) {
            if (((!mc.player.horizontalCollision) || (!(mc.player.onGround()))) || (this.jumpPressed)) break label_16;
            double var4 = (this.hole.getCenter().x - mc.player.getX());
            double var6 = (this.hole.getCenter().z - mc.player.getZ());
            if (var4 == 0.0 && var6 == 0.0) {
            } else {
            }
            Object var8 = new Vec3(var4, 0.0, var6).normalize();
            Object var9 = mc.player.getBoundingBox();
            Object var10 = new AABB(var9.minX, (mc.player.getY() + 2.0), var9.minZ, var9.maxX, (mc.player.getY() + 2.5), var9.maxZ).move((var8.x * 0.5), 0.0, (var8.z * 0.5));
            if (!(mc.level.noCollision(var10))) {
                this.unreachableHole = this.hole;
                this.hole = null;
                this.startingHole = null;
                this.jumpAttempts = 0;
                this.giveUpCount = (this.giveUpCount + 1);
                if ((this.giveUpCount + 1) >= 3) {
                    LightningClient.CHAT_MANAGER.tagged(("Gave up reaching a hole " + this.giveUpCount + " times in a row, disabling."), getName());
                    setToggled(false);
                    return;
                }
                pickTarget();
                return;
            }
            mc.options.keyJump.setDown(true);
            this.jumpPressed = true;
            this.jumpWaitTicks = 0;
            this.jumpAttempts = (this.jumpAttempts + 1);
            if (this.jumpAttempts <= 5) break label_16;
            mc.options.keyJump.setDown(false);
            this.jumpPressed = false;
            this.jumpAttempts = 0;
            this.unreachableHole = this.hole;
            this.hole = null;
            this.startingHole = null;
            this.giveUpCount = (this.giveUpCount + 1);
            if ((this.giveUpCount + 1) >= 3) {
                LightningClient.CHAT_MANAGER.tagged(("Gave up reaching a hole " + this.giveUpCount + " times in a row, disabling."), getName());
                setToggled(false);
            }
            return;
            label_16:
            if (!this.jumpPressed) break label_18;
            this.jumpWaitTicks = (this.jumpWaitTicks + 1);
            if (mc.player.onGround()) {
                if (this.jumpWaitTicks <= 10) break label_18;
            }
            mc.options.keyJump.setDown(false);
            this.jumpPressed = false;
            this.jumpWaitTicks = 0;
            label_18:
            MovementUtils.moveTowards(arg0, this.hole.getCenter(), MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
            return;
        }
        this.jumpAttempts = 0;
        this.giveUpCount = 0;
        arg0.setMovement(new Vec3(0.0, arg0.getMovement().y, 0.0));
        ((Vec3dAccessor) mc.player.getDeltaMovement()).setX(0.0);
        ((Vec3dAccessor) mc.player.getDeltaMovement()).setZ(0.0);
        arg0.setCancelled(true);
        if (((StepModule) LightningClient.MODULE_MANAGER.getModule(StepModule.class)).isToggled()) {
            ((StepModule) LightningClient.MODULE_MANAGER.getModule(StepModule.class)).setToggled(false);
        }
        if (((SpeedModule) LightningClient.MODULE_MANAGER.getModule(SpeedModule.class)).isToggled()) {
            ((SpeedModule) LightningClient.MODULE_MANAGER.getModule(SpeedModule.class)).setToggled(false);
        }
        if (((TickShiftModule) LightningClient.MODULE_MANAGER.getModule(TickShiftModule.class)).isToggled()) {
            ((TickShiftModule) LightningClient.MODULE_MANAGER.getModule(TickShiftModule.class)).setToggled(false);
        }
        if (this.fillHole.getValue() && this.step.getValue() && this.startingHole != null) {
            this.pendingFillHole = this.startingHole.box();
            this.fillWaitTicks = 0;
            queueFillPositions(this.startingHole.box());
            this.hole = null;
            this.startingHole = null;
            return;
        }
        setToggled(false);
        this.hole = null;
        this.startingHole = null;
    }

    private boolean isHoleReachable(AABB arg0) {
        Object var4 = BlockPos.containing((arg0.minX + 0.5), arg0.minY, (arg0.minZ + 0.5));
        int var5 = ((int) Math.floor(mc.player.getY()));
        var tmp0 = var4.getY();
        int var3 = tmp0;
        if (var5 > tmp0) {
            int var6 = (var3 + 3);
            while (var6 <= (var5 + 1)) {
                if (mc.level.getBlockState(new BlockPos(var4.getX(), var6, var4.getZ())).canBeReplaced()) {
                } else {
                    return false;
                }
                var6++;
            }
            return true;
        }
        if ((var3 - var5) >= 2) {
            var tmp1 = mc.player.blockPosition().above(2);
            Object var2 = tmp1;
            if (mc.level.getBlockState(tmp1).canBeReplaced() && mc.level.getBlockState(var2.above()).canBeReplaced()) {
            }
        } else {
        }
        return false;
    }

    private boolean pickTarget() {
        Object var3 = currentHole();
        if ((var3 == null) || (this.lastUsedHole == null)) break label_0;
        var tmp0 = (var3.box().equals(this.lastUsedHole) ? null : var3);
        Object var2 = tmp0;
        Object var4 = tmp0;
        if (var2 != null && !(this.step.getValue())) {
            break label_4;
        }
        if (var2 != null) {
            break label_4;
        }
        label_4:
        Object var5 = getHoles();
        if (this.lastUsedHole == null) break label_5;
        var tmp1 = var5.stream().filter(() -> !(arg0.box().equals(this.lastUsedHole))).toList();
        Object var1 = tmp1;
        if (tmp1.isEmpty()) break label_5;
        var5 = var1;
        label_5:
        if (this.unreachableHole != null) {
            var5 = var5.stream().filter(() -> !(arg0.box().equals(this.unreachableHole))).toList();
        }
        if (var5.isEmpty()) {
            setToggled(false);
            return false;
        }
        this.startingHole = var2;
        this.hole = ((HoleUtils.Hole) var5.get(0)).box();
        if (!(this.hole.equals(this.lastUsedHole))) {
            this.lastUsedHole = this.hole;
        }
        return true;
    }

    private boolean isHoleStillValid(AABB arg0) {
        Object var4 = BlockPos.containing((arg0.minX + 0.5), arg0.minY, (arg0.minZ + 0.5));
        Object var5 = HoleUtils.getSingleHole(var4, 1.0);
        if (var5 != null && var5.box().equals(arg0)) return true;
        if (this.doubleHoles.getValue()) {
            var tmp0 = HoleUtils.getDoubleHole(var4, 1.0);
            Object var3 = tmp0;
            if (tmp0 != null && var3.box().equals(arg0)) return true;
        }
        if (!(this.quadHoles.getValue())) break label_2;
        var tmp1 = HoleUtils.getQuadHole(var4, 1.0);
        Object var2 = tmp1;
        if (tmp1 == null) break label_2;
        return var2.box().equals(arg0);
    }

    private HoleUtils.Hole currentHole() {
        Object var1 = mc.player.blockPosition();
        double var2 = (((double) var1.getX()) + 0.5);
        double var4 = (((double) var1.getY()) + 0.5);
        double var6 = (((double) var1.getZ()) + 0.5);
        int var8 = -1;
        while (var8 <= 0) {
            int var9 = -1;
            while (var9 <= 0) {
                Object var12 = var1.offset(var8, 0, var9);
                Object var13 = HoleUtils.getSingleHole(var12, 1.0);
                if (var13 != null && var13.box().contains(var2, var4, var6)) return var13;
                if (this.doubleHoles.getValue()) {
                    var tmp0 = HoleUtils.getDoubleHole(var12, 1.0);
                    Object var11 = tmp0;
                    if (tmp0 != null && var11.box().contains(var2, var4, var6)) return var11;
                }
                if (!(this.quadHoles.getValue())) break label_5;
                var tmp1 = HoleUtils.getQuadHole(var12, 1.0);
                Object var10 = tmp1;
                if (tmp1 == null) break label_5;
                if (!(var10.box().contains(var2, var4, var6))) {
                    break label_5;
                }
                return var10;
                label_5:
                var9++;
            }
            var8++;
        }
        return null;
    }

    private List prioritizeSingle(List arg0) {
        return arg0.stream().sorted(Comparator.comparingInt((arg0) -> (arg0.type() == HoleUtils.HoleType.SINGLE ? 0 : 1))).toList();
    }

    private void queueFillPositions(AABB arg0) {
        int var2 = ((int) Math.floor(arg0.minX));
        int var3 = (((int) Math.ceil(arg0.maxX)) - 1);
        int var4 = ((int) Math.floor(arg0.minZ));
        int var5 = (((int) Math.ceil(arg0.maxZ)) - 1);
        int var6 = ((int) arg0.minY);
        int var7 = var2;
        while (var7 <= var3) {
            int var8 = var4;
            while (var8 <= var5) {
                this.fillQueue.add(new BlockPos(var7, var6, var8));
                var8++;
            }
            var7++;
        }
    }

    private void fillNext() {
        Object var1 = ((BlockPos) this.fillQueue.peek());
        if (var1 == null) return;
        if (WorldUtils.isPlaceable(var1)) break label_1;
        this.fillQueue.poll();
        return;
        label_1:
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var2 = 0;
        int var3 = InventoryUtils.findHardestBlock(0, (!(!var2) ? 35 : 8));
        if (var3 == -1) {
            this.fillQueue.clear();
            return;
        }
        int var4 = mc.player.getInventory().getSelectedSlot();
        Object var5 = WorldUtils.getDirection(var1, this.filledPositions, this.strictDirection.getValue());
        if (var5 == null) {
            Object var6 = var1.offset(0, -1, 0);
            if (!(WorldUtils.isPlaceable(var6))) {
                this.fillQueue.poll();
                return;
            }
            Object var7 = WorldUtils.getDirection(var6, this.filledPositions, this.strictDirection.getValue());
            if (var7 == null) {
                this.fillQueue.poll();
                return;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), var3, var4);
            int var8 = WorldUtils.placeBlock(var6, var7, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
            InventoryUtils.switchBack(this.autoSwitch.getValue(), var3, var4);
            if (!var8) return;
            this.filledPositions.add(var6);
            var5 = WorldUtils.getDirection(var1, this.filledPositions, this.strictDirection.getValue());
            if (var5 == null) return;
        }
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), var3, var4);
        var6 = WorldUtils.placeBlock(var1, var5, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        InventoryUtils.switchBack(this.autoSwitch.getValue(), var3, var4);
        if (!(!var6)) {
            this.filledPositions.add(var1);
            this.fillQueue.poll();
        }
    }

    private List getHoles() {
        Object var1 = new ArrayList();
        int var2 = 0;
        while (var2 < LightningClient.WORLD_MANAGER.getRadius(this.range.getValue().doubleValue())) {
            Object var5 = mc.player.blockPosition().offset(LightningClient.WORLD_MANAGER.getOffset(var2));
            if (((double) var5.getY()) > mc.player.getY()) {
                break label_5;
            }
            Object var6 = HoleUtils.getSingleHole(var5, 1.0);
            if (var6 != null) {
                var1.add(var6);
                break label_5;
            }
            if (!(this.doubleHoles.getValue())) break label_3;
            var tmp0 = HoleUtils.getDoubleHole(var5, 1.0);
            Object var4 = tmp0;
            if (tmp0 == null) break label_3;
            var1.add(var4);
            break label_5;
            label_3:
            if (!(this.quadHoles.getValue())) break label_5;
            var tmp1 = HoleUtils.getQuadHole(var5, 1.0);
            Object var3 = tmp1;
            if (tmp1 == null) {
                break label_5;
            }
            var1.add(var3);
            label_5:
            var2++;
        }
        return var1.stream().sorted(Comparator.comparing((arg0) -> mc.player.distanceToSqr(arg0.box().getCenter().x, arg0.box().getCenter().y, arg0.box().getCenter().z))).toList();
    }

}
