package lightning.managers;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lightning.utils.IMinecraft;
import lightning.utils.NightBeacon;
import net.minecraft.world.entity.player.Player;

/**
 * Purified NightUserManager: Dead 30-second background polling thread and empty poll() stub removed.
 */
public class NightUserManager implements IMinecraft {
    private final Set<String> onlineUsernames = ConcurrentHashMap.newKeySet();

    public NightUserManager() {
    }

    public boolean isNightUser(Player player) {
        if (player == null) {
            return false;
        }
        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
            return true;
        }
        return NightBeacon.or(player, isNightUser(player.getName().getString()));
    }

    public boolean isNightUser(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        if (mc.player != null && mc.player.getName().getString().equalsIgnoreCase(username)) {
            return true;
        }
        return onlineUsernames.contains(username.toLowerCase());
    }
}
