package lightning.modules.impl.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.http.WebSocket;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ChatInputEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.text.FormattingUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

@RegisterModule(name = "IRC", description = "Internet Relay Chat with other Lightning users.", category = Module.Category.CORE, toggled = true, persistent = true, drawn = false)
public class IRCModule extends Module {
    private static final Gson GSON;
    private static final String[] IRC_SERVERS;
    public StringSetting prefix;
    public BooleanSetting showPrefix;
    public ModeSetting primaryColor;
    public ModeSetting secondaryColor;
    public BooleanSetting notifyConnect;
    private WebSocket webSocket;
    private volatile boolean connected;
    private volatile String myUsername;
    private final ScheduledExecutorService scheduler;
    private long lastMessageTime;
    private static final long CHAT_COOLDOWN_MS = 3000L;

    public IRCModule() {
        this.prefix = new StringSetting("Prefix", "The prefix character for IRC messages in chat.", "$");
        this.showPrefix = new BooleanSetting("ShowPrefix", "Show [Lightning] tag on IRC messages.", true);
        this.primaryColor = new ModeSetting("PrimaryColor", "Color of username and tag.", "Client", FormattingUtils.FORMATS);
        this.secondaryColor = new ModeSetting("SecondaryColor", "Color of IRC message text.", "White", FormattingUtils.FORMATS);
        this.notifyConnect = new BooleanSetting("NotifyConnect", "Show connection status notifications in chat.", false);
        this.connected = false;
        this.myUsername = null;
        this.scheduler = Executors.newSingleThreadScheduledExecutor((arg0) -> {
    Object var1 = new Thread(arg0, "Lightning-IRC-Service");
    var1.setDaemon(true);
    return var1;
});
        this.lastMessageTime = 0L;
        connect();
    }

    public void onEnable() {
        if (this.connected) break label_1;
        if (this.webSocket != null && !(this.webSocket.isInputClosed())) {
            if (!(this.webSocket.isOutputClosed())) break label_1;
        }
        connect();
        label_1:
    }

    public void onDisable() {
        disconnect();
    }

    public synchronized void connect() {
    }

    private void doConnect() {
    }

    private void scheduleReconnect(long arg0) {
    }

    public synchronized void disconnect() {
        this.connected = false;
        if (this.webSocket == null) break label_4;
        try {
            this.webSocket.sendClose(1000, "Module disabled");
        } catch (Exception ex2) {
            Object var1 = ex2;
        }
        this.webSocket = null;
        label_4:
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent arg0) {
        if (!(isToggled())) return;
        Object var2 = arg0.getMessage();
        if (var2 == null) return;
        Object var3 = this.prefix.getValue();
        if (var3 != null) {
            if (var3.isEmpty()) {
            }
            var3 = "$";
        }
        if (var2.startsWith(var3)) {
            arg0.setCancelled(true);
            Object var4 = var2.substring(var3.length()).trim();
            if (var4.isEmpty()) {
                LightningClient.CHAT_MANAGER.warn(("Usage: " + var3 + "<message>"));
                return;
            }
            sendMessage(var4);
        }
    }

    public void sendMessage(String arg0) {
        if (!(!this.connected) && this.webSocket != null) {
            if (this.webSocket.isOutputClosed()) {
            }
            LightningClient.CHAT_MANAGER.warn("IRC is currently not connected to server. Reconnecting...");
            connect();
            return;
        }
        long var2 = System.currentTimeMillis();
        long var4 = (var2 - this.lastMessageTime);
        if (var4 < 3000L) {
            double var6 = (((double) (3000L - var4)) / 1000.0);
            var tmp0 = new Object[1];
            tmp0[0] = var6;
            LightningClient.CHAT_MANAGER.warn(String.format("Please wait %.1fs before sending another message", tmp0));
            return;
            try {
            }
            var6 = new JsonObject();
            var6.addProperty("type", "chat");
            var6.addProperty("content", arg0);
            this.webSocket.sendText(var6.toString(), true);
            this.lastMessageTime = var2;
        } catch (Exception ex4) {
            var6 = ex4;
            LightningClient.CHAT_MANAGER.error(("Failed to send IRC message: " + var6.getMessage()));
            scheduleReconnect(3L);
        }
    }

