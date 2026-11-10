package lightning.modules.impl.movement;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFastClimbModule;
import lightning.settings.Setting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "FastClimb", description = "Modifies your climbing speed on ladders, scaffolding, vines and other climbables.", category = Module.Category.MOVEMENT)
public class FastClimbModule extends Module implements IFastClimbModule {
    public NumberSetting speed;

    public FastClimbModule() {
        this.speed = new NumberSetting("Speed", "Speed", "How much of vanilla's climb speed to use. Ignore walks past climbables like normal blocks, 1 is vanilla's normal speed.", new Setting.Visibility(), 1.0f, 0.0f, 1.0f, 0.1f, true);
    }

    public float getSpeedValue() {
        return this.speed.getValue().floatValue();
    }

}
