package lightning.modules.impl.visuals;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "AspectRatio", description = "Modifies the game's aspect ratio.", category = Module.Category.VISUALS)
public class AspectRatioModule extends Module {
    public NumberSetting ratio;

    public AspectRatioModule() {
        this.ratio = new NumberSetting("Ratio", "The aspect ratio that will be applied to the game's rendering.", 1.78f, 0.0f, 5.0f);
    }

    public String getMetaData() {
        return String.valueOf(this.ratio.getValue().floatValue());
    }

}
