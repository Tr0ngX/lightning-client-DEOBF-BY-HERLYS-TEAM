package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;

@RegisterCommand(name = "itemdrop", aliases = "drop", tag = "ItemDrop", description = "Drops items from your inventory.", syntax = "<all|item name>")
public class ItemDropCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) {
            Object var2 = new ArrayList(List.of("all"));
            Object var3 = BuiltInRegistries.ITEM.iterator();
            while (var3.hasNext()) {
                Object var4 = ((Item) var3.next());
                var2.add(BuiltInRegistries.ITEM.getKey(var4).getPath());
            }
            return var2;
        }
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length == 0) {
            messageSyntax();
            return;
        }
        if (arg0[0].equalsIgnoreCase("all")) {
            dropAllItems();
            return;
        }
        Object var2 = String.join(" ", arg0);
        dropSpecificItem(var2);
    }

    private void dropAllItems() {
        if ((mc.player == null) || (mc.gameMode == null)) {
            return;
        }
        int var1 = mc.player.containerMenu.containerId;
        int var2 = 0;
        while (var2 < 36) {
            Object var3 = mc.player.getInventory().getItem(var2);
            if (var3.isEmpty()) {
            } else {
                mc.gameMode.handleContainerInput(var1, invToHandlerSlot(var2), 1, ContainerInput.THROW, mc.player);
            }
            var2++;
        }
    }

    private void dropSpecificItem(String arg0) {
        if ((mc.player == null) || (mc.gameMode == null)) {
            return;
        }
        int var2 = mc.player.containerMenu.containerId;
        int var3 = 0;
        while (var3 < 36) {
            Object var4 = mc.player.getInventory().getItem(var3);
            if (var4.isEmpty()) {
                break label_5;
            }
            Object var5 = BuiltInRegistries.ITEM.getKey(var4.getItem()).toString();
            Object var6 = var4.getHoverName().getString();
            if (!(var5.equalsIgnoreCase(arg0)) && var5.substring((var5.indexOf(58) + 1)).equalsIgnoreCase(arg0) == 0 && !(var6.equalsIgnoreCase(arg0))) {
                break label_5;
            }
            mc.gameMode.handleContainerInput(var2, invToHandlerSlot(var3), 1, ContainerInput.THROW, mc.player);
            label_5:
            var3++;
        }
    }

    private static int invToHandlerSlot(int arg0) {
        if (arg0 <= 8) {
        } else {
        }
        return arg0;
    }

}
