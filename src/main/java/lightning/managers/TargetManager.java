package lightning.managers;

import java.util.ArrayList;
import java.util.Objects;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.TargetDeathEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.modules.impl.combat.KillAuraModule;
import lightning.utils.IMinecraft;
import net.minecraft.world.entity.player.Player;

public class TargetManager implements IMinecraft {
    private final ArrayList targets;

    public TargetManager() {
        this.targets = new ArrayList();
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent arg0) {
        this.targets.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = ((AutoCrystalModule) LightningClient.MODULE_MANAGER.getModule(AutoCrystalModule.class)).getTarget();
        Object var3 = ((KillAuraModule) LightningClient.MODULE_MANAGER.getModule(KillAuraModule.class)).target;
        Object var4 = this.targets;
        Object var5 = var4;
        // synchronized (var4)
        this.targets.removeIf((arg0) -> (System.currentTimeMillis() - arg0.time) > 15000L);
        if (var2 != null) {
            this.targets.add(new TargetManager.Target(this, this, var2));
        }
        if ((var3 instanceof Player)) {
            this.targets.add(new TargetManager.Target(this, this, ((Player) var3)));
        }
        break label_8;
        Object var6 = null;
        throw var6;
        label_8:
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (!(isTarget(arg0.getPlayer())))) {
            return;
        }
        Object var2 = this.targets;
        Object var3 = var2;
        // synchronized (var2)
        LightningClient.EVENT_HANDLER.post(new TargetDeathEvent(arg0.getPlayer()));
        this.targets.remove(getTarget(arg0.getPlayer()));
        break label_6;
        Object var4 = null;
        throw var4;
        label_6:
    }

    private TargetManager.Target getTarget(Player arg0) {
        Object var2 = this.targets.iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((TargetManager.Target) var2.next());
            if (var3.player != arg0) {
                break label_0;
            }
            return var3;
        }
        return null;
    }

    public boolean isTarget(Player arg0) {
        Object var2 = this.targets.iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((TargetManager.Target) var2.next());
            if (var3.player != arg0) {
                break label_0;
            }
            return true;
        }
        return false;
    }


    class Target {
        private final Player player;
        private final long time;

        public Target(TargetManager arg0, TargetManager arg1, Player arg2) {
            Objects.requireNonNull(arg0);
            Objects.requireNonNull(arg1);
            this.player = arg2;
            this.time = System.currentTimeMillis();
        }

    }
}
