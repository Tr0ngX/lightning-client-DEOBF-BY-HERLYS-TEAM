package lightning.accounts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lightning.LightningClient;
import net.minecraft.util.Util;

public class MicrosoftAuth {
    public static final String CLIENT_ID = "54fd49e4-2103-4044-9603-2b028c814ec3";
    public static final String SCOPE = "XboxLive.signin XboxLive.offline_access";
    public static final String REDIRECT_PATH = "/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something";

    public static MicrosoftAuth.BrowserLoginSession startBrowserLogin(Consumer arg0) throws Exception {
        Object var1 = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int var2 = var1.getAddress().getPort();
        Object var3 = ("http://localhost:" + var2 + "/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something");
        Object var4 = UUID.randomUUID().toString().replace("-", "");
        Object var5 = ("https://login.live.com/oauth20_authorize.srf?client_id=54fd49e4-2103-4044-9603-2b028c814ec3&response_type=code&scope=" + URLEncoder.encode("XboxLive.signin XboxLive.offline_access", StandardCharsets.UTF_8) + "&redirect_uri=" + URLEncoder.encode(var3, StandardCharsets.UTF_8) + "&prompt=select_account&state=" + var4);
        Object var6 = new CompletableFuture();
        var1.createContext("/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something", (arg2) -> {
    Object var3 = arg2.getRequestURI().getRawQuery();
    Object var4 = parseQueryParams(var3);
    if (!(var4.containsKey("code"))) break label_8;
    Object var5 = ((String) var4.get("code"));
    Object var6 = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Lightning</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #6450a0;box-shadow:0 8px 24px rgba(0,0,0,0.5);}h1{color:#55ffff;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Successful!</h1><p>You can close this browser tab and return to Minecraft.</p></div></body></html>";
    Object var7 = var6.getBytes(StandardCharsets.UTF_8);
    arg2.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
    arg2.sendResponseHeaders(200, ((long) var7.length));
    Object var8 = arg2.getResponseBody();
    try {
        var8.write(var7);
        if (var8 == null) break label_7;
        var8.close();
    } catch (Throwable ex2) {
        Object var9 = ex2;
        if (var8 == null) break label_6;
        try {
            var8.close();
        } catch (Throwable ex5) {
            Object var10 = ex5;
            var9.addSuppressed(var10);
        }
        label_6:
        throw var9;
    }
    label_7:
    arg0.complete(var5);
    break label_17;
    label_8:
    var5 = ((String) var4.getOrDefault("error_description", ((String) var4.getOrDefault("error", "Login was cancelled or failed"))));
    var6 = ("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Lightning</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #a03030;}h1{color:#ff5555;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Cancelled</h1><p>" + var5 + "</p></div></body></html>");
    var7 = var6.getBytes(StandardCharsets.UTF_8);
    arg2.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
    arg2.sendResponseHeaders(400, ((long) var7.length));
    var8 = arg2.getResponseBody();
    try {
        var8.write(var7);
        if (var8 == null) break label_16;
        var8.close();
    } catch (Throwable ex11) {
        var9 = ex11;
        if (var8 == null) break label_15;
        try {
            var8.close();
        } catch (Throwable ex14) {
            var10 = ex14;
            var9.addSuppressed(var10);
        }
        label_15:
        throw var9;
    }
    label_16:
    arg0.completeExceptionally(new RuntimeException(var5));
    label_17:
    new Thread(() -> {
    try {
        Thread.sleep(1000L);
    } catch (InterruptedException ex2) {
        Object var1 = ex2;
    }
    arg0.stop(0);
}).start();
});
        var1.start();
        Object var7 = var6.thenApplyAsync((arg2) -> {
    try {
        arg0.accept("Exchanging authorization code with Microsoft...");
        Object var3 = new HashMap();
        var3.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
        var3.put("grant_type", "authorization_code");
        var3.put("code", ((String) arg2));
        var3.put("redirect_uri", arg1);
        var3.put("scope", "XboxLive.signin XboxLive.offline_access");
        Object var4 = postForm("https://login.live.com/oauth20_token.srf", encodeParams(var3));
        if (!(var4.has("access_token"))) {
            if (var4.has("error_description")) {
            } else {
            }
            Object var5 = "Failed to obtain Microsoft token.";
            return new MicrosoftAuth.AuthResult(false, null, null, null, null, var5);
            try {
            }
            var5 = var4.get("access_token").getAsString();
            if (var4.has("refresh_token")) {
            } else {
            }
            Object var6 = "";
            return completeMinecraftLogin(var5, var6, arg0);
        } catch (Exception ex8) {
            var3 = ex8;
            LightningClient.LOGGER.error("Error during Microsoft login exchange", var3);
            return new MicrosoftAuth.AuthResult(false, null, null, null, null, var3.getMessage());
        }
    }
});
        try {
            Util.getPlatform().openUri(URI.create(var5));
        } catch (Exception ex2) {
            Object var8 = ex2;
        }
        return new MicrosoftAuth.BrowserLoginSession(var5, ((CompletableFuture) var7), () -> {
    arg0.cancel(true);
    try {
        arg1.stop(0);
    } catch (Exception ex2) {
        Object var2 = ex2;
    }
});
    }

