package lightning.pingbypass.modules;

import java.util.List;
import lightning.settings.Setting;

public abstract class PbModule {
    private final String name;
    private boolean toggled;

    protected PbModule(String arg0) {
        this.toggled = false;
        this.name = arg0;
    }

    public String getName() {
        return this.name;
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public void setToggled(boolean arg0) {
        this.toggled = arg0;
        if (!(!arg0)) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public abstract void tick();

    public abstract List getSettings();

    public Setting getSetting(String arg0) {
        return ((Setting) getSettings().stream().filter((arg1) -> arg1.getName().equalsIgnoreCase(arg0)).findFirst().orElse(null));
    }

}
