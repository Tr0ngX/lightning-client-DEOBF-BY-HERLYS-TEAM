package lightning.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import lightning.utils.IMinecraft;
import lightning.utils.text.CustomFormatting;
import lightning.utils.text.FormattingUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StringDecomposer.class)
public class TextVisitFactoryMixin implements IMinecraft {
    @WrapOperation(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At(value = "INVOKE", target = "Ljava/lang/String;charAt(I)C", ordinal = 1))
    private static char visitFormatted(String arg0, int arg1, Operation arg2, LocalRef arg3) {
        Object var4 = CustomFormatting.byCode(arg0.charAt(arg1));
        if (var4 != null) {
            arg3.set(FormattingUtils.withExclusiveFormatting(((Style) arg3.get()), var4));
        }
        var tmp0 = new Object[2];
        tmp0[0] = arg0;
        tmp0[1] = arg1;
        return ((Character) arg2.call(tmp0)).charValue();
    }

}
