package lightning.pingbypass.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class PbModuleManager {
    public static final Set MIGRATED_MODULE_NAMES;
    private final List modules;

    public PbModuleManager() {
        this.modules = new ArrayList();
    }

    public void register(PbModule arg0) {
        this.modules.add(arg0);
        this.modules.sort(Comparator.comparing(PbModule::getName));
    }

    public PbModule getModule(String arg0) {
        return ((PbModule) this.modules.stream().filter((arg1) -> arg1.getName().equalsIgnoreCase(arg0)).findFirst().orElse(null));
    }

    public List getModules() {
        return this.modules;
    }

    public void tick() {
        Object var1 = this.modules.iterator();
        while (var1.hasNext()) {
            Object var2 = ((PbModule) var1.next());
            if (!(var2.isToggled())) {
                break label_0;
            }
            var2.tick();
        }
    }

    static {
        MIGRATED_MODULE_NAMES = Set.of("AutoCrystal", "AutoTotem", "SpeedMine");
    }

}
