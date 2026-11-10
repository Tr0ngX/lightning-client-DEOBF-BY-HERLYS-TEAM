package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.Module;
import lightning.utils.chat.ChatUtils;
import lightning.utils.input.KeyboardUtils;

@RegisterCommand(name = "bind", tag = "Bind", description = "Changes the toggle keybind of a module.", syntax = "<[module]> <[key]|reset> | <reset|list> ", aliases = {"b", "key", "keybind"})
public class BindCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) {
            Object var2 = new ArrayList(moduleNames());
            var2.add("reset");
            var2.add("list");
            return var2;
        }
        if (arg0.length == 1 && LightningClient.MODULE_MANAGER.getModule(arg0[0]) != null) return List.of("<bind>");
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length == 2) {
            Object var2 = LightningClient.MODULE_MANAGER.getModule(arg0[0]);
            if (var2 == null) {
                LightningClient.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
                return;
            }
            if (!(arg0[1].equalsIgnoreCase("reset"))) break label_1;
            var2.setBind(0);
            LightningClient.CHAT_MANAGER.tagged(("Successfully reset the toggle keybind of the " + String.valueOf(ChatUtils.getPrimary()) + var2.getName() + String.valueOf(ChatUtils.getSecondary()) + " module."), getTag(), getName());
            break label_2;
            label_1:
            var2.setBind(KeyboardUtils.getKeyNumber(arg0[1]));
            LightningClient.CHAT_MANAGER.tagged(("Successfully bound the " + String.valueOf(ChatUtils.getPrimary()) + var2.getName() + String.valueOf(ChatUtils.getSecondary()) + " module to the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(var2.getBind()) + String.valueOf(ChatUtils.getSecondary()) + " key."), getTag(), getName());
            label_2:
            break label_17;
        }
        if (arg0.length == 1) {
            var2 = arg0[0].toLowerCase();
            int var3 = -1;
            switch (var2.hashCode()) {
                case 3322014 -> { break label_5; }
                case 108404047 -> { break label_4; }
                default -> { break label_6; }
            }
            label_4:
            if (!(var2.equals("reset"))) break label_6;
            var3 = 0;
            break label_6;
            label_5:
            if (!(var2.equals("list"))) break label_6;
            var3 = 1;
            label_6:
            switch (var3) {
                case 0 -> { break label_7; }
                case 1 -> { break label_8; }
                default -> { break label_14; }
            }
            label_7:
            LightningClient.MODULE_MANAGER.getModules().forEach((arg0) -> arg0.bind.resetValue());
            LightningClient.CHAT_MANAGER.tagged("Successfully reset every module's toggle keybind.", getTag(), getName());
            break label_15;
            label_8:
            Object var4 = LightningClient.MODULE_MANAGER.getModules().stream().filter((arg0) -> arg0.getBind()).toList();
            if (var4.isEmpty()) {
                LightningClient.CHAT_MANAGER.tagged("There are currently no bound modules.", getTag(), (getName() + "-list"));
                break label_15;
            }
            Object var5 = new StringBuilder();
            int var6 = 0;
            Object var7 = var4.iterator();
            while (var7.hasNext()) {
                Object var8 = ((Module) var7.next());
                var6++;
                var5.append(ChatUtils.getSecondary()).append(var8.getName()).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary()).append(KeyboardUtils.getKeyName(var8.getBind()).toUpperCase()).append(ChatUtils.getPrimary()).append("]").append((var6 == var4.size() ? "" : ", "));
            }
            LightningClient.CHAT_MANAGER.message(("Bound Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + var4.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var5)), (getName() + "-list"));
            break label_15;
            label_14:
            messageSyntax();
            label_15:
            break label_17;
        }
        messageSyntax();
        label_17:
    }

}
