package lightning.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import lightning.LightningClient;
import lightning.managers.ChatManager;
import lightning.modules.impl.core.HUDModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.graphics.HotbarCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
    private static final Identifier POWDER_SNOW_OUTLINE_LOCATION;

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void renderStatusEffectOverlay(GuiGraphicsExtractor arg0, DeltaTracker arg1, CallbackInfo arg2) {
        if (LightningClient.MODULE_MANAGER != null && ((HUDModule) LightningClient.MODULE_MANAGER.getModule(HUDModule.class)).isToggled() && ((HUDModule) LightningClient.MODULE_MANAGER.getModule(HUDModule.class)).vanillaPotions.getValue().equalsIgnoreCase("Hide")) {
            arg2.cancel();
        }
    }

    @Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPortalOverlay(GuiGraphicsExtractor arg0, float arg1, CallbackInfo arg2) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).portalOverlay.getValue()) {
            arg2.cancel();
        }
    }

    @Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
    private void renderVignetteOverlay(GuiGraphicsExtractor arg0, Entity arg1, CallbackInfo arg2) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).vignette.getValue()) {
            arg2.cancel();
        }
    }

    @Inject(method = "extractSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void renderSelectedItemName(GuiGraphicsExtractor arg0, CallbackInfo arg1) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).itemName.getValue()) {
            arg1.cancel();
        }
    }

    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void renderScoreboardSidebar(GuiGraphicsExtractor arg0, DeltaTracker arg1, CallbackInfo arg2) {
        if (LightningClient.MODULE_MANAGER != null && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).scoreboard.getValue()) {
            arg2.cancel();
        }
    }

    @WrapWithCondition(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V"))
    private boolean renderTextureOverlay(Hud arg0, GuiGraphicsExtractor arg1, Identifier arg2, float arg3) {
        if (arg2.equals(POWDER_SNOW_OUTLINE_LOCATION)) {
            if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled()) {
                if (!(((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).snowOverlay.getValue())) {
                }
            } else {
            }
            return false;
        }
        if (arg2.getPath().contains("pumpkin")) {
            if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled()) {
                if (!(((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).pumpkinOverlay.getValue())) {
                }
            } else {
            }
            return false;
        }
        return true;
    }

    @Inject(method = "extractChat", at = @At("HEAD"))
    private void night$onExtractChatHead(GuiGraphicsExtractor arg0, DeltaTracker arg1, CallbackInfo arg2) {
        ChatManager.isInsideHudExtractChat = true;
    }

    @Inject(method = "extractChat", at = @At("RETURN"))
    private void night$onExtractChatReturn(GuiGraphicsExtractor arg0, DeltaTracker arg1, CallbackInfo arg2) {
        ChatManager.isInsideHudExtractChat = false;
    }

    @Redirect(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack night$grimHotbarVisual(Inventory arg0, int arg1) {
        Object var3 = arg0.getItem(arg1);
        if (LightningClient.MODULE_MANAGER == null) return var3;
        Object var4 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var4.getGrimParkedSlot() == arg1) return var4.getGrimParkedDisplaced();
        return var3;
    }

    @Redirect(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void night$extractSlotCached(GuiGraphicsExtractor arg0, LivingEntity arg1, ItemStack arg2, int arg3, int arg4, int arg5) {
        HotbarCache.extractItem(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Inject(method = "clearCache", at = @At("HEAD"))
    private void night$onClearCache(CallbackInfo arg0) {
        HotbarCache.clear();
    }

    @Inject(method = "onDisconnected", at = @At("HEAD"))
    private void night$onDisconnected(CallbackInfo arg0) {
        HotbarCache.clear();
    }

    static {
        POWDER_SNOW_OUTLINE_LOCATION = Identifier.withDefaultNamespace("textures/misc/powder_snow_outline.png");
    }

}
