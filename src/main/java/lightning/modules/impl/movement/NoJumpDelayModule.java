package lightning.modules.impl.movement;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.INoJumpDelayModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "NoJumpDelay", description = "Removes the delay that slows down your jumping.", category = Module.Category.MOVEMENT)
public class NoJumpDelayModule extends Module implements INoJumpDelayModule {
    public NumberSetting ticks;

    public NoJumpDelayModule() {
        this.ticks = new NumberSetting("Ticks", "The amount of ticks that have to be waited for before jumping again.", 1, 0, 20);
    }

    public String getMetaData() {
        return String.valueOf(this.ticks.getValue());
    }

    public int getTicksValue() {
        return this.ticks.getValue().intValue();
    }

}
