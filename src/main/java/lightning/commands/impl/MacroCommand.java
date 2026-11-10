package lightning.commands.impl;

import java.util.Arrays;
import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.utils.chat.ChatUtils;
import lightning.utils.input.KeyboardUtils;

@RegisterCommand(name = "macro", tag = "Macro", description = "Allows you to manage the client's macro system.", syntax = "add <[key]> <[message]> | remove <[key]> | <clear|list>")
public class MacroCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) return List.of("add", "remove", "clear", "list");
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length >= 3) {
            if (arg0[0].equalsIgnoreCase("add")) {
                int var2 = KeyboardUtils.getKeyNumber(arg0[1]);
                if (!var2) {
                    LightningClient.CHAT_MANAGER.tagged("The keybind specified is not valid.", getTag());
                    return;
                }
                Object var3 = new StringBuilder();
                Object var4 = ((String[]) Arrays.copyOfRange(arg0, 2, arg0.length));
                int var5 = 0;
                Object var6 = var4;
                int var7 = var6.length;
                int var8 = 0;
                while (var8 < var7) {
                    Object var9 = var6[var8];
                    var5++;
                    var3.append(var9).append((var5 == var4.length ? "" : " "));
                    var8++;
                }
                LightningClient.MACRO_MANAGER.add(var3.toString(), var2);
                LightningClient.CHAT_MANAGER.tagged(("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + String.valueOf(var3) + String.valueOf(ChatUtils.getSecondary()) + " as a macro, bound to the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(var2) + String.valueOf(ChatUtils.getSecondary()) + " key."), getTag(), ("command-" + getName()));
                break label_20;
            }
            messageSyntax();
            break label_20;
        }
        if (arg0.length == 2) {
            if (arg0[0].equalsIgnoreCase("remove")) {
                var2 = KeyboardUtils.getKeyNumber(arg0[1]);
                if (!var2) {
                    LightningClient.CHAT_MANAGER.tagged("The keybind specified is not valid.", getTag());
                    return;
                }
                if (!(LightningClient.MACRO_MANAGER.containsValue(var2))) {
                    LightningClient.CHAT_MANAGER.tagged(("There is no macro with the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(var2) + String.valueOf(ChatUtils.getSecondary()) + " key."), getTag(), ("command-" + getName()));
                } else {
                    LightningClient.CHAT_MANAGER.tagged(("Successfully removed the " + String.valueOf(ChatUtils.getPrimary()) + LightningClient.MACRO_MANAGER.getKey(var2) + String.valueOf(ChatUtils.getSecondary()) + " macro."), getTag(), ("command-" + getName()));
                    LightningClient.MACRO_MANAGER.remove(LightningClient.MACRO_MANAGER.getKey(var2));
                }
                break label_20;
            }
            messageSyntax();
            break label_20;
        }
        if (arg0.length == 1) {
            if (arg0[0].equalsIgnoreCase("clear")) {
                LightningClient.MACRO_MANAGER.clear();
                LightningClient.CHAT_MANAGER.tagged("Successfully removed all macros.", getTag(), ("command-" + getName()));
                break label_20;
            }
            if (arg0[0].equalsIgnoreCase("list")) {
                if (LightningClient.MACRO_MANAGER.getMacros().isEmpty()) {
                    LightningClient.CHAT_MANAGER.tagged("There are currently no macros added.", getTag(), ("command-" + getName() + "-list"));
                    break label_20;
                }
                var2 = new StringBuilder();
                var3 = 0;
                var4 = LightningClient.MACRO_MANAGER.getMacros().keySet().iterator();
                while (var4.hasNext()) {
                    var5 = ((String) var4.next());
                    var6 = LightningClient.MACRO_MANAGER.getValue(var5);
                    var3++;
                    var2.append(ChatUtils.getSecondary()).append(var5).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary()).append(KeyboardUtils.getKeyName(var6)).append(ChatUtils.getPrimary()).append("]").append(ChatUtils.getSecondary()).append((var3 == LightningClient.MACRO_MANAGER.getMacros().size() ? "" : ", "));
                }
                LightningClient.CHAT_MANAGER.message(("Macros " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + LightningClient.MACRO_MANAGER.getMacros().size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var2)), ("command-" + getName() + "-list"));
                break label_20;
            }
            messageSyntax();
            break label_20;
        }
        messageSyntax();
        label_20:
    }

}
