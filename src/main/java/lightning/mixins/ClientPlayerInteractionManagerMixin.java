package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.AttackBlockEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.BreakBlockEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.impl.core.NoMiddleClickModule;
import lightning.modules.impl.movement.InventoryControlModule;
import lightning.modules.impl.player.NoBreakDelayModule;
import lightning.modules.impl.player.NoInteractModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int destroyDelay;
    @Unique
    private boolean night$stoppedSprintForClick;

    public ClientPlayerInteractionManagerMixin() {
        this.night$stoppedSprintForClick = false;
    }

    @Inject(method = "handleContainerInput", at = @At("HEAD"))
    private void beforeContainerClick(int arg0, int arg1, int arg2, ContainerInput arg3, Player arg4, CallbackInfo arg5) {
        this.night$stoppedSprintForClick = false;
        Object var7 = ((InventoryControlModule) LightningClient.MODULE_MANAGER.getModule(InventoryControlModule.class));
        if ((!(var7.isToggled())) || (!(var7.grimV2.getValue()))) {
            return;
        }
        if ((this.minecraft.player == null) || (this.minecraft.getConnection() == null)) {
            return;
        }
        Object var8 = this.minecraft.player.input.keyPresses;
        if (!(var8.forward()) && !(var8.backward()) && !(var8.left()) && !(var8.right())) {
            if (var8.jump()) {
            }
            Object var9 = new Input(false, false, false, false, false, var8.shift(), var8.sprint());
            this.minecraft.getConnection().send(new ServerboundPlayerInputPacket(var9));
            ((ClientPlayerEntityAccessor) this.minecraft.player).setLastSentInput(var9);
        }
        if (this.minecraft.player.isSprinting()) {
            this.minecraft.getConnection().send(new ServerboundPlayerCommandPacket(((Entity) this.minecraft.player), ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
            this.night$stoppedSprintForClick = true;
        }
    }

    @Inject(method = "handleContainerInput", at = @At("TAIL"))
    private void afterContainerClick(int arg0, int arg1, int arg2, ContainerInput arg3, Player arg4, CallbackInfo arg5) {
        if (!this.night$stoppedSprintForClick) return;
        this.night$stoppedSprintForClick = false;
        if ((this.minecraft.player == null) || (this.minecraft.getConnection() == null)) {
            return;
        }
        this.minecraft.getConnection().send(new ServerboundPlayerCommandPacket(((Entity) this.minecraft.player), ServerboundPlayerCommandPacket.Action.START_SPRINTING));
    }

    @Inject(method = "ensureHasSentCarriedItem", at = @At("HEAD"), cancellable = true)
    private void syncSelectedSlot(CallbackInfo arg0) {
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) {
            arg0.cancel();
        }
    }

    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void stopUsingItem(CallbackInfo arg0) {
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) {
            arg0.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((NoBreakDelayModule) LightningClient.MODULE_MANAGER.getModule(NoBreakDelayModule.class));
        Object var2 = tmp0;
        if (((tmp0 == null) || (!(var2.isToggled()))) || (this.destroyDelay <= var2.delay.getValue().intValue())) break label_0;
        this.destroyDelay = var2.delay.getValue().intValue();
        label_0:
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void onContinueDestroyBlock(BlockPos arg0, Direction arg1, CallbackInfoReturnable arg2) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((NoBreakDelayModule) LightningClient.MODULE_MANAGER.getModule(NoBreakDelayModule.class));
        Object var4 = tmp0;
        if (((tmp0 == null) || (!(var4.isToggled()))) || (this.destroyDelay <= var4.delay.getValue().intValue())) break label_0;
        this.destroyDelay = var4.delay.getValue().intValue();
        label_0:
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void attackBlock(BlockPos arg0, Direction arg1, CallbackInfoReturnable arg2) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((NoBreakDelayModule) LightningClient.MODULE_MANAGER.getModule(NoBreakDelayModule.class));
        Object var4 = tmp0;
        if (((tmp0 == null) || (!(var4.isToggled()))) || (this.destroyDelay <= var4.delay.getValue().intValue())) break label_0;
        this.destroyDelay = var4.delay.getValue().intValue();
        label_0:
        Object var5 = new AttackBlockEvent(arg0, arg1);
        LightningClient.EVENT_HANDLER.post(var5);
        if (var5.isCancelled()) {
            arg2.setReturnValue(false);
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void interactBlock(LocalPlayer arg0, InteractionHand arg1, BlockHitResult arg2, CallbackInfoReturnable arg3) {
        Object var5 = ((NoInteractModule) LightningClient.MODULE_MANAGER.getModule(NoInteractModule.class));
        if (var5.isToggled() && var5.shouldNoInteract() && var5.mode.getValue().equalsIgnoreCase("Disable") && WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(this.minecraft.level.getBlockState(arg2.getBlockPos()).getBlock())) {
            arg3.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"))
    private void interactBlock$BEFORE(LocalPlayer arg0, InteractionHand arg1, BlockHitResult arg2, CallbackInfoReturnable arg3) {
        Object var5 = ((NoInteractModule) LightningClient.MODULE_MANAGER.getModule(NoInteractModule.class));
        if (!(this.minecraft.player.isShiftKeyDown()) && var5.isToggled() && var5.shouldNoInteract() && var5.mode.getValue().equalsIgnoreCase("Sneak") && WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(this.minecraft.level.getBlockState(arg2.getBlockPos()).getBlock())) {
            this.minecraft.player.connection.send(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, true, false)));
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", shift = At.Shift.AFTER))
    private void interactBlock$AFTER(LocalPlayer arg0, InteractionHand arg1, BlockHitResult arg2, CallbackInfoReturnable arg3) {
        Object var5 = ((NoInteractModule) LightningClient.MODULE_MANAGER.getModule(NoInteractModule.class));
        if (!(this.minecraft.player.isShiftKeyDown()) && var5.isToggled() && var5.shouldNoInteract() && var5.mode.getValue().equalsIgnoreCase("Sneak") && WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(this.minecraft.level.getBlockState(arg2.getBlockPos()).getBlock())) {
            this.minecraft.player.connection.send(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, false, false)));
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void attackEntity(Player arg0, Entity arg1, CallbackInfo arg2) {
        LightningClient.EVENT_HANDLER.post(new AttackEntityEvent(arg0, arg1));
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void breakBlock(BlockPos arg0, CallbackInfoReturnable arg1) {
        LightningClient.EVENT_HANDLER.post(new BreakBlockEvent(arg0));
    }

    @Inject(method = "destroyBlock", at = @At("TAIL"))
    private void afterBreakBlock(BlockPos arg0, CallbackInfoReturnable arg1) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((NoBreakDelayModule) LightningClient.MODULE_MANAGER.getModule(NoBreakDelayModule.class));
        Object var3 = tmp0;
        if (((tmp0 == null) || (!(var3.isToggled()))) || (this.destroyDelay <= var3.delay.getValue().intValue())) break label_0;
        this.destroyDelay = var3.delay.getValue().intValue();
        label_0:
    }

    @Inject(method = "handlePickItemFromBlock", at = @At("HEAD"), cancellable = true)
    private void onPickBlock(BlockPos arg0, boolean arg1, CallbackInfo arg2) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((NoMiddleClickModule) LightningClient.MODULE_MANAGER.getModule(NoMiddleClickModule.class));
        Object var4 = tmp0;
        if ((tmp0 == null) || (!(var4.isToggled()))) break label_0;
        arg2.cancel();
        label_0:
    }

    @Inject(method = "handlePickItemFromEntity", at = @At("HEAD"), cancellable = true)
    private void onPickEntity(Entity arg0, boolean arg1, CallbackInfo arg2) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((NoMiddleClickModule) LightningClient.MODULE_MANAGER.getModule(NoMiddleClickModule.class));
        Object var4 = tmp0;
        if ((tmp0 == null) || (!(var4.isToggled()))) break label_0;
        arg2.cancel();
        label_0:
    }

}
