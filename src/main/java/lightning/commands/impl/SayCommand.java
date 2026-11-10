package lightning.commands.impl;

import lightning.commands.Command;
import lightning.commands.RegisterCommand;

@RegisterCommand(name = "say", description = "Sends a message through commands, allowing you to bypass the client's prefix and minecraft's command prefix.", syntax = "<[message]>")
public class SayCommand extends Command {
    public void execute(String[] arg0) {
        if (arg0.length >= 1) {
            Object var2 = new StringBuilder();
            int var3 = 0;
            Object var4 = arg0;
            int var5 = var4.length;
            int var6 = 0;
            while (var6 < var5) {
                Object var7 = var4[var6];
                var2.append(var7).append(((var3 + 1) == arg0.length ? "" : " "));
                var3++;
                var6++;
            }
            mc.getConnection().sendChat(var2.toString());
        } else {
            messageSyntax();
        }
    }

}
