package lightning.modules.impl.core;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;

@RegisterModule(name = "Menu", description = "Replaces the default title screen with the client's custom main menu screen.", category = Module.Category.CORE, persistent = true, drawn = false)
public class MenuModule extends Module {
    public CategorySetting mainMenuCategory;
    public BooleanSetting mainMenu;

    public MenuModule() {
        this.mainMenuCategory = new CategorySetting("MainMenu", "The category for settings related to the main menu.");
        this.mainMenu = new BooleanSetting("MainMenu", "Enabled", "Replaces Minecraft's default main menu with a customizable one.", new CategorySetting.Visibility(this.mainMenuCategory), true);
    }

}
