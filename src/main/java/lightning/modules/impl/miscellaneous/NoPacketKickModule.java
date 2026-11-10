package lightning.modules.impl.miscellaneous;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lightning.LightningClient;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterModule(name = "NoPacketKick", description = "Prevents you from being kicked from the server due to Netty, packet decoding, or codec exceptions.", category = Module.Category.MISCELLANEOUS)
public class NoPacketKickModule extends Module {
    private static final Logger LOGGER;
    public BooleanSetting logChat;
    public BooleanSetting logConsole;
    public BooleanSetting onlyDecoder;
    private long lastChatNotification;
    private long lastConsoleLog;
    private int suppressedCountSinceLastLog;

    public NoPacketKickModule() {
        this.logChat = new BooleanSetting("LogChat", "Notifies you in chat when a corrupted or invalid packet is caught and suppressed.", true);
        this.logConsole = new BooleanSetting("LogConsole", "Prints the full exception stacktrace to the console log.", true);
        this.onlyDecoder = new BooleanSetting("OnlyDecoder", "Only suppresses decoder and codec errors, allowing critical socket disconnects through.", false);
        this.lastChatNotification = 0L;
        this.lastConsoleLog = 0L;
        this.suppressedCountSinceLastLog = 0;
    }

    public boolean shouldSuppress(Throwable arg0) {
        if (!(this.onlyDecoder.getValue())) return true;
        if (!(arg0 instanceof DecoderException) && !(arg0 instanceof EncoderException) && !(arg0 instanceof IllegalArgumentException) && !(arg0 instanceof IndexOutOfBoundsException)) {
            if ((arg0 instanceof NullPointerException)) {
            }
        } else {
        }
        return false;
    }

    public void onExceptionCaught(Throwable arg0) {
        long var3 = System.currentTimeMillis();
        this.suppressedCountSinceLastLog = (this.suppressedCountSinceLastLog + 1);
        if (this.logConsole.getValue() && (var3 - this.lastConsoleLog) > 1000L) {
            this.lastConsoleLog = var3;
            if (arg0.getCause() != null) {
                break label_1;
            }
            label_1:
            var tmp0 = arg0.getMessage();
            Object var2 = tmp0;
            Object var5 = tmp0;
            if (var2 != null) {
                if (((String) var2).isBlank()) {
                }
                var2 = arg0.getClass().getSimpleName();
            }
            LOGGER.warn("[NoPacketKick] Suppressed packet exception ({} occurrences in last sec): {}", this.suppressedCountSinceLastLog, var2);
            this.suppressedCountSinceLastLog = 0;
        }
        if (this.logChat.getValue() && mc.player != null && mc.level != null && (var3 - this.lastChatNotification) > 1000L) {
            this.lastChatNotification = var3;
            if (arg0.getCause() != null) {
            } else {
            }
            var tmp1 = arg0.getMessage();
            var2 = tmp1;
            var5 = tmp1;
            if (var2 != null) {
                if (((String) var2).isBlank()) {
                }
                var2 = arg0.getClass().getSimpleName();
            }
            if (((String) var2).length() > 100) {
                var2 = (((String) var2).substring(0, 100) + "...");
            }
            LightningClient.CHAT_MANAGER.tagged(("Suppressed packet error: " + ((String) var2)), getName());
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger("Lightning/NoPacketKick");
    }

}
