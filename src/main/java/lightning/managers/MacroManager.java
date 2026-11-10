package lightning.managers;

import java.util.HashMap;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.utils.IMinecraft;
import lightning.utils.chat.ChatUtils;
import lightning.utils.input.KeyboardUtils;
import lombok.Generated;

public class MacroManager implements IMinecraft {
    private final HashMap macros;

    public MacroManager() {
        this.macros = new HashMap();
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }
        Object var2 = this.macros.keySet().iterator();
        while (var2.hasNext()) {
            Object var3 = ((String) var2.next());
            int var5 = ((Integer) this.macros.get(var3)).intValue();
            if (arg0.getKey() != var5) {
                break label_2;
            }
            if (var3 == null) {
                LightningClient.CHAT_MANAGER.error(("An error happened while executing the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(var5) + String.valueOf(ChatUtils.getSecondary()) + " macro."));
                break label_2;
            }
            var tmp0 = var3.split(";");
            Object var4 = tmp0;
            Object var6 = tmp0;
            int var7 = var6.length;
            int var8 = 0;
            while (var8 < var7) {
                Object var9 = var6[var8];
                if (var9.startsWith("/")) {
                    mc.player.connection.sendCommand(var9.substring(1));
                    break label_8;
                }
                if (var9.startsWith(LightningClient.COMMAND_MANAGER.getPrefix())) {
                    LightningClient.COMMAND_MANAGER.execute(var9);
                    break label_8;
                }
                mc.player.connection.sendChat(var9);
                label_8:
                var8++;
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }
        Object var2 = this.macros.keySet().iterator();
        while (var2.hasNext()) {
            Object var3 = ((String) var2.next());
            int var4 = ((Integer) this.macros.get(var3)).intValue();
            if (var4 != (-(arg0.getButton()) - 1)) {
                break label_2;
            }
            if (var3 == null) {
                LightningClient.CHAT_MANAGER.error(("An error happened while executing the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(var4) + String.valueOf(ChatUtils.getSecondary()) + " macro."));
                break label_2;
            }
            if (var3.startsWith("/")) {
                mc.player.connection.sendCommand(var3.substring(1));
                break label_2;
            }
            mc.player.connection.sendChat(var3);
        }
    }

    public String getKey(int arg0) {
        Object var2 = this.macros.keySet().iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((String) var2.next());
            if (arg0 != ((Integer) this.macros.get(var3)).intValue()) {
                break label_0;
            }
            return var3;
        }
        return null;
    }

    public int getValue(String arg0) {
        return ((Integer) this.macros.get(arg0)).intValue();
    }

    public boolean containsKey(String arg0) {
        return this.macros.containsKey(arg0);
    }

    public boolean containsValue(int arg0) {
        return this.macros.containsValue(arg0);
    }

    public void add(String arg0, int arg1) {
        this.macros.put(arg0, arg1);
    }

    public void remove(String arg0) {
        this.macros.remove(arg0);
    }

    public void clear() {
        this.macros.clear();
    }

    @Generated
    public HashMap getMacros() {
        return this.macros;
    }

}
