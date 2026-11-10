package lightning.modules.impl.miscellaneous;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.RenderOverlayEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.system.MathUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "LagNotify", description = "Notifies you when you lag.", category = Module.Category.MISCELLANEOUS)
public class LagNotifyModule extends Module {
    BooleanSetting server;
    BooleanSetting lagback;
    ColorSetting color;
    Vec3 lagPos;
    double lagDistance;
    long lagTime;

    public LagNotifyModule() {
        this.server = new BooleanSetting("Server", "Notifies you when the server stops responding.", true);
        this.lagback = new BooleanSetting("Lagback", "Notifies you when you lagback.", true);
        this.color = new ColorSetting("Color", "The color of the notification text.", ColorUtils.getDefaultOutlineColor());
        this.lagPos = null;
        this.lagTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (getNull()) return;
        Object var2 = arg0.getPacket();
        if ((var2 instanceof ClientboundPlayerPositionPacket)) {
            Object var3 = ((ClientboundPlayerPositionPacket) var2);
            this.lagPos = new Vec3(var3.change().position().x(), var3.change().position().y(), var3.change().position().z());
            this.lagDistance = mc.player.position().distanceTo(this.lagPos);
            this.lagTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent arg0) {
        if (getNull()) return;
        int var3 = (mc.getWindow().getGuiScaledWidth() / 2);
        int var4 = (mc.getWindow().getGuiScaledHeight() / 4);
        int var5 = 0;
        if (this.server.getValue() && LightningClient.SERVER_MANAGER.getResponseTimer().hasTimeElapsed(1000)) {
            Object var2 = ("Detected server not responding for " + MathUtils.round(((double) (((float) LightningClient.SERVER_MANAGER.getResponseTimer().timeElapsed()) / 1000.0f)), 1) + "s.");
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0.getContext(), var2, (var3 - (LightningClient.FONT_MANAGER.getWidth(var2) / 2)), (var4 - LightningClient.FONT_MANAGER.getHeight()), this.color.getColor());
            var5 = 1;
        }
        if (this.lagback.getValue() && (System.currentTimeMillis() - this.lagTime) < 3000L) {
            var2 = ("Detected lagback of " + MathUtils.round(this.lagDistance, 1) + " blocks " + MathUtils.round(((double) (((float) (System.currentTimeMillis() - this.lagTime)) / 1000.0f)), 1) + "s.");
            if (!(!var5)) {
            } else {
            }
            arg0.getContext().drawTextWithShadow(var2, (var3 - (LightningClient.FONT_MANAGER.getWidth(var2) / 2)), (var4 - LightningClient.FONT_MANAGER.getHeight()), (LightningClient.FONT_MANAGER.getHeight() + 0), this.color.getColor());
        }
    }

}
