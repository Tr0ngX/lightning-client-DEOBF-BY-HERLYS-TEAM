package lightning.modules.impl.miscellaneous;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;

@RegisterModule(name = "MouseFix", description = "Fixes multiple mouse issues.", category = Module.Category.MISCELLANEOUS)
public class MouseFixModule extends Module {
    public BooleanSetting customDebounce;

    public MouseFixModule() {
        this.customDebounce = new BooleanSetting("CustomDebounce", "Implements a custom debounce timer on mouse inputs.", true);
    }

}
