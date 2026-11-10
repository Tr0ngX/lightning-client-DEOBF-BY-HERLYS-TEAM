package lightning.commands.impl;

import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name = "grab", description = "Lets you copy various things to your clipboard.", syntax = "<ip|coords|name>")
public class GrabCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) {
        } else {
        }
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length == 1) {
            Object var2 = arg0[0];
            int var3 = -1;
            switch (var2.hashCode()) {
                case -1354750946 -> { break label_1; }
                case 3367 -> { break label_0; }
                case 3373707 -> { break label_2; }
                default -> { break label_3; }
            }
            label_0:
            if (!(var2.equals("ip"))) break label_3;
            var3 = 0;
            break label_3;
            label_1:
            if (!(var2.equals("coords"))) break label_3;
            var3 = 1;
            break label_3;
            label_2:
            if (!(var2.equals("name"))) break label_3;
            var3 = 2;
            label_3:
            switch (var3) {
                case 0 -> { break label_4; }
                case 1 -> { break label_5; }
                case 2 -> { break label_6; }
                default -> { break label_7; }
            }
            label_4:
            copy(LightningClient.SERVER_MANAGER.getServer());
            break label_8;
            label_5:
            copy(("[" + ((int) mc.player.getX()) + ", " + ((int) mc.player.getY()) + ", " + ((int) mc.player.getZ()) + "]"));
            break label_8;
            label_6:
            copy(mc.player.getName().getString());
            break label_8;
            label_7:
            messageSyntax();
            label_8:
        } else {
            messageSyntax();
        }
    }

    private void copy(String arg0) {
        mc.keyboardHandler.setClipboard(arg0);
        LightningClient.CHAT_MANAGER.tagged(("Successfully copied " + String.valueOf(ChatUtils.getPrimary()) + arg0 + String.valueOf(ChatUtils.getSecondary()) + " to your clipboard."), getTag(), getName());
    }

}
