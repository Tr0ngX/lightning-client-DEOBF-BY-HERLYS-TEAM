package lightning.modules.impl.visuals;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IViewClipModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "ViewClip", description = "Makes your camera clip through walls and allows you to change the camera's distance from yourself.", category = Module.Category.VISUALS)
public class ViewClipModule extends Module implements IViewClipModule {
    public BooleanSetting extend;
    public NumberSetting distance;

    public ViewClipModule() {
        this.extend = new BooleanSetting("Extend", "Changes the distance of the third person camera from yourself.", false);
        this.distance = new NumberSetting("Distance", "The distance of the third person camera from your character.", new BooleanSetting.Visibility(this.extend, true), 4.0f, -50.0f, 50.0f);
    }

    public String getMetaData() {
        if (this.extend.getValue()) {
        } else {
        }
        return "Vanilla";
    }

    public boolean isExtend() {
        return this.extend.getValue();
    }

    public float getDistanceValue() {
        return this.distance.getValue().floatValue();
    }

}
