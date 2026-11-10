package lightning.modules.impl.miscellaneous;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFOVModifierModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "FOVModifier", description = "Gives you more customizability for the games FOV.", category = Module.Category.MISCELLANEOUS)
public class FOVModifierModule extends Module implements IFOVModifierModule {
    public NumberSetting fov;

    public FOVModifierModule() {
        this.fov = new NumberSetting("FOV", "The FOV you want to use.", 120, 50, 150);
    }

    public String getMetaData() {
        return (this.fov.getValue().intValue());
    }

    public float getFovValue() {
        return this.fov.getValue().floatValue();
    }

}
