package lightning.mixins;

import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.LightningSkin;
import lightning.mixins.accessors.AbstractContainerScreenAccessor;
import lightning.modules.impl.combat.MainhandModule;
import lightning.modules.impl.core.MenuModule;
import lightning.modules.impl.miscellaneous.ShulkerInfoModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.IMinecraft;
import lightning.utils.color.ColorUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin implements IMinecraft {
    @Shadow
    public int width;
    @Shadow
    public int height;

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void renderBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, CallbackInfo arg4) {
        if (((MenuModule) LightningClient.MODULE_MANAGER.getModule(MenuModule.class)).isToggled() && ((MenuModule) LightningClient.MODULE_MANAGER.getModule(MenuModule.class)).mainMenu.getValue() && mc.level == null) {
            LightningSkin.screenBg(arg0, 0.0f, 0.0f, ((float) this.width), ((float) this.height), new Color(25, 25, 25, 255));
            int var6 = 0;
            while (var6 < this.width) {
                Object var7 = ColorUtils.getRainbow(2L, 0.7f, 1.0f, 255, (((long) var6) * 5L));
                LightningSkin.topLine(arg0, ((float) var6), 0.0f, ((float) (var6 + 1)), 1.0f, var7);
                var6++;
            }
            arg4.cancel();
            return;
        }
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).background.getValue() && ((Screen) this).isInGameUi()) {
            arg4.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void night$hideLegitInventory(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, CallbackInfo arg4) {
        if (!(!MainhandModule.hidingLegitInventory) && (this instanceof InventoryScreen)) {
            arg4.cancel();
        }
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void night$shulkerInfoOnTop(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, CallbackInfo arg4) {
        Object var7 = this;
        if (!((var7 instanceof AbstractContainerScreen))) break label_0;
        Object var6 = ((AbstractContainerScreen) var7);
        var7 = ((ShulkerInfoModule) LightningClient.MODULE_MANAGER.getModule(ShulkerInfoModule.class));
        if ((var7 == null) || (!(var7.isToggled()))) break label_0;
        Object var8 = ((AbstractContainerScreenAccessor) var6).getHoveredSlot();
        if ((((var8 == null) || (var8.getItem().isEmpty())) || (!(var6.getMenu().getCarried().isEmpty()))) || (!(var7.hasItems(var8.getItem())))) break label_0;
        var7.renderInfo(arg0, arg1, arg2, var8.getItem());
        label_0:
    }

}
