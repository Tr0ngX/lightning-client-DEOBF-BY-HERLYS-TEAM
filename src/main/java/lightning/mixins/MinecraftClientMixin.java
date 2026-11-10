package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lightning.LightningClient;
import lightning.events.impl.GameLoopEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.combat.MainhandModule;
import lightning.modules.impl.core.NoMiddleClickModule;
import lightning.modules.impl.miscellaneous.AutoEscapeModule;
import lightning.modules.impl.player.FastPlaceModule;
import lightning.modules.impl.player.MultiTaskModule;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements IMinecraft {
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    private int rightClickDelay;
    @Shadow
    @Final
    public Options options;
    @Shadow
    @Nullable
    public HitResult hitResult;
    @Shadow
    @Nullable
    public Entity crosshairPickEntity;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(GameConfig arg0, CallbackInfo arg1) {
        LightningClient.onPostInitialize();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runAllTasks()V", shift = At.Shift.AFTER))
    private void runTickHook(boolean arg0, CallbackInfo arg1) {
        LightningClient.EVENT_HANDLER.post(new GameLoopEvent());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo arg0) {
        LightningClient.EVENT_HANDLER.post(new TickEvent());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick$post(CallbackInfo arg0) {
        LightningClient.EVENT_HANDLER.post(new TickEvent.Post());
    }

    @Inject(method = "startUseItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift = At.Shift.AFTER))
    private void doItemUse(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER != null && ((FastPlaceModule) LightningClient.MODULE_MANAGER.getModule(FastPlaceModule.class)).isToggled() && ((FastPlaceModule) LightningClient.MODULE_MANAGER.getModule(FastPlaceModule.class)).isValidItem(this.player.getMainHandItem().getItem())) {
            this.rightClickDelay = ((FastPlaceModule) LightningClient.MODULE_MANAGER.getModule(FastPlaceModule.class)).ticks.getValue().intValue();
        }
    }

    @ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean handleBlockBreaking(boolean arg0) {
        if (LightningClient.MODULE_MANAGER != null && ((MultiTaskModule) LightningClient.MODULE_MANAGER.getModule(MultiTaskModule.class)).isToggled()) return false;
        return arg0;
    }

    @ModifyExpressionValue(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"))
    private boolean handleInputEvents(boolean arg0) {
        if (LightningClient.MODULE_MANAGER != null && ((MultiTaskModule) LightningClient.MODULE_MANAGER.getModule(MultiTaskModule.class)).isToggled()) return false;
        return arg0;
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
    private void night$dontReleaseFakedUse(MultiPlayerGameMode arg0, Player arg1) {
        if (((AutoEscapeModule) LightningClient.MODULE_MANAGER.getModule(AutoEscapeModule.class)).isEating()) return;
        arg0.releaseUsingItem(arg1);
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
    private void night$redirectSetSelectedSlot(Inventory arg0, int arg1) {
        if (LightningClient.MODULE_MANAGER != null) {
            var tmp0 = ((MainhandModule) LightningClient.MODULE_MANAGER.getModule(MainhandModule.class));
            Object var3 = tmp0;
            if (tmp0 != null && var3.shouldLockSlot()) return;
        }
        arg0.setSelectedSlot(arg1);
    }

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void pick(float arg0, CallbackInfo arg1) {
        if ((mc.level == null) || (this.player == null)) {
            return;
        }
        Object var3 = ((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam"));
        if (var3.isToggled()) {
            this.hitResult = WorldUtils.getRaytraceTarget(var3.getFreeYaw(), var3.getFreePitch(), var3.getFreeX(), var3.getFreeY(), var3.getFreeZ());
            Object var5 = this.hitResult;
            if ((var5 instanceof EntityHitResult)) {
                Object var6 = ((EntityHitResult) var5);
                Object var4 = var6.getEntity();
            } else {
                var4 = null;
            }
            this.crosshairPickEntity = var4;
            arg1.cancel();
        }
    }

    @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true, require = 1)
    private void night$onPickBlock(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((NoMiddleClickModule) LightningClient.MODULE_MANAGER.getModule(NoMiddleClickModule.class));
        Object var2 = tmp0;
        if ((tmp0 == null) || (!(var2.isToggled()))) break label_0;
        arg0.cancel();
        label_0:
    }

}
