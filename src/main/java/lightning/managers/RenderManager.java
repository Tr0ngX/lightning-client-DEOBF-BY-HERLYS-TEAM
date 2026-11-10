package lightning.managers;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderOverlayEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.modules.impl.core.RendersModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.S2CRenderPositionPacket;
import lightning.utils.IMinecraft;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.system.Counter;
import lightning.utils.system.MathUtils;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RenderManager implements IMinecraft {
    public static final List POST_GUI_RENDERERS;
    private final Counter counter;
    private int fps;
    public CopyOnWriteArrayList renderPositions;
    private RenderManager.Target crystalTarget;
    private BlockPos prevPosition;
    private Vec3 renderPosition;
    private long animationStart;

    public RenderManager() {
        this.counter = new Counter();
        this.renderPositions = new CopyOnWriteArrayList();
        this.prevPosition = null;
        this.renderPosition = null;
        this.animationStart = 0L;
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent arg0) {
        this.fps = this.counter.getCount();
        this.counter.increment();
    }

    @SubscribeEvent
    public void onRenderWorld$placePositions(RenderWorldEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (this.renderPositions.isEmpty())) {
            return;
        }
        Object var2 = ((RendersModule) LightningClient.MODULE_MANAGER.getModule(RendersModule.class));
        Object var3 = this.renderPositions.iterator();
        while (var3.hasNext()) {
            Object var4 = ((RenderPosition) var3.next());
            float var5 = var4.get();
            Object var6 = new AABB(var4.getPos());
            if (var2.mode.getValue().equals("Shrink")) {
                var6 = new AABB(var4.getPos()).deflate(0.5).inflate(Mth.clamp((((double) var5) / 2.0), 0.0, 0.5));
            }
            if (!(var2.renderMode.getValue().equalsIgnoreCase("Fill"))) {
                if (var2.renderMode.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderBox(arg0.getMatrices(), var6, var2.getColor(var2.mode.getValue(), var2.fillColor.getColor(), var5));
            }
            if (!(var2.renderMode.getValue().equalsIgnoreCase("Outline")) && !(var2.renderMode.getValue().equalsIgnoreCase("Both"))) {
                break label_2;
            }
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var6, var2.getColor(var2.mode.getValue(), var2.outlineColor.getColor(), var5));
        }
        this.renderPositions.removeIf((arg0) -> arg0.get() <= 0.0f);
    }

    @SubscribeEvent
    public void onRenderWorld$autoCrystal(RenderWorldEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.crystalTarget == null) break label_2;
        if (this.crystalTarget.getPosition() == null) {
            label_2:
            return;
        }
        Object var3 = ((AutoCrystalModule) LightningClient.MODULE_MANAGER.getModule(AutoCrystalModule.class));
        if (this.crystalTarget.getTarget() == 1) {
        } else {
        }
        float var4 = (1.0f - Easing.ease(Easing.toDelta(this.crystalTarget.getTime(), var3.duration.getValue().intValue()), Easing.Method.EASE_IN_CUBIC));
        Object var5 = new AABB(this.crystalTarget.getPosition());
        if (var3.mode.getValue().equals("Shrink")) {
            var5 = new AABB(this.crystalTarget.getPosition()).deflate(0.5).inflate(Mth.clamp((((double) var4) / 2.0), 0.0, 0.5));
        }
        if (!(var3.animationMode.getValue().equals("Slide"))) break label_9;
        if (this.renderPosition == null) {
            this.renderPosition = MathUtils.getVec(this.crystalTarget.getPosition());
        }
        if (!(WorldUtils.equals(this.crystalTarget.getPosition(), this.prevPosition))) {
            this.animationStart = System.currentTimeMillis();
            this.prevPosition = this.crystalTarget.getPosition();
        }
        float var6 = Easing.ease(Easing.toDelta(this.animationStart, ((int) (Math.pow(var3.slideSmoothness.getValue().doubleValue(), 1.4) * 1000.0))), Easing.Method.EASE_OUT_QUART);
        this.renderPosition = this.renderPosition.add(MathUtils.scale(MathUtils.getVec(this.crystalTarget.getPosition()).subtract(this.renderPosition), var6));
        var5 = MathUtils.getBox(this.renderPosition);
        if (!(var3.mode.getValue().equals("Shrink"))) break label_9;
        var5 = MathUtils.getBox(this.renderPosition).deflate(0.5).inflate(Mth.clamp((((double) var4) / 2.0), 0.0, 0.5));
        label_9:
        var6 = ((RendersModule) LightningClient.MODULE_MANAGER.getModule(RendersModule.class));
        if (!(var3.renderMode.getValue().equalsIgnoreCase("Fill"))) {
            if (var3.renderMode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        int var7 = 0;
        if (!(var3.renderMode.getValue().equalsIgnoreCase("Outline"))) {
            if (var3.renderMode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        int var8 = 0;
        int var9 = EspShader.modeIndex(var3.shader.getValue());
        Object var10 = (!var9 ? Renderer3D.QUADS : Renderer3D.SHADER_QUADS);
        var tmp0 = (!var9 ? Renderer3D.DEBUG_LINES : Renderer3D.SHADER_DEBUG_LINES);
        Object var2 = tmp0;
        Object var11 = tmp0;
        if (!(!var9)) {
            float var12 = var3.shaderSpeed.getValue().floatValue();
            float var13 = var3.shaderStep.getValue().floatValue();
            float var14 = (((((float) (System.currentTimeMillis() % 2000000L)) / 1000.0f) * var12) * (var9 == 1 ? var13 : 1.0f));
            if (var3.shaderDistanceScaling.getValue()) {
            } else {
            }
            float var15 = 1.0f;
            if (var9 == 7) {
            } else {
            }
            EspShader.setSettings(new EspShader.Settings);
        }
        if (!(!var7)) {
            Renderer3D.renderGradientBox(var10, arg0.getMatrices(), var5, var6.getColor(var3.mode.getValue(), var3.fillColorUp.getColor(), var4), var6.getColor(var3.mode.getValue(), var3.fillColorDown.getColor(), var4));
        }
        if (!(!var8)) {
            Renderer3D.renderGradientBoxOutline(var2, arg0.getMatrices(), var5, var6.getColor(var3.mode.getValue(), var3.outlineColorUp.getColor(), var4), var6.getColor(var3.mode.getValue(), var3.outlineColorDown.getColor(), var4));
        }
    }

    @SubscribeEvent
    public void onRenderWorld$autoCrystalExtra(RenderWorldEvent.Post arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if ((this.crystalTarget == null) || (this.crystalTarget.getPosition() == null)) {
            return;
        }
        if (this.crystalTarget.getTarget() != 1) return;
        Object var2 = arg0.getMatrices();
        Object var3 = ((AutoCrystalModule) LightningClient.MODULE_MANAGER.getModule(AutoCrystalModule.class));
        Object var4 = Vec3.atCenterOf(this.crystalTarget.getPosition());
        Object var5 = new Vec3((var4.x - mc.gameRenderer.mainCamera().position().x), (var4.y - mc.gameRenderer.mainCamera().position().y), (var4.z - mc.gameRenderer.mainCamera().position().z));
        if (var3.animationMode.getValue().equals("Slide")) {
            var5 = new Vec3(((this.renderPosition.x + 0.5) - mc.gameRenderer.mainCamera().position().x), ((this.renderPosition.y + 0.5) - mc.gameRenderer.mainCamera().position().y), ((this.renderPosition.z + 0.5) - mc.gameRenderer.mainCamera().position().z));
        }
        if (var3.icon.getValue()) {
            float var6 = (var3.iconScale.getValue().floatValue() / 100.0f);
            var2.pushPose();
            var2.translate(var5.x, var5.y, var5.z);
            var2.mulPose(mc.gameRenderer.mainCamera().rotation());
            var2.scale(var6, -(var6), var6);
            Renderer2D.renderCircle(var2, 0.0f, 0.0f, 12.0f, new Color(0, 0, 0, 100));
            Renderer2D.renderCircle(var2, 0.0f, 0.0f, (12.0f - var3.iconRadius.getValue().floatValue()), var3.iconColor.getColor());
            if (var3.renderDamage.getValue()) {
                Renderer2D.renderTexture(var2, -5.5f, -8.5f, 5.5f, 2.5f, Identifier.fromNamespaceAndPath("lightning", "textures/crystal.png"), Color.WHITE);
                var2.pushPose();
                var2.scale(0.45f, 0.45f, 0.45f);
                Object var7 = var3.getCalculationDamage();
                LightningClient.FONT_MANAGER.drawTextWithShadow(var2, var7, ((-(LightningClient.FONT_MANAGER.getWidth(var7)) / 2) - 1), 7, Color.WHITE);
                var2.popPose();
            } else {
                Renderer2D.renderTexture(var2, -6.5f, -6.5f, 6.5f, 6.5f, Identifier.fromNamespaceAndPath("lightning", "textures/crystal.png"), Color.WHITE);
            }
            var2.popPose();
            break label_9;
        }
        if (!(var3.renderDamage.getValue())) break label_9;
        var2.pushPose();
        var2.translate(var5.x, var5.y, var5.z);
        var2.mulPose(mc.gameRenderer.mainCamera().rotation());
        var2.scale(0.025f, -0.025f, 0.025f);
        var6 = var3.getCalculationDamage();
        LightningClient.FONT_MANAGER.drawTextWithShadow(var2, var6, (-(LightningClient.FONT_MANAGER.getWidth(var6)) / 2), (-(LightningClient.FONT_MANAGER.getHeight()) / 2), Color.WHITE);
        var2.popPose();
        label_9:
    }

    public void setRenderPosition(BlockPos arg0) {
        if (!(((AutoCrystalModule) LightningClient.MODULE_MANAGER.getModule(AutoCrystalModule.class)).isToggled())) {
            arg0 = null;
        }
        if (arg0 == null) {
            if (this.crystalTarget != null) {
                if (!(this.crystalTarget.getTarget())) break label_5;
                this.crystalTarget.setTarget(0);
                this.crystalTarget.setTime(System.currentTimeMillis());
                break label_5;
            }
            this.crystalTarget = new RenderManager.Target(null, 0, System.currentTimeMillis());
            break label_5;
        }
        if (this.crystalTarget != null) {
            if (!(this.crystalTarget.getTarget())) {
            }
            this.crystalTarget = new RenderManager.Target(arg0, 1, System.currentTimeMillis());
            break label_5;
        }
        this.crystalTarget.setPosition(arg0);
        label_5:
        if (!(!PingBypassFlags.proxyForwardingActive) && LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer() && LightningClient.PROXY_SERVER != null) {
            sendRenderPositionToClient(arg0);
        }
    }

    private void sendRenderPositionToClient(BlockPos arg0) {
        Object var2 = new ClientboundCustomPayloadPacket(PbCustomPayload.fromPacket(new S2CRenderPositionPacket(arg0)));
        Object var3 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Connection) var3.next());
            if (!(var4.isConnected())) {
                break label_0;
            }
            var4.send(var2);
        }
    }

    @Generated
    public int getFps() {
        return this.fps;
    }

    @Generated
    public CopyOnWriteArrayList getRenderPositions() {
        return this.renderPositions;
    }

    static {
        POST_GUI_RENDERERS = new CopyOnWriteArrayList();
    }


    public class Target {
        private BlockPos position;
        private int target;
        private long time;

        @Generated
        public Target(BlockPos arg0, int arg1, long arg2) {
            this.position = arg0;
            this.target = arg1;
            this.time = arg2;
        }

        @Generated
        public BlockPos getPosition() {
            return this.position;
        }

        @Generated
        public int getTarget() {
            return this.target;
        }

        @Generated
        public long getTime() {
            return this.time;
        }

        @Generated
        public void setPosition(BlockPos arg0) {
            this.position = arg0;
        }

        @Generated
        public void setTarget(int arg0) {
            this.target = arg0;
        }

        @Generated
        public void setTime(long arg0) {
            this.time = arg0;
        }

    }
}
