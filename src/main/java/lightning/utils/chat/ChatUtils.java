package lightning.utils.chat;

import lightning.LightningClient;
import lightning.modules.impl.core.CommandsModule;
import lightning.utils.text.FormattingUtils;

public class ChatUtils {
    public static Object getPrimary() {
        return FormattingUtils.getFormatting(((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).primaryMessageColor.getValue());
    }

    public static Object getSecondary() {
        return FormattingUtils.getFormatting(((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).secondaryMessageColor.getValue());
    }

}
