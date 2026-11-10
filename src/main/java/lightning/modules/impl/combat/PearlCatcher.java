package lightning.modules.impl.combat;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "PearlCatcher", description = "Places obsidian in the air to stop players pearls from reaching their destination.", category = Module.Category.COMBAT)
public class PearlCatcher extends Module {
    public ModeSetting autoSwitch;
    public BooleanSetting rotate;
    public NumberSetting range;
    public NumberSetting enemyRange;
    public NumberSetting distance;
    public BooleanSetting holeCheck;
    public BooleanSetting whileEating;
    public BooleanSetting itemDisable;
    public BooleanSetting render;

    public PearlCatcher() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
        this.range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
        this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", 8.0f, 0.0f, 16.0f);
        this.distance = new NumberSetting("Distance", "The distance at which the obisidan block will be placed from the player.", 4.0f, 3.0f, 6.0f);
        this.holeCheck = new BooleanSetting("HoleCheck", "Only self traps whenever you are in a hole.", false);
        this.whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
        this.itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent arg0) {
        Object var4 = arg0.getEntity();
        if (!((var4 instanceof ThrownEnderpearl))) {
            break label_1;
        }
        Object var3 = ((ThrownEnderpearl) var4);
        if (this.whileEating.getValue()) break label_2;
        if (EntityUtils.isEating()) break label_1;
        break label_2;
        label_1:
        return;
        label_2:
        var4 = var3.getOwner();
        if ((var4 instanceof Player)) {
            var tmp0 = ((Player) var4);
            Object var2 = tmp0;
            if (!(validTarget(tmp0))) {
            }
            return;
        }
        catchPearl(var2);
    }

    private void catchPearl(Player arg0) {
        if (this.autoSwitch.getValue().equalsIgnoreCase("None") && !(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
            if (this.itemDisable.getValue()) {
                LightningClient.CHAT_MANAGER.tagged("You are currently not holding any blocks.", getName());
                setToggled(false);
            }
            return;
        }
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var2 = InventoryUtils.findHardestBlock(35, 8);
        int var3 = mc.player.getInventory().getSelectedSlot();
        if (var2 == -1) {
            if (this.itemDisable.getValue()) {
                LightningClient.CHAT_MANAGER.tagged("No blocks could be found in your hotbar.", getName());
                setToggled(false);
            }
            return;
        }
        Object var4 = arg0.pick(((double) this.distance.getValue().floatValue()), 0.0f, false);
        if (!((var4 instanceof BlockHitResult))) return;
        Object var5 = ((BlockHitResult) var4);
        Object var6 = var5.getBlockPos();
        if (mc.player.distanceToSqr(Vec3.atCenterOf(var6)) > Mth.square(this.range.getValue().doubleValue())) return;
        if (mc.level.getBlockState(var6).getBlock().equals(Blocks.AIR)) {
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), var2, var3);
            WorldUtils.placeBlock(var6, WorldUtils.getDirection(var6, false), InteractionHand.MAIN_HAND, this.rotate.getValue(), false, this.render.getValue());
            InventoryUtils.switchBack(this.autoSwitch.getValue(), var2, var3);
        }
    }

    private boolean validTarget(Player arg0) {
        if (arg0 == mc.player) return false;
        if (mc.player.distanceToSqr(arg0) > Mth.square(this.enemyRange.getValue().doubleValue())) return false;
        if (LightningClient.FRIEND_MANAGER.contains(arg0.getName().getString())) return false;
        if (this.holeCheck.getValue()) {
            if (HoleUtils.isPlayerInHole(arg0)) {
            }
        } else {
        }
        return false;
    }

}
