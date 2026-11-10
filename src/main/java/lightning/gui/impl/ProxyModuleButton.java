package lightning.gui.impl;

import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.modules.impl.core.PingBypassModule;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public class ProxyModuleButton extends Button {
    private final String moduleName;

    public ProxyModuleButton(String arg0, Frame arg1, int arg2) {
        super(arg1, arg2, ("PingBypass proxy module: " + arg0));
        this.moduleName = arg0;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    private boolean isEnabled() {
        Object var1 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class));
        if (var1 == null) return false;
        return var1.isProxyModuleEnabled(this.moduleName);
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        if (isHovering(((double) arg1), ((double) arg2)) && LightningClient.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
            LightningClient.CLICK_GUI.getDescriptionFrame().setDescription(getDescription());
        }
        if (isHovering(((double) arg1), ((double) arg2))) {
        } else {
        }
        var tmp0 = new Color(0, 0, 0, 0);
        Object var6 = tmp0;
        Object var7 = tmp0;
        if (var6.getAlpha() > 0) {
            Renderer2D.renderQuad(arg0, ((float) (getX() + getPadding())), ((float) getY()), ((float) ((getX() + getWidth()) - getPadding())), ((float) ((getY() + getHeight()) - 1)), var6);
        }
        var tmp1 = isEnabled();
        int var5 = tmp1;
        if (!(!tmp1)) {
            Object var8 = ClickGuiScreen.getButtonColor(getY(), 255);
            Renderer2D.renderQuad(arg0, ((float) (getX() + getPadding())), ((float) (getY() + 1)), ((float) ((getX() + getPadding()) + 2)), ((float) ((getY() + getHeight()) - 2)), var8);
        }
        var8 = this.moduleName;
        int var9 = ((getX() + getTextPadding()) + (!(!var5) ? 1 : 0));
        int var10 = (getY() + 2);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, (String.valueOf((!(!var5) ? "" : ChatFormatting.GRAY)) + var8), var9, var10, Color.WHITE);
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (isHovering(arg0, arg1) && arg2 == 0) {
            int var6 = (!(isEnabled()) ? 1 : 0);
            sendModuleToggle(var6);
            playClickSound();
        }
    }

    private void sendModuleToggle(boolean arg0) {
        if (mc.getConnection() == null) return;
        Object var2 = new C2SModuleTogglePacket(this.moduleName, arg0);
        mc.getConnection().getConnection().send(new ServerboundCustomPayloadPacket(PbCustomPayload.fromPacket(var2)));
    }

}
