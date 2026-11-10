package lightning.commands.impl;

import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name = "help", tag = "Help", description = "Shows you a list of all of the client's commands or information about a certain command.", syntax = "empty | <[command]>", aliases = "cmds")
public class HelpCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length != 0) return List.of();
        return LightningClient.COMMAND_MANAGER.getCommands().stream().map(Command::getName).toList();
    }

    public void execute(String[] arg0) {
        if (arg0.length == 1) {
            Object var2 = LightningClient.COMMAND_MANAGER.getCommand(arg0[0]);
            if (var2 == null) {
                LightningClient.CHAT_MANAGER.tagged("Could not find the command specified.", getTag(), getName());
                return;
            }
            LightningClient.CHAT_MANAGER.info((var2.getTag() + String.valueOf(ChatUtils.getPrimary()) + " - " + String.valueOf(ChatUtils.getSecondary()) + var2.getName() + " " + var2.getSyntax()));
            LightningClient.CHAT_MANAGER.info(var2.getDescription());
            break label_9;
        }
        if (arg0.length == 0) {
            var2 = LightningClient.COMMAND_MANAGER.getCommands();
            if (var2.isEmpty()) {
                LightningClient.CHAT_MANAGER.tagged("There are currently no registered commands.", getTag(), (getName() + "-list"));
            } else {
                Object var3 = new StringBuilder();
                int var4 = 0;
                Object var5 = var2.iterator();
                while (var5.hasNext()) {
                    Object var6 = ((Command) var5.next());
                    var4++;
                    var3.append(ChatUtils.getSecondary()).append(var6.getName()).append((var4 == var2.size() ? "" : ", "));
                }
                LightningClient.CHAT_MANAGER.message(("Commands " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + var2.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var3)), (getName() + "-list"));
            }
            break label_9;
        }
        messageSyntax();
        label_9:
    }

}
