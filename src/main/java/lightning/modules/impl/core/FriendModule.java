package lightning.modules.impl.core;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.chat.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

@RegisterModule(name = "Friend", description = "Allows you to manage the client's friend system.", category = Module.Category.CORE, persistent = true, drawn = false)
public class FriendModule extends Module {
    public BooleanSetting middleClick;
    public BooleanSetting friendlyFire;
    public BooleanSetting friendMessage;
    public StringSetting content;

    public FriendModule() {
        this.middleClick = new BooleanSetting("MiddleClick", "Adds whichever entity you middle click to your friends list.", true);
        this.friendlyFire = new BooleanSetting("FriendlyFire", "Disables the friend system and allows the attacking of friends.", false);
        this.friendMessage = new BooleanSetting("FriendMessage", "Sends a message to a player whenever you add them to your friends list.", false);
        this.content = new StringSetting("Content", "The message that will be sent to the people you add. $player is placeholder for their name.", new BooleanSetting.Visibility(this.friendMessage, true), "/msg $player I have just added you to my friends list!");
    }

    public void sendFriendMessage(String arg0) {
        if ((mc.level == null) || (mc.player == null)) {
            return;
        }
        if (!(this.friendMessage.getValue())) return;
        if (mc.getConnection().getOnlinePlayers().stream().noneMatch((arg1) -> arg1.getProfile().name().equalsIgnoreCase(arg0))) return;
        if (this.content.getValue().startsWith("/")) {
            mc.getConnection().sendCommand(this.content.getValue().substring(1).replace("$player", arg0));
        } else {
            mc.getConnection().sendChat(this.content.getValue().replace("$player", arg0));
        }
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent arg0) {
        if (arg0.getSetting().equals(this.friendlyFire)) {
            if (this.friendlyFire.getValue()) {
            } else {
            }
            String.valueOf(ChatUtils.getPrimary()).warn((String.valueOf(ChatUtils.getSecondary()) + "Friendly fire" + (String.valueOf(ChatFormatting.GREEN) + "on" + String.valueOf(ChatFormatting.RESET) + ". Friends will now be attacked.") + " has been turned " + (String.valueOf(ChatFormatting.RED) + "off" + String.valueOf(ChatFormatting.RESET) + ". Friends will now no longer be attacked.")));
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent arg0) {
        if ((!(this.middleClick.getValue())) || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }
        if (arg0.getButton() != 2) return;
        if (mc.crosshairPickEntity == null) return;
        Object var2 = mc.crosshairPickEntity;
        if (!((var2 instanceof Player))) return;
        Object var3 = ((Player) var2);
        Object var4 = var3.getGameProfile().name();
        if (LightningClient.FRIEND_MANAGER.contains(var4)) {
            LightningClient.FRIEND_MANAGER.remove(var4);
            LightningClient.CHAT_MANAGER.tagged(("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + var4 + String.valueOf(ChatUtils.getSecondary()) + " from your friends list."), "MCF", getName());
        } else {
            LightningClient.FRIEND_MANAGER.add(var4);
            LightningClient.CHAT_MANAGER.tagged(("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + var4 + String.valueOf(ChatUtils.getSecondary()) + " to your friends list."), "MCF", getName());
        }
    }

}
