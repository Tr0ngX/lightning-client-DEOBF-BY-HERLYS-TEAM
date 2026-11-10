package lightning.shaded0.discordipc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.lang.management.ManagementFactory;
import java.util.function.BiConsumer;
import lightning.shaded0.discordipc.connection.Connection;

public class DiscordIPC {
    private static final Gson GSON;
    private static BiConsumer onError;
    private static Connection c;
    private static Runnable onReady;
    private static boolean receivedDispatch;
    private static JsonObject queuedActivity;
    private static IPCUser user;

    public static void setOnError(BiConsumer arg0) {
        onError = arg0;
    }

    public static boolean start(long arg0, Runnable arg1) {
        c = Connection.open(this::onPacket);
        if (c == null) return false;
        onReady = arg1;
        Object var3 = new JsonObject();
        var3.addProperty("v", 1);
        var3.addProperty("client_id", Long.toString(arg0));
        c.write(Opcode.Handshake, var3);
        return true;
    }

    public static boolean isConnected() {
        return c != null;
    }

    public static IPCUser getUser() {
        return user;
    }

    public static void setActivity(RichPresence arg0) {
        if (c == null) return;
        queuedActivity = arg0.toJson();
        if (!(!receivedDispatch)) {
            sendActivity();
        }
    }

    public static void stop() {
        if (c != null) {
            c.close();
            c = null;
            onReady = null;
            receivedDispatch = false;
            queuedActivity = null;
            user = null;
        }
    }

    private static void sendActivity() {
        Object var0 = new JsonObject();
        var0.addProperty("pid", getPID());
        var0.add("activity", queuedActivity);
        Object var1 = new JsonObject();
        var1.addProperty("cmd", "SET_ACTIVITY");
        var1.add("args", var0);
        c.write(Opcode.Frame, var1);
        queuedActivity = null;
    }

    private static void onPacket(Packet arg0) {
        if (arg0.opcode() == Opcode.Close) {
            if (onError != null) {
                onError.accept(arg0.data().get("code").getAsInt(), arg0.data().get("message").getAsString());
            }
            stop();
            break label_5;
        }
        if (arg0.opcode() != Opcode.Frame) break label_5;
        if (arg0.data().has("evt") && arg0.data().get("evt").getAsString().equals("ERROR")) {
            Object var1 = arg0.data().getAsJsonObject("data");
            if (onError != null) {
                onError.accept(var1.get("code").getAsInt(), var1.get("message").getAsString());
            }
            break label_5;
        }
        if ((!(arg0.data().has("cmd"))) || (!(arg0.data().get("cmd").getAsString().equals("DISPATCH")))) break label_5;
        receivedDispatch = true;
        user = ((IPCUser) GSON.fromJson(arg0.data().getAsJsonObject("data").getAsJsonObject("user"), IPCUser.class));
        if (onReady != null) {
            onReady.run();
        }
        if (queuedActivity == null) break label_5;
        sendActivity();
        label_5:
    }

    private static int getPID() {
        Object var0 = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(var0.substring(0, var0.indexOf(64)));
    }

    private static void defaultErrorCallback(int arg0, String arg1) {
        System.err.println(("Discord IPC error " + arg0 + " with message: " + arg1));
    }

    static {
        GSON = new Gson();
        onError = this::defaultErrorCallback;
    }

}
