package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.Module;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name = "drawn", tag = "Drawn", description = "Manages the modules that will be shown on the HUD's module list.", syntax = "<true|false|list|reset> | <[module]> <true|false|reset>")
public class DrawnCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) {
            Object var2 = new ArrayList(moduleNames());
            var2.add("true");
            var2.add("false");
            var2.add("list");
            var2.add("reset");
            return var2;
        }
        if (arg0.length == 1 && LightningClient.MODULE_MANAGER.getModule(arg0[0]) != null) return List.of("true", "false", "reset");
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length == 2) {
            Object var2 = LightningClient.MODULE_MANAGER.getModule(arg0[0]);
            if (var2 == null) {
                LightningClient.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
                return;
            }
            Object var3 = arg0[1];
            int var4 = -1;
            switch (var3.hashCode()) {
                case 3569038 -> { break label_1; }
                case 97196323 -> { break label_2; }
                case 108404047 -> { break label_3; }
                default -> { break label_4; }
            }
            label_1:
            if (!(var3.equals("true"))) break label_4;
            var4 = 0;
            break label_4;
            label_2:
            if (!(var3.equals("false"))) break label_4;
            var4 = 1;
            break label_4;
            label_3:
            if (!(var3.equals("reset"))) break label_4;
            var4 = 2;
            label_4:
            switch (var4) {
                case 0 -> { break label_5; }
                case 1 -> { break label_6; }
                case 2 -> { break label_7; }
                default -> { break label_8; }
            }
            label_5:
            var2.drawn.setValue(true);
            LightningClient.CHAT_MANAGER.tagged(("Successfully set the module's drawn status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
            break label_9;
            label_6:
            var2.drawn.setValue(false);
            LightningClient.CHAT_MANAGER.tagged(("Successfully set the module's drawn status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
            break label_9;
            label_7:
            var2.drawn.setValue(var2.drawn.getDefaultValue());
            LightningClient.CHAT_MANAGER.tagged("Successfully set the module's drawn status to it's default value.", getTag(), getName());
            break label_9;
            label_8:
            messageSyntax();
            label_9:
            break label_34;
        }
        if (arg0.length == 1) {
            var2 = arg0[0];
            var3 = -1;
            switch (var2.hashCode()) {
                case 3322014 -> { break label_13; }
                case 3569038 -> { break label_11; }
                case 97196323 -> { break label_12; }
                case 108404047 -> { break label_14; }
                default -> { break label_15; }
            }
            label_11:
            if (!(var2.equals("true"))) break label_15;
            var3 = 0;
            break label_15;
            label_12:
            if (!(var2.equals("false"))) break label_15;
            var3 = 1;
            break label_15;
            label_13:
            if (!(var2.equals("list"))) break label_15;
            var3 = 2;
            break label_15;
            label_14:
            if (!(var2.equals("reset"))) break label_15;
            var3 = 3;
            label_15:
            switch (var3) {
                case 0 -> { break label_16; }
                case 1 -> { break label_19; }
                case 2 -> { break label_22; }
                case 3 -> { break label_28; }
                default -> { break label_31; }
            }
            label_16:
            var4 = LightningClient.MODULE_MANAGER.getModules().iterator();
            while (var4.hasNext()) {
                Object var5 = ((Module) var4.next());
                var5.drawn.setValue(true);
            }
            LightningClient.CHAT_MANAGER.tagged(("Successfully set every module's drawn status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
            break label_32;
            label_19:
            var4 = LightningClient.MODULE_MANAGER.getModules().iterator();
            while (var4.hasNext()) {
                var5 = ((Module) var4.next());
                var5.drawn.setValue(false);
            }
            LightningClient.CHAT_MANAGER.tagged(("Successfully set every module's drawn status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
            break label_32;
            label_22:
            var4 = new ArrayList(LightningClient.MODULE_MANAGER.getModules().stream().filter((arg0) -> arg0.drawn.getValue()).toList());
            if (var4.isEmpty()) {
                LightningClient.CHAT_MANAGER.tagged(("There are currently no modules with their drawn status set to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), (getName() + "-list"));
                break label_32;
            }
            var5 = new StringBuilder();
            int var6 = 0;
            Object var7 = var4.iterator();
            while (var7.hasNext()) {
                Object var8 = ((Module) var7.next());
                var5.append(ChatUtils.getSecondary()).append(var8.getName()).append(((var6 + 1) == var4.size() ? "" : ", "));
                var6++;
            }
            LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getSecondary()) + "Drawn Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + var4.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var5)), (getName() + "-list"));
            break label_32;
            label_28:
            var4 = LightningClient.MODULE_MANAGER.getModules().iterator();
            while (var4.hasNext()) {
                var5 = ((Module) var4.next());
                var5.drawn.setValue(var5.drawn.getDefaultValue());
            }
            LightningClient.CHAT_MANAGER.tagged("Successfully set every module's drawn status to it's default value.", getTag(), getName());
            break label_32;
            label_31:
            messageSyntax();
            label_32:
            break label_34;
        }
        messageSyntax();
        label_34:
    }

}
