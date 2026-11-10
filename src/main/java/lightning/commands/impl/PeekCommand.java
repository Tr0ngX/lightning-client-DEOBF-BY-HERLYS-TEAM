package lightning.commands.impl;

import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.gui.PeekMenu;
import lightning.gui.PeekScreen;
import lightning.utils.minecraft.EChestMemory;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

@RegisterCommand(name = "peek", tag = "Peek", description = "Peeks a shulker box held in hand, or your last-opened ender chest, without opening it.", syntax = "")
public class PeekCommand extends Command {
    public List getSuggestions(String[] arg0) {
        return List.of();
    }

    public void execute(String[] arg0) {
        if (mc.player == null) return;
        EChestMemory.init();
        Object var2 = mc.player.getMainHandItem();
        Object var3 = ((ItemContainerContents) var2.get(DataComponents.CONTAINER));
        if (var3 == null) {
            var2 = mc.player.getOffhandItem();
            var3 = ((ItemContainerContents) var2.get(DataComponents.CONTAINER));
        }
        if (var3 != null) {
            Object var4 = NonNullList.withSize(27, ItemStack.EMPTY);
            var3.copyInto(var4);
            openPreview(var4, var2.getHoverName());
            return;
        }
        if (EChestMemory.hasItems()) {
            openPreview(EChestMemory.getItems(), Component.literal("Ender Chest (remembered)"));
            return;
        }
        LightningClient.CHAT_MANAGER.tagged("Hold a shulker box to peek it, or open your ender chest once so it can be remembered.", getTag(), getName());
    }

    private void openPreview(NonNullList arg0, Component arg1) {
        Object var3 = new SimpleContainer(27);
        int var4 = 0;
        while (var4 < 27 && var4 < arg0.size()) {
            var3.setItem(var4, ((ItemStack) arg0.get(var4)).copy());
            var4++;
        }
        mc.execute(() -> {
    Object var2 = new PeekMenu(0, mc.player.getInventory(), arg0);
    mc.player.containerMenu = var2;
    mc.gui.setScreen(new PeekScreen(var2, mc.player.getInventory(), arg1));
});
    }

}
