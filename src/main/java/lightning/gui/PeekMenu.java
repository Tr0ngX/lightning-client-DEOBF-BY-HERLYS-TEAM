package lightning.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class PeekMenu extends ChestMenu {
    public PeekMenu(int arg0, Inventory arg1, Container arg2) {
        super(MenuType.GENERIC_9x3, arg0, arg1, arg2, 3);
    }

    public void clicked(int arg0, int arg1, ContainerInput arg2, Player arg3) {
    }

    public ItemStack quickMoveStack(Player arg0, int arg1) {
        return ItemStack.EMPTY;
    }

}