    public static MicrosoftAuth.AuthResult refreshAccount(String arg0) {
        try {
            Object var1 = new HashMap();
            var1.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
            var1.put("grant_type", "refresh_token");
            var1.put("refresh_token", arg0);
            var1.put("scope", "XboxLive.signin XboxLive.offline_access");
            Object var2 = postForm("https://login.live.com/oauth20_token.srf", encodeParams(var1));
            if (!(var2.has("access_token"))) {
                if (var2.has("error_description")) {
                } else {
                }
                Object var3 = "Failed to refresh Microsoft token.";
                return new MicrosoftAuth.AuthResult(false, null, null, null, null, var3);
                try {
                }
                var3 = var2.get("access_token").getAsString();
                if (var2.has("refresh_token")) {
                } else {
                }
                Object var4 = arg0;
                return completeMinecraftLogin(var3, var4, (arg0) -> {
});
            } catch (Exception ex8) {
                var1 = ex8;
                LightningClient.LOGGER.error("Failed to refresh Microsoft account", var1);
                return new MicrosoftAuth.AuthResult(false, null, null, null, null, var1.getMessage());
            }
        }
    }

    public static MicrosoftAuth.AuthResult completeMinecraftLogin(String arg0, String arg1, Consumer arg2) throws Exception {
        arg2.accept("Authenticating with Xbox Live...");
        Object var3 = new JsonObject();
        Object var4 = new JsonObject();
        var4.addProperty("AuthMethod", "RPS");
        var4.addProperty("SiteName", "user.auth.xboxlive.com");
        var4.addProperty("RpsTicket", ("d=" + arg0));
        var3.add("Properties", var4);
        var3.addProperty("RelyingParty", "http://auth.xboxlive.com");
        var3.addProperty("TokenType", "JWT");
        Object var5 = postJson("https://user.auth.xboxlive.com/user/authenticate", var3);
        Object var6 = var5.get("Token").getAsString();
        Object var7 = var5.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
        arg2.accept("Requesting XSTS authorization...");
        Object var8 = new JsonObject();
        Object var9 = new JsonObject();
        var9.addProperty("SandboxId", "RETAIL");
        Object var10 = new JsonArray();
        var10.add(var6);
        var9.add("UserTokens", var10);
        var8.add("Properties", var9);
        var8.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        var8.addProperty("TokenType", "JWT");
        Object var11 = postJson("https://xsts.auth.xboxlive.com/xsts/authorize", var8);
        if (var11.has("XErr")) {
            long var12 = var11.get("XErr").getAsLong();
            if (var12 == 2148916233L) return new MicrosoftAuth.AuthResult(false, null, null, null, null, "This Microsoft account does not have an Xbox Live account.");
            if (var12 == 2148916238L) return new MicrosoftAuth.AuthResult(false, null, null, null, null, "This account is a child account and must be added to a Family.");
            return new MicrosoftAuth.AuthResult(false, null, null, null, null, ("XSTS Error: " + var12));
        }
        var12 = var11.get("Token").getAsString();
        arg2.accept("Logging into Minecraft Services...");
        Object var13 = new JsonObject();
        var13.addProperty("identityToken", ("XBL3.0 x=" + var7 + ";" + var12));
        Object var14 = postJson("https://api.minecraftservices.com/authentication/login_with_xbox", var13);
        if (!(var14.has("access_token"))) {
            Object var15 = "Failed to obtain Minecraft access token.";
            if (var14.has("errorMessage")) {
                var15 = var14.get("errorMessage").getAsString();
                break label_4;
            }
            if (!(var14.has("error"))) break label_4;
            var15 = var14.get("error").getAsString();
            label_4:
            return new MicrosoftAuth.AuthResult(false, null, null, null, null, var15);
        }
        var15 = var14.get("access_token").getAsString();
        arg2.accept("Retrieving Minecraft profile...");
        Object var16 = getJson("https://api.minecraftservices.com/minecraft/profile", var15);
        if ((!(var16.has("name"))) || (!(var16.has("id")))) {
            return new MicrosoftAuth.AuthResult(false, null, null, null, null, "You do not own Minecraft on this Microsoft account.");
        }
        Object var17 = var16.get("name").getAsString();
        Object var18 = var16.get("id").getAsString();
        arg2.accept(("Logged in as " + var17 + "!"));
        return new MicrosoftAuth.AuthResult(true, var17, var18, var15, arg1, null);
    }

