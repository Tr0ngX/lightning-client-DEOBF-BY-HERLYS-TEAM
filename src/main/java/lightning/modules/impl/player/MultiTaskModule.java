package lightning.modules.impl.player;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;

@RegisterModule(name = "MultiTask", description = "Allows you to interact with blocks while eating or using an item.", category = Module.Category.PLAYER)
public class MultiTaskModule extends Module {
    public BooleanSetting pearl;
    public BooleanSetting autoTotem;

    public MultiTaskModule() {
        this.pearl = new BooleanSetting("Pearl", "Don't stop eating when you throw a pearl with Phase / KeyAction.", true);
        this.autoTotem = new BooleanSetting("AutoTotem", "Don't stop/reset your eating when AutoTotem swaps a totem into your offhand.", true);
    }

}
