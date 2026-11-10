package lightning.modules.impl.miscellaneous;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TargetDeathEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import net.minecraft.client.Minecraft;

@RegisterModule(name = "FightLog", description = "Records your fights: kills, totems lost and who you fought. Shown in the ClickGui player card.", category = Module.Category.MISCELLANEOUS, toggled = true, drawn = false)
public class FightLogModule extends Module {
    private static final int MAX_HISTORY = 30;
    private static final long OPPONENT_WINDOW_MS = 25000L;
    private static final List HISTORY;
    private static int kills;
    private static int survived;
    private static int totemsLost;
    private static int baseline;
    private static boolean hasLast;
    private static boolean lastWin;
    private static String opponent;
    private static long opponentTime;
    private static String lastKillName;
    private static long lastKillTime;
    private static boolean subscribed;
    private static final FightLogModule.Listener LISTENER;

    public FightLogModule() {
        if (subscribed) break label_7;
        subscribed = true;
        try {
            LightningClient.EVENT_HANDLER.subscribe(LISTENER);
        } catch (Throwable ex5) {
            Throwable t = ex5;
            subscribed = false;
        }
        label_7:
    }

    public static int getKills() {
        return kills;
    }

    public static int getSurvived() {
        return survived;
    }

    public static int getTotemsLost() {
        return totemsLost;
    }

    public static boolean hasLast() {
        return hasLast;
    }

    public static boolean isLastWin() {
        return lastWin;
    }

    public static List getHistory() {
        return new ArrayList(HISTORY);
    }

    public static void reset() {
        HISTORY.clear();
        kills = 0;
        survived = 0;
        totemsLost = 0;
        baseline = 0;
        hasLast = false;
        lastWin = false;
        opponent = null;
    }

    private static void record(boolean win, String name) {
        long now = System.currentTimeMillis();
        if (!(!win)) {
            if (name != null && name.equals(lastKillName) && (now - lastKillTime) < 5000L) return;
            lastKillName = name;
            lastKillTime = now;
        }
        int lost = Math.max(0, (totemsLost - baseline));
        baseline = totemsLost;
        HISTORY.add(0, new FightLogModule.Entry(name, win, lost));
        while (HISTORY.size() > 30) {
            HISTORY.remove((HISTORY.size() - 1));
        }
        if (!(!win)) {
            kills = (kills + 1);
            survived = (survived + 1);
        }
        hasLast = true;
        lastWin = win;
        opponent = null;
    }

    private static Object call(Object o, String m) {
        try {
            if (o == null) {
                return null;
                try {
                }
                return o.getClass().getMethod(m, new Class[0]).invoke(o, new Object[0]);
            } catch (Throwable ex5) {
                Throwable t = ex5;
                return null;
            }
        }
    }

    private static boolean isPlayer(Object o) {
        if (o == null) {
        } else {
        }
        Class c = o.getClass();
        while (c != null) {
            if (c.getName().equals("net.minecraft.world.entity.player.Player")) return true;
            c = c.getSuperclass();
        }
        return false;
    }

    private static String nameOf(Object o) {
        Object comp = call(o, "getName");
        Object s = call(comp, "getString");
        if (s == null) {
        } else {
        }
        return String.valueOf(s);
    }

    private static Object selfObj() {
        try {
            Minecraft mc = Minecraft.getInstance();
            return mc.getClass().getField("player").get(mc);
        } catch (Throwable ex3) {
            Throwable t = ex3;
            return null;
        }
    }

    private static boolean isSelf(Object o) {
        if (o == null) return false;
        Object me = selfObj();
        if (me == null) return false;
        if (o == me) return true;
        Object a = call(o, "getUUID");
        Object b = call(me, "getUUID");
        if (a != null && a.equals(b)) return true;
        String n1 = nameOf(o);
        String n2 = nameOf(me);
        if (n1 == null) break label_14;
        return n1.equals(n2);
    }

    static {
        HISTORY = new ArrayList();
        kills = 0;
        survived = 0;
        totemsLost = 0;
        baseline = 0;
        hasLast = false;
        lastWin = false;
        opponent = null;
        opponentTime = 0L;
        lastKillName = null;
        lastKillTime = 0L;
        subscribed = false;
        LISTENER = new FightLogModule.Listener();
    }

    private static String selfName() {
        Object var0 = nameOf(selfObj());
        if (var0 != null && !(var0.isEmpty())) return var0;
        return "?";
    }

    private static boolean isMe(Object arg0) {
        if (arg0 == null) return false;
        Object var1 = selfObj();
        if (var1 == null) return false;
        if (arg0 == var1) return true;
        Object var2 = call(arg0, "getUUID");
        Object var3 = call(var1, "getUUID");
        if (var2 != null) return var2.equals(var3);
        return false;
    }


    public final class Listener {
        @SubscribeEvent
        public void onAttack(AttackEntityEvent event) {
            Object target = event.getTarget();
            if ((!(FightLogModule.isPlayer(target))) || (FightLogModule.isSelf(target))) {
                return;
            }
            String n = FightLogModule.nameOf(target);
            if ((n == null) || (n.isEmpty())) {
                return;
            }
            FightLogModule.opponent = n;
            FightLogModule.opponentTime = System.currentTimeMillis();
        }

        @SubscribeEvent
        public void onPlayerPop(PlayerPopEvent event) {
            Object p = event.getPlayer();
            if (FightLogModule.isMe(p)) {
                FightLogModule.totemsLost = (FightLogModule.totemsLost + 1);
            }
        }

        @SubscribeEvent
        public void onPlayerDeath(PlayerDeathEvent event) {
            Object p = event.getPlayer();
            long now = System.currentTimeMillis();
            if (FightLogModule.opponent == null) break label_3;
            boolean recent = (now - FightLogModule.opponentTime) <= 25000L;
            if (FightLogModule.isSelf(p)) {
                if (!(!recent)) {
                } else {
                }
                FightLogModule.record(FightLogModule.opponent, FightLogModule.selfName());
                return;
            }
            String n = FightLogModule.nameOf(p);
            if (!(!recent) && n != null && n.equals(FightLogModule.opponent)) {
                FightLogModule.record(true, n);
            }
        }

        @SubscribeEvent
        public void onTargetDeath(TargetDeathEvent event) {
            Object p = event.getPlayer();
            if (FightLogModule.isSelf(p)) return;
            String n = FightLogModule.nameOf(p);
            if (n != null && !(n.isEmpty())) {
                FightLogModule.record(true, n);
            }
        }

        @SubscribeEvent
        public void onClientConnect(ClientConnectEvent event) {
            FightLogModule.opponent = null;
            FightLogModule.baseline = FightLogModule.totemsLost;
        }

    }

    public final class Entry {
        public final String name;
        public final boolean win;
        public final int totems;

        public Entry(String name, boolean win, int totems) {
            this.name = name;
            this.win = win;
            this.totems = totems;
        }

    }
}
