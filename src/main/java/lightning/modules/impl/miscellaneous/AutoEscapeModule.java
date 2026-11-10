package lightning.modules.impl.miscellaneous;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.system.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

@RegisterModule(name = "AutoEscape", description = "Eats a chorus fruit to escape as soon as you get trapped on all sides.", category = Module.Category.MISCELLANEOUS)
public class AutoEscapeModule extends Module {
    public BooleanSetting swapBack;
    public NumberSetting cooldown;
    public BooleanSetting autoDisable;
    private static final long EAT_DURATION_MS = 1600L;
    private final Timer timer;
    private boolean eating;
    private long eatingSince;
    private int previousSlot;

    public AutoEscapeModule() {
        this.swapBack = new BooleanSetting("SwapBack", "Switches back to the item you were previously holding after eating the chorus fruit.", true);
        this.cooldown = new NumberSetting("Cooldown", "The amount of time that has to pass before escaping again.", 1000, 0, 5000);
        this.autoDisable = new BooleanSetting("AutoDisable", "Disables the module after it escapes once.", false);
        this.timer = new Timer();
        this.eating = false;
        this.eatingSince = 0L;
        this.previousSlot = -1;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        if (!(!this.eating)) {
            if ((System.currentTimeMillis() - this.eatingSince) < 1600L) return;
            if (this.swapBack.getValue()) {
                InventoryUtils.switchBackNormal(this.previousSlot);
            }
            this.eating = false;
            this.timer.reset();
            LightningClient.CHAT_MANAGER.tagged("Escaped by eating a chorus fruit.", getName());
            if (this.autoDisable.getValue()) {
                setToggled(false, true);
            }
            return;
        }
        if (!(this.timer.hasTimeElapsed(this.cooldown.getValue().longValue()))) return;
        if (!(isTrapped())) return;
        int var2 = InventoryUtils.findHotbar(Items.CHORUS_FRUIT);
        if (var2 == -1) return;
        this.previousSlot = mc.player.getInventory().getSelectedSlot();
        if (var2 != this.previousSlot) {
            InventoryUtils.switchSlot("Normal", var2, this.previousSlot);
        }
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        this.eating = true;
        this.eatingSince = System.currentTimeMillis();
    }

    public boolean isEating() {
        return this.eating;
    }

    public void onDisable() {
        this.eating = false;
    }

    private boolean isTrapped() {
        Object var1 = mc.player.blockPosition().above();
        if ((((!(isSolid(var1.above()))) || (!(isSolid(var1.north())))) || (!(isSolid(var1.south())))) || (!(isSolid(var1.east())))) break label_0;
        return isSolid(var1.west());
    }

    private boolean isSolid(BlockPos arg0) {
        return !(mc.level.getBlockState(arg0).getCollisionShape(mc.level, arg0).isEmpty());
    }

}
