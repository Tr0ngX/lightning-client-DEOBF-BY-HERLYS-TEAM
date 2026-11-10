package lightning.modules.impl.movement;

import java.util.ArrayList;
import java.util.List;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.WebBlock;

@RegisterModule(name = "FastWeb", description = "Allows you to move quickly through webs.", category = Module.Category.MOVEMENT)
public class FastWebModule extends Module {
    public ModeSetting mode;
    public NumberSetting speed;
    public NumberSetting horizontal;
    public NumberSetting vertical;
    public BooleanSetting sneak;
    public BooleanSetting grim;

    public FastWebModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Normal";
        tmp0[1] = "Ignore";
        tmp0[2] = "Strong";
        this.mode = new ModeSetting("Mode", "The method that will be used to speed you through webs.", "Normal", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Normal";
        this.speed = new NumberSetting("Speed", "The speed at which you will fall through webs.", new ModeSetting.Visibility(this.mode, tmp1), 3.0, 0.1, 10.0);
        var tmp2 = new String[1];
        tmp2[0] = "Strong";
        this.horizontal = new NumberSetting("Horizontal", "The speed at which you will move horizontally.", new ModeSetting.Visibility(this.mode, tmp2), 2.5, 0.1, 5.0);
        var tmp3 = new String[1];
        tmp3[0] = "Strong";
        this.vertical = new NumberSetting("Vertical", "The speed at which you will move vertically.", new ModeSetting.Visibility(this.mode, tmp3), 2.5, 0.1, 5.0);
        var tmp4 = new String[1];
        tmp4[0] = "Normal";
        this.sneak = new BooleanSetting("Sneak", "Only bypasses web slowdown when sneaking.", new ModeSetting.Visibility(this.mode, tmp4), false);
        this.grim = new BooleanSetting("Grim", "Includes bypasses for the Grim anticheat.", false);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(mc.options.keyShift.isDown())) {
            if (this.sneak.getValue()) break label_5;
        }
        if (this.mode.getValue().equals("Normal") && !(mc.player.onGround()) && EntityUtils.isInWeb(mc.player)) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, (mc.player.getDeltaMovement().y - this.speed.getValue().doubleValue()), mc.player.getDeltaMovement().z);
        }
        if (!(this.grim.getValue())) break label_5;
        Object var2 = getWebs().iterator();
        while (var2.hasNext()) {
            Object var3 = ((BlockPos) var2.next());
            mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, var3, Direction.DOWN));
            mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, var3, Direction.DOWN));
        }
        label_5:
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

    public List getWebs() {
        Object var1 = new ArrayList();
        int var2 = 2;
        while (var2 > -2) {
            int var3 = 2;
            while (var3 > -2) {
                int var4 = 2;
                while (var4 > -2) {
                    Object var5 = mc.player.blockPosition().offset(var2, var3, var4);
                    if (!((mc.level.getBlockState(var5).getBlock() instanceof WebBlock))) {
                    } else {
                        var1.add(var5);
                    }
                    var4--;
                }
                var3--;
            }
            var2--;
        }
        return var1;
    }

}
