package lightning.modules.impl.core;

import lightning.gui.HUDEditorScreen;
import lightning.modules.Module;
import lightning.modules.RegisterModule;

@RegisterModule(name = "HUDEditor", description = "Lets you drag-and-drop and toggle individual HUD elements directly on screen.", category = Module.Category.CORE, drawn = false)
public class HUDEditorModule extends Module {
    public void onEnable() {
        mc.gui.setScreen(new HUDEditorScreen());
    }

    public void onDisable() {
        if ((mc.gui.screen() instanceof HUDEditorScreen)) {
            mc.gui.setScreen(null);
        }
    }

}
