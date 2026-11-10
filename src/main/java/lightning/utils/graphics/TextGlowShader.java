package lightning.utils.graphics;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class TextGlowShader {
    private static final Identifier TEXT_GLOW_FRAGMENT;
    public static final RenderPipeline TEXT_GLOW_PIPELINE;
    private static final Map WORLD_GLOW_TYPES;

    public static RenderType getWorldGlowType(Identifier arg0) {
        return ((RenderType) WORLD_GLOW_TYPES.computeIfAbsent(arg0, (arg0) -> RenderType.create(("night_text_glow_" + arg0.getNamespace() + "_" + arg0.getPath().replace(47, 95)), RenderSetup.builder(TEXT_GLOW_PIPELINE).withTexture("Sampler0", arg0).createRenderSetup())));
    }

    static {
        TEXT_GLOW_FRAGMENT = Identifier.fromNamespaceAndPath("lightning", "core/text_glow");
        var tmp0 = new RenderPipeline.Snippet[1];
        tmp0[0] = RenderPipelinesAccessor.getGuiTexturedSnippet();
        TEXT_GLOW_PIPELINE = RenderPipeline.builder(tmp0).withLocation("lightning/text_glow").withFragmentShader(TEXT_GLOW_FRAGMENT).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build();
        WORLD_GLOW_TYPES = new ConcurrentHashMap();
    }

}
