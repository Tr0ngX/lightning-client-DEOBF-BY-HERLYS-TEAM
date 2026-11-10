package lightning.modules.impl.miscellaneous;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;

public class TableTrollModule extends Module {
    public NumberSetting limit;
    public NumberSetting delay;
    private int ticks;

    public TableTrollModule() {
        this.limit = new NumberSetting("Limit", "The maximum number of blocks that can be placed each group.", 4, 1, 20);
        this.delay = new NumberSetting("Delay", "The delay in ticks between each group of placements.", 0, 0, 20);
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (this.ticks < this.delay.getValue().intValue()) {
            this.ticks = (this.ticks + 1);
            return;
        }
        int var2 = 0;
        int var3 = 0;
        while (var3 < LightningClient.WORLD_MANAGER.getRadius(5.0) && var2 <= this.limit.getValue().intValue()) {
            Object var4 = mc.player.blockPosition().offset(LightningClient.WORLD_MANAGER.getOffset(var3));
            if ((!(WorldUtils.isPlaceable(var4))) || (mc.level.getBlockState(var4.below()).canBeReplaced())) break label_3;
            if (mc.level.getBlockState(var4.below()).getBlock() == Blocks.ENDER_CHEST) {
                break label_3;
            }
            WorldUtils.placeBlock(var4, WorldUtils.getDirection(var4, false), InteractionHand.MAIN_HAND, false, false);
            this.ticks = this.delay.getValue().intValue();
            var2++;
            label_3:
            var3++;
        }
    }

}
