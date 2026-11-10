package lightning.modules.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.mixins.accessors.CreativeInventoryScreenAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;

@RegisterModule(name = "ArrowControls", description = "Allows you to control the camera using your arrow keys.", category = Module.Category.MOVEMENT)
public class ArrowControlsModule extends Module {
    public NumberSetting speed;
    public BooleanSetting inventory;
    private long lastTime;

    public ArrowControlsModule() {
        this.speed = new NumberSetting("Speed", "The speed at which the camera will be moving at.", 1.0f, 0.1f, 10.0f);
        this.inventory = new BooleanSetting("Inventory", "Allows you to control the camera when a screen is opened.", true);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (mc.gui.screen() != null) {
            if ((((((((!(this.inventory.getValue())) || ((mc.gui.screen() instanceof ChatScreen))) || ((mc.gui.screen() instanceof BookEditScreen))) || ((mc.gui.screen() instanceof SignEditScreen))) || ((mc.gui.screen() instanceof JigsawBlockEditScreen))) || ((mc.gui.screen() instanceof StructureBlockEditScreen))) || ((mc.gui.screen() instanceof AnvilScreen))) || ((mc.gui.screen() instanceof CreativeModeInventoryScreen) && CreativeInventoryScreenAccessor.getSelectedTab().getType() == CreativeModeTab.Type.SEARCH)) {
                return;
            }
        }
        float var2 = 0.0f;
        float var3 = 0.0f;
        float var4 = ((((float) (System.currentTimeMillis() - this.lastTime)) / 10.0f) * this.speed.getValue().floatValue());
        this.lastTime = System.currentTimeMillis();
        if (InputConstants.isKeyDown(mc.getWindow(), 263)) {
            var2 = (var2 - var4);
        }
        if (InputConstants.isKeyDown(mc.getWindow(), 262)) {
            var2 = (var2 + var4);
        }
        if (InputConstants.isKeyDown(mc.getWindow(), 265)) {
            var3 = (var3 - var4);
        }
        if (InputConstants.isKeyDown(mc.getWindow(), 264)) {
            var3 = (var3 + var4);
        }
        mc.player.setYRot((mc.player.getYRot() + var2));
        mc.player.setXRot(Mth.clamp((mc.player.getXRot() + var3), -90.0f, 90.0f));
    }

    public String getMetaData() {
        return String.valueOf(this.speed.getValue().floatValue());
    }

}
