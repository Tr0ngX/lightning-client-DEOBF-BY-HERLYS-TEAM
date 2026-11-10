package lightning.modules.impl.miscellaneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Scaffold", description = "Places blocks under the player to bridge automatically.", category = Module.Category.MISCELLANEOUS)
public class ScaffoldModule extends Module {
    public ModeSetting autoSwitch;
    public NumberSetting range;
    public BooleanSetting keepY;
    public BooleanSetting downwards;
    public BooleanSetting tower;
    public NumberSetting towerSpeed;
    public BooleanSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting airPlace;
    public NumberSetting delay;
    public BooleanSetting render;
    private int groundPosY;
    private BlockPos lastPlacement;
    private Block currentScaffoldBlock;
    private int ticks;
    private final Map pendingAttempts;

    public ScaffoldModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to blocks.", "Silent", InventoryUtils.SWITCH_MODES);
        this.range = new NumberSetting("Range", "Range to place blocks.", 4.0, 1.0, 6.0);
        this.keepY = new BooleanSetting("KeepY", "Maintains the player's Y level while moving.", false);
        this.downwards = new BooleanSetting("Downwards", "Places blocks below your feet when sneaking.", true);
        this.tower = new BooleanSetting("Tower", "Quickly towers up when jumping.", true);
        this.towerSpeed = new NumberSetting("TowerSpeed", "Vertical tower speed.", new BooleanSetting.Visibility(this.tower, true), 0.42, 0.1, 1.0);
        this.rotate = new BooleanSetting("Rotate", "Rotates towards block placements.", true);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions facing you.", false);
        this.airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without support.", false);
        this.delay = new NumberSetting("Delay", "Delay in ticks between placements.", 0, 0, 10);
        this.render = new BooleanSetting("Render", "Renders placed positions.", true);
        this.groundPosY = -2147483648;
        this.lastPlacement = null;
        this.currentScaffoldBlock = null;
        this.ticks = 0;
        this.pendingAttempts = new HashMap();
    }

    public void onEnable() {
        this.groundPosY = -2147483648;
        this.lastPlacement = null;
        this.ticks = 0;
        this.pendingAttempts.clear();
        WorldUtils.clearPredictedPlacements();
    }

    public void onDisable() {
        this.groundPosY = -2147483648;
        this.lastPlacement = null;
        this.currentScaffoldBlock = null;
        this.pendingAttempts.clear();
        WorldUtils.clearPredictedPlacements();
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.ticks >= this.delay.getValue().intValue()) break label_2;
        this.ticks = (this.ticks + 1);
        return;
        label_2:
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var2 = 8;
        int var3 = findValidBlockSlot(0, var2);
        if (var3 == -1) {
            if (mc.player.input != null) {
            } else {
            }
            Object var4 = Input.EMPTY;
            Object var5 = Input.EMPTY;
            if (!(var4.shift())) {
                mc.player.input.keyPresses = new Input(var4.forward(), var4.backward(), var4.left(), var4.right(), var4.jump(), true, var4.sprint());
            }
            return;
        }
        if (this.tower.getValue() && mc.options.keyJump.isDown() && !(isMovingHorizontally())) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, this.towerSpeed.getValue().doubleValue(), mc.player.getDeltaMovement().z);
        }
        var4 = ((int) Math.floor(mc.player.getY()));
        if (this.keepY.getValue() && isMovingHorizontally()) {
            if (!(mc.player.onGround())) {
                if (this.groundPosY < mc.level.getMinY()) {
                }
                this.groundPosY = var4;
            }
            var4 = this.groundPosY;
            break label_15;
        }
        if (this.downwards.getValue() && mc.options.keyShift.isDown()) {
            var4 = (mc.player.getBlockY() - 1);
            break label_15;
        }
        this.groundPosY = var4;
        label_15:
        var5 = new BlockPos(mc.player.getBlockX(), var4, mc.player.getBlockZ());
        Object var6 = var5.below();
        Object var7 = getScaffoldPlacements(var6);
        if (var7.isEmpty()) return;
        int var8 = mc.player.getInventory().getSelectedSlot();
        if (!(InventoryUtils.switchSlot(this.autoSwitch.getValue(), var3, var8))) return;
        Object var9 = var7.iterator();
        while (var9.hasNext()) {
            Object var10 = ((BlockPos) var9.next());
            if (!(mc.level.getBlockState(var10).canBeReplaced())) break label_18;
            if (isOnCooldown(var10)) {
                break label_18;
            }
            Object var11 = WorldUtils.getDirection(var10, this.strictDirection.getValue());
            if (var11 == null) {
                if (!(this.airPlace.getValue())) {
                    break label_18;
                }
                WorldUtils.airPlaceBlock(var10, InteractionHand.MAIN_HAND, (this.rotate.getValue() ? "Normal" : "None"), this.render.getValue());
                this.lastPlacement = var10;
                predictCollision(var10);
                markAttempted(var10);
                break label_18;
            }
            if (!(WorldUtils.placeBlock(var10, var11, InteractionHand.MAIN_HAND, this.rotate.getValue(), false, this.render.getValue()))) {
                break label_18;
            }
            this.lastPlacement = var10;
            predictCollision(var10);
            markAttempted(var10);
        }
        InventoryUtils.switchBack(this.autoSwitch.getValue(), var3, var8);
        this.ticks = 0;
    }

    private void predictCollision(BlockPos arg0) {
        if (this.currentScaffoldBlock != null) {
            WorldUtils.predictPlacement(arg0, this.currentScaffoldBlock);
        }
    }

    @SubscribeEvent
    public void night$onBlockUpdate(PacketReceiveEvent arg0) {
        Object var2 = arg0.getPacket();
        if ((var2 instanceof ClientboundBlockUpdatePacket)) {
            Object var3 = ((ClientboundBlockUpdatePacket) var2);
            WorldUtils.clearPredictedPlacement(var3.getPos());
            this.pendingAttempts.remove(var3.getPos());
        }
    }

    private boolean isOnCooldown(BlockPos arg0) {
        Object var2 = ((Long) this.pendingAttempts.get(arg0));
        if (var2 == null) return false;
        long var3 = Math.max(50L, (((long) LightningClient.SERVER_MANAGER.getPing()) * 2L));
        if ((System.currentTimeMillis() - var2.longValue()) < var3) return true;
        this.pendingAttempts.remove(arg0);
        return false;
    }

    private void markAttempted(BlockPos arg0) {
        this.pendingAttempts.put(arg0, System.currentTimeMillis());
    }

    private boolean isMovingHorizontally() {
        if (Math.abs(mc.player.getDeltaMovement().x) <= 0.05) {
            if (Math.abs(mc.player.getDeltaMovement().z) > 0.05) {
            }
        } else {
        }
        return false;
    }

    private int findValidBlockSlot(int arg0, int arg1) {
        int var3 = arg0;
        while (var3 <= arg1) {
            Object var7 = mc.player.getInventory().getItem(var3);
            if (var7.isEmpty()) break label_2;
            var tmp0 = var7.getItem();
            Object var6 = tmp0;
            if (!((tmp0 instanceof BlockItem))) break label_2;
            var tmp1 = ((BlockItem) var6);
            Object var4 = tmp1;
            var tmp2 = tmp1.getBlock();
            Object var5 = tmp2;
            if (tmp2.defaultBlockState().canBeReplaced()) break label_2;
            if (!(var5.defaultBlockState().blocksMotion())) {
                break label_2;
            }
            this.currentScaffoldBlock = var5;
            return var3;
            label_2:
            var3++;
        }
        return -1;
    }

    private List getScaffoldPlacements(BlockPos arg0) {
        Object var2 = new ArrayList();
        if (!(this.airPlace.getValue())) break label_1;
        if (isWithinRange(arg0)) {
            var2.add(arg0);
        }
        return var2;
        label_1:
        ensurePlaceableWithSupport(arg0, var2);
        if (this.lastPlacement == null) break label_18;
        int var3 = this.lastPlacement.getX();
        int var4 = this.lastPlacement.getY();
        int var5 = this.lastPlacement.getZ();
        int var6 = arg0.getX();
        int var7 = arg0.getY();
        int var8 = arg0.getZ();
        int var9 = (var6 - var3);
        int var10 = (var7 - var4);
        int var11 = (var8 - var5);
        int var12 = Integer.compare(var9, 0);
        int var13 = Integer.compare(var10, 0);
        int var14 = Integer.compare(var11, 0);
        var9 = Math.abs(var9);
        var10 = Math.abs(var10);
        var11 = Math.abs(var11);
        int var15 = (var9 << 1);
        int var16 = (var10 << 1);
        int var17 = (var11 << 1);
        int var18 = 0;
        if (var9 >= var10 && var9 >= var11) {
            int var19 = (var16 - var9);
            int var20 = (var17 - var9);
            label_2:
            Object var21 = new BlockPos(var3, var4, var5);
            ensurePlaceableWithSupport(var21, var2);
            var18++;
            if (var18 > 8) break label_6;
            if (var3 == var6 && var4 == var7) {
                if (var5 == var8) break label_6;
            }
            if (var19 >= 0) {
                var4 = (var4 + var13);
                var19 = (var19 - var15);
            }
            if (var20 >= 0) {
                var5 = (var5 + var14);
                var20 = (var20 - var15);
            }
            var3 = (var3 + var12);
            var19 = (var19 + var16);
            var20 = (var20 + var17);
            break label_2;
            label_6:
            break label_18;
        }
        if (var10 >= var9 && var10 >= var11) {
            var19 = (var15 - var10);
            var20 = (var17 - var10);
            label_8:
            var21 = new BlockPos(var3, var4, var5);
            ensurePlaceableWithSupport(var21, var2);
            var18++;
            if (var18 > 8) break label_12;
            if (var3 == var6 && var4 == var7) {
                if (var5 == var8) break label_12;
            }
            if (var19 >= 0) {
                var3 = (var3 + var12);
                var19 = (var19 - var16);
            }
            if (var20 >= 0) {
                var5 = (var5 + var14);
                var20 = (var20 - var16);
            }
            var4 = (var4 + var13);
            var19 = (var19 + var15);
            var20 = (var20 + var17);
            break label_8;
            label_12:
            break label_18;
        }
        var19 = (var15 - var11);
        var20 = (var16 - var11);
        label_14:
        var21 = new BlockPos(var3, var4, var5);
        ensurePlaceableWithSupport(var21, var2);
        var18++;
        if (var18 > 8) break label_18;
        if (var3 == var6 && var4 == var7 && var5 == var8) {
            break label_18;
        }
        if (var19 >= 0) {
            var3 = (var3 + var12);
            var19 = (var19 - var17);
        }
        if (var20 >= 0) {
            var4 = (var4 + var13);
            var20 = (var20 - var17);
        }
        var5 = (var5 + var14);
        var19 = (var19 + var15);
        var20 = (var20 + var16);
        break label_14;
        label_18:
        return var2;
    }

    private void ensurePlaceableWithSupport(BlockPos arg0, List arg1) {
        if (!(mc.level.getBlockState(arg0).canBeReplaced())) return;
        Object var3 = WorldUtils.getDirection(arg0, this.strictDirection.getValue());
        if (var3 != null) {
            if (!(arg1.contains(arg0)) && isWithinRange(arg0)) {
                arg1.add(arg0);
            }
            return;
        }
        Object var4 = getSupportingBlock(arg0);
        if (var4 != null) {
            if ((arg1.contains(var4)) || (!(isWithinRange(var4)))) break label_7;
            arg1.add(var4);
            break label_7;
        }
        Object var5 = arg0.below();
        int var6 = 0;
        label_4:
        var6++;
        if ((var6 >= 3) || (!(mc.level.getBlockState(var5).canBeReplaced()))) break label_7;
        if (WorldUtils.getDirection(var5, this.strictDirection.getValue()) != null) {
            if (arg1.contains(var5)) break label_7;
            if (!(isWithinRange(var5))) {
                break label_7;
            }
            arg1.add(var5);
            break label_7;
        }
        var5 = var5.below();
        break label_4;
        label_7:
        if (!(arg1.contains(arg0)) && isWithinRange(arg0)) {
            arg1.add(arg0);
        }
    }

    private BlockPos getSupportingBlock(BlockPos arg0) {
        Object var2 = Direction.Plane.HORIZONTAL.iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((Direction) var2.next());
            Object var4 = arg0.relative(var3);
            if (WorldUtils.getDirection(var4, this.strictDirection.getValue()) == null) {
                break label_0;
            }
            return var4;
        }
        return null;
    }

    private boolean isWithinRange(BlockPos arg0) {
        return mc.player.distanceToSqr(Vec3.atCenterOf(arg0)) <= (this.range.getValue().doubleValue() * this.range.getValue().doubleValue());
    }

}
