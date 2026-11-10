package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lightning.LightningClient;
import lightning.modules.impl.player.SwingModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.modules.impl.visuals.ViewModelModule;
import lightning.utils.mixins.HandsRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float offHandHeight;
    @Shadow
    private float oOffHandHeight;
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void night$startHands(float arg0, PoseStack arg1, SubmitNodeCollector arg2, LocalPlayer arg3, int arg4, CallbackInfo arg5) {
        HandsRenderState.renderingHands = true;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void night$tickViewModel(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if ((var2 == null) || (!(var2.isToggled()))) {
            return;
        }
        Object var4 = Minecraft.getInstance();
        if (var4.player == null) return;
        if (!(var2.mainhandSwap.getValue())) {
            this.mainHandHeight = 1.0f;
            this.oMainHandHeight = 1.0f;
            this.mainHandItem = var4.player.getMainHandItem();
        }
        if (!(var2.offhandSwap.getValue())) {
            this.offHandHeight = 1.0f;
            this.oOffHandHeight = 1.0f;
            this.offHandItem = var4.player.getOffhandItem();
        }
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"))
    private void night$applyViewModelTransformHead(AbstractClientPlayer arg0, float arg1, float arg2, InteractionHand arg3, float arg4, ItemStack arg5, float arg6, PoseStack arg7, SubmitNodeCollector arg8, int arg9, CallbackInfo arg10) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var13 = null;
        Object var14 = null;
        if ((var13 == null) || (!(var13.isToggled()))) {
            return;
        }
        arg7.pushPose();
        if (arg3 != InteractionHand.MAIN_HAND) {
            if (var13.mode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        int var12 = 0;
        int var15 = 0;
        if (!(!var12)) {
            arg7.translate(var13.positionMainX.getValue().floatValue(), var13.positionMainY.getValue().floatValue(), var13.positionMainZ.getValue().floatValue());
            arg7.scale(var13.scaleMainX.getValue().floatValue(), var13.scaleMainY.getValue().floatValue(), var13.scaleMainZ.getValue().floatValue());
            arg7.mulPose(Axis.XP.rotationDegrees(var13.rotationMainX.getValue().floatValue()));
            arg7.mulPose(Axis.YP.rotationDegrees(var13.rotationMainY.getValue().floatValue()));
            arg7.mulPose(Axis.ZP.rotationDegrees(var13.rotationMainZ.getValue().floatValue()));
        } else {
            arg7.translate(var13.positionOffX.getValue().floatValue(), var13.positionOffY.getValue().floatValue(), var13.positionOffZ.getValue().floatValue());
            arg7.scale(var13.scaleOffX.getValue().floatValue(), var13.scaleOffY.getValue().floatValue(), var13.scaleOffZ.getValue().floatValue());
            arg7.mulPose(Axis.XP.rotationDegrees(var13.rotationOffX.getValue().floatValue()));
            arg7.mulPose(Axis.YP.rotationDegrees(var13.rotationOffY.getValue().floatValue()));
            arg7.mulPose(Axis.ZP.rotationDegrees(var13.rotationOffZ.getValue().floatValue()));
        }
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"))
    private void night$applyViewModelTransformReturn(AbstractClientPlayer arg0, float arg1, float arg2, InteractionHand arg3, float arg4, ItemStack arg5, float arg6, PoseStack arg7, SubmitNodeCollector arg8, int arg9, CallbackInfo arg10) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var12 = null;
        Object var13 = null;
        if ((var12 == null) || (!(var12.isToggled()))) {
            return;
        }
        arg7.popPose();
    }

    @Inject(method = "applyEatTransform", at = @At("HEAD"), cancellable = true)
    private void night$applyEatTransform(PoseStack arg0, float arg1, HumanoidArm arg2, ItemStack arg3, Player arg4, CallbackInfo arg5) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var7 = null;
        Object var8 = null;
        if ((var7 == null) || (!(var7.isToggled()))) {
            return;
        }
        float var9 = var7.eatingMultiplier.getValue().floatValue();
        if (var9 < 1.0f) {
            float var10 = ((((float) arg4.getUseItemRemainingTicks()) - arg1) + 1.0f);
            float var11 = (var10 / ((float) arg3.getUseDuration(arg4)));
            if (var11 < 0.8f) {
                float var12 = (Mth.abs((Mth.cos(((double) ((var10 / 4.0f) * 3.1415927f))) * 0.1f)) * var9);
                arg0.translate(0.0f, var12, 0.0f);
            }
            var12 = (1.0f - ((float) Math.pow(((double) var11), 27.0)));
            int var13 = (arg2 == HumanoidArm.RIGHT ? 1 : -1);
            arg0.translate(((var12 * 0.6f) * ((float) var13)), (var12 * -0.5f), (var12 * 0.0f));
            arg0.mulPose(Axis.YP.rotationDegrees(((((float) var13) * var12) * 90.0f)));
            arg0.mulPose(Axis.XP.rotationDegrees((var12 * 10.0f)));
            arg0.mulPose(Axis.ZP.rotationDegrees(((((float) var13) * var12) * 30.0f)));
            arg5.cancel();
        }
        if (var7.eatAnimation.getValue()) {
            arg0.translate(var7.eatX.getValue().floatValue(), var7.eatY.getValue().floatValue(), 0.0f);
        }
    }

    @ModifyArg(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"), index = 4)
    private int night$handsOutline(int arg0, ItemStack arg1) {
        return computeHandsOutlineColor(arg0);
    }

    private static int computeHandsOutlineColor(int arg0) {
        Object var1 = Minecraft.getInstance();
        if (var1.player == null) return arg0;
        Object var2 = ((ShadersModule) LightningClient.MODULE_MANAGER.getModule(ShadersModule.class));
        if (var2.isToggled() && var2.hands.getValue()) return var2.getFillColor(var1.player);
        return arg0;
    }

    @Inject(method = "submitHandsWithItems", at = @At("RETURN"))
    private void night$flushHandOutline(float arg0, PoseStack arg1, SubmitNodeCollector arg2, LocalPlayer arg3, int arg4, CallbackInfo arg5) {
        HandsRenderState.renderingHands = false;
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void night$swingArm(float arg0, PoseStack arg1, int arg2, HumanoidArm arg3, CallbackInfo arg4) {
        Object var6 = ((SwingModule) LightningClient.MODULE_MANAGER.getModule(SwingModule.class));
        if (!(var6.isToggled())) return;
        if (var6.translateX.getValue()) {
        } else {
        }
        float var7 = 0.0f;
        if (var6.translateY.getValue()) {
        } else {
        }
        float var8 = 0.0f;
        if (var6.translateZ.getValue()) {
        } else {
        }
        float var9 = 0.0f;
        arg1.translate((((float) arg2) * var7), var8, var9);
        night$applyItemArmAttackTransform(arg1, arg3, arg0, var6);
        arg4.cancel();
    }

    private static void night$applyItemArmAttackTransform(PoseStack arg0, HumanoidArm arg1, float arg2, SwingModule arg3) {
        int var4 = (arg1 == HumanoidArm.RIGHT ? 1 : -1);
        if (arg3.rotationY.getValue()) {
        } else {
        }
        float var5 = 0.0f;
        arg0.mulPose(Axis.YP.rotationDegrees((((float) var4) * (45.0f + (var5 * -20.0f)))));
        float var6 = Mth.sin(((double) (Mth.sqrt(arg2) * 3.1415927f)));
        arg0.mulPose(Axis.ZP.rotationDegrees(((((float) var4) * (arg3.rotationZ.getValue() ? var6 : 0.0f)) * -20.0f)));
        arg0.mulPose(Axis.XP.rotationDegrees(((arg3.rotationX.getValue() ? var6 : 0.0f) * -80.0f)));
        arg0.mulPose(Axis.YP.rotationDegrees((((float) var4) * -45.0f)));
    }

}
