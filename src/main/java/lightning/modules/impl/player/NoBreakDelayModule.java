package lightning.modules.impl.player;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "NoBreakDelay", description = "Allows you to customize the delay between breaking blocks.", category = Module.Category.PLAYER)
public class NoBreakDelayModule extends Module {
    public NumberSetting delay;

    public NoBreakDelayModule() {
        this.delay = new NumberSetting("Delay", "The delay between block breaks in ticks (0 is no delay, 5 is vanilla).", 0, 0, 5);
    }

    public String getMetaData() {
        return String.valueOf(this.delay.getValue().intValue());
    }

}
