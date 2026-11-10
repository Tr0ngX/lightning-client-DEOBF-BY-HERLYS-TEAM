package lightning.modules.impl.miscellaneous;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "ExtraTab", description = "Extends the size of the player list.", category = Module.Category.MISCELLANEOUS)
public class ExtraTabModule extends Module {
    public NumberSetting limit;
    public BooleanSetting friends;

    public ExtraTabModule() {
        this.limit = new NumberSetting("Limit", "The maximum amount of players that will be listed.", 1000, 1, 1000);
        this.friends = new BooleanSetting("Friends", "Highlights your friends on the player list.", true);
    }

    public String getMetaData() {
        return String.valueOf(this.limit.getValue().intValue());
    }

}
