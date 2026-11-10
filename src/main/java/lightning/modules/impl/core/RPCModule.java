package lightning.modules.impl.core;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.shaded0.discordipc.DiscordIPC;
import lightning.shaded0.discordipc.RichPresence;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterModule(name = "RPC", description = "Enables Discord Rich Presence for the client.", category = Module.Category.CORE)
public class RPCModule extends Module {
    private static final Logger LOGGER;
    public BooleanSetting showUid;
    public ModeSetting detailsMode;
    public StringSetting customDetails;
    public ModeSetting imageMode;
    private static final String LOGO_ASSET_KEY = "logo";
    private static final String IMAGE_ASSET_KEY = "image";
    private static final long LOGO_APP_ID = 1567958367148707900L;
    private static final long IMAGE_APP_ID = 1567958367148707900L;
    private static final int ANIMATED_FRAME_COUNT = 32;
    public NumberSetting animatedDelay;
    private int frameIndex;
    private final Timer frameTimer;
    private final String[] DETAILS;
    private final RichPresence rpc;
    private final Timer timer;

    public RPCModule() {
        this.showUid = new BooleanSetting("UUID", "Whether or not to show your client UID on the Discord presence.", true);
        var tmp0 = new String[2];
        tmp0[0] = "Custom";
        tmp0[1] = "Random";
        this.detailsMode = new ModeSetting("Details", "The mode for the discord presence details.", "Random", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Custom";
        this.customDetails = new StringSetting("CustomDetails", "Custom RPC text.", new ModeSetting.Visibility(this.detailsMode, tmp1), "Recode by PhatNho");
        var tmp2 = new String[3];
        tmp2[0] = "Logo";
        tmp2[1] = "Image";
        tmp2[2] = "Animated";
        this.imageMode = new ModeSetting("Image", "Which large image to show on the Rich Presence card.", "Image", tmp2);
        this.animatedDelay = new NumberSetting("AnimatedDelay", "Milliseconds between each frame swap.", 1500, 0, 10000);
        this.frameIndex = 0;
        this.frameTimer = new Timer();
        var tmp3 = new String[1];
        tmp3[0] = "Recode by PhatNho";
        this.DETAILS = tmp3;
        this.rpc = new RichPresence();
        this.timer = new Timer();
    }

    public void onEnable() {
        DiscordIPC.setOnError((arg0, arg1) -> LOGGER.warn("[RPC] Discord IPC error {}: {}", arg0, arg1));
        this.rpc.setStart((LightningClient.UPTIME / 1000L));
        connect();
    }

    private void connect() {
        DiscordIPC.stop();
        int var1 = DiscordIPC.start(appIdForMode(), () -> LOGGER.info("[RPC] Connected to Discord IPC"));
        if (!var1) {
            LOGGER.warn("[RPC] DiscordIPC.start() returned false -- no local Discord IPC pipe found (is Discord running?)");
            return;
        }
        this.rpc.setDetails(getDetails());
        applyState();
        applyImageMode();
        DiscordIPC.setActivity(this.rpc);
    }

    private void applyState() {
        if (!(this.showUid.getValue())) {
            this.rpc.setState(null);
            return;
        }
        Object var1 = "30th6_";
        if (var1 != null) {
        } else {
        }
        ("UID " + var1).setState(null);
    }

    private long appIdForMode() {
        return (this.imageMode.getValue().equals("Logo") ? 1567958367148707900L : 1567958367148707900L);
    }

    private void applyImageMode() {
        if (this.imageMode.getValue().equals("Logo")) {
            this.rpc.setLargeImage("logo", "LightningClient");
            break label_2;
        }
        if (this.imageMode.getValue().equals("Animated")) {
            this.rpc.setLargeImage(frameKey(), "LightningClient");
            break label_2;
        }
        this.rpc.setLargeImage("image", "LightningClient");
        label_2:
    }

    private String frameKey() {
        var tmp0 = new Object[1];
        tmp0[0] = (this.frameIndex + 1);
        return String.format("frame_%02d", tmp0);
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent arg0) {
        if (arg0.getSetting() == this.imageMode) {
            connect();
            break label_4;
        }
        if (arg0.getSetting() != this.showUid && arg0.getSetting() != this.customDetails) {
            if (arg0.getSetting() != this.detailsMode) break label_4;
        }
        if (this.detailsMode.getValue().equals("Random")) {
        } else {
        }
        getDetails().setDetails(this.customDetails.getValue());
        applyState();
        applyImageMode();
        DiscordIPC.setActivity(this.rpc);
        label_4:
    }

    public void onDisable() {
        DiscordIPC.stop();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (this.detailsMode.getValue().equals("Random") && this.timer.hasTimeElapsed(300000)) {
            this.rpc.setDetails(getDetails());
            applyState();
            applyImageMode();
            DiscordIPC.setActivity(this.rpc);
            this.timer.reset();
        }
        if (this.imageMode.getValue().equals("Animated") && this.frameTimer.hasTimeElapsed(this.animatedDelay.getValue().longValue())) {
            this.frameIndex = ((this.frameIndex + 1) % 32);
            this.rpc.setLargeImage(frameKey(), "LightningClient");
            DiscordIPC.setActivity(this.rpc);
            this.frameTimer.reset();
        }
    }

    private String getDetails() {
        return this.DETAILS[((int) MathUtils.random(((double) this.DETAILS.length), 0.0))];
    }

    static {
        LOGGER = LoggerFactory.getLogger("Lightning/RPC");
    }

}
