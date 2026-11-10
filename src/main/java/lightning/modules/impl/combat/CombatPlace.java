package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
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
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "CombatPlace", description = "Automatically places blocks in front of moving targets to block them.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class CombatPlace extends Module {
    public BooleanSetting flatten;
    public ModeSetting mode;
    public ModeSetting targetPriority;
    public NumberSetting predictTicks;
    public NumberSetting minKmh;
    public NumberSetting limit;
    public NumberSetting delay;
    public BooleanSetting strict;
    public BooleanSetting safety;
    public NumberSetting range;
    public BooleanSetting rotate;
    public BooleanSetting crystalDestruction;
    private int delayTicks;

    public CombatPlace() {
        this.flatten = new BooleanSetting("Flatten", "Place blocks under the target.", true);
        var tmp0 = new String[4];
        tmp0[0] = "None";
        tmp0[1] = "One";
        tmp0[2] = "Two";
        tmp0[3] = "Three";
        this.mode = new ModeSetting("Mode", "Prediction mode.", "Two", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Closest";
        tmp1[1] = "Health";
        this.targetPriority = new ModeSetting("Priority", "Target selection priority.", "Closest", tmp1);
        this.predictTicks = new NumberSetting("PredictTicks", "Prediction scale.", 2, 1, 8);
        this.minKmh = new NumberSetting("MinKMH", "Min speed to trigger blocker.", 20, 1, 40);
        this.limit = new NumberSetting("Limit", "The maximum number of blocks that can be placed per tick.", 4, 1, 20);
        this.delay = new NumberSetting("Delay", "The delay in ticks between placements.", 0, 0, 20);
        this.strict = new BooleanSetting("Strict", "Prevent placement if entities intersect.", true);
        this.safety = new BooleanSetting("Safety", "Prevent trapping yourself.", true);
        this.range = new NumberSetting("Range", "The maximum range at which blocks will be placed.", 5.0, 0.0, 12.0);
        this.rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
        this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
        this.delayTicks = 0;
    }

    public void onEnable() {
        this.delayTicks = 0;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) return;
        if (getNull()) return;
        Object var2 = findTarget();
        if (var2 == null) return;
        if (this.delayTicks < this.delay.getValue().intValue()) {
            this.delayTicks = (this.delayTicks + 1);
            return;
        }
        Object var3 = getPlacePositions(var2);
        if (var3.isEmpty()) return;
        double var4 = Math.pow(this.range.getValue().doubleValue(), 2.0);
        var3.removeIf((arg1) -> mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg1)) > arg0);
        if (var3.isEmpty()) return;
        placeBlocks(var3);
        this.delayTicks = 0;
    }

    private void placeBlocks(List arg0) {
        if (mc.player == null) return;
        int var2 = InventoryUtils.findHardestBlock(0, 8);
        if (var2 == -1) return;
        int var3 = mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot("Silent", var2, var3);
        int var4 = 0;
        Object var5 = arg0.iterator();
        while (var5.hasNext()) {
            Object var6 = ((BlockPos) var5.next());
            if (var4 >= this.limit.getValue().intValue()) {
                break label_5;
            }
            Object var7 = WorldUtils.getDirection(var6, false);
            if (var7 == null) {
                break label_2;
            }
            WorldUtils.placeBlock(var6, var7, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), false);
            var4++;
        }
        label_5:
        InventoryUtils.switchBack("Silent", var2, var3);
    }

    private List getPlacePositions(Player arg0) {
        Object var6 = new ArrayList();
        if (!(this.flatten.getValue())) break label_0;
        var tmp0 = BlockPos.containing(arg0.position()).below();
        Object var5 = tmp0;
        if (!(isValidSpot(tmp0))) break label_0;
        var6.add(var5);
        label_0:
        var tmp1 = this.mode.getValue();
        Object var4 = tmp1;
        if (tmp1.equals("None")) break label_5;
        double var2 = EntityUtils.getSpeed(arg0, EntityUtils.SpeedUnit.KILOMETERS);
        if (EntityUtils.getSpeed(arg0, EntityUtils.SpeedUnit.KILOMETERS) < this.minKmh.getValue().doubleValue()) break label_5;
        Object var9 = arg0.getInterpolation();
        if (var9 != null) {
        } else {
        }
        Object var10 = arg0.position();
        double var11 = var10.x;
        double var13 = var10.z;
        double var15 = (var11 - arg0.getX());
        double var7 = (var13 - arg0.getZ());
        double var17 = Math.sqrt(((var15 * var15) + ((var13 - arg0.getZ()) * var7)));
        if (var17 <= 0.001) break label_5;
        double var21 = (var15 / var17);
        double var23 = (var7 / var17);
        double var25 = Math.sqrt((Math.pow((arg0.getX() - arg0.xo), 2.0) + Math.pow((arg0.getZ() - arg0.zo), 2.0)));
        double var19 = this.predictTicks.getValue().doubleValue();
        Object var27 = BlockPos.containing((var11 + ((var21 * var25) * this.predictTicks.getValue().doubleValue())), arg0.getY(), (var13 + ((var23 * var25) * var19)));
        if (!(isValidSpot(var27))) break label_5;
        var6.add(var27);
        if (!(var4.equals("Two"))) {
            if (!(var4.equals("Three"))) break label_4;
        }
        var tmp2 = var27.above();
        Object var29 = tmp2;
        if (!(isValidSpot(tmp2))) break label_4;
        var6.add(var29);
        label_4:
        if (!(var4.equals("Three"))) break label_5;
        var tmp3 = var27.above(2);
        Object var28 = tmp3;
        if (!(isValidSpot(tmp3))) break label_5;
        var6.add(var28);
        label_5:
        return var6;
    }

    private boolean isValidSpot(BlockPos arg0) {
        if (mc.level == null) return false;
        if (mc.level.isOutsideBuildHeight(arg0)) return false;
        if (!(mc.level.getBlockState(arg0).canBeReplaced())) return false;
        if (this.strict.getValue()) {
            var tmp0 = new AABB(arg0);
            Object var2 = tmp0;
            if (mc.level.getEntities(((Entity) null), tmp0, (arg0) -> true).stream().anyMatch((arg0) -> (arg0 instanceof Player))) return false;
        }
        if (this.safety.getValue() && mc.player != null) {
            if (!(mc.player.getBoundingBox().intersects(new AABB(arg0)))) {
            }
        } else {
        }
        return false;
    }

    private Player findTarget() {
        if ((mc.level == null) || (mc.player == null)) {
            return null;
        }
        return ((Player) mc.level.players().stream().filter((arg0) -> {
    if ((arg0 == mc.player) || (arg0.isRemoved())) break label_0;
    return !(LightningClient.FRIEND_MANAGER.contains(arg0.getName().getString()));
}).filter(() -> ((double) mc.player.distanceTo(arg0)) <= (this.range.getValue().doubleValue() + 4.0)).min(getComparator()).orElse(null));
    }

    private Comparator getComparator() {
        if (this.targetPriority.getValue().equals("Health")) return Comparator.comparingDouble((arg0) -> ((double) (arg0.getHealth() + arg0.getAbsorptionAmount())));
        return Comparator.comparingDouble((arg0) -> {
    if (mc.player != null) {
    } else {
    }
    return 0.0;
});
    }

}
