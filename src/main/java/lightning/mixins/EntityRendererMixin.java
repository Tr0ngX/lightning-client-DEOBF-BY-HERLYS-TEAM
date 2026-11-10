package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.ChamsModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.NameTagsModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.utils.mixins.IChamsCapture;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "getNameTag", at = @At("HEAD"), cancellable = true)
    private void getDisplayName(Entity arg0, CallbackInfoReturnable arg1) {
        if (!((arg0 instanceof RemotePlayer))) break label_1;
        Object var3 = ((RemotePlayer) arg0);
        Object var4 = ((PopChamsModule) LightningClient.MODULE_MANAGER.getModule(PopChamsModule.class));
        if (var4 != null && var4.isToggled() && var4.isGhost(var3)) {
            arg1.setReturnValue(null);
            return;
        }
        Object var5 = ((LogoutSpotModule) LightningClient.MODULE_MANAGER.getModule(LogoutSpotModule.class));
        if (((var5 == null) || (!(var5.isToggled()))) || (!(var5.isGhost(var3)))) break label_1;
        arg1.setReturnValue(null);
        return;
        label_1:
        if ((arg0 instanceof Player) && ((NameTagsModule) LightningClient.MODULE_MANAGER.getModule(NameTagsModule.class)).isToggled()) {
            arg1.setReturnValue(null);
        }
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void night$limitItemRendering(Entity arg0, Frustum arg1, double arg2, double arg3, double arg4, CallbackInfoReturnable arg5) {
        if (!((arg0 instanceof RemotePlayer))) break label_2;
        Object var12 = ((RemotePlayer) arg0);
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var11 = null;
        Object var13 = null;
        if (((var11 == null) || (!(var11.isToggled()))) || (!(var11.isGhost(var12)))) break label_2;
        arg5.setReturnValue(true);
        return;
        label_2:
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var10 = null;
        var11 = null;
        if ((var10 == null) || (!(var10.isToggled()))) break label_8;
        if (var10.items.getValue() && (arg0 instanceof ItemEntity) && !(var10.shouldRenderItem())) {
            arg5.setReturnValue(false);
        }
        if (var10.displays.getValue() && (arg0 instanceof Display)) {
            arg5.setReturnValue(false);
        }
        if (var10.armorStand.getValue() && (arg0 instanceof ArmorStand)) {
            arg5.setReturnValue(false);
        }
        if ((!(var10.minecart.getValue())) || (!((arg0 instanceof AbstractMinecart)))) break label_8;
        arg5.setReturnValue(false);
        label_8:
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void night$chams(Entity arg0, EntityRenderState arg1, float arg2, CallbackInfo arg3) {
        Object var5 = ((IChamsCapture) arg1);
        var5.night$setChams(false, 0, false, 0, false);
        var5.night$setChamsYOffset(0.0f);
        Object var6 = ((PopChamsModule) LightningClient.MODULE_MANAGER.getModule(PopChamsModule.class));
        Object var7 = ((LogoutSpotModule) LightningClient.MODULE_MANAGER.getModule(LogoutSpotModule.class));
        if (!((arg0 instanceof RemotePlayer))) break label_0;
        Object var9 = ((RemotePlayer) arg0);
        if (var6 == null) break label_0;
        int var8 = (var6.isGhost(var9) ? 1 : 0);
        if (!((arg0 instanceof RemotePlayer))) break label_2;
        Object var10 = ((RemotePlayer) arg0);
        if (var7 == null) break label_2;
        var9 = (var7.isGhost(var10) ? 1 : 0);
        if (!var8) {
            if (!(!var9)) {
            }
        } else {
        }
        var10 = 0;
        if (var10) break label_11;
        Object var11 = ((ChamsModule) LightningClient.MODULE_MANAGER.getModule(ChamsModule.class));
        if (!(var11.isToggled())) break label_10;
        if (!((arg0 instanceof LivingEntity))) break label_7;
        Object var12 = ((LivingEntity) arg0);
        if (!(var11.isValidEntity(var12))) break label_7;
        var11.applyEntityChams(var12, var5);
        break label_10;
        label_7:
        if (var11.crystals.getValue() && (arg0 instanceof EndCrystal) && var11.inRange(arg0)) {
            var11.applyCrystalChams(var5);
            break label_10;
        }
        if ((!(var11.vehicles.getValue())) || (!(var11.inRange(arg0)))) break label_10;
        if (!((arg0 instanceof AbstractBoat))) {
            if (!((arg0 instanceof AbstractMinecart))) break label_10;
        }
        var11.applyVehicleChams(var5);
        label_10:
        var12 = ((ShadersModule) LightningClient.MODULE_MANAGER.getModule(ShadersModule.class));
        if ((!(var12.isToggled())) || (!(var12.isValidEntity(arg0)))) break label_11;
        arg1.outlineColor = var12.getFillColor(arg0);
        label_11:
        if (var6 != null && var6.isToggled() && !(!var8)) {
            var11 = ((RemotePlayer) arg0);
            var5.night$setChams(var6.shouldFill(), var6.getFillColor(var11).getRGB(), var6.shouldOutline(), var6.getOutlineColor(var11).getRGB(), false, true);
            var5.night$setChamsYOffset(var6.getYOffset(var11));
        }
        if (var7 != null && var7.isToggled() && !(!var9)) {
            var11 = ((RemotePlayer) arg0);
            var5.night$setChams(var7.shouldFill(), var7.getFillColor(var11).getRGB(), var7.shouldOutline(), var7.getOutlineColor(var11).getRGB(), false, true);
        }
    }

}
