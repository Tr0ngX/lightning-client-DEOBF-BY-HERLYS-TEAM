package lightning.mixins.accessors;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface LayerRenderStateAccessor {
    @Accessor("quads")
    List night$getQuads();

    @Accessor("foilType")
    ItemStackRenderState.FoilType night$getFoilType();

    @Accessor("tintLayers")
    IntList night$getTintLayers();

    @Invoker("applyTransform")
    void night$applyTransform(PoseStack.Pose arg0);

}
