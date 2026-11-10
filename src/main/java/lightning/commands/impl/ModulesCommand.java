package lightning.commands.impl;

import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.Module;
import lightning.utils.chat.ChatUtils;
import net.minecraft.ChatFormatting;

@RegisterCommand(name = "modules", tag = "Modules", description = "Shows you a list of all of the client's modules and their toggle status.", aliases = "mods")
public class ModulesCommand extends Command {
    public void execute(String[] arg0) {
        if (arg0.length == 0) {
            Object var2 = LightningClient.MODULE_MANAGER.getModules();
            if (var2.isEmpty()) {
                LightningClient.CHAT_MANAGER.tagged("There are currently no registered modules.", getTag(), getName());
            } else {
                Object var3 = new StringBuilder();
                int var4 = 0;
                Object var5 = var2.iterator();
                while (var5.hasNext()) {
                    Object var6 = ((Module) var5.next());
                    if (var6.isToggled()) {
                    } else {
                    }
                    var4++;
                    (String.valueOf(ChatFormatting.GREEN) + "ON").append((String.valueOf(ChatFormatting.RED) + "OFF")).append(ChatUtils.getPrimary()).append("]").append((var4 == var2.size() ? "" : ", "));
                }
                LightningClient.CHAT_MANAGER.message(("Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + var2.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var3)), getName());
            }
        } else {
            messageSyntax();
        }
    }

}
