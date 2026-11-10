package lightning.modules.impl.player;

import java.text.Normalizer;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;

@RegisterModule(name = "NoSpamKeyBind", description = "Blocks commands sent by modules/keybinds (AutoShop, KeyAction, auto totem...) while you are at the login screen or in the lobby, so the server never flags command spam.", category = Module.Category.PLAYER, toggled = true)
public class NoSpamKeyBindModule extends Module {
    private final StringSetting server;
    private final NumberSetting lobbyGap;
    private final BooleanSetting blockManual;
    private final StringSetting allowed;
    private final BooleanSetting notify;
    private static final String[] STRONG;
    private static final String[] WEAK;
    private volatile boolean blocked;
    private volatile boolean sawJoin;
    private volatile long lastMarker;
    private volatile long lastNotify;

    public NoSpamKeyBindModule() {
        this.server = new StringSetting("Server", "Only active on servers whose address contains one of these words (comma separated). Empty = every server.", "kingmc");
        this.lobbyGap = new NumberSetting("LobbyGap", "Seconds without any login/lobby message before a world change counts as entering the real server.", 3, 1, 30);
        this.blockManual = new BooleanSetting("BlockManual", "Also block commands you type yourself (except the allowed login commands).", false);
        this.allowed = new StringSetting("Allowed", "Commands that are never blocked (comma separated, without the slash).", "dn,dk,dmk,login,register,l,reg");
        this.notify = new BooleanSetting("Notify", "Prints a chat message when blocking starts/stops.", true);
    }

    private static String norm(String arg0) {
        return Normalizer.normalize(arg0, Normalizer.Form.NFC).toLowerCase();
    }

