package lightning.commands.impl;

import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name = "coords", tag = "Coords", description = "Copies your position, or whispers it to a player.", syntax = "| <[player]>")
public class CoordsCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0 && mc.getConnection() != null) {
            if (mc.player != null) {
            } else {
            }
            Object var2 = "";
            return mc.getConnection().getOnlinePlayers().stream().map((arg0) -> arg0.getProfile().name()).filter((arg1) -> {
    if ((arg1 == null) || (arg1.isEmpty())) break label_0;
    return !(arg1.equalsIgnoreCase(arg0));
}).distinct().toList();
        }
        return List.of();
    }

    public void execute(String[] arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = mc.player.level().dimension().identifier().toString().replace("minecraft:", "");
        Object var3 = (((int) mc.player.getX()) + " " + ((int) mc.player.getY()) + " " + ((int) mc.player.getZ()) + " (" + var2 + ")");
        if (arg0.length == 0) {
            mc.keyboardHandler.setClipboard(var3);
            LightningClient.CHAT_MANAGER.tagged(("Copied your position (" + String.valueOf(ChatUtils.getPrimary()) + var3 + String.valueOf(ChatUtils.getSecondary()) + ") to the clipboard."), getTag(), getName());
            return;
        }
        if (arg0.length == 1) {
            Object var4 = arg0[0];
            if (mc.getConnection() != null) {
                mc.getConnection().sendCommand(("w " + var4 + " " + var3));
                LightningClient.CHAT_MANAGER.tagged(("Sent your position to " + String.valueOf(ChatUtils.getPrimary()) + var4 + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
            }
            return;
        }
        messageSyntax();
    }

}
