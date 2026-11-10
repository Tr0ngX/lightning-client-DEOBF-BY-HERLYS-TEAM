package lightning.modules.impl.miscellaneous;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;

@RegisterModule(name = "Notifications", description = "Notifies you in chat whenever something significant happens.", category = Module.Category.MISCELLANEOUS)
public class NotificationsModule extends Module {
    public BooleanSetting totemPops;
    public BooleanSetting visualRange;
    public BooleanSetting pearlThrows;
    private final Map loadedPlayers;
    private final ArrayList thrownPearls;

    public NotificationsModule() {
        this.totemPops = new BooleanSetting("TotemPops", "Notifies you in chat whenever a player pops a totem.", true);
        this.visualRange = new BooleanSetting("VisualRange", "Notifies you in chat whenever a player enters your render distance.", false);
        this.pearlThrows = new BooleanSetting("PearlThrows", "Notifies you in chat whenever a player throws a pearl.", true);
        this.loadedPlayers = new LinkedHashMap();
        this.thrownPearls = new ArrayList();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        if (!(this.visualRange.getValue())) break label_11;
        Object var2 = new HashSet();
        Object var3 = mc.level.entitiesForRendering().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Entity) var3.next());
            if (!((var4 instanceof Player))) {
                break label_1;
            }
            Object var6 = ((Player) var4);
            if (var4 == mc.player) break label_1;
            if (EntityUtils.isGhost(var6)) {
                break label_1;
            }
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var5 = null;
            Object var7 = null;
            if (var5 != null && var5.isToggled() && var4 == var5.getPlayer()) {
                break label_1;
            }
            var2.add(var6.getUUID());
            if (this.loadedPlayers.containsKey(var6.getUUID())) {
                break label_1;
            }
            this.loadedPlayers.put(var6.getUUID(), var6.getName().getString());
            LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getPrimary()) + var6.getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " has entered your visual range."), ("visual-range-" + var6.getName().getString()));
        }
        if (this.loadedPlayers.isEmpty()) break label_11;
        var3 = this.loadedPlayers.entrySet().iterator();
        while (var3.hasNext()) {
            var4 = ((Map.Entry) var3.next());
            if (var2.contains(var4.getKey())) {
                break label_9;
            }
            var3.remove();
            LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getPrimary()) + ((String) var4.getValue()) + String.valueOf(ChatUtils.getSecondary()) + " has left your visual range."), ("visual-range-" + ((String) var4.getValue())));
        }
        label_11:
        if (this.pearlThrows.getValue()) {
            var2 = mc.level.entitiesForRendering().iterator();
            while (var2.hasNext()) {
                var3 = ((Entity) var2.next());
                if (!((var3 instanceof ThrownEnderpearl))) break label_12;
                var tmp0 = ((ThrownEnderpearl) var3);
                var4 = tmp0;
                if (tmp0.getOwner() == null) break label_12;
                if (this.thrownPearls.contains(var4.getId())) {
                    break label_12;
                }
                var5 = var4.getOwner().getName().getString();
                LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getPrimary()) + var5 + String.valueOf(ChatUtils.getSecondary()) + " threw a pearl towards " + EntityUtils.getPearlDirection(var4).toString() + "."), ("pearl-throws-" + var5));
                this.thrownPearls.add(var4.getId());
            }
            this.thrownPearls.removeIf((arg0) -> !((mc.level.getEntity(arg0.intValue()) instanceof ThrownEnderpearl)));
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent arg0) {
        this.loadedPlayers.clear();
    }

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent arg0) {
        if (this.totemPops.getValue()) {
            LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getPrimary()) + arg0.getPlayer().getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " has popped " + String.valueOf(ChatUtils.getPrimary()) + arg0.getPops() + String.valueOf(ChatUtils.getSecondary()) + " totem" + (arg0.getPops() > 1 ? "s" : "") + "."), ("totem-pop-" + arg0.getPlayer().getName().getString()));
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        int var2 = ((Integer) LightningClient.WORLD_MANAGER.getPoppedTotems().getOrDefault(arg0.getPlayer().getUUID(), 0)).intValue();
        if (this.totemPops.getValue() && var2 > 0) {
            LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getPrimary()) + arg0.getPlayer().getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " has died after popping " + String.valueOf(ChatUtils.getPrimary()) + var2 + String.valueOf(ChatUtils.getSecondary()) + " totem" + (var2 > 1 ? "s" : "") + "."), ("totem-pop-" + arg0.getPlayer().getName().getString()));
        }
    }

}
