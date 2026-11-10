package lightning.managers;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lightning.utils.IMinecraft;
import lightning.utils.NightBeacon;
import net.minecraft.world.entity.player.Player;

public class NightUserManager implements IMinecraft {
    private final Set onlineUsernames;

    public NightUserManager() {
        this.onlineUsernames = ConcurrentHashMap.newKeySet();
    }

    public boolean isNightUser(Player arg0) {
        if (arg0 == null) return false;
        if (mc.player != null && arg0.getUUID().equals(mc.player.getUUID())) return true;
        return NightBeacon.or(arg0, isNightUser(arg0.getName().getString()));
    }

    public boolean isNightUser(String arg0) {
        if ((arg0 == null) || (arg0.isEmpty())) {
            return false;
        }
        if (mc.player != null && mc.player.getName().getString().equalsIgnoreCase(arg0)) return true;
        return this.onlineUsernames.contains(arg0.toLowerCase());
    }

}
