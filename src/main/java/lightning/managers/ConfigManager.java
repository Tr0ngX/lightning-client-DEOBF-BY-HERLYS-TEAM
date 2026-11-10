package lightning.managers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.Objects;
import java.util.StringJoiner;
import lightning.LightningClient;
import lightning.modules.Module;
import lightning.modules.impl.core.PingBypassModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ImageSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.PositionSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.minecraft.IdentifierUtils;
import lightning.utils.system.FileUtils;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;

public class ConfigManager {
    private String currentConfig;
    private boolean loadingConfig;

    public ConfigManager() {
        this.currentConfig = "default";
        this.loadingConfig = false;
        loadConfig();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveConfig));
    }

    public void loadConfig() {
        try {
            migrateOldConfig();
            FileUtils.createDirectory("Lightning");
            FileUtils.createDirectory("Lightning/Configs");
            FileUtils.createDirectory("Lightning/Client");
            FileUtils.createDirectory("Lightning/Modules");
            FileUtils.createDirectory("Lightning/Binds");
            FileUtils.createDirectory("Lightning/Visuals");
            FileUtils.createDirectory("Lightning/GlobalConfigs");
            loadGeneral();
            loadWaypoints();
            loadModules(this.currentConfig);
        } catch (Exception ex2) {
            Object var1 = ex2;
            LightningClient.LOGGER.error("Failed to load the client's configuration!", var1);
            if (LightningClient.CHAT_MANAGER == null) {
                break label_4;
            }
            LightningClient.CHAT_MANAGER.await("The configuration has not been loaded properly. Read the stacktrace for more information.");
        }
        label_4:
    }

    private void migrateOldConfig() {
        try {
            Object var1 = Paths.get("EUClient", new String[0]);
            Object var2 = Paths.get("Lightning", new String[0]);
            if (!(Files.exists(var1, new LinkOption[0]))) break label_2;
            if (Files.exists(var2, new LinkOption[0])) {
                if (Files.exists(var2.resolve("Configs"), new LinkOption[0])) break label_2;
            }
            LightningClient.LOGGER.info("Migrating configuration from EUClient to Lightning...");
            copyDirectory(var1, var2);
            label_2:
        } catch (Exception ex3) {
            var1 = ex3;
            LightningClient.LOGGER.warn("Failed to migrate old EUClient configuration: {}", var1.getMessage());
        }
    }

    private static void copyDirectory(Path arg0, Path arg1) throws IOException {
        Object var2 = Files.walk(arg0, new FileVisitOption[0]);
        try {
            var2.forEach((arg2) -> {
    try {
        Object var3 = arg0.resolve(arg1.relativize(arg2));
        if (Files.isDirectory(arg2, new LinkOption[0])) {
            if (Files.exists(var3, new LinkOption[0])) break label_3;
            Files.createDirectories(var3, new FileAttribute[0]);
            break label_3;
        }
        if (Files.exists(var3, new LinkOption[0])) break label_3;
        if (var3.getParent() != null && !(Files.exists(var3.getParent(), new LinkOption[0]))) {
            Files.createDirectories(var3.getParent(), new FileAttribute[0]);
        }
        var tmp0 = new CopyOption[1];
        tmp0[0] = StandardCopyOption.COPY_ATTRIBUTES;
        Files.copy(arg2, var3, tmp0);
        label_3:
    } catch (IOException ex4) {
        var3 = ex4;
        LightningClient.LOGGER.warn("Could not copy config file {}: {}", arg2, var3.getMessage());
    }
});
            if (var2 == null) break label_7;
            var2.close();
        } catch (Throwable ex2) {
            Object var3 = ex2;
            if (var2 == null) break label_6;
            try {
                var2.close();
            } catch (Throwable ex5) {
                Object var4 = ex5;
                var3.addSuppressed(var4);
            }
            label_6:
            throw var3;
        }
        label_7:
    }

    public void saveConfig() {
        try {
            FileUtils.createDirectory("Lightning");
            FileUtils.createDirectory("Lightning/Configs");
            FileUtils.createDirectory("Lightning/Client");
            FileUtils.createDirectory("Lightning/Modules");
            FileUtils.createDirectory("Lightning/Binds");
            FileUtils.createDirectory("Lightning/Visuals");
            FileUtils.createDirectory("Lightning/GlobalConfigs");
            saveGeneral();
            saveWaypoints();
            saveModules(this.currentConfig);
        } catch (IOException ex2) {
            Object var1 = ex2;
            LightningClient.LOGGER.error("Failed to save the client's configuration!", var1);
        }
    }

    public void loadWaypoints() throws IOException {
        if (!(FileUtils.fileExists("Lightning/Waypoints.json"))) return;
        Object var1 = Files.newInputStream(Paths.get("Lightning/Waypoints.json", new String[0]), new OpenOption[0]);
        try {
            Object var2 = JsonParser.parseReader(new InputStreamReader(var1)).getAsJsonObject();
        } catch (IllegalStateException ex3) {
            Object var3 = ex3;
            LightningClient.LOGGER.error("Failed to load the client's Waypoint configuration!", var3);
            LightningClient.CHAT_MANAGER.await("The Waypoint configuration has not been loaded properly. Read the stacktrace for more information.");
            if (Collections.singletonList(var1).get(0) != null) {
                var1.close();
            }
            if (Collections.singletonList(var1).get(0) != null) {
                var1.close();
            }
            return;
        }
        if (!(var2.has("Waypoints"))) break label_9;
        var3 = var2.get("Waypoints").getAsJsonArray().iterator();
        while (var3.hasNext()) {
            Object var4 = ((JsonElement) var3.next());
            Object var5 = var4.getAsString().split(":");
            if (LightningClient.WAYPOINT_MANAGER.contains(var5[0])) {
                break label_7;
            }
            LightningClient.WAYPOINT_MANAGER.add(var5[0], new Vec3(((double) Integer.parseInt(var5[1])), ((double) Integer.parseInt(var5[2])), ((double) Integer.parseInt(var5[3]))), var5[4], var5[5]);
        }
        label_9:
        if (Collections.singletonList(var1).get(0) == null) break label_13;
        var1.close();
        break label_13;
        Object var6 = null;
        if (Collections.singletonList(var1).get(0) != null) {
            var1.close();
        }
        throw var6;
        label_13:
    }

    public void loadGeneral() throws IOException {
        if (!(FileUtils.fileExists("Lightning/General.json"))) return;
        Object var1 = Files.newInputStream(Paths.get("Lightning/General.json", new String[0]), new OpenOption[0]);
        Object var3 = JsonParser.parseReader(new InputStreamReader(var1));
        if (var3 == null) break label_2;
        if (!(var3.isJsonObject())) {
            label_2:
            LightningClient.LOGGER.warn("General.json is empty or invalid JSON.");
            if (Collections.singletonList(var1).get(0) != null) {
                var1.close();
            }
            return;
            try {
            }
            Object var2 = var3.getAsJsonObject();
        } catch (Exception ex7) {
            Object var4 = ex7;
            LightningClient.LOGGER.error("Failed to load the client's General configuration!", var4);
            if (LightningClient.CHAT_MANAGER != null) {
                LightningClient.CHAT_MANAGER.await("The General configuration has not been loaded properly. Read the stacktrace for more information.");
            }
            if (Collections.singletonList(var1).get(0) != null) {
                var1.close();
            }
            return;
        }
        if (var2.has("Config")) {
            this.currentConfig = var2.get("Config").getAsString();
        }
        if (var2.has("Prefix") && LightningClient.COMMAND_MANAGER != null) {
            LightningClient.COMMAND_MANAGER.setPrefix(var2.get("Prefix").getAsString());
        }
        if ((!(var2.has("Friends"))) || (LightningClient.FRIEND_MANAGER == null)) break label_15;
        var4 = var2.get("Friends").getAsJsonArray().iterator();
        while (var4.hasNext()) {
            Object var5 = ((JsonElement) var4.next());
            if (LightningClient.FRIEND_MANAGER.contains(var5.getAsString())) {
                break label_13;
            }
            LightningClient.FRIEND_MANAGER.add(var5.getAsString());
        }
        label_15:
        if ((!(var2.has("Macros"))) || (LightningClient.MACRO_MANAGER == null)) break label_24;
        var4 = var2.get("Macros").getAsJsonArray().iterator();
        while (var4.hasNext()) {
            var5 = ((JsonElement) var4.next());
            try {
                Object var6 = var5.getAsString().split(":", 2);
                if (var6.length <= 1) {
                    break label_16;
                    try {
                    }
                    int var7 = Integer.parseInt(var6[0]);
                    Object var8 = var6[1];
                    LightningClient.MACRO_MANAGER.add(var8, var7);
                } catch (NumberFormatException ex21) {
                    var6 = ex21;
                    LightningClient.LOGGER.error(("Failed to load the " + var5.getAsString() + " macro!"), var6);
                    if (LightningClient.CHAT_MANAGER == null) {
                        break label_16;
                    }
                    LightningClient.CHAT_MANAGER.await(("The " + var5.getAsString() + " macro has failed to load. Read the stacktrace for more information."));
                }
            }
            label_24:
            if (Collections.singletonList(var1).get(0) == null) break label_28;
            var1.close();
            break label_28;
            Object var9 = null;
            if (Collections.singletonList(var1).get(0) != null) {
                var1.close();
            }
            throw var9;
            label_28:
        }
    }

    public void saveGeneral() throws IOException {
        FileUtils.resetFile("Lightning/General.json");
        Object var1 = new JsonObject();
        var1.add("Config", new JsonPrimitive(this.currentConfig));
        var1.add("Prefix", new JsonPrimitive(LightningClient.COMMAND_MANAGER.getPrefix()));
        Object var2 = new JsonArray();
        LightningClient.FRIEND_MANAGER.getFriends().forEach((arg1) -> arg0.add(arg1));
        var1.add("Friends", var2);
        Object var3 = new JsonArray();
        LightningClient.MACRO_MANAGER.getMacros().forEach((arg1, arg2) -> arg0.add((arg2 + ":" + arg1)));
        var1.add("Macros", var3);
        Object var4 = new OutputStreamWriter(new FileOutputStream("Lightning/General.json"), StandardCharsets.UTF_8);
        var4.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(var1.toString())));
        if (Collections.singletonList(var4).get(0) == null) break label_5;
        var4.close();
        break label_5;
        Object var5 = null;
        if (Collections.singletonList(var4).get(0) != null) {
            var4.close();
        }
        throw var5;
        label_5:
    }

    public void saveWaypoints() throws IOException {
        FileUtils.resetFile("Lightning/Waypoints.json");
        Object var1 = new JsonObject();
        Object var2 = new JsonArray();
        Object var3 = LightningClient.WAYPOINT_MANAGER.getWaypoints().iterator();
        while (var3.hasNext()) {
            Object var4 = ((WaypointManager.Waypoint) var3.next());
            var2.add((var4.getName() + ":" + ((int) var4.getPos().x) + ":" + ((int) var4.getPos().y) + ":" + ((int) var4.getPos().z) + ":" + var4.getDimension() + ":" + var4.getServer()));
        }
        var1.add("Waypoints", var2);
        var3 = new OutputStreamWriter(new FileOutputStream("Lightning/Waypoints.json"), StandardCharsets.UTF_8);
        var3.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(var1.toString())));
        if (Collections.singletonList(var3).get(0) == null) break label_7;
        var3.close();
        break label_7;
        Object var5 = null;
        if (Collections.singletonList(var3).get(0) != null) {
            var3.close();
        }
        throw var5;
        label_7:
    }

    public void loadModules(String arg0) throws IOException {
        if (!(FileUtils.fileExists(("Lightning/Configs/" + arg0 + ".json")))) return;
        Object var2 = Files.newInputStream(Paths.get(("Lightning/Configs/" + arg0 + ".json"), new String[0]), new OpenOption[0]);
        Object var5 = JsonParser.parseReader(new InputStreamReader(var2));
        if (var5 == null) break label_2;
        if (!(var5.isJsonObject())) {
            label_2:
            LightningClient.LOGGER.warn("Module configuration is empty or invalid JSON: {}", arg0);
            if (Collections.singletonList(var2).get(0) != null) {
                var2.close();
            }
            return;
            try {
            }
            Object var4 = var5.getAsJsonObject();
        } catch (Exception ex7) {
            Object var6 = ex7;
            LightningClient.LOGGER.error("Failed to load the client's Module configuration!", var6);
            if (LightningClient.CHAT_MANAGER != null) {
                LightningClient.CHAT_MANAGER.await("The configuration for the Modules has not been loaded properly. Read the stacktrace for more information.");
            }
            if (Collections.singletonList(var2).get(0) != null) {
                var2.close();
            }
            return;
        }
        if (!(var4.has("Modules"))) {
            if (Collections.singletonList(var2).get(0) != null) {
                var2.close();
            }
            return;
        }
        var6 = var4.get("Modules").getAsJsonObject();
        this.loadingConfig = true;
        loadModulesInner(var6);
        this.loadingConfig = false;
        break label_18;
        Object var7 = null;
        this.loadingConfig = false;
        throw var7;
        label_18:
        this.currentConfig = arg0;
        try {
            saveGeneral();
        } catch (IOException ex21) {
            var7 = ex21;
        }
        if (((!PingBypassFlags.proxyForwardingActive) || (LightningClient.PINGBYPASS_CONFIG == null)) || (LightningClient.PINGBYPASS_CONFIG.isServer())) break label_23;
        var tmp0 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class));
        Object var3 = tmp0;
        if ((tmp0 == null) || (!(var3.isToggled()))) break label_23;
        var3.syncAllSettingsToProxy();
        label_23:
        if (Collections.singletonList(var2).get(0) == null) break label_27;
        var2.close();
        break label_27;
        Object var8 = null;
        if (Collections.singletonList(var2).get(0) != null) {
            var2.close();
        }
        throw var8;
        label_27:
    }

    private void loadModulesInner(JsonObject arg0) {
        Object var2 = LightningClient.MODULE_MANAGER.getModules().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Module) var2.next());
            if (!(arg0.has(var3.getName()))) {
                var3.setToggled(false);
                var3.resetValues();
                break label_0;
            }
            applyModuleJson(var3, arg0.get(var3.getName()).getAsJsonObject());
        }
    }

    public void applyModuleJson(Module arg0, JsonObject arg1) {
        applyModuleJson(arg0, arg1, true);
    }

    public void applyModuleJson(Module arg0, JsonObject arg1, boolean arg2) {
        if (!(arg1.has("Status"))) break label_0;
        int var4 = (arg1.get("Status").getAsBoolean() ? 1 : 0);
        if (arg1.has("Settings")) break label_3;
        if (!arg2) break label_2;
        arg0.resetValues();
        label_2:
        arg0.setToggled(var4, false);
        break label_40;
        label_3:
        Object var5 = arg1.get("Settings").getAsJsonObject();
        Object var6 = arg0.getSettings().iterator();
        while (var6.hasNext()) {
            Object var7 = ((Setting) var6.next());
            if (!arg2) {
                if ((var7 instanceof BindSetting)) break label_38;
            }
            Object var8 = var5.get(var7.getName());
            if (var8 != null && var8.isJsonPrimitive()) {
                Object var9 = var7;
                Objects.requireNonNull(var9);
                Object var10 = var9;
                int var11 = 0;
                switch (typeSwitch(var10, var11)) {
                    case 0 -> { break label_7; }
                    case 1 -> { break label_9; }
                    case 2 -> { break label_11; }
                    case 3 -> { break label_12; }
                    case 4 -> { break label_14; }
                    case 5 -> { break label_15; }
                    case 6 -> { break label_18; }
                    case 7 -> { break label_19; }
                    case 8 -> { break label_20; }
                    default -> { break label_26; }
                }
                label_7:
                Object var12 = ((BooleanSetting) var10);
                if (var8.getAsJsonPrimitive().isBoolean()) {
                    var12.setValue(var8.getAsBoolean());
                    break label_26;
                }
                var12.resetValue();
                break label_26;
                label_9:
                Object var13 = ((NumberSetting) var10);
                if (var8.getAsJsonPrimitive().isNumber()) {
                    var13.setValue(var8.getAsNumber());
                    break label_26;
                }
                var13.resetValue();
                break label_26;
                label_11:
                Object var14 = ((ModeSetting) var10);
                var14.setValue(var8.getAsString());
                break label_26;
                label_12:
                Object var15 = ((StringSetting) var10);
                Object var16 = var8.getAsString();
                if (var16 != null && var16.equalsIgnoreCase("EUClient")) {
                    var16 = "Lightning";
                }
                var15.setValue(var16);
                break label_26;
                label_14:
                Object var17 = ((ImageSetting) var10);
                var17.setValue(var8.getAsString());
                if (LightningClient.IMAGE_MANAGER == null) break label_26;
                LightningClient.IMAGE_MANAGER.setCurrentActiveImage(var8.getAsString());
                break label_26;
                label_15:
                Object var18 = ((BindSetting) var10);
                if (var8.isJsonPrimitive() && var8.getAsJsonPrimitive().isNumber()) {
                    var18.setValue(var8.getAsInt());
                    break label_26;
                }
                Object var19 = var8.getAsString().split(",", 2);
                var18.setValue(Integer.parseInt(var19[0]));
                if (var19.length > 1) {
                    var18.setMode(var19[1]);
                }
                break label_26;
                label_18:
                var19 = ((ColorSetting) var10);
                Object var20 = var8.getAsString().split(",");
                if (var20.length != 6) break label_26;
                var19.setColor(new Color(Math.clamp(((long) Integer.parseInt(var20[0])), 0, 255), Math.clamp(((long) Integer.parseInt(var20[1])), 0, 255), Math.clamp(((long) Integer.parseInt(var20[2])), 0, 255), Math.clamp(((long) Integer.parseInt(var20[3])), 0, 255)));
                var19.setSync(Boolean.parseBoolean(var20[4]));
                var19.setRainbow(Boolean.parseBoolean(var20[5]));
                break label_26;
                label_19:
                Object var21 = ((PositionSetting) var10);
                Object var22 = var8.getAsString().split(",");
                if (var22.length != 2) break label_26;
                var21.set(Float.parseFloat(var22[0]), Float.parseFloat(var22[1]));
                break label_26;
                label_20:
                Object var23 = ((WhitelistSetting) var10);
                Object var24 = ((WhitelistSetting) var9);
                var24.clear();
                Object var25 = var8.getAsString().split(",");
                if (var25.length == 0) break label_26;
                Object var26 = var25;
                int var27 = var26.length;
                int var28 = 0;
                while (var28 < var27) {
                    Object var29 = var26[var28];
                    if (var24.getType() == WhitelistSetting.Type.ITEMS) {
                        Object var30 = IdentifierUtils.getItem(var29);
                        if (var30 != null) {
                            var24.add(var30);
                        }
                        break label_24;
                    }
                    if (var24.getType() != WhitelistSetting.Type.BLOCKS) break label_24;
                    var30 = IdentifierUtils.getBlock(var29);
                    if (var30 == null) break label_24;
                    var24.add(var30);
                    label_24:
                    var28++;
                }
                break label_26;
                label_26:
                break label_38;
            }
            Objects.requireNonNull(var7);
            var9 = var7;
            var10 = 0;
            switch (typeSwitch(var9, var10)) {
                case 0 -> { break label_29; }
                case 1 -> { break label_30; }
                case 2 -> { break label_31; }
                case 3 -> { break label_32; }
                case 4 -> { break label_33; }
                case 5 -> { break label_34; }
                case 6 -> { break label_35; }
                case 7 -> { break label_36; }
                case 8 -> { break label_37; }
                default -> { break label_38; }
            }
            label_29:
            var11 = ((BooleanSetting) var9);
            var11.resetValue();
            break label_38;
            label_30:
            var12 = ((NumberSetting) var9);
            var12.resetValue();
            break label_38;
            label_31:
            var13 = ((ModeSetting) var9);
            var13.resetValue();
            break label_38;
            label_32:
            var14 = ((StringSetting) var9);
            var14.resetValue();
            break label_38;
            label_33:
            var15 = ((ImageSetting) var9);
            var15.resetValue();
            break label_38;
            label_34:
            var16 = ((BindSetting) var9);
            var16.resetValue();
            break label_38;
            label_35:
            var17 = ((ColorSetting) var9);
            var17.resetValue();
            break label_38;
            label_36:
            var18 = ((WhitelistSetting) var9);
            var18.clear();
            break label_38;
            label_37:
            var19 = ((PositionSetting) var9);
            var19.resetValue();
            break label_38;
            label_38:
        }
        arg0.setToggled(var4, false);
        label_40:
    }

    public void saveModules(String arg0) throws IOException {
        FileUtils.resetFile(("Lightning/Configs/" + arg0 + ".json"));
        Object var2 = new JsonObject();
        var2.add("Config", new JsonPrimitive(arg0));
        Object var3 = new JsonObject();
        Object var4 = LightningClient.MODULE_MANAGER.getModules().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Module) var4.next());
            Object var6 = serializeModuleJson(var5);
            if (!((var5 instanceof FakePlayerModule))) break label_1;
            var6.add("Status", new JsonPrimitive(false));
            label_1:
            var3.add(var5.getName(), var6);
        }
        var2.add("Modules", var3);
        var4 = new OutputStreamWriter(new FileOutputStream(("Lightning/Configs/" + arg0 + ".json")), StandardCharsets.UTF_8);
        var4.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(var2.toString())));
        this.currentConfig = arg0;
        try {
            saveGeneral();
        } catch (IOException ex6) {
            var5 = ex6;
        }
        if (Collections.singletonList(var4).get(0) == null) break label_11;
        var4.close();
        break label_11;
        Object var7 = null;
        if (Collections.singletonList(var4).get(0) != null) {
            var4.close();
        }
        throw var7;
        label_11:
    }

    public JsonObject serializeModuleJson(Module arg0) {
        return serializeModuleJson(arg0, true);
    }

    public JsonObject serializeModuleJson(Module arg0, boolean arg1) {
        Object var3 = new JsonObject();
        var3.add("Status", new JsonPrimitive(arg0.isToggled()));
        Object var4 = new JsonObject();
        Object var5 = arg0.getSettings().iterator();
        while (var5.hasNext()) {
            Object var6 = ((Setting) var5.next());
            if (arg1) break label_1;
            if ((var6 instanceof BindSetting)) break label_14;
            label_1:
            Object var7 = var6;
            Objects.requireNonNull(var7);
            Object var8 = var7;
            int var9 = 0;
            switch (typeSwitch(var8, var9)) {
                case 0 -> { break label_3; }
                case 1 -> { break label_4; }
                case 2 -> { break label_5; }
                case 3 -> { break label_6; }
                case 4 -> { break label_7; }
                case 5 -> { break label_8; }
                case 6 -> { break label_9; }
                case 7 -> { break label_10; }
                case 8 -> { break label_13; }
                default -> { break label_14; }
            }
            label_3:
            Object var10 = ((BooleanSetting) var8);
            var4.add(var10.getName(), new JsonPrimitive(var10.getValue()));
            break label_14;
            label_4:
            Object var11 = ((NumberSetting) var8);
            var4.add(var11.getName(), new JsonPrimitive(var11.getValue()));
            break label_14;
            label_5:
            Object var12 = ((ModeSetting) var8);
            var4.add(var12.getName(), new JsonPrimitive(var12.getValue()));
            break label_14;
            label_6:
            Object var13 = ((StringSetting) var8);
            var4.add(var13.getName(), new JsonPrimitive(var13.getValue()));
            break label_14;
            label_7:
            Object var14 = ((ImageSetting) var8);
            var4.add(var14.getName(), new JsonPrimitive(var14.getValue()));
            break label_14;
            label_8:
            Object var15 = ((BindSetting) var8);
            var4.add(var15.getName(), new JsonPrimitive((var15.getValue() + "," + var15.getMode())));
            break label_14;
            label_9:
            Object var16 = ((ColorSetting) var8);
            var4.add(var16.getName(), new JsonPrimitive((var16.getValue().getColor().getRed() + "," + var16.getValue().getColor().getGreen() + "," + var16.getValue().getColor().getBlue() + "," + var16.getValue().getColor().getAlpha() + "," + var16.isSync() + "," + var16.isRainbow())));
            break label_14;
            label_10:
            Object var17 = ((WhitelistSetting) var8);
            Object var18 = new StringJoiner(",");
            Object var19 = var17.getWhitelistIds().iterator();
            while (var19.hasNext()) {
                Object var20 = ((String) var19.next());
                var18.add(var20);
            }
            var4.add(var17.getName(), new JsonPrimitive(var18.toString()));
            break label_14;
            label_13:
            var19 = ((PositionSetting) var8);
            var20 = ((PositionSetting) var7);
            var4.add(var20.getName(), new JsonPrimitive((var20.getX() + "," + var20.getY())));
            break label_14;
            label_14:
        }
        var3.add("Settings", var4);
        return var3;
    }

    @Generated
    public String getCurrentConfig() {
        return this.currentConfig;
    }

    @Generated
    public boolean isLoadingConfig() {
        return this.loadingConfig;
    }

    @Generated
    public void setCurrentConfig(String arg0) {
        this.currentConfig = arg0;
    }

    @Generated
    public void setLoadingConfig(boolean arg0) {
        this.loadingConfig = arg0;
    }

}
