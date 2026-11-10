package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.core.IgnoreNakedModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Tracers", description = "Renders a line showing where other players are located.", category = Module.Category.VISUALS)
public class TracersModule extends Module {
    public BooleanSetting antiBot;
    public ModeSetting mode;
    public ColorSetting color;

    public TracersModule() {
        this.antiBot = new BooleanSetting("AntiBot", "Prevents bots from having arrow tracers rendered for them.", false);
        var tmp0 = new String[2];
        tmp0[0] = "Distance";
        tmp0[1] = "Custom";
        this.mode = new ModeSetting("Mode", "The mode for the tracers color.", "Distance", tmp0);
        this.color = new ColorSetting("Color", "The color used for the fill rendering.", ColorUtils.getDefaultOutlineColor());
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (getNull()) return;
        if (mc.player == null) return;
        Object var4 = ((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam"));
        if (var4 == null) break label_2;
        var tmp0 = (var4.isToggled() ? 1 : 0);
        int var3 = tmp0;
        int var5 = tmp0;
        if (!(!var3)) {
            float var6 = var4.getFreePitch();
            float var7 = var4.getFreeYaw();
            float var8 = (var6 * 0.017453292f);
            float var9 = (-(var7) * 0.017453292f);
            float var10 = Mth.cos(((double) var9));
            float var11 = Mth.sin(((double) var9));
            float var12 = Mth.cos(((double) var8));
            float var13 = Mth.sin(((double) var8));
            Object var14 = new Vec3(((double) (var11 * var12)), ((double) -(var13)), ((double) (var10 * var12)));
            Object var2 = new Vec3(var4.getFreeX(), var4.getFreeY(), var4.getFreeZ()).add(var14);
        } else {
            var6 = mc.gameRenderer.mainCamera().position();
            var7 = Vec3.directionFromRotation(mc.gameRenderer.mainCamera().xRot(), mc.gameRenderer.mainCamera().yRot());
            var2 = var6.add(var7);
        }
        var6 = mc.level.players().iterator();
        while (var6.hasNext()) {
            var7 = ((Player) var6.next());
            if (var7 == mc.player) {
                if (!var3) break label_6;
            }
            if (EntityUtils.isGhost(var7)) break label_6;
            if (EntityUtils.isBot(var7)) {
                if (this.antiBot.getValue()) break label_6;
            }
            if (((IgnoreNakedModule) LightningClient.MODULE_MANAGER.getModule(IgnoreNakedModule.class)).isToggled() && EntityUtils.isNaked(var7)) {
                break label_6;
            }
            var8 = EntityUtils.getRenderPos(var7, arg0.getTickDelta());
            Renderer3D.renderLine(Renderer3D.TRACER_DEBUG_LINES, arg0.getMatrices(), var2, var8, getColor(var7, var2));
        }
    }

    private Color getColor(Player arg0, Vec3 arg1) {
        if (LightningClient.FRIEND_MANAGER.contains(arg0.getName().getString())) return LightningClient.FRIEND_MANAGER.getDefaultFriendColor(this.color.getColor().getAlpha());
        if (this.mode.getValue().equals("Custom")) return this.color.getColor();
        float var3 = 80.0f;
        double var4 = arg1.distanceToSqr(arg0.getX(), arg0.getY(), arg0.getZ());
        float var6 = Mth.clamp(((float) Math.sqrt(var4)), 0.0f, var3);
        return new Color(((var3 - var6) / var3), (1.0f - ((var3 - var6) / var3)), 0.0f, (((float) this.color.getColor().getAlpha()) / 255.0f));
    }

}
