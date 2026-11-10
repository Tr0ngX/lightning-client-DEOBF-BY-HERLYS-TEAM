package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@RegisterModule(name = "Step", description = "Gives you the ability to instantly climb over a customizable amount of blocks.", category = Module.Category.MOVEMENT)
public class StepModule extends Module {
    public ModeSetting mode;
    public NumberSetting height;
    public BooleanSetting useTimer;
    private boolean resetTimer;

    public StepModule() {
        var tmp0 = new String[2];
        tmp0[0] = "Vanilla";
        tmp0[1] = "NCP";
        this.mode = new ModeSetting("Mode", "The mode that will be used to climb over blocks.", "Vanilla", tmp0);
        this.height = new NumberSetting("Height", "The maximum height at which blocks can be climbed over.", 2.0f, 0.0f, 12.0f);
        var tmp1 = new String[1];
        tmp1[0] = "NCP";
        this.useTimer = new BooleanSetting("UseTimer", "Uses timer to slow you down while climbing.", new ModeSetting.Visibility(this.mode, tmp1), true);
        this.resetTimer = false;
    }

    public void onDisable() {
        LightningClient.WORLD_MANAGER.setTimerMultiplier(1.0f);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (!(!this.resetTimer)) {
            LightningClient.WORLD_MANAGER.setTimerMultiplier(1.0f);
            this.resetTimer = false;
        }
    }

    @SubscribeEvent
    public void onUpdateMovement(UpdateMovementEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.mode.getValue().equalsIgnoreCase("NCP"))) return;
        double var2 = (mc.player.getY() - mc.player.yo);
        if ((var2 <= 0.75) || (var2 > this.height.getValue().doubleValue())) {
            return;
        }
        Object var4 = getOffset(var2);
        if ((var4 == null) || (var4.length <= 1)) break label_7;
        if (this.useTimer.getValue()) {
            LightningClient.WORLD_MANAGER.setTimerMultiplier((1.0f / ((float) var4.length)));
            this.resetTimer = true;
        }
        Object var5 = var4;
        int var6 = var5.length;
        int var7 = 0;
        while (var7 < var6) {
            double var8 = var5[var7];
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.xo, (mc.player.yo + var8), mc.player.zo, false, mc.player.horizontalCollision));
            var7++;
        }
        label_7:
    }

    public String getMetaData() {
        return String.valueOf(this.height.getValue().floatValue());
    }

    public double[] getOffset(double arg0) {
        switch (((int) (arg0 * 10000.0))) {
            case 7500 -> { break label_0; }
            case 8125 -> { break label_1; }
            case 8750 -> { break label_1; }
            case 10000 -> { break label_0; }
            case 15000 -> { break label_2; }
            case 20000 -> { break label_3; }
            case 250000 -> { break label_4; }
            default -> { break label_5; }
        }
        label_0:
        Object var4 = new double[2];
        var4[0] = 0.42;
        Object var3 = var4;
        var4[1] = 0.753;
        break label_6;
        label_1:
        var4 = new double[2];
        var4[0] = 0.39;
        var3 = var4;
        var4[1] = 0.7;
        break label_6;
        label_2:
        var4 = new double[6];
        var4[0] = 0.42;
        var4[1] = 0.75;
        var4[2] = 1.0;
        var4[3] = 1.16;
        var4[4] = 1.23;
        var3 = var4;
        var4[5] = 1.2;
        break label_6;
        label_3:
        var4 = new double[8];
        var4[0] = 0.42;
        var4[1] = 0.78;
        var4[2] = 0.63;
        var4[3] = 0.51;
        var4[4] = 0.9;
        var4[5] = 1.21;
        var4[6] = 1.45;
        var3 = var4;
        var4[7] = 1.43;
        break label_6;
        label_4:
        var4 = new double[10];
        var4[0] = 0.425;
        var4[1] = 0.821;
        var4[2] = 0.699;
        var4[3] = 0.599;
        var4[4] = 1.022;
        var4[5] = 1.372;
        var4[6] = 1.652;
        var4[7] = 1.869;
        var4[8] = 2.019;
        var3 = var4;
        var4[9] = 1.907;
        break label_6;
        label_5:
        var3 = null;
        label_6:
        return var3;
    }

}
