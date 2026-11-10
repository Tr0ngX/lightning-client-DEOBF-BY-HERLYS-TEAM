package lightning.modules.impl.combat;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

@RegisterModule(name = "AntiPhase", description = "Places a climbable block (Scaffolding, Ladder, Vines, Rail) directly at a target enemy's feet.", category = Module.Category.COMBAT)
public class AntiPhaseModule extends Module {
    public ModeSetting block;
    public ModeSetting autoSwitch;
    public ModeSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting render;
    public NumberSetting range;
    public NumberSetting wallRange;
    public BooleanSetting onlyHole;
    public BooleanSetting pauseOnEat;
    private Player target;

    public AntiPhaseModule() {
        var tmp0 = new String[7];
        tmp0[0] = "Scaffolding";
        tmp0[1] = "Ladder";
        tmp0[2] = "Vines";
        tmp0[3] = "Rail";
        tmp0[4] = "PoweredRail";
        tmp0[5] = "DetectorRail";
        tmp0[6] = "ActivatorRail";
        this.block = new ModeSetting("Block", "The item placed at the enemy's feet.", "Scaffolding", tmp0);
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to the selected item.", "Silent", InventoryUtils.SWITCH_MODES);
        var tmp1 = new String[3];
        tmp1[0] = "None";
        tmp1[1] = "Normal";
        tmp1[2] = "Grim";
        this.rotate = new ModeSetting("Rotate", "The rotation mode when placing.", "Grim", tmp1);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.range = new NumberSetting("Range", "The maximum distance at which enemies will be targeted.", 5.0f, 1.0f, 6.0f);
        this.wallRange = new NumberSetting("WallRange", "The maximum distance at which enemies will be targeted through walls (no line of sight required).", 3.0f, 0.0f, 6.0f);
        this.onlyHole = new BooleanSetting("OnlyHole", "Only places while the enemy is already standing in a hole (feet surrounded by blocks).", false);
        this.pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses placing while eating or using items.", false);
    }

    public String getMetaData() {
        if (this.target != null) {
        } else {
        }
        return "None";
    }

    public void onDisable() {
        this.target = null;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.pauseOnEat.getValue())) break label_3;
        if (mc.player.isUsingItem()) break label_2;
        if (mc.player.getUseItemRemainingTicks() <= 0) break label_3;
        label_2:
        return;
        label_3:
        this.target = findTarget();
        if (this.target == null) return;
        if (this.onlyHole.getValue() && !(HoleUtils.isPlayerInHole(this.target))) return;
        int var2 = findItemSlot(resolveItem());
        if (var2 == -1) return;
        Object var3 = footCells(this.target);
        int var4 = mc.player.getInventory().getSelectedSlot();
        LightningClient.ROTATION_MANAGER.beginBatchRotation();
        if (!(InventoryUtils.switchSlot(this.autoSwitch.getValue(), var2, var4))) {
            LightningClient.ROTATION_MANAGER.endBatchRotation();
            return;
        }
        Object var5 = var3.iterator();
        while (var5.hasNext()) {
            Object var6 = ((BlockPos) var5.next());
            if (!(isPlaceable(var6, this.target))) {
                break label_10;
            }
            int var7 = 0;
            Object var8 = WorldUtils.getDirections(var6, List.of(), this.strictDirection.getValue()).iterator();
            label_12:
            if (!(var8.hasNext())) break label_14;
            Object var9 = ((Direction) var8.next());
            if (!(WorldUtils.placeBlock(var6, var9, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue()))) {
                break label_12;
            }
            var7 = 1;
            break label_14;
            label_14:
            if (!(!var7)) {
                break label_10;
            }
            WorldUtils.airPlaceBlock(var6, InteractionHand.MAIN_HAND, "None", this.render.getValue());
        }
        InventoryUtils.switchBack(this.autoSwitch.getValue(), var2, var4);
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        break label_20;
        Object var10 = null;
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        throw var10;
        label_20:
    }

    private Item resolveItem() {
        if (this.block.getValue().equals("Rail")) return Items.RAIL;
        if (this.block.getValue().equals("PoweredRail")) return Items.POWERED_RAIL;
        if (this.block.getValue().equals("DetectorRail")) return Items.DETECTOR_RAIL;
        if (this.block.getValue().equals("ActivatorRail")) return Items.ACTIVATOR_RAIL;
        Object var1 = this.block.getValue();
        int var2 = -1;
        switch (var1.hashCode()) {
            case -2026491838 -> { break label_4; }
            case 82659817 -> { break label_5; }
            default -> { break label_6; }
        }
        label_4:
        if (!(var1.equals("Ladder"))) break label_6;
        var2 = 0;
        break label_6;
        label_5:
        if (!(var1.equals("Vines"))) break label_6;
        var2 = 1;
        label_6:
        switch (var2) {
            case 0 -> { break label_7; }
            case 1 -> { break label_8; }
            default -> { break label_9; }
        }
        label_7:
        break label_10;
        label_8:
        break label_10;
        label_9:
        label_10:
        return Items.SCAFFOLDING;
    }

    private int findItemSlot(Item arg0) {
        int var2 = 0;
        while (var2 <= 8) {
            if (!(mc.player.getInventory().getItem(var2).is(arg0))) {
            } else {
                return var2;
            }
            var2++;
        }
        return -1;
    }

    private Player findTarget() {
        return ((Player) mc.level.players().stream().filter(this::isValidTarget).min(Comparator.comparingDouble((arg0) -> mc.player.distanceToSqr(arg0))).orElse(null));
    }

    private boolean isValidTarget(Player arg0) {
        if ((((arg0 == mc.player) || (!(arg0.isAlive()))) || (arg0.isRemoved())) || (arg0.isSpectator())) {
            return false;
        }
        if (EntityUtils.isGhost(arg0)) return false;
        if (LightningClient.FRIEND_MANAGER.contains(arg0.getName().getString())) return false;
        double var2 = mc.player.distanceToSqr(arg0);
        if (var2 > Mth.square(this.range.getValue().doubleValue())) return false;
        int var4 = (var2 > Mth.square(this.wallRange.getValue().doubleValue()) ? 1 : 0);
        if (!(!var4)) {
            if (WorldUtils.canSee(arg0)) {
            }
        } else {
        }
        return false;
    }

    private boolean isPlaceable(BlockPos arg0, Player arg1) {
        if (mc.level == null) return false;
        if (mc.level.isOutsideBuildHeight(arg0)) return false;
        if (!(mc.level.getBlockState(arg0).canBeReplaced())) return false;
        return mc.level.getEntities(((Entity) null), new AABB(arg0), (arg0) -> true).stream().noneMatch((arg1) -> {
    if ((((arg1 == arg0) || ((arg1 instanceof ExperienceOrb))) || ((arg1 instanceof ItemEntity))) || ((arg1 instanceof AbstractArrow))) break label_0;
    return !(EntityUtils.isGhost(arg1));
});
    }

    private Set footCells(Player arg0) {
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

}
