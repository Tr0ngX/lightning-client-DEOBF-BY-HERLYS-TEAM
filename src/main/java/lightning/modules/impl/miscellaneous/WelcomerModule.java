package lightning.modules.impl.miscellaneous;

import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ChatInputEvent;
import lightning.events.impl.PlayerConnectEvent;
import lightning.events.impl.PlayerDisconnectEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;
import net.minecraft.ChatFormatting;

@RegisterModule(name = "Welcomer", description = "Sends a message when a player joins or leaves the server.", category = Module.Category.MISCELLANEOUS)
public class WelcomerModule extends Module {
    public BooleanSetting joins;
    public BooleanSetting leaves;
    public BooleanSetting clientside;
    public BooleanSetting unicode;
    public BooleanSetting greenText;
    public NumberSetting delay;
    private final String[] JOIN_MESSAGES;
    private final String[] LEAVE_MESSAGES;
    private final ConcurrentLinkedQueue queue;
    private final Timer messageTimer;

    public WelcomerModule() {
        this.joins = new BooleanSetting("Joins", "Sends messages when a player joins the server", true);
        this.leaves = new BooleanSetting("Leaves", "Sends messages when a player leaves the server", true);
        this.clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
        this.unicode = new BooleanSetting("Unicode", "Uses prettier unicode icons instead of normal arrows.", false);
        this.greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
        this.delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 30);
        var tmp0 = new String[6];
        tmp0[0] = "Hello, ";
        tmp0[1] = "Welcome to the server, ";
        tmp0[2] = "Good to see you, ";
        tmp0[3] = "Greetings, ";
        tmp0[4] = "Good evening, ";
        tmp0[5] = "Hey, ";
        this.JOIN_MESSAGES = tmp0;
        var tmp1 = new String[6];
        tmp1[0] = "Goodbye, ";
        tmp1[1] = "See you later, ";
        tmp1[2] = "Bye bye, ";
        tmp1[3] = "I hope you had a good time, ";
        tmp1[4] = "Farewell, ";
        tmp1[5] = "See you next time, ";
        this.LEAVE_MESSAGES = tmp1;
        this.queue = new ConcurrentLinkedQueue();
        this.messageTimer = new Timer();
    }

    public void onEnable() {
        this.queue.clear();
        this.messageTimer.reset();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        if (this.messageTimer.hasTimeElapsed((this.delay.getValue().intValue() * 1000)) && !(this.queue.isEmpty()) && !(this.clientside.getValue())) {
            Object var2 = ((String) this.queue.poll());
            if (this.clientside.getValue()) {
                LightningClient.CHAT_MANAGER.message(var2);
            } else {
                mc.player.connection.sendChat(((this.greenText.getValue() ? "> " : "") + var2));
            }
            this.messageTimer.reset();
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent arg0) {
        if (getNull()) return;
        this.messageTimer.reset();
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerConnectEvent arg0) {
        if (getNull()) return;
        Object var2 = mc.level.getPlayerByUUID(arg0.getId());
        if ((var2 == null) || (!(this.joins.getValue()))) break label_4;
        if (this.clientside.getValue()) {
            LightningClient.CHAT_MANAGER.message((String.valueOf(ChatFormatting.DARK_GRAY) + "[" + String.valueOf(ChatFormatting.GREEN) + (this.unicode.getValue() ? "\u00bb" : ">") + String.valueOf(ChatFormatting.DARK_GRAY) + "] " + String.valueOf(ChatFormatting.GRAY) + var2.getName().getString()));
            break label_4;
        }
        this.queue.add((this.JOIN_MESSAGES[((int) MathUtils.random(((double) this.JOIN_MESSAGES.length), 0.0))] + var2.getName().getString()));
        label_4:
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerDisconnectEvent arg0) {
        if (getNull()) return;
        Object var2 = mc.level.getPlayerByUUID(arg0.getId());
        if ((var2 == null) || (!(this.leaves.getValue()))) break label_4;
        if (this.clientside.getValue()) {
            LightningClient.CHAT_MANAGER.message((String.valueOf(ChatFormatting.DARK_GRAY) + "[" + String.valueOf(ChatFormatting.RED) + (this.unicode.getValue() ? "\u00ab" : "<") + String.valueOf(ChatFormatting.DARK_GRAY) + "] " + String.valueOf(ChatFormatting.GRAY) + var2.getName().getString()));
            break label_4;
        }
        this.queue.add((this.LEAVE_MESSAGES[((int) MathUtils.random(((double) this.LEAVE_MESSAGES.length), 0.0))] + var2.getName().getString()));
        label_4:
    }

}
