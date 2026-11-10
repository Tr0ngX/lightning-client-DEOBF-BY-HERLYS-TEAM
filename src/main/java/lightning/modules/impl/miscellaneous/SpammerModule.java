package lightning.modules.impl.miscellaneous;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.FileUtils;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;

@RegisterModule(name = "Spammer", description = "Spams messages in chat from a text file.", category = Module.Category.MISCELLANEOUS)
public class SpammerModule extends Module {
    public StringSetting fileName;
    public NumberSetting delay;
    public BooleanSetting greenText;
    public BooleanSetting shuffled;
    private final Timer timer;
    private List messages;
    private int line;

    public SpammerModule() {
        this.fileName = new StringSetting("FileName", "The name of the spammer text file.", "spammer.txt");
        this.delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 30);
        this.greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
        this.shuffled = new BooleanSetting("Shuffled", "Sends the spammer messages out of order.", false);
        this.timer = new Timer();
        this.messages = new ArrayList();
    }

    public void onEnable() {
        this.line = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        Object var2 = new File(("Lightning/Client/" + this.fileName.getValue()));
        this.messages = FileUtils.readLines(var2);
        if (!(this.messages.isEmpty()) && this.timer.hasTimeElapsed((this.delay.getValue().intValue() * 1000))) {
            if (this.line >= this.messages.size()) {
                this.line = 0;
            }
            if (this.shuffled.getValue()) {
            } else {
            }
            Object var3 = ((String) this.messages.get(this.line));
            mc.player.connection.sendChat(((this.greenText.getValue() ? "> " : "") + var3));
            this.line = (this.line + 1);
            this.timer.reset();
        }
    }

}
