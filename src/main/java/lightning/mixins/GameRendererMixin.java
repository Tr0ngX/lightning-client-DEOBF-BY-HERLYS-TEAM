package lightning.mixins;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.events.impl.RenderWorldEvent;
import lightning.mixins.accessors.LevelRendererAccessor;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.Glint;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    private static ProjectionMatrixBuffer night$noBobProjectionBuffer;
    private static final List night$emptyQuads;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void renderWorld$HEAD(DeltaTracker arg0, CallbackInfo arg1) {
        Renderer3D.prepare();
    }

    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V"))
    private void night$resolveOutline(DeltaTracker arg0, boolean arg1, CallbackInfo arg2) {
        Object var4 = ShadersModule.pickActiveOutlineChain();
        if (var4 == null) return;
        Object var5 = Minecraft.getInstance();
        if (var5.levelRenderer == null) return;
        Object var6 = ((LevelRendererAccessor) var5.levelRenderer).night$getEntityOutlineTarget();
        if (var6 == null) return;
        Object var7 = var5.getShaderManager().getPostChain(var4, LevelTargetBundle.MAIN_TARGETS);
        if (var7 == null) return;
        EspShader.writeOutlineSettings(var7, ShadersModule.shaderSettings(), ShadersModule.glowIntensity());
        var7.process(var6, GraphicsResourceAllocator.UNPOOLED);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift = At.Shift.AFTER))
    private void renderWorld$swap(DeltaTracker arg0, CallbackInfo arg1, Matrix4fc arg2, CameraRenderState arg3) {
        float var5 = arg0.getGameTimeDeltaPartialTick(false);
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().mul(arg2);
        Object var6 = new PoseStack();
        LightningClient.EVENT_HANDLER.post(new RenderWorldEvent(var6, var5));
        if (Renderer3D.QUADS.isEmpty()) {
            if (Renderer3D.DEBUG_LINES.isEmpty()) break label_1;
        }
        Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
        label_1:
        if (Renderer3D.SHINE_QUADS.isEmpty()) {
            if (!(Renderer3D.SHINE_DEBUG_LINES.isEmpty())) {
            }
            Renderer3D.draw(Renderer3D.SHINE_QUADS, Renderer3D.SHINE_DEBUG_LINES, true);
        }
        if (Renderer3D.SHADER_QUADS.isEmpty()) {
            if (!(Renderer3D.SHADER_DEBUG_LINES.isEmpty())) {
            }
            EspShader.draw(Renderer3D.SHADER_QUADS, Renderer3D.SHADER_DEBUG_LINES);
        }
        if (!(Glint.QUADS.isEmpty())) {
            Glint.draw();
        }
        LightningClient.EVENT_HANDLER.post(new RenderWorldEvent.Post(var6, var5));
        if (Renderer3D.QUADS.isEmpty()) {
            if (!(Renderer3D.DEBUG_LINES.isEmpty())) {
            }
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
        }
        if (Renderer3D.SHINE_QUADS.isEmpty()) {
            if (!(Renderer3D.SHINE_DEBUG_LINES.isEmpty())) {
            }
            Renderer3D.draw(Renderer3D.SHINE_QUADS, Renderer3D.SHINE_DEBUG_LINES, true);
        }
        if (Renderer3D.SHADER_QUADS.isEmpty()) {
            if (!(Renderer3D.SHADER_DEBUG_LINES.isEmpty())) {
            }
            EspShader.draw(Renderer3D.SHADER_QUADS, Renderer3D.SHADER_DEBUG_LINES);
        }
        if (!(Glint.QUADS.isEmpty())) {
            Glint.draw();
        }
        if (!(Renderer3D.TRACER_DEBUG_LINES.isEmpty())) {
            if (night$noBobProjectionBuffer == null) {
                night$noBobProjectionBuffer = new ProjectionMatrixBuffer("night_no_bob_tracer");
            }
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(night$noBobProjectionBuffer.getBuffer(arg3.projectionMatrix), ProjectionType.PERSPECTIVE);
            Renderer3D.draw(night$emptyQuads, Renderer3D.TRACER_DEBUG_LINES, false);
            RenderSystem.restoreProjectionMatrix();
        }
        RenderSystem.getModelViewStack().popMatrix();
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurt(CameraRenderState arg0, PoseStack arg1, CallbackInfo arg2) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).hurtCamera.getValue()) {
            arg2.cancel();
        }
    }

    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void showFloatingItem(ItemStack arg0, CallbackInfo arg1) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).totemAnimation.getValue()) {
            arg1.cancel();
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift = At.Shift.AFTER))
    private void renderWorld(DeltaTracker arg0, CallbackInfo arg1, Matrix4fc arg2, Matrix4f arg3) {
        Object var5 = new PoseStack();
        var5.last().pose().mul(arg2);
        Renderer2D.LAST_PROJECTION_MATRIX.set(arg3);
        Renderer2D.LAST_MODEL_MATRIX.set(RenderSystem.getModelViewMatrixCopy());
        Renderer2D.LAST_WORLD_MATRIX.set(var5.last().pose());
    }

    static {
        night$emptyQuads = new ArrayList();
    }

}
