package lightning.modules;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.events.impl.ToggleModuleEvent;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.modules.PbModuleManager;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.pingbypass.protocol.packets.S2CModuleStatePacket;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.PositionSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.IMinecraft;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.chat.ChatUtils;
import lightning.utils.minecraft.InventoryUtils;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public abstract class Module implements IMinecraft {
    private final String name;
    private final String description;
    private final Module.Category category;
    private final boolean persistent;
    private final boolean proxyEnhanced;
    private boolean toggled;
    private final List settings;
    public BooleanSetting chatNotify;
    public BooleanSetting drawn;
    public BindSetting bind;
    public ModeSetting proxyMode;
    private final Animation animationOffset;

    public Module() {
        Object var1 = ((RegisterModule) getClass().getAnnotation(RegisterModule.class));
        this.name = var1.name();
        this.description = var1.description();
        this.category = var1.category();
        this.persistent = var1.persistent();
        this.proxyEnhanced = var1.proxyEnhanced();
        this.toggled = var1.toggled();
        this.settings = new ArrayList();
        this.animationOffset = new Animation(300, Easing.Method.EASE_IN_OUT_CUBIC);
        this.chatNotify = new BooleanSetting("ChatNotify", "Notifies you in chat whenever the module gets toggled on or off.", true);
        this.drawn = new BooleanSetting("Drawn", "Renders the module's name on the HUD's module list.", var1.drawn());
        this.bind = new BindSetting("Bind", "The keybind that toggles the module on and off.", var1.bind());
        if (!(!this.proxyEnhanced)) {
            var tmp0 = new String[3];
            tmp0[0] = "Auto";
            tmp0[1] = "Proxy";
            tmp0[2] = "Local";
            this.proxyMode = new ModeSetting("ProxyMode", "Where this module executes when connected to a PingBypass proxy.", "Auto", tmp0);
        }
        if (!(!this.persistent)) {
            this.toggled = true;
        }
        if (!(!this.toggled)) {
            LightningClient.EVENT_HANDLER.subscribe(this);
        }
    }

    public boolean getNull() {
        if (mc.player != null) {
            if (mc.level == null) {
            }
        } else {
        }
        return false;
    }

    public boolean shouldRunOnProxy() {
        if ((!this.proxyEnhanced) || (this.proxyMode == null)) {
            return false;
        }
        if (!PingBypassFlags.proxyForwardingActive) return false;
        if (LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) return false;
        Object var1 = this.proxyMode.getValue();
        int var2 = -1;
        switch (var1.hashCode()) {
            case 2052559 -> { break label_4; }
            case 73592651 -> { break label_6; }
            case 77388366 -> { break label_5; }
            default -> { break label_7; }
        }
        label_4:
        if (!(var1.equals("Auto"))) break label_7;
        var2 = 0;
        break label_7;
        label_5:
        if (!(var1.equals("Proxy"))) break label_7;
        var2 = 1;
        break label_7;
        label_6:
        if (!(var1.equals("Local"))) break label_7;
        var2 = 2;
        label_7:
        switch (var2) {
            case 0 -> { break label_8; }
            case 1 -> { break label_8; }
            case 2 -> { break label_9; }
            default -> { break label_10; }
        }
        label_8:
        break label_11;
        label_9:
        break label_11;
        label_10:
        label_11:
        return true;
    }

    public boolean shouldSkipActions() {
        return shouldRunOnProxy();
    }

    public boolean isRunningOnProxy() {
        if (!this.proxyEnhanced) return false;
        if (!PingBypassFlags.proxyForwardingActive) return false;
        if ((LightningClient.PINGBYPASS_CONFIG == null) || (!(LightningClient.PINGBYPASS_CONFIG.isServer()))) {
            return false;
        }
        if (this.proxyMode != null) {
            if (!(this.proxyMode.getValue().equals("Local"))) {
            }
        } else {
        }
        return false;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public String getMetaData() {
        return "";
    }

    public String getProxyIndicator() {
        if (shouldRunOnProxy() && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) return " \u00a7d[PB]";
        return "";
    }

    private boolean shouldSkipLocalExecution() {
        return shouldRunOnProxy();
    }

    public void setToggled(boolean arg0) {
        setToggled(arg0, true);
    }

    public void setToggled(boolean arg0, boolean arg1) {
        if (!(!this.persistent)) return;
        if (arg0 == this.toggled) return;
        if (!(!arg0)) {
        }
        this.toggled = arg0;
        LightningClient.EVENT_HANDLER.post(new ToggleModuleEvent(this, this.toggled));
        if (!(shouldRunOnProxy())) {
            if (!(PbModuleManager.MIGRATED_MODULE_NAMES.contains(this.name))) break label_4;
        }
        if (((!arg1) || (LightningClient.PINGBYPASS_CONFIG == null)) || (LightningClient.PINGBYPASS_CONFIG.isServer())) break label_4;
        sendProxyToggle(this.toggled);
        label_4:
        if (((((!this.proxyEnhanced) || (LightningClient.PINGBYPASS_CONFIG == null)) || (!(LightningClient.PINGBYPASS_CONFIG.isServer()))) || (!PingBypassFlags.proxyForwardingActive)) || (LightningClient.PROXY_SERVER == null)) break label_7;
        Object var3 = new ClientboundCustomPayloadPacket(PbCustomPayload.fromPacket(new S2CModuleStatePacket(this.name, this.toggled)));
        Object var4 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Connection) var4.next());
            if (!(var5.isConnected())) {
                break label_5;
            }
            var5.send(var3);
        }
        label_7:
        if (!(!this.toggled)) {
            this.animationOffset.setEasing(Easing.Method.EASE_IN_OUT_CUBIC);
            if (!(!arg1) && this.chatNotify.getValue()) {
                LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getPrimary()) + this.name + String.valueOf(ChatUtils.getSecondary()) + " = " + String.valueOf(ChatFormatting.GREEN) + "true" + String.valueOf(ChatUtils.getSecondary()) + ";"), ("toggle-" + getName().toLowerCase()));
            }
            onEnable();
            if (!this.toggled) break label_10;
            LightningClient.EVENT_HANDLER.subscribe(this);
            break label_10;
        }
        this.animationOffset.setEasing(Easing.Method.EASE_IN_OUT_CUBIC);
        LightningClient.EVENT_HANDLER.unsubscribe(this);
        onDisable();
        InventoryUtils.releaseAllBorrows();
        if ((!arg1) || (!(this.chatNotify.getValue()))) break label_10;
        LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getPrimary()) + this.name + String.valueOf(ChatUtils.getSecondary()) + " = " + String.valueOf(ChatFormatting.RED) + "false" + String.valueOf(ChatUtils.getSecondary()) + ";"), ("toggle-" + getName().toLowerCase()));
        label_10:
    }

    public int getBind() {
        return this.bind.getValue();
    }

    public void setBind(int arg0) {
        this.bind.setValue(arg0);
    }

    public void resetValues() {
        Object var1 = this.settings.iterator();
        while (var1.hasNext()) {
            Object var2 = ((Setting) var1.next());
            if ((var2 instanceof BooleanSetting)) {
                Object var3 = ((BooleanSetting) var2);
                ((BooleanSetting) var3).resetValue();
            }
            if ((var2 instanceof NumberSetting)) {
                var3 = ((NumberSetting) var2);
                ((NumberSetting) var3).resetValue();
            }
            if ((var2 instanceof ModeSetting)) {
                var3 = ((ModeSetting) var2);
                ((ModeSetting) var3).resetValue();
            }
            if ((var2 instanceof StringSetting)) {
                var3 = ((StringSetting) var2);
                ((StringSetting) var3).resetValue();
            }
            if ((var2 instanceof BindSetting)) {
                var3 = ((BindSetting) var2);
                ((BindSetting) var3).resetValue();
            }
            if ((var2 instanceof WhitelistSetting)) {
                var3 = ((WhitelistSetting) var2);
                ((WhitelistSetting) var3).clear();
            }
            if ((var2 instanceof ColorSetting)) {
                var3 = ((ColorSetting) var2);
                ((ColorSetting) var3).resetValue();
            }
            if (!((var2 instanceof PositionSetting))) {
                break label_0;
            }
            var3 = ((PositionSetting) var2);
            ((PositionSetting) var3).resetValue();
        }
    }

    public Setting getSetting(String arg0) {
        return ((Setting) this.settings.stream().filter((arg1) -> {
    if (!(arg1.getName().equalsIgnoreCase(arg0))) break label_0;
    return !((arg1 instanceof CategorySetting));
}).findFirst().orElse(null));
    }

    private void sendProxyToggle(boolean arg0) {
        try {
            if (mc.getConnection() != null) {
                Object var2 = PbCustomPayload.fromPacket(new C2SModuleTogglePacket(this.name, arg0));
                mc.getConnection().getConnection().send(new ServerboundCustomPayloadPacket(var2));
            }
        } catch (Exception ex2) {
            var2 = ex2;
            LightningClient.LOGGER.warn("[PingBypass] Failed to send module toggle to proxy", var2);
        }
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public Module.Category getCategory() {
        return this.category;
    }

    @Generated
    public boolean isPersistent() {
        return this.persistent;
    }

    @Generated
    public boolean isProxyEnhanced() {
        return this.proxyEnhanced;
    }

    @Generated
    public boolean isToggled() {
        return this.toggled;
    }

    @Generated
    public List getSettings() {
        return this.settings;
    }

    @Generated
    public BooleanSetting getChatNotify() {
        return this.chatNotify;
    }

    @Generated
    public BooleanSetting getDrawn() {
        return this.drawn;
    }

    @Generated
    public ModeSetting getProxyMode() {
        return this.proxyMode;
    }

    @Generated
    public Animation getAnimationOffset() {
        return this.animationOffset;
    }


    public enum Category {
        COMBAT,
        PLAYER,
        VISUALS,
        MOVEMENT,
        MISCELLANEOUS,
        CORE;

        private final String name;

        private Category(String arg2) {
            this.name = arg2;
        }

        @Generated
        public String getName() {
            return this.name;
        }

    }
}
