package lightning.commands.impl;

import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.events.impl.PlayerPopEvent;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Pose;

@RegisterCommand(name = "fakeplayer", aliases = "fp", description = "Spawns a client-side fake player to test on.", syntax = "[remove|pop|swim|record|stop|play|<name>]")
public class FakePlayerCommand extends Command {
    private static final byte TOTEM_POP_EVENT_ID = 35;

    public void execute(String[] arg0) {
        Object var2 = ((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class));
        if (var2 == null) return;
        if (arg0.length == 0) {
            var2.setToggled(!(var2.isToggled()));
            return;
        }
        Object var3 = arg0[0].toLowerCase();
        int var4 = -1;
        switch (var3.hashCode()) {
            case -934908847 -> { break label_7; }
            case -934610812 -> { break label_4; }
            case 111185 -> { break label_5; }
            case 3443508 -> { break label_9; }
            case 3540994 -> { break label_8; }
            case 3543688 -> { break label_6; }
            default -> { break label_10; }
        }
        label_4:
        if (!(var3.equals("remove"))) break label_10;
        var4 = 0;
        break label_10;
        label_5:
        if (!(var3.equals("pop"))) break label_10;
        var4 = 1;
        break label_10;
        label_6:
        if (!(var3.equals("swim"))) break label_10;
        var4 = 2;
        break label_10;
        label_7:
        if (!(var3.equals("record"))) break label_10;
        var4 = 3;
        break label_10;
        label_8:
        if (!(var3.equals("stop"))) break label_10;
        var4 = 4;
        break label_10;
        label_9:
        if (!(var3.equals("play"))) break label_10;
        var4 = 5;
        label_10:
        switch (var4) {
            case 0 -> { break label_11; }
            case 1 -> { break label_13; }
            case 2 -> { break label_14; }
            case 3 -> { break label_15; }
            case 4 -> { break label_16; }
            case 5 -> { break label_17; }
            default -> { break label_18; }
        }
        label_11:
        if (var2.isToggled()) {
            var2.setToggled(false);
            break label_20;
        }
        LightningClient.CHAT_MANAGER.warn("No fake player spawned.");
        break label_20;
        label_13:
        popBot(var2);
        break label_20;
        label_14:
        toggleSwim(var2);
        break label_20;
        label_15:
        var2.startRecording();
        break label_20;
        label_16:
        var2.stopRecording();
        break label_20;
        label_17:
        var2.startPlaying();
        break label_20;
        label_18:
        var2.name.setValue(arg0[0]);
        if (!(var2.isToggled())) {
            var2.setToggled(true);
            break label_20;
        }
        var2.setToggled(false);
        var2.setToggled(true);
        label_20:
    }

    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) return List.of("remove", "pop", "swim", "record", "stop", "play");
        return List.of();
    }

    private boolean nullCheck() {
        if (mc.level != null) {
            if (mc.player == null) {
            }
        } else {
        }
        return false;
    }

    private void popBot(FakePlayerModule arg0) {
        if (!(nullCheck())) {
            if (mc.getConnection() == null) {
            }
            LightningClient.CHAT_MANAGER.warn("You need to be in a world to do that.");
            return;
        }
        Object var2 = arg0.getPlayer();
        if (var2 != null) {
            if (var2.level() != mc.level) {
            }
            LightningClient.CHAT_MANAGER.warn("No fake player spawned. Spawn one first.");
            return;
        }
        mc.getConnection().handleEntityEvent(new ClientboundEntityEventPacket(var2, 35));
        LightningClient.EVENT_HANDLER.post(new PlayerPopEvent(var2, 1));
        LightningClient.CHAT_MANAGER.tagged("Popped fake player.", "FakePlayer");
    }

    private void toggleSwim(FakePlayerModule arg0) {
        if (nullCheck()) {
            LightningClient.CHAT_MANAGER.warn("You need to be in a world to do that.");
            return;
        }
        Object var2 = arg0.getPlayer();
        if (var2 != null) {
            if (var2.level() != mc.level) {
            }
            LightningClient.CHAT_MANAGER.warn("No fake player spawned. Spawn one first.");
            return;
        }
        Object var3 = (var2.getPose() != Pose.SWIMMING ? Pose.SWIMMING : Pose.STANDING);
        var2.setPose(var3);
        var2.refreshDimensions();
        LightningClient.CHAT_MANAGER.tagged((var3 == Pose.SWIMMING ? "Fake player now in swim pose." : "Fake player now standing."), "FakePlayer");
    }

}
