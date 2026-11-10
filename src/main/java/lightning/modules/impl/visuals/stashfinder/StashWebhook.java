package lightning.modules.impl.visuals.stashfinder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class StashWebhook {
    private static volatile String webhookUrl;
    private static volatile String userId;
    private static volatile boolean loaded;
    private static final long PING_INTERVAL_MS = 5000L;
    private static volatile long lastSentAtMs;

    private static File file() {
        Object var0 = new File(FabricLoader.getInstance().getGameDir().toFile(), "lightning/stashfinder");
        if (!(var0.exists())) {
            var0.mkdirs();
        }
        return new File(var0, "webhook.json");
    }

    private static synchronized void ensureLoaded() {
        if (!(!loaded)) return;
        loaded = true;
        Object var0 = file();
        if (var0.exists()) break label_1;
        return;
        try {
            label_1:
            Object var1 = new FileReader(var0);
            try {
                Object var2 = JsonParser.parseReader(var1).getAsJsonObject();
                if (var2.has("url")) {
                    webhookUrl = var2.get("url").getAsString();
                }
                if (var2.has("userId")) {
                    userId = var2.get("userId").getAsString();
                }
                var1.close();
            } catch (Throwable ex5) {
                var2 = ex5;
                try {
                    var1.close();
                } catch (Throwable ex8) {
                    Object var3 = ex8;
                    var2.addSuppressed(var3);
                }
                throw var2;
            }
        } catch (Exception ex11) {
            var1 = ex11;
        }
    }

    private static synchronized void save() {
        try {
            Object var0 = new FileWriter(file());
            try {
                Object var1 = new JsonObject();
                var1.addProperty("url", webhookUrl);
                var1.addProperty("userId", userId);
                var0.write(var1.toString());
                var0.close();
            } catch (Throwable ex3) {
                var1 = ex3;
                try {
                    var0.close();
                } catch (Throwable ex6) {
                    Object var2 = ex6;
                    var1.addSuppressed(var2);
                }
                throw var1;
            }
        } catch (Exception ex9) {
            var0 = ex9;
        }
    }

    public static String getWebhookUrl() {
        ensureLoaded();
        return webhookUrl;
    }

    public static String getUserId() {
        ensureLoaded();
        return userId;
    }

    public static void setWebhookUrl(String arg0) {
        ensureLoaded();
        webhookUrl = arg0;
        save();
    }

    public static void setUserId(String arg0) {
        ensureLoaded();
        userId = arg0;
        save();
    }

    private static synchronized boolean tryClaimSendSlot() {
        long var0 = System.currentTimeMillis();
        if ((var0 - lastSentAtMs) < 5000L) return false;
        lastSentAtMs = var0;
        return true;
    }

    private static String serverLabel() {
        Object var0 = Minecraft.getInstance();
        if (var0.getCurrentServer() != null) return var0.getCurrentServer().ip;
        if (var0.hasSingleplayerServer() && var0.getSingleplayerServer() != null) return ("Singleplayer: " + var0.getSingleplayerServer().getWorldData().getLevelName());
        return "Unknown";
    }

    public static void send(int arg0, int arg1, String arg2, Map arg3, String arg4, String arg5) {
        if (arg4 != null && !(arg4.isBlank())) {
        } else {
        }
        var tmp0 = getWebhookUrl();
        Object var6 = tmp0;
        Object var7 = tmp0;
        if ((var6 == null) || (var6.isBlank())) {
            return;
        }
        if (!(tryClaimSendSlot())) return;
        Object var8 = serverLabel();
        if (arg5 != null && !(arg5.isBlank())) {
        } else {
        }
        Object var9 = getUserId();
        CompletableFuture.runAsync(() -> {
    try {
        Object var7 = new JsonObject();
        var7.addProperty("title", "Stash Found!");
        var7.addProperty("description", ("Coordinates: **X: " + arg0 + " Z: " + arg1 + "** (" + arg2 + ")"));
        var7.addProperty("color", 3066993);
        Object var8 = new JsonArray();
        Object var9 = new JsonObject();
        var9.addProperty("name", "Server");
        var9.addProperty("value", arg3);
        var9.addProperty("inline", false);
        var8.add(var9);
        Object var10 = arg4.entrySet().iterator();
        while (var10.hasNext()) {
            Object var11 = ((Map.Entry) var10.next());
            Object var12 = new JsonObject();
            var12.addProperty("name", ((String) var11.getKey()));
            var12.addProperty("value", String.valueOf(var11.getValue()));
            var12.addProperty("inline", true);
            var8.add(var12);
        }
        var7.add("fields", var8);
        var10 = new JsonArray();
        var10.add(var7);
        var11 = new JsonObject();
        if (arg5 != null) {
            if (arg5.isBlank()) {
            }
        } else {
        }
        "content".addProperty("", ("<@" + arg5 + ">"));
        var11.add("embeds", var10);
        var12 = var11.toString().getBytes(StandardCharsets.UTF_8);
        Object var13 = URI.create(arg6).toURL();
        Object var14 = ((HttpURLConnection) var13.openConnection());
        var14.setRequestMethod("POST");
        var14.setRequestProperty("Content-Type", "application/json");
        var14.setRequestProperty("User-Agent", "Mozilla/5.0");
        var14.setDoOutput(true);
        var14.setConnectTimeout(8000);
        var14.setReadTimeout(8000);
        Object var15 = var14.getOutputStream();
        try {
            var15.write(var12);
            var15.flush();
            if (var15 == null) break label_13;
            var15.close();
        } catch (Throwable ex8) {
            Object var16 = ex8;
            if (var15 == null) break label_12;
            try {
                var15.close();
            } catch (Throwable ex11) {
                Object var17 = ex11;
                var16.addSuppressed(var17);
            }
            label_12:
            throw var16;
        }
        label_13:
        var15 = var14.getResponseCode();
        var14.disconnect();
    } catch (Exception ex15) {
        var7 = ex15;
    }
});
    }

    static {
        webhookUrl = "";
        userId = "";
        loaded = false;
        lastSentAtMs = 0L;
    }

}
