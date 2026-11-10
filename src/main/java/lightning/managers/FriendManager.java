package lightning.managers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import lightning.LightningClient;
import lightning.modules.impl.core.FriendModule;
import lightning.modules.impl.core.PingBypassModule;
import lightning.pingbypass.PingBypassFlags;
import lombok.Generated;

public class FriendManager {
    private final ArrayList friends;

    public FriendManager() {
        this.friends = new ArrayList();
    }

    public boolean contains(String arg0) {
        if (getFriendFire()) return false;
        Objects.requireNonNull(arg0);
        return this.friends.stream().anyMatch(arg0::equalsIgnoreCase);
    }

    public void add(String arg0) {
        if (contains(arg0)) return;
        this.friends.add(arg0);
        syncToProxy();
    }

    public void remove(String arg0) {
        Objects.requireNonNull(arg0);
        this.friends.removeIf(arg0::equalsIgnoreCase);
        syncToProxy();
    }

    public void clear() {
        this.friends.clear();
        syncToProxy();
    }

    private void syncToProxy() {
        if (((!PingBypassFlags.proxyForwardingActive) || (LightningClient.PINGBYPASS_CONFIG == null)) || (LightningClient.PINGBYPASS_CONFIG.isServer())) break label_0;
        var tmp0 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class));
        Object var1 = tmp0;
        if (tmp0 == null) break label_0;
        var1.syncFriendsToProxy();
        label_0:
    }

    public boolean getFriendFire() {
        if (LightningClient.MODULE_MANAGER == null) return false;
        Object var1 = ((FriendModule) LightningClient.MODULE_MANAGER.getModule(FriendModule.class));
        if ((var1 == null) || (var1.friendlyFire == null)) break label_1;
        return var1.friendlyFire.getValue();
    }

    public void sendFriendMessage(String arg0) {
        if (LightningClient.MODULE_MANAGER == null) return;
        Object var2 = ((FriendModule) LightningClient.MODULE_MANAGER.getModule(FriendModule.class));
        if (var2 != null) {
            var2.sendFriendMessage(arg0);
        }
    }

    public Color getDefaultFriendColor() {
        return getDefaultFriendColor(255);
    }

    public Color getDefaultFriendColor(int arg0) {
        return new Color(85, 255, 255, arg0);
    }

    @Generated
    public ArrayList getFriends() {
        return this.friends;
    }

}
