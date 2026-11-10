package lightning.modules.impl.movement;

import lightning.mixins.accessors.ShulkerBoxProgressAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.ModeSetting;
import lightning.utils.BlockEntityInstanceTracker;
import lightning.utils.EarlyTickHooks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;

@RegisterModule(name = "HighJump", description = "Alek Grim Shulker HighJump.", category = Module.Category.MOVEMENT)
public class HighJumpModule extends Module {
    private long timestamp;
    public final ModeSetting mode;
    private final Runnable earlyTickCallback;

    public HighJumpModule() {
        this.timestamp = System.currentTimeMillis();
        var tmp0 = new String[1];
        tmp0[0] = "Grim Shulker";
        this.mode = new ModeSetting("Mode", "Selects the type of mode", "Grim Shulker", tmp0);
        this.earlyTickCallback = this::onEarlyTick;
    }

    public void onEnable() {
        EarlyTickHooks.register(this.earlyTickCallback);
    }

    public void onDisable() {
        EarlyTickHooks.unregister(this.earlyTickCallback);
    }

    private boolean passed(int arg0) {
        return (System.currentTimeMillis() - ((long) arg0)) >= this.timestamp;
    }

    private void onEarlyTick() {
        if (getNull()) return;
        if (passed(1000)) {
            BlockEntityInstanceTracker.snapshot().stream().filter(this::qualifies).findFirst().ifPresent(() -> this.timestamp = System.currentTimeMillis());
        }
        if (!(passed(199))) {
            Object var1 = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(var1.x, (var1.y + 0.899), var1.z);
        }
    }

    private static boolean qualifies(BlockEntity arg0) {
        if ((arg0 instanceof ShulkerBoxBlockEntity)) {
            Object var1 = ((ShulkerBoxBlockEntity) arg0);
        } else {
            return false;
        }
        if (!(((Direction) var1.getBlockState().getValue(ShulkerBoxBlock.FACING)).getStepY())) return false;
        float var2 = ((ShulkerBoxProgressAccessor) var1).night$getCurrentProgress();
        if (var2 <= 0.0f) return false;
        return mc.player.getBoundingBox().inflate(0.7, 1.0, 0.7).intersects(new AABB(var1.getBlockPos()));
    }

}
