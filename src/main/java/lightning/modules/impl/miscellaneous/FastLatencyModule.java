package lightning.modules.impl.miscellaneous;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.system.Timer;
import lightning.utils.system.ZeroTimer;
import lombok.Generated;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;

@RegisterModule(name = "FastLatency", description = "Makes ping resolving much faster.", category = Module.Category.MISCELLANEOUS)
public class FastLatencyModule extends Module {
    public NumberSetting delay;
    public BooleanSetting spikeNotifier;
    public NumberSetting threshold;
    private final Timer timer;
    private final ZeroTimer receivedTimer;
    private long time;
    private int latency;

    public FastLatencyModule() {
        this.delay = new NumberSetting("Delay", "The amount of milliseconds that have to be waited for before resolving your ping again.", 100L, 0, 1000L);
        this.spikeNotifier = new BooleanSetting("SpikeNotifier", "Notifies you in chat whenever your ping spikes.", false);
        this.threshold = new NumberSetting("Threshold", "The amount of milliseconds that your ping has to increase for before notifying you.", new BooleanSetting.Visibility(this.spikeNotifier, true), 30, 0, 1000);
        this.timer = new Timer();
        this.receivedTimer = new ZeroTimer();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.player == null) return;
        if (this.receivedTimer.hasTimeElapsed(1000L) && this.timer.hasTimeElapsed(this.delay.getValue().longValue())) {
            mc.getConnection().send(new ServerboundCommandSuggestionPacket(1000, "/w "));
            this.time = System.currentTimeMillis();
            this.receivedTimer.reset();
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ClientboundCommandSuggestionsPacket))) break label_1;
        var tmp0 = ((ClientboundCommandSuggestionsPacket) var3);
        Object var2 = tmp0;
        if (tmp0.id() != 1000) break label_1;
        int var4 = ((int) (System.currentTimeMillis() - this.time));
        if (this.spikeNotifier.getValue() && (var4 - this.latency) > this.threshold.getValue().intValue()) {
            LightningClient.CHAT_MANAGER.message(("Your ping has spiked to " + String.valueOf(ChatUtils.getPrimary()) + var4 + "ms" + String.valueOf(ChatUtils.getSecondary()) + " from " + String.valueOf(ChatUtils.getPrimary()) + this.latency + "ms" + String.valueOf(ChatUtils.getSecondary()) + "!"), ("module-" + getName().toLowerCase() + "-spike"));
        }
        this.latency = var4;
        this.receivedTimer.zero();
        label_1:
    }

    public String getMetaData() {
        return (this.latency + "ms");
    }

    @Generated
    public int getLatency() {
        return this.latency;
    }

}
