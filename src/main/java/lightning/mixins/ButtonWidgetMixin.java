package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.utils.graphics.NeekeriFill;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public abstract class ButtonWidgetMixin {
    @Inject(method = "extractDefaultSprite", at = @At("TAIL"))
    private void night$neekeriFill(GuiGraphicsExtractor arg0, CallbackInfo arg1) {
        Object var3 = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
        if (var3.fillMode.getValue().equalsIgnoreCase("Default")) return;
        Object var4 = ((AbstractButton) this);
        int var5 = Math.round(((var3.neekeriOpacity.getValue().floatValue() / 100.0f) * 255.0f));
        NeekeriFill.fill(arg0, var4.getX(), var4.getY(), var4.getWidth(), var4.getHeight(), var5);
    }

}
