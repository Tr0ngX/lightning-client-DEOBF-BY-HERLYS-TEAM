package lightning.modules.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.CreativeInventoryScreenAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.world.item.CreativeModeTab;

@RegisterModule(name = "InventoryControl", description = "Allows you to control things such as movement and your camera while a GUI is open.", category = Module.Category.MOVEMENT)
public class InventoryControlModule extends Module {
    public BooleanSetting movement;
    public BooleanSetting portals;
    public BooleanSetting dragClick;
    public BooleanSetting grimV2;

    public InventoryControlModule() {
        this.movement = new BooleanSetting("Movement", "Allows you to control movement.", true);
        this.portals = new BooleanSetting("Portals", "Allows you to interact with GUIs while inside of a portal.", true);
        this.dragClick = new BooleanSetting("DragClick", "Hold shift + left-click and drag over slots to move each item you pass over.", true);
        this.grimV2 = new BooleanSetting("GrimV2", "Bypasses inventory-while-moving checks on GrimV2/NCP servers.", false);
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.player == null) return;
        if ((((((((!(this.movement.getValue())) || (mc.gui.screen() == null)) || ((mc.gui.screen() instanceof ChatScreen))) || ((mc.gui.screen() instanceof BookEditScreen))) || ((mc.gui.screen() instanceof SignEditScreen))) || ((mc.gui.screen() instanceof JigsawBlockEditScreen))) || ((mc.gui.screen() instanceof StructureBlockEditScreen))) || ((mc.gui.screen() instanceof AnvilScreen))) break label_3;
        if ((mc.gui.screen() instanceof CreativeModeInventoryScreen)) {
            if (CreativeInventoryScreenAccessor.getSelectedTab().getType() == CreativeModeTab.Type.SEARCH) break label_3;
        }
        var tmp0 = new KeyMapping[7];
        tmp0[0] = mc.options.keyUp;
        tmp0[1] = mc.options.keyDown;
        tmp0[2] = mc.options.keyRight;
        tmp0[3] = mc.options.keyLeft;
        tmp0[4] = mc.options.keySprint;
        tmp0[5] = mc.options.keyShift;
        tmp0[6] = mc.options.keyJump;
        Object var2 = tmp0;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            var5.setDown(InputConstants.isKeyDown(mc.getWindow(), InputConstants.getKey(var5.saveString()).getValue()));
            var4++;
        }
        label_3:
    }

}
