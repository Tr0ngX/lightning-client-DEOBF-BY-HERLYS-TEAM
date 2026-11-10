package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Model.class)
public abstract class ModelMixin {
    @ModifyReturnValue(method = "renderType(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At("RETURN"))
    private RenderType night$crystalTranslucent(RenderType arg0, Identifier arg1) {
        if ((this instanceof EndCrystalModel)) {
        } else {
        }
        return arg0;
    }

}
