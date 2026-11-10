package lightning.mixins;

import com.mojang.authlib.GameProfile;
import lightning.LightningClient;
import lightning.modules.impl.core.CapesModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerListEntryMixin implements IMinecraft {
    @Shadow
    @Final
    private GameProfile profile;

    @Inject(method = "getSkin", at = @At("TAIL"), cancellable = true)
    private void getSkinTextures(CallbackInfoReturnable arg0) {
        if (this.profile.name().equals(mc.player.getGameProfile().name()) && this.profile.id().equals(mc.player.getGameProfile().id()) && ((CapesModule) LightningClient.MODULE_MANAGER.getModule(CapesModule.class)).isToggled() && ((CapesModule) LightningClient.MODULE_MANAGER.getModule(CapesModule.class)).getCapeTexture() != null) {
            Object var2 = ((CapesModule) LightningClient.MODULE_MANAGER.getModule(CapesModule.class)).getCapeTexture();
            Object var3 = ((PlayerSkin) arg0.getReturnValue());
            arg0.setReturnValue(new PlayerSkin(var3.body(), new ClientAsset.ResourceTexture(var2), var3.elytra(), var3.model(), var3.secure()));
        }
    }

}