    private void handleServerMessage(String arg0) {
    }

    private Component createRoleComponent(String arg0) {
        if (arg0 != null) {
            if (!(arg0.isBlank())) break label_1;
        }
        arg0 = "stable";
        label_1:
        var tmp0 = arg0.toLowerCase();
        Object var2 = tmp0;
        if (tmp0.equals("user")) break label_2;
        if (var2.equals("member")) {
            label_2:
            var2 = "stable";
        }
        Object var4 = var2;
        int var5 = -1;
        switch (var4.hashCode()) {
            case -2004703995 -> { break label_12; }
            case -892499141 -> { break label_9; }
            case -877169473 -> { break label_7; }
            case -80681014 -> { break label_11; }
            case 99349 -> { break label_10; }
            case 108290 -> { break label_13; }
            case 116765 -> { break label_8; }
            case 3020272 -> { break label_6; }
            case 92668751 -> { break label_4; }
            case 106164915 -> { break label_5; }
            default -> { break label_14; }
        }
        label_4:
        if (!(var4.equals("admin"))) break label_14;
        var5 = 0;
        break label_14;
        label_5:
        if (!(var4.equals("owner"))) break label_14;
        var5 = 1;
        break label_14;
        label_6:
        if (!(var4.equals("beta"))) break label_14;
        var5 = 2;
        break label_14;
        label_7:
        if (!(var4.equals("tester"))) break label_14;
        var5 = 3;
        break label_14;
        label_8:
        if (!(var4.equals("vip"))) break label_14;
        var5 = 4;
        break label_14;
        label_9:
        if (!(var4.equals("stable"))) break label_14;
        var5 = 5;
        break label_14;
        label_10:
        if (!(var4.equals("dev"))) break label_14;
        var5 = 6;
        break label_14;
        label_11:
        if (!(var4.equals("developer"))) break label_14;
        var5 = 7;
        break label_14;
        label_12:
        if (!(var4.equals("moderator"))) break label_14;
        var5 = 8;
        break label_14;
        label_13:
        if (!(var4.equals("mod"))) break label_14;
        var5 = 9;
        label_14:
        switch (var5) {
            case 0 -> { break label_15; }
            case 1 -> { break label_15; }
            case 2 -> { break label_16; }
            case 3 -> { break label_16; }
            case 4 -> { break label_16; }
            case 5 -> { break label_17; }
            case 6 -> { break label_18; }
            case 7 -> { break label_18; }
            case 8 -> { break label_19; }
            case 9 -> { break label_19; }
            default -> { break label_20; }
        }
        label_15:
        break label_21;
        label_16:
        break label_21;
        label_17:
        break label_21;
        label_18:
        break label_21;
        label_19:
        break label_21;
        label_20:
        label_21:
        Object var3 = TextColor.fromRgb(12616956);
        return Component.literal("<").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(var2).withStyle(Style.EMPTY.withColor(var3))).append(Component.literal("> ").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Style getStyleForSetting(String arg0) {
        if (arg0 == null) return Style.EMPTY;
        if ("client".equalsIgnoreCase(arg0)) break label_1;
        if (!"rainbow".equalsIgnoreCase(arg0)) break label_2;
        label_1:
        return Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.getGlobalColor().getRGB()));
        label_2:
        Object var2 = arg0.toLowerCase();
        int var3 = -1;
        switch (var2.hashCode()) {
            case -1924313178 -> { break label_13; }
            case -1910830810 -> { break label_9; }
            case -1910805820 -> { break label_5; }
            case -1910651699 -> { break label_17; }
            case -734239628 -> { break label_14; }
            case 112785 -> { break label_10; }
            case 3002044 -> { break label_8; }
            case 3027034 -> { break label_4; }
            case 3178592 -> { break label_15; }
            case 3181155 -> { break label_16; }
            case 93818879 -> { break label_3; }
            case 98619139 -> { break label_6; }
            case 899342809 -> { break label_7; }
            case 1252881926 -> { break label_12; }
            case 1739491559 -> { break label_11; }
            default -> { break label_18; }
        }
        label_3:
        if (!(var2.equals("black"))) break label_18;
        var3 = 0;
        break label_18;
        label_4:
        if (!(var2.equals("blue"))) break label_18;
        var3 = 1;
        break label_18;
        label_5:
        if (!(var2.equals("dark blue"))) break label_18;
        var3 = 2;
        break label_18;
        label_6:
        if (!(var2.equals("green"))) break label_18;
        var3 = 3;
        break label_18;
        label_7:
        if (!(var2.equals("dark green"))) break label_18;
        var3 = 4;
        break label_18;
        label_8:
        if (!(var2.equals("aqua"))) break label_18;
        var3 = 5;
        break label_18;
        label_9:
        if (!(var2.equals("dark aqua"))) break label_18;
        var3 = 6;
        break label_18;
        label_10:
        if (!(var2.equals("red"))) break label_18;
        var3 = 7;
        break label_18;
        label_11:
        if (!(var2.equals("dark red"))) break label_18;
        var3 = 8;
        break label_18;
        label_12:
        if (!(var2.equals("light purple"))) break label_18;
        var3 = 9;
        break label_18;
        label_13:
        if (!(var2.equals("dark purple"))) break label_18;
        var3 = 10;
        break label_18;
        label_14:
        if (!(var2.equals("yellow"))) break label_18;
        var3 = 11;
        break label_18;
        label_15:
        if (!(var2.equals("gold"))) break label_18;
        var3 = 12;
        break label_18;
        label_16:
        if (!(var2.equals("gray"))) break label_18;
        var3 = 13;
        break label_18;
        label_17:
        if (!(var2.equals("dark gray"))) break label_18;
        var3 = 14;
        label_18:
        switch (var3) {
            case 0 -> { break label_19; }
            case 1 -> { break label_20; }
            case 2 -> { break label_21; }
            case 3 -> { break label_22; }
            case 4 -> { break label_23; }
            case 5 -> { break label_24; }
            case 6 -> { break label_25; }
            case 7 -> { break label_26; }
            case 8 -> { break label_27; }
            case 9 -> { break label_28; }
            case 10 -> { break label_29; }
            case 11 -> { break label_30; }
            case 12 -> { break label_31; }
            case 13 -> { break label_32; }
            case 14 -> { break label_33; }
            default -> { break label_34; }
        }
        label_19:
        break label_35;
        label_20:
        break label_35;
        label_21:
        break label_35;
        label_22:
        break label_35;
        label_23:
        break label_35;
        label_24:
        break label_35;
        label_25:
        break label_35;
        label_26:
        break label_35;
        label_27:
        break label_35;
        label_28:
        break label_35;
        label_29:
        break label_35;
        label_30:
        break label_35;
        label_31:
        break label_35;
        label_32:
        break label_35;
        label_33:
        break label_35;
        label_34:
        label_35:
        Object var1 = ChatFormatting.WHITE;
        return Style.EMPTY.withColor(var1);
    }

    static {
        GSON = new Gson();
        var tmp0 = new String[3];
        tmp0[0] = "wss://nightclient.info.vn/irc";
        tmp0[1] = "ws://nightclient.info.vn/irc";
        tmp0[2] = "ws://103.78.2.201/irc";
        IRC_SERVERS = tmp0;
    }


    class IrcWebSocketListener implements WebSocket.Listener {
        private final StringBuilder buffer;
        final IRCModule this$0;

        private IrcWebSocketListener(IRCModule arg0, IRCModule arg1) {
            Objects.requireNonNull(arg0);
            Object var3 = arg1;
            Objects.requireNonNull(var3);
            this.this$0 = var3;
            this.buffer = new StringBuilder();
        }

        public void onOpen(WebSocket arg0) {
            arg0.request(1L);
        }

        public CompletionStage onText(WebSocket arg0, CharSequence arg1, boolean arg2) {
            this.buffer.append(arg1);
            if (!(!arg2)) {
                Object var4 = this.buffer.toString();
                this.buffer.setLength(0);
                this.this$0.handleServerMessage(var4);
            }
            arg0.request(1L);
            return null;
        }

        public CompletionStage onClose(WebSocket arg0, int arg1, String arg2) {
            this.this$0.connected = false;
            this.this$0.scheduleReconnect(3L);
            return null;
        }

        public void onError(WebSocket arg0, Throwable arg1) {
            this.this$0.connected = false;
            this.this$0.scheduleReconnect(5L);
        }

    }
}
