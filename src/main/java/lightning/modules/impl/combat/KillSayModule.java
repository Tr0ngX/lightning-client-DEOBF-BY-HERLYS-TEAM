package lightning.modules.impl.combat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ChatInputEvent;
import lightning.events.impl.CommandInputEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TargetDeathEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.FileUtils;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;

@RegisterModule(name = "KillSay", description = "Automatically sends a message when you kill someone.", category = Module.Category.COMBAT)
public class KillSayModule extends Module {
    private final String[] KILL_MESSAGES;
    public BooleanSetting kills;
    public BooleanSetting pops;
    public ModeSetting mode;
    public StringSetting killFile;
    public StringSetting popFile;
    public NumberSetting delay;
    public BooleanSetting clientside;
    public BooleanSetting greenText;
    public BooleanSetting killStreak;
    public BooleanSetting suicideIgnore;
    private final ConcurrentLinkedQueue queue;
    private List killMessages;
    private List popMessages;
    private final Timer timer;
    private String lastMessage;
    private int streak;
    private boolean suicide;

    public KillSayModule() {
        var tmp0 = new String[4];
        tmp0[0] = "Sit [username], Lightning owns me and all!";
        tmp0[1] = "[username] I killed you with the power of Lightning!";
        tmp0[2] = "Sorry [username], get good get Lightning!";
        tmp0[3] = "I just killed [username] thanks to Lightning!";
        this.KILL_MESSAGES = tmp0;
        this.kills = new BooleanSetting("Kills", "Sends a message when you kill someone.", true);
        this.pops = new BooleanSetting("Pops", "Sends a message when you pop someone.", false);
        var tmp1 = new String[2];
        tmp1[0] = "Default";
        tmp1[1] = "Custom";
        this.mode = new ModeSetting("Mode", "The mode for the messages.", "Default", tmp1);
        this.killFile = new StringSetting("KillFile", "The name of the file containing the kill messages.", "killfile.txt");
        this.popFile = new StringSetting("PopFile", "The name of the file containing the pop messages.", "popfile.txt");
        this.delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 10);
        this.clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
        this.greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
        this.killStreak = new BooleanSetting("KillStreak", "Keeps track of your kills.", false);
        this.suicideIgnore = new BooleanSetting("SuicideIgnore", "Doesn't reset kill streak when you suicide.", false);
        this.queue = new ConcurrentLinkedQueue();
        this.killMessages = new ArrayList();
        this.popMessages = new ArrayList();
        this.timer = new Timer();
        this.suicide = false;
    }

    public void onEnable() {
        this.queue.clear();
        this.timer.reset();
        this.streak = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((getNull()) || (this.queue.isEmpty())) {
            return;
        }
        this.killMessages = FileUtils.readLines(new File(("Lightning/Client/" + this.killFile.getValue())));
        this.popMessages = FileUtils.readLines(new File(("Lightning/Client/" + this.popFile.getValue())));
        Object var2 = this.queue;
        Object var3 = var2;
        // synchronized (var2)
        if (this.timer.hasTimeElapsed((this.delay.getValue().intValue() * 1000))) {
            Object var4 = ((String) this.queue.poll());
            if (!(var4.isEmpty())) {
                if (this.clientside.getValue()) {
                    LightningClient.CHAT_MANAGER.message(var4);
                } else {
                    mc.player.connection.sendChat(((this.greenText.getValue() ? "> " : "") + var4));
                }
                this.lastMessage = var4;
            }
            this.timer.reset();
        }
        break label_12;
        Object var5 = null;
        throw var5;
        label_12:
    }

    @SubscribeEvent
    public void onTargetDeath(TargetDeathEvent arg0) {
        if ((getNull()) || (arg0.getPlayer() == mc.player)) {
            return;
        }
        this.streak = (this.streak + 1);
        Object var2 = this.queue;
        Object var3 = var2;
        // synchronized (var2)
        if (this.kills.getValue()) {
            this.queue.clear();
            Object var4 = getKillMessage(arg0.getPlayer().getName().getString());
            this.queue.add(var4);
        }
        break label_7;
        Object var5 = null;
        throw var5;
        label_7:
        if (this.killStreak.getValue() && this.streak > 1) {
            LightningClient.CHAT_MANAGER.message(("You are on a " + this.streak + " kill streak."));
        }
    }

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent arg0) {
        if ((((getNull()) || (arg0.getPlayer() == mc.player)) || (!(LightningClient.TARGET_MANAGER.isTarget(arg0.getPlayer())))) || (!(this.pops.getValue()))) {
            return;
        }
        Object var2 = this.queue;
        Object var3 = var2;
        // synchronized (var2)
        Object var4 = getPopMessage(arg0.getPlayer().getName().getString(), arg0.getPops());
        this.queue.add(var4);
        break label_6;
        Object var5 = null;
        throw var5;
        label_6:
    }

    @SubscribeEvent
    public void onCommandInput(CommandInputEvent arg0) {
        if (getNull()) return;
        if (arg0.getMessage().contains("/kill")) {
            this.suicide = true;
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent arg0) {
        if (getNull()) return;
        this.timer.reset();
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        if (arg0.getPlayer() != mc.player) break label_1;
        if (this.suicideIgnore.getValue()) {
            if ((this.suicide) || (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) break label_1;
        }
        this.streak = 0;
        label_1:
        this.suicide = false;
    }

    private String getPopMessage(String arg0, int arg1) {
        if (this.mode.getValue().equals("Custom") && !(this.popMessages.isEmpty())) {
            label_0:
            Object var3 = ((String) this.popMessages.get(((int) MathUtils.random(((double) this.popMessages.size()), 0.0)))).replace("[username]", arg0);
            if (!(var3.equals(this.lastMessage))) break label_4;
            if (this.popMessages.size() > 1) break label_0;
            break label_4;
        }
        var3 = (arg1 + (arg1 > 1 ? " pops " : " pop ") + arg0 + "!");
        label_4:
        return var3;
    }

    private String getKillMessage(String arg0) {
        if ((!(this.mode.getValue().equals("Custom"))) || (this.killMessages.isEmpty())) break label_1;
        label_0:
        var tmp0 = ((String) this.killMessages.get(((int) MathUtils.random(((double) this.killMessages.size()), 0.0)))).replace("[username]", arg0);
        Object var2 = tmp0;
        if ((!(tmp0.equals(this.lastMessage))) || (this.killMessages.size() <= 1)) break label_2;
        break label_0;
        label_1:
        var tmp1 = this.KILL_MESSAGES[((int) MathUtils.random(((double) this.KILL_MESSAGES.length), 0.0))].replace("[username]", arg0);
        var2 = tmp1;
        if (!(tmp1.equals(this.lastMessage))) break label_2;
        break label_1;
        label_2:
        return var2;
    }

    public String getMetaData() {
        return (this.streak);
    }

}