    private static JsonObject postForm(String arg0, String arg1) throws Exception {
        Object var2 = new URI(arg0).toURL();
        Object var3 = ((HttpURLConnection) var2.openConnection());
        var3.setRequestMethod("POST");
        var3.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        var3.setRequestProperty("Accept", "application/json");
        var3.setDoOutput(true);
        var3.setConnectTimeout(10000);
        var3.setReadTimeout(15000);
        Object var4 = var3.getOutputStream();
        try {
            var4.write(arg1.getBytes(StandardCharsets.UTF_8));
            if (var4 == null) break label_7;
            var4.close();
        } catch (Throwable ex2) {
            Object var5 = ex2;
            if (var4 == null) break label_6;
            try {
                var4.close();
            } catch (Throwable ex5) {
                Object var6 = ex5;
                var5.addSuppressed(var6);
            }
            label_6:
            throw var5;
        }
        label_7:
        if (var3.getResponseCode() >= 200 && var3.getResponseCode() < 400) {
        } else {
        }
        var4 = new InputStreamReader(var3.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader(var4).getAsJsonObject();
    }

    private static JsonObject postJson(String arg0, JsonObject arg1) throws Exception {
        Object var2 = new URI(arg0).toURL();
        Object var3 = ((HttpURLConnection) var2.openConnection());
        var3.setRequestMethod("POST");
        var3.setRequestProperty("Content-Type", "application/json");
        var3.setRequestProperty("Accept", "application/json");
        var3.setDoOutput(true);
        var3.setConnectTimeout(10000);
        var3.setReadTimeout(15000);
        Object var4 = var3.getOutputStream();
        try {
            var4.write(arg1.toString().getBytes(StandardCharsets.UTF_8));
            if (var4 == null) break label_7;
            var4.close();
        } catch (Throwable ex2) {
            Object var5 = ex2;
            if (var4 == null) break label_6;
            try {
                var4.close();
            } catch (Throwable ex5) {
                Object var6 = ex5;
                var5.addSuppressed(var6);
            }
            label_6:
            throw var5;
        }
        label_7:
        if (var3.getResponseCode() >= 200 && var3.getResponseCode() < 400) {
        } else {
        }
        var4 = new InputStreamReader(var3.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader(var4).getAsJsonObject();
    }

    private static JsonObject getJson(String arg0, String arg1) throws Exception {
        Object var2 = new URI(arg0).toURL();
        Object var3 = ((HttpURLConnection) var2.openConnection());
        var3.setRequestMethod("GET");
        var3.setRequestProperty("Authorization", ("Bearer " + arg1));
        var3.setRequestProperty("Accept", "application/json");
        var3.setConnectTimeout(10000);
        var3.setReadTimeout(15000);
        if (var3.getResponseCode() >= 200 && var3.getResponseCode() < 400) {
        } else {
        }
        Object var4 = new InputStreamReader(var3.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader(var4).getAsJsonObject();
    }

    private static String encodeParams(Map arg0) {
        Object var1 = new StringBuilder();
        int var2 = 1;
        Object var3 = arg0.entrySet().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Map.Entry) var3.next());
            if (!(!var2)) {
                var2 = 0;
            } else {
                var1.append("&");
            }
            var1.append(URLEncoder.encode(((String) var4.getKey()), StandardCharsets.UTF_8));
            var1.append("=");
            var1.append(URLEncoder.encode(((String) var4.getValue()), StandardCharsets.UTF_8));
        }
        return var1.toString();
    }

    private static Map parseQueryParams(String arg0) {
        Object var1 = new HashMap();
        if ((arg0 == null) || (arg0.isEmpty())) {
            return var1;
        }
        Object var2 = arg0.split("&");
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            Object var6 = var5.split("=", 2);
            if (var6.length == 2) {
                var1.put(URLDecoder.decode(var6[0], StandardCharsets.UTF_8), URLDecoder.decode(var6[1], StandardCharsets.UTF_8));
                break label_5;
            }
            if (var6.length != 1) {
                break label_5;
            }
            var1.put(URLDecoder.decode(var6[0], StandardCharsets.UTF_8), "");
            label_5:
            var4++;
        }
        return var1;
    }


    public record AuthResult(boolean success, String username, String uuid, String accessToken, String refreshToken, String errorMessage) {

        public AuthResult(boolean arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
            this.success = arg0;
            this.username = arg1;
            this.uuid = arg2;
            this.accessToken = arg3;
            this.refreshToken = arg4;
            this.errorMessage = arg5;
        }

        public boolean success() {
            return this.success;
        }

        public String username() {
            return this.username;
        }

        public String uuid() {
            return this.uuid;
        }

        public String accessToken() {
            return this.accessToken;
        }

        public String refreshToken() {
            return this.refreshToken;
        }

        public String errorMessage() {
            return this.errorMessage;
        }

    }

    public record BrowserLoginSession(String loginUrl, CompletableFuture future, Runnable cancelAction) {

        public BrowserLoginSession(String arg0, CompletableFuture arg1, Runnable arg2) {
            this.loginUrl = arg0;
            this.future = arg1;
            this.cancelAction = arg2;
        }

        public String loginUrl() {
            return this.loginUrl;
        }

        public CompletableFuture future() {
            return this.future;
        }

        public Runnable cancelAction() {
            return this.cancelAction;
        }

    }
}
