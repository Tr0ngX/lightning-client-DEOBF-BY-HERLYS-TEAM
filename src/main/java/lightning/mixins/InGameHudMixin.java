package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.RenderOverlayEvent;
import lightning.gui.ClickGuiScreen;
import lightning.gui.special.MainMenuScreen;
import lightning.modules.impl.core.MenuModule;
import lightning.modules.impl.miscellaneous.AutoRespawnModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.modules.impl.player.AutoShopSync;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER))
    private void render(DeltaTracker arg0, boolean arg1, boolean arg2, CallbackInfo arg3, GuiGraphicsExtractor arg4) {
        if (this.minecraft.gui.hud.isHidden()) return;
        LightningClient.EVENT_HANDLER.post(new RenderOverlayEvent(arg4, arg0.getGameTimeDeltaPartialTick(true)));
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void night$setScreen(Screen arg0, CallbackInfo arg1) {
        if (LightningClient.MODULE_MANAGER == null) break label_1;
        Object var5 = ((AutoShopModule) LightningClient.MODULE_MANAGER.getModule(AutoShopModule.class));
        if (var5 == null) break label_1;
        Object var6 = AutoShopSync.classify(var5, arg0);
        if (var6 == AutoShopModule.ScreenDecision.SUPPRESS_EXPECTED) {
            arg1.cancel();
            return;
        }
        if (var6 == AutoShopModule.ScreenDecision.FAIL_OPEN_UNEXPECTED) return;
        label_1:
        if ((arg0 instanceof SocialInteractionsScreen)) {
            arg1.cancel();
            return;
        }
        if (arg0 != null) {
            if (!((arg0 instanceof PauseScreen))) break label_4;
        }
        var tmp0 = this.minecraft.gui.screen();
        Object var4 = tmp0;
        if (!((tmp0 instanceof ClickGuiScreen))) break label_4;
        var tmp1 = ((ClickGuiScreen) var4);
        Object var3 = tmp1;
        if (tmp1.isReadyToRemove()) break label_4;
        var3.requestClose();
        arg1.cancel();
        return;
        label_4:
        if ((arg0 instanceof AbstractContainerScreen) && !(arg0 instanceof InventoryScreen) && (System.currentTimeMillis() - WorldUtils.lastInteractablePlaceTime) < 500L) {
            arg1.cancel();
            if (this.minecraft.getConnection() != null && this.minecraft.player != null && this.minecraft.player.containerMenu != null) {
                this.minecraft.getConnection().send(new ServerboundContainerClosePacket(this.minecraft.player.containerMenu.containerId));
            }
            return;
        }
        if (LightningClient.MODULE_MANAGER == null) return;
        if ((!((arg0 instanceof DeathScreen))) || (this.minecraft.player == null)) break label_9;
        var6 = ((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class));
        if (var6 != null && var6.isToggled()) {
            var6.setToggled(false);
        }
        var tmp2 = ((AutoRespawnModule) LightningClient.MODULE_MANAGER.getModule(AutoRespawnModule.class));
        var5 = tmp2;
        if ((tmp2 == null) || (!(var5.isToggled()))) break label_9;
        this.minecraft.player.respawn();
        arg1.cancel();
        return;
        label_9:
        if (!((arg0 instanceof TitleScreen))) break label_10;
        LightningClient.checkForUpdates();
        var5 = ((MenuModule) LightningClient.MODULE_MANAGER.getModule(MenuModule.class));
        if (((var5 == null) || (!(var5.isToggled()))) || (!(var5.mainMenu.getValue()))) break label_10;
        this.minecraft.gui.setScreen(new MainMenuScreen());
        arg1.cancel();
        label_10:
    }

}
