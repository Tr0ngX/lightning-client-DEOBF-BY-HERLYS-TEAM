package lightning.commands.impl;

import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.impl.core.FriendModule;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name = "friend", tag = "Friend", description = "Allows you to manage the client's friend list.", syntax = "<add|del> <[player]> | <clear|list>", aliases = {"f", "friends"})
public class FriendCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) return List.of("add", "del", "clear", "list");
        if (arg0.length == 1 && arg0[0].equalsIgnoreCase("del")) return LightningClient.FRIEND_MANAGER.getFriends();
        if (arg0.length == 1 && arg0[0].equalsIgnoreCase("add") && mc.level != null && mc.player != null) return mc.level.players().stream().filter((arg0) -> arg0 != mc.player).map((arg0) -> arg0.getName().getString()).filter((arg0) -> !(LightningClient.FRIEND_MANAGER.contains(arg0))).toList();
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length == 2) {
            Object var2 = arg0[0].toLowerCase();
            int var3 = -1;
            switch (var2.hashCode()) {
                case 96417 -> { break label_0; }
                case 99339 -> { break label_1; }
                default -> { break label_2; }
            }
            label_0:
            if (!(var2.equals("add"))) break label_2;
            var3 = 0;
            break label_2;
            label_1:
            if (!(var2.equals("del"))) break label_2;
            var3 = 1;
            label_2:
            switch (var3) {
                case 0 -> { break label_3; }
                case 1 -> { break label_6; }
                default -> { break label_8; }
            }
            label_3:
            if (!(LightningClient.FRIEND_MANAGER.contains(arg0[1]))) {
                if (((FriendModule) LightningClient.MODULE_MANAGER.getModule(FriendModule.class)).friendMessage.getValue()) {
                    LightningClient.FRIEND_MANAGER.sendFriendMessage(arg0[1]);
                }
                LightningClient.FRIEND_MANAGER.add(arg0[1]);
                LightningClient.CHAT_MANAGER.tagged(("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + arg0[1] + String.valueOf(ChatUtils.getSecondary()) + " to your friends list."), getTag(), getName());
                break label_9;
            }
            LightningClient.CHAT_MANAGER.tagged((String.valueOf(ChatUtils.getPrimary()) + arg0[1] + String.valueOf(ChatUtils.getSecondary()) + " is already on your friends list."), getTag(), getName());
            break label_9;
            label_6:
            if (LightningClient.FRIEND_MANAGER.contains(arg0[1])) {
                LightningClient.FRIEND_MANAGER.remove(arg0[1]);
                LightningClient.CHAT_MANAGER.tagged(("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + arg0[1] + String.valueOf(ChatUtils.getSecondary()) + " from your friends list."), getTag(), getName());
                break label_9;
            }
            LightningClient.CHAT_MANAGER.tagged((String.valueOf(ChatUtils.getPrimary()) + arg0[1] + String.valueOf(ChatUtils.getSecondary()) + " is not on your friends list."), getTag(), getName());
            break label_9;
            label_8:
            messageSyntax();
            label_9:
            break label_24;
        }
        if (arg0.length == 1) {
            var2 = arg0[0].toLowerCase();
            var3 = -1;
            switch (var2.hashCode()) {
                case 3322014 -> { break label_12; }
                case 94746189 -> { break label_11; }
                default -> { break label_13; }
            }
            label_11:
            if (!(var2.equals("clear"))) break label_13;
            var3 = 0;
            break label_13;
            label_12:
            if (!(var2.equals("list"))) break label_13;
            var3 = 1;
            label_13:
            switch (var3) {
                case 0 -> { break label_14; }
                case 1 -> { break label_15; }
                default -> { break label_21; }
            }
            label_14:
            LightningClient.FRIEND_MANAGER.clear();
            LightningClient.CHAT_MANAGER.tagged("Successfully cleared your friends list.", getTag(), (getName() + "-list"));
            break label_22;
            label_15:
            Object var4 = LightningClient.FRIEND_MANAGER.getFriends();
            if (var4.isEmpty()) {
                LightningClient.CHAT_MANAGER.tagged("You currently have no friends.", getTag());
                break label_22;
            }
            Object var5 = new StringBuilder();
            int var6 = 0;
            Object var7 = var4.iterator();
            while (var7.hasNext()) {
                Object var8 = ((String) var7.next());
                var6++;
                var5.append(ChatUtils.getSecondary()).append(var8).append((var6 == var4.size() ? "" : ", "));
            }
            LightningClient.CHAT_MANAGER.message(("Friends " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + var4.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var5)), (getName() + "-list"));
            break label_22;
            label_21:
            messageSyntax();
            label_22:
            break label_24;
        }
        messageSyntax();
        label_24:
    }

}
