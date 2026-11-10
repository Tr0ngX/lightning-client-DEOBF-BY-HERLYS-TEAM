package lightning.mixins.accessors;

import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostChain.class)
public interface PostEffectProcessorAccessor {
    @Accessor("passes")
    List getPasses();

    @Accessor("persistentTargets")
    Map getPersistentTargets();

}
