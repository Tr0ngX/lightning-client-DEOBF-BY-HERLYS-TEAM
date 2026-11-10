package lightning.modules.impl.player;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import net.minecraft.world.item.Items;

@RegisterModule(name = "NoInteract", description = "Prevents you from interacting with right-clickable blocks.", category = Module.Category.PLAYER)
public class NoInteractModule extends Module {
    public ModeSetting mode;
    public BooleanSetting gapple;

    public NoInteractModule() {
        var tmp0 = new String[2];
        tmp0[0] = "Sneak";
        tmp0[1] = "Disable";
        this.mode = new ModeSetting("Mode", "The way that right-clickable blocks will be ignored.", "Sneak", tmp0);
        this.gapple = new BooleanSetting("Gapple", "Only disables interactions when holding a golden apple in your main hand.", false);
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

    public boolean shouldNoInteract() {
        if (!(this.gapple.getValue())) return true;
        Object var1 = mc.player.getMainHandItem().getItem();
        if (!(var1.equals(Items.ENCHANTED_GOLDEN_APPLE))) {
            if (var1.equals(Items.GOLDEN_APPLE)) {
            }
        } else {
        }
        return false;
    }

}
