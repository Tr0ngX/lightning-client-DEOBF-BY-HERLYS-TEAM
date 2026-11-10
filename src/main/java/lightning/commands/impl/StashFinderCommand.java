package lightning.commands.impl;

import java.util.List;
import java.util.Map;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.impl.visuals.StashFinderModule;
import lightning.modules.impl.visuals.stashfinder.StashWebhook;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name = "stashfinder", aliases = "stash", tag = "StashFinder", description = "Configures StashFinder's Discord webhook and controls scan state.", syntax = "<webhook <url> | userid <id> | test | reset>")
public class StashFinderCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) return List.of("webhook", "userid", "test", "reset");
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length == 0) {
            messageSyntax();
            return;
        }
        Object var2 = arg0[0].toLowerCase();
        int var3 = -1;
        switch (var2.hashCode()) {
            case -836029914 -> { break label_2; }
            case 3556498 -> { break label_3; }
            case 108404047 -> { break label_4; }
            case 1224013431 -> { break label_1; }
            default -> { break label_5; }
        }
        label_1:
        if (!(var2.equals("webhook"))) break label_5;
        var3 = 0;
        break label_5;
        label_2:
        if (!(var2.equals("userid"))) break label_5;
        var3 = 1;
        break label_5;
        label_3:
        if (!(var2.equals("test"))) break label_5;
        var3 = 2;
        break label_5;
        label_4:
        if (!(var2.equals("reset"))) break label_5;
        var3 = 3;
        label_5:
        switch (var3) {
            case 0 -> { break label_6; }
            case 1 -> { break label_11; }
            case 2 -> { break label_16; }
            case 3 -> { break label_22; }
            default -> { break label_24; }
        }
        label_6:
        if (arg0.length < 2) {
            if (StashWebhook.getWebhookUrl().isBlank()) {
            } else {
            }
            String.valueOf(ChatUtils.getPrimary()).tagged(("Current webhook: " + "None" + StashWebhook.getWebhookUrl()), getTag());
            return;
        }
        Object var4 = arg0[1];
        StashWebhook.setWebhookUrl(var4);
        Object var5 = ((StashFinderModule) LightningClient.MODULE_MANAGER.getModule(StashFinderModule.class));
        if (var5 != null) {
            var5.webhookUrl.setValue(var4);
        }
        LightningClient.CHAT_MANAGER.tagged("Webhook URL updated.", getTag());
        break label_25;
        label_11:
        if (arg0.length < 2) {
            if (StashWebhook.getUserId().isBlank()) {
            } else {
            }
            String.valueOf(ChatUtils.getPrimary()).tagged(("Current User ID: " + "None" + StashWebhook.getUserId()), getTag());
            return;
        }
        var4 = arg0[1];
        StashWebhook.setUserId(var4);
        var5 = ((StashFinderModule) LightningClient.MODULE_MANAGER.getModule(StashFinderModule.class));
        if (var5 != null) {
            var5.userId.setValue(var4);
        }
        LightningClient.CHAT_MANAGER.tagged("User ID updated (will be pinged on stash finds).", getTag());
        break label_25;
        label_16:
        var5 = ((StashFinderModule) LightningClient.MODULE_MANAGER.getModule(StashFinderModule.class));
        if (var5 != null && !(var5.webhookUrl.getValue().isBlank())) {
        } else {
        }
        Object var6 = StashWebhook.getWebhookUrl();
        if (var5 != null && !(var5.userId.getValue().isBlank())) {
        } else {
        }
        var tmp0 = StashWebhook.getUserId();
        var4 = tmp0;
        Object var7 = tmp0;
        if (var6.isBlank()) {
            LightningClient.CHAT_MANAGER.tagged(("No webhook URL configured. Use " + String.valueOf(ChatUtils.getPrimary()) + ".stashfinder webhook <url>" + String.valueOf(ChatUtils.getSecondary()) + " first."), getTag());
            break label_25;
        }
        StashWebhook.send(0, 0, "Test Dimension", Map.of("Chests", 25, "Shulkers", 8), var6, var4);
        LightningClient.CHAT_MANAGER.tagged("Test webhook dispatched.", getTag());
        break label_25;
        label_22:
        var4 = ((StashFinderModule) LightningClient.MODULE_MANAGER.getModule(StashFinderModule.class));
        if (var4 == null) {
            break label_25;
        }
        var4.resetCurrentWorldStore();
        LightningClient.CHAT_MANAGER.tagged("Scan history cleared for this world. All chunks will be re-evaluated.", getTag());
        break label_25;
        label_24:
        messageSyntax();
        label_25:
    }

}
