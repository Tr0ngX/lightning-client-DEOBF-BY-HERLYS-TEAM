package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMineEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.WorldUtils;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "BreakHighlight", description = "Renders blocks that are being mined by other players.", category = Module.Category.VISUALS)
public class BreakHighlightModule extends Module {
    public ModeSetting mode;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    public BooleanSetting name;
    public ColorSetting nameColor;
    public BooleanSetting glow;
    public NumberSetting scale;
    private final Map mineMap;

    public BreakHighlightModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Fill";
        tmp0[1] = "Outline";
        tmp0[2] = "Both";
        this.mode = new ModeSetting("Mode", "The rendering that will be applied to the mine esp.", "Outline", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Fill";
        tmp1[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, tmp1), ColorUtils.getDefaultFillColor());
        var tmp2 = new String[2];
        tmp2[0] = "Outline";
        tmp2[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, tmp2), ColorUtils.getDefaultOutlineColor());
        this.name = new BooleanSetting("Name", "Renders the name of the player mining the block.", true);
        this.nameColor = new ColorSetting("NameColor", "The color used for the name text rendering.", new BooleanSetting.Visibility(this.name, true), new ColorSetting.Color(Color.WHITE, false, false));
        this.glow = new BooleanSetting("Glow", "Renders a glow shader effect around the breaker name text.", new BooleanSetting.Visibility(this.name, true), false);
        this.scale = new NumberSetting("Scale", "The scale of the name text.", new BooleanSetting.Visibility(this.name, true), 30, 10, 100);
        this.mineMap = new HashMap();
    }

    @SubscribeEvent
    public void onPlayerMine(PlayerMineEvent arg0) {
        if (((getNull()) || (arg0.getActorID() == mc.player.getId())) || (mc.level == null)) break label_5;
        Object var2 = mc.level.getBlockState(arg0.getPosition()).getBlock();
        if (var2.equals(Blocks.AIR)) break label_5;
        Object var3 = mc.level.getEntity(arg0.getActorID());
        if ((var3 instanceof Player)) {
            Object var5 = ((Player) var3);
        } else {
        }
        Object var4 = "Player";
        if ((var3 instanceof Player)) {
            Object var6 = ((Player) var3);
        } else {
        }
        var5 = 1000.0f;
        var6 = new BreakHighlightModule.Mine(arg0.getPosition(), var2, var5, System.currentTimeMillis(), var4);
        if (!(this.mineMap.containsKey(arg0.getActorID()))) {
            this.mineMap.put(arg0.getActorID(), var6);
            break label_5;
        }
        if (((BreakHighlightModule.Mine) this.mineMap.get(arg0.getActorID())).pos.equals(arg0.getPosition())) break label_5;
        this.mineMap.replace(arg0.getActorID(), var6);
        label_5:
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((getNull()) || (this.mineMap.isEmpty())) {
            return;
        }
        this.mineMap.entrySet().removeIf(() -> clearMine(((Integer) arg0.getKey()).intValue(), ((BreakHighlightModule.Mine) arg0.getValue())));
        this.mineMap.forEach((arg2) -> {
    float var4 = Easing.toDelta(arg2.time, ((int) arg2.breakTime));
    Object var5 = new AABB(arg2.pos).deflate(0.5).inflate((((double) var4) / 2.0));
    if (!(this.mode.getValue().equalsIgnoreCase("Fill"))) {
        if (this.mode.getValue().equalsIgnoreCase("Both")) {
        }
        Renderer3D.renderBox(arg0.getMatrices(), var5, this.fillColor.getColor());
    }
    if (!(this.mode.getValue().equalsIgnoreCase("Outline"))) {
        if (this.mode.getValue().equalsIgnoreCase("Both")) {
        }
        Renderer3D.renderBoxOutline(arg0.getMatrices(), var5, this.outlineColor.getColor());
    }
    if (this.name.getValue() && arg2.playerName != null) {
        Object var6 = Vec3.atCenterOf(arg2.pos);
        int var7 = Math.min(100, Math.max(0, ((int) (var4 * 100.0f))));
        Object var8 = (arg2.playerName + "\n" + var7 + "%");
        Renderer3D.renderCenteredScaledText(arg0.getMatrices(), var8, var6.x, var6.y, var6.z, this.scale.getValue().intValue(), false, this.nameColor.getColor(), this.glow.getValue());
    }
});
    }

    private boolean clearMine(int arg0, BreakHighlightModule.Mine arg1) {
        if (mc.level == null) return true;
        if (!(mc.level.getBlockState(arg1.pos).getBlock().equals(arg1.block))) return true;
        if (((float) (System.currentTimeMillis() - arg1.time)) > (arg1.breakTime + 3000.0f)) return true;
        Object var3 = mc.level.getEntity(arg0);
        if (var3 == null) break label_3;
        return Math.sqrt(var3.distanceToSqr(Vec3.atCenterOf(arg1.pos))) > 8.0;
    }


    class Mine {
        private final BlockPos pos;
        private final Block block;
        private final float breakTime;
        private final long time;
        private final String playerName;

        @Generated
        public Mine(BlockPos arg0, Block arg1, float arg2, long arg3, String arg4) {
            this.pos = arg0;
            this.block = arg1;
            this.breakTime = arg2;
            this.time = arg3;
            this.playerName = arg4;
        }

    }
}
