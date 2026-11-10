package lightning.mixins.accessors;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Accessor("LINES_SNIPPET")
    static RenderPipeline.Snippet getLinesSnippet() {
        throw new AssertionError();
    }

    @Accessor("DEBUG_FILLED_SNIPPET")
    static RenderPipeline.Snippet getDebugFilledSnippet() {
        throw new AssertionError();
    }

    @Accessor("ITEM_SNIPPET")
    static RenderPipeline.Snippet getItemSnippet() {
        throw new AssertionError();
    }

    @Accessor("ENTITY_SNIPPET")
    static RenderPipeline.Snippet getEntitySnippet() {
        throw new AssertionError();
    }

    @Accessor("GLOBALS_SNIPPET")
    static RenderPipeline.Snippet getGlobalsSnippet() {
        throw new AssertionError();
    }

    @Accessor("GUI_TEXTURED_SNIPPET")
    static RenderPipeline.Snippet getGuiTexturedSnippet() {
        throw new AssertionError();
    }

}
