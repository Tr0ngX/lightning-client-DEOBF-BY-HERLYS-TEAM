package lightning.mixins;

import java.util.ArrayList;
import lightning.LightningClient;
import lightning.modules.impl.miscellaneous.ExtraTabModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListHudMixin {
    @Shadow
    private Component decorateName(PlayerInfo arg0, MutableComponent arg1) {
        throw new UnsupportedOperationException();
    }

    @ModifyConstant(method = "getPlayerInfos", constant = @Constant(longValue = 80), require = 0)
    private long extraTabLimit(long arg0) {
        Object var3 = ((ExtraTabModule) LightningClient.MODULE_MANAGER.getModule(ExtraTabModule.class));
        if (var3.isToggled()) {
        } else {
        }
        return arg0;
    }

    @Inject(method = "getNameForDisplay", at = @At("HEAD"), cancellable = true)
    private void getPlayerName(PlayerInfo arg0, CallbackInfoReturnable arg1) {
        if (((ExtraTabModule) LightningClient.MODULE_MANAGER.getModule(ExtraTabModule.class)).isToggled() && ((ExtraTabModule) LightningClient.MODULE_MANAGER.getModule(ExtraTabModule.class)).friends.getValue() && LightningClient.FRIEND_MANAGER.contains(arg0.getProfile().name())) {
            if (arg0.getTabListDisplayName() != null) {
                Object var3 = Component.empty();
                Object var4 = new ArrayList();
                var4.add(arg0.getTabListDisplayName().plainCopy());
                var4.addAll(arg0.getTabListDisplayName().getSiblings());
                Object var5 = var4.iterator();
                while (var5.hasNext()) {
                    Object var6 = ((Component) var5.next());
                    if (var6.getString().equals(arg0.getProfile().name())) {
                        var3.append(Component.literal(arg0.getProfile().name()).withStyle(ChatFormatting.AQUA));
                        break label_3;
                    }
                    if (var6.getString().equals(("] " + arg0.getProfile().name()))) {
                        var3.append(Component.literal("] ").withStyle(ChatFormatting.WHITE).append(Component.literal(arg0.getProfile().name()).withStyle(ChatFormatting.AQUA)));
                        break label_3;
                    }
                    var3.append(var6);
                    label_3:
                }
                arg1.setReturnValue(decorateName(arg0, var3));
                return;
            }
            arg1.setReturnValue(decorateName(arg0, PlayerTeam.formatNameForTeam(arg0.getTeam(), Component.literal(arg0.getProfile().name()))));
        }
    }

}