    private static boolean containsAny(String arg0, String[] arg1) {
        Object var2 = arg1;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            if (arg0.contains(norm(var5))) return true;
            var4++;
        }
        return false;
    }

    private boolean serverMatches() {
        try {
            Object var1 = this.server.getValue();
            if (var1 == null) break label_1;
            if (var1.isBlank()) {
                label_1:
                return true;
                try {
                }
                Object var2 = LightningClient.SERVER_MANAGER.getServer();
                if (var2 == null) {
                    return false;
                    try {
                    }
                    var2 = var2.toLowerCase();
                    Object var3 = var1.toLowerCase().split(",");
                    int var4 = var3.length;
                    int var5 = 0;
                    while (var5 < var4) {
                        Object var6 = var3[var5];
                        var6 = var6.trim();
                        if (!(var6.isEmpty()) && var2.contains(var6)) {
                            return true;
                            try {
                            }
                            var5++;
                        }
                    } catch (Throwable ex10) {
                        var1 = ex10;
                    }
                    return false;
                }
            }
        }
    }

    private void say(String arg0) {
        if (!(this.notify.getValue())) {
            return;
            try {
            }
            LightningClient.CHAT_MANAGER.tagged(arg0, getName());
        } catch (Throwable ex2) {
            Object var2 = ex2;
        }
    }

    private void startBlocking(boolean arg0) {
        long var2 = System.currentTimeMillis();
        int var4 = this.blocked;
        this.blocked = true;
        if (!arg0) {
            if (!(!this.sawJoin)) {
            }
        } else {
        }
        1.sawJoin = false;
        this.lastMarker = var2;
        if (!var4) {
            say("Login/lobby detected - commands from modules & keybinds are paused.");
        }
    }

    public void onEnable() {
        this.blocked = false;
        this.sawJoin = false;
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent arg0) {
        this.sawJoin = false;
        if (serverMatches()) {
            startBlocking(false);
        } else {
            this.blocked = false;
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent arg0) {
        this.blocked = false;
        this.sawJoin = false;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundSystemChatPacket))) break label_6;
        Object var3 = ((ClientboundSystemChatPacket) var2);
        if (!(serverMatches())) {
            return;
            try {
            }
            Object var4 = norm(var3.content().getString());
        } catch (Throwable ex2) {
            Object var5 = ex2;
            return;
        }
        if (containsAny(var4, STRONG)) {
            startBlocking(true);
            break label_5;
        }
        if ((!this.blocked) || (!(containsAny(var4, WEAK)))) break label_5;
        this.lastMarker = System.currentTimeMillis();
        label_5:
        break label_10;
        label_6:
        if (!((var2 instanceof ClientboundLoginPacket))) {
            if (!((var2 instanceof ClientboundRespawnPacket))) break label_10;
        }
        if (!this.blocked) return;
        var4 = System.currentTimeMillis();
        if (!this.sawJoin) {
            this.sawJoin = true;
            this.lastMarker = var4;
            return;
        }
        long var6 = (this.lobbyGap.getValue().longValue() * 1000L);
        if ((var4 - this.lastMarker) < var6) break label_10;
        this.blocked = false;
        say("Entered the server - commands from modules & keybinds work again.");
        label_10:
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if (!this.blocked) return;
        Object var2 = arg0.getPacket();
        try {
            if ((var2 instanceof ServerboundChatCommandPacket)) {
                Object var5 = ((ServerboundChatCommandPacket) var2);
                Object var3 = var5.command();
                break label_4;
            }
            if ((var2 instanceof ServerboundChatCommandSignedPacket)) {
                Object var4 = ((ServerboundChatCommandSignedPacket) var2);
                var3 = var4.command();
                break label_4;
            }
            return;
            label_4:
        } catch (Throwable ex5) {
            var4 = ex5;
            var3 = "";
        }
        if (var3 == null) {
            var3 = "";
        }
        var4 = var3.trim().toLowerCase();
        if (var4.startsWith("/")) {
            var4 = var4.substring(1);
        }
        var5 = var4.indexOf(32);
        if (var5 >= 0) {
            var4 = var4.substring(0, var5);
        }
        if (isAllowed(var4)) return;
        if (!(this.blockManual.getValue()) && !(fromClientFeature())) return;
        arg0.cancel();
        long var6 = System.currentTimeMillis();
        if ((var6 - this.lastNotify) > 3000L) {
            this.lastNotify = var6;
            say(("Blocked /" + var4 + " (login/lobby)."));
        }
    }

    private boolean isAllowed(String arg0) {
        Object var2 = this.allowed.getValue();
        if (var2 == null) return false;
        Object var3 = var2.toLowerCase().split(",");
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Object var6 = var3[var5];
            if (var6.trim().equals(arg0)) return true;
            var5++;
        }
        return false;
    }

    private boolean fromClientFeature() {
        Object var1 = NoSpamKeyBindModule.class.getName();
        try {
            return ((Boolean) StackWalker.getInstance().walk((arg1) -> arg1.anyMatch((arg1) -> {
    Object var2 = arg1.getClassName();
    if ((var2.equals(arg0)) || (var2.startsWith("lightning.modules.impl.miscellaneous.AutoLoginModule"))) {
        return false;
    }
    if (!(var2.startsWith("lightning.modules."))) {
        if (var2.startsWith("lightning.managers.MacroManager")) {
        }
    } else {
    }
    return false;
}))).booleanValue();
        } catch (Throwable ex2) {
            Object var2 = ex2;
            return true;
        }
    }

    static {
        var tmp0 = new String[4];
        tmp0[0] = "\u0111\u0103ng nh\u1eadp b\u1eb1ng l\u1ec7nh";
        tmp0[1] = "c\u1ea7n \u0111\u0103ng nh\u1eadp";
        tmp0[2] = "/dn ";
        tmp0[3] = "/dk ";
        STRONG = tmp0;
        var tmp1 = new String[4];
        tmp1[0] = "s\u1ea3nh";
        tmp1[1] = "\u0111\u0103ng nh\u1eadp th\u00e0nh c\u00f4ng";
        tmp1[2] = "k\u1ebft n\u1ed1i tr\u1edf l\u1ea1i";
        tmp1[3] = "m\u00e3 pin";
        WEAK = tmp1;
    }

}
