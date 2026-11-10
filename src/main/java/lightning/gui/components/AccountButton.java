package lightning.gui.components;

import lightning.gui.screens.account.AccountManagerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AccountButton {
    public static Button create(int arg0, int arg1, int arg2, int arg3, Screen arg4) {
        return Button.builder(Component.literal("Accounts"), (arg1) -> {
    if (Minecraft.getInstance() != null) {
        Minecraft.getInstance().gui.setScreen(new AccountManagerScreen(arg0));
    }
}).bounds(arg0, arg1, arg2, arg3).build();
    }

}
