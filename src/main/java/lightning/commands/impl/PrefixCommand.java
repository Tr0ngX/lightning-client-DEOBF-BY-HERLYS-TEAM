package lightning.commands.impl;

import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name = "prefix", tag = "Prefix", description = "Allows you to change the client's command prefix.", syntax = "<new_prefix> | reset", aliases = {"setprefix", "p"})
public class PrefixCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) {
        } else {
        }
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length != 1) break label_11;
        if (!(arg0[0].equalsIgnoreCase("reset"))) break label_4;
        LightningClient.COMMAND_MANAGER.setPrefix(".");
        try {
            if (LightningClient.CONFIG_MANAGER == null) break label_1;
            LightningClient.CONFIG_MANAGER.saveGeneral();
            label_1:
        } catch (Exception ex2) {
            Object var2 = ex2;
        }
        LightningClient.CHAT_MANAGER.tagged("Successfully reset the command's prefix back to it's default (.).", getTag(), getName());
        break label_12;
        label_4:
        if (arg0[0].length() > 3) {
            LightningClient.CHAT_MANAGER.tagged("The specified prefix is longer than the limit of 3 characters.", getTag(), getName());
            return;
        }
        if (arg0[0].equalsIgnoreCase("/")) {
            LightningClient.CHAT_MANAGER.tagged("The specified prefix would interfere with vanilla Minecraft commands.", getTag(), getName());
            return;
        }
        LightningClient.COMMAND_MANAGER.setPrefix(arg0[0]);
        try {
            if (LightningClient.CONFIG_MANAGER != null) {
                LightningClient.CONFIG_MANAGER.saveGeneral();
            }
        } catch (Exception ex9) {
            var2 = ex9;
        }
        LightningClient.CHAT_MANAGER.tagged(("Successfully set the client's command prefix to " + String.valueOf(ChatUtils.getPrimary()) + arg0[0] + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
        break label_12;
        label_11:
        messageSyntax();
        label_12:
    }

}
