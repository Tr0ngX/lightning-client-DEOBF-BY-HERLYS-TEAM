package lightning.commands.impl;

import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.chat.ChatUtils;
import net.minecraft.ChatFormatting;

@RegisterCommand(name = "toggle", tag = "Toggle", description = "Toggles a specified module or a setting on and off.", syntax = "<[module]> | <[module]> <[setting]>", aliases = "t")
public class ToggleCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) return moduleNames();
        if (arg0.length == 1) {
            Object var2 = LightningClient.MODULE_MANAGER.getModule(arg0[0]);
            if (var2 == null) return List.of();
            return var2.getSettings().stream().filter((arg0) -> (arg0 instanceof BooleanSetting)).map((arg0) -> arg0.getName().toLowerCase()).toList();
        }
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length != 1) {
            if (arg0.length == 2) {
            }
            Object var2 = LightningClient.MODULE_MANAGER.getModule(arg0[0]);
            if (var2 != null) break label_1;
            LightningClient.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
            return;
            label_1:
            if (arg0.length == 1) {
                if (var2.isPersistent()) {
                    LightningClient.CHAT_MANAGER.tagged("Cannot toggle a persistent module.", getTag(), getName());
                    return;
                }
                var2.setToggled(!(var2.isToggled()), false);
                if (var2.isToggled()) {
                } else {
                }
                String.valueOf(ChatUtils.getPrimary()).tagged((var2.getName() + String.valueOf(ChatUtils.getSecondary()) + (String.valueOf(ChatFormatting.GREEN) + "on") + " has been toggled " + (String.valueOf(ChatFormatting.RED) + "off") + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), (getName() + "-cmd-" + var2.getName()));
            }
            if (arg0.length == 2) {
                Object var3 = var2.getSetting(arg0[1]);
                if (var3 == null) {
                    LightningClient.CHAT_MANAGER.tagged("Could not find the setting specified.", getTag(), getName());
                    return;
                }
                if (!((var3 instanceof BooleanSetting))) {
                    LightningClient.CHAT_MANAGER.tagged(("This command only works for " + String.valueOf(ChatUtils.getPrimary()) + "boolean" + String.valueOf(ChatUtils.getSecondary()) + " settings."), getTag(), getName());
                    return;
                }
                Object var4 = ((BooleanSetting) var3);
                var4.setValue(!(var4.getValue()));
                if (var4.getValue()) {
                } else {
                }
                String.valueOf(ChatUtils.getPrimary()).tagged((var3.getName() + String.valueOf(ChatUtils.getSecondary()) + (String.valueOf(ChatFormatting.GREEN) + "on") + " has been toggled " + (String.valueOf(ChatFormatting.RED) + "off") + String.valueOf(ChatUtils.getSecondary()) + " for " + String.valueOf(ChatUtils.getPrimary()) + var2.getName() + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), (getName() + "-cmd-" + var2.getName() + "-" + var3.getName()));
            }
        } else {
            messageSyntax();
        }
    }

}
