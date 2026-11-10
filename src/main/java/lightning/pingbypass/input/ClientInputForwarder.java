package lightning.pingbypass.input;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.TickEvent;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.C2SInputPacket;
import lightning.pingbypass.protocol.packets.C2SOpenInventoryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public class ClientInputForwarder {
    private boolean inventoryOpen;

    public ClientInputForwarder() {
        this.inventoryOpen = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (!PingBypassFlags.proxyForwardingActive) return;
        if (LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) return;
        Object var4 = Minecraft.getInstance();
        if ((var4.player == null) || (var4.getConnection() == null)) {
            return;
        }
        Object var5 = var4.gui.screen();
        if (!((var5 instanceof InventoryScreen))) break label_4;
        var tmp0 = ((InventoryScreen) var5);
        Object var2 = tmp0;
        var tmp1 = (tmp0.getMenu() == var4.player.inventoryMenu ? 1 : 0);
        int var3 = tmp1;
        int var6 = tmp1;
        if (var3 == this.inventoryOpen) return;
        this.inventoryOpen = var3;
        try {
            Object var7 = PbCustomPayload.fromPacket(new C2SOpenInventoryPacket(var3));
            var4.getConnection().getConnection().send(new ServerboundCustomPayloadPacket(var7));
        } catch (Exception ex9) {
            var7 = ex9;
            LightningClient.LOGGER.warn("[PingBypass] Failed to send inventory-open state", var7);
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent arg0) {
        if (!PingBypassFlags.proxyForwardingActive) return;
        if (LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) return;
        Object var2 = Minecraft.getInstance();
        if (var2.getConnection() == null) return;
        int var3 = arg0.getButton();
        if (!(!var3) && var3 != 1) return;
        sendInput(var2, 0, var3, 1);
    }

    private void sendInput(Minecraft arg0, int arg1, int arg2, int arg3) {
        try {
            Object var5 = PbCustomPayload.fromPacket(new C2SInputPacket(arg1, arg2, arg3));
            arg0.getConnection().getConnection().send(new ServerboundCustomPayloadPacket(var5));
        } catch (Exception ex2) {
            var5 = ex2;
            LightningClient.LOGGER.warn("[PingBypass] Failed to send input", var5);
        }
    }

    public void start() {
        LightningClient.EVENT_HANDLER.subscribe(this);
        LightningClient.LOGGER.info("[PingBypass] Client input forwarder started");
    }

    public void stop() {
        LightningClient.EVENT_HANDLER.unsubscribe(this);
    }

}
