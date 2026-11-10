package lightning.commands.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.Module;
import lightning.modules.impl.core.CapesModule;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.modules.impl.core.ColorModule;
import lightning.modules.impl.core.HUDModule;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.system.FileUtils;

@RegisterCommand(name = "config", aliases = "cfg", tag = "Config", description = "Allows you to manage the client's configuration system.", syntax = "<load|save|reset> <[name]> | <name> reset | global <[module]> <load|save|reset> <[name]> | bind <load|save|reset> <[name]> | visual <load|save|reset> <[name]> | modules <load|save|reset> <[name]> | <reload|save|current|reset>")
public class ConfigCommand extends Command {
    private static final List EXTRA_VISUAL_MODULES;

    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) return List.of("load", "save", "reset", "reload", "current", "global", "bind", "visual", "modules");
        if (arg0.length == 1) {
            if ((arg0[0].equalsIgnoreCase("load")) || (arg0[0].equalsIgnoreCase("save"))) break label_1;
            if (arg0[0].equalsIgnoreCase("reset")) {
                label_1:
                return savedConfigNames();
            }
            if (arg0[0].equalsIgnoreCase("global")) return LightningClient.MODULE_MANAGER.getModules().stream().map(Module::getName).toList();
            if (((arg0[0].equalsIgnoreCase("bind")) || (arg0[0].equalsIgnoreCase("visual"))) || (arg0[0].equalsIgnoreCase("modules"))) {
                return List.of("load", "save", "reset");
            }
            return List.of("reset", "load", "save");
        }
        if (arg0.length != 2) break label_13;
        if (arg0[0].equalsIgnoreCase("global")) return List.of("load", "save", "reset");
        if (arg0[0].equalsIgnoreCase("bind")) {
            if (((arg0[1].equalsIgnoreCase("load")) || (arg0[1].equalsIgnoreCase("save"))) || (arg0[1].equalsIgnoreCase("reset"))) {
                return bindProfileNames();
            }
        }
        if (arg0[0].equalsIgnoreCase("visual")) {
            if (((arg0[1].equalsIgnoreCase("load")) || (arg0[1].equalsIgnoreCase("save"))) || (arg0[1].equalsIgnoreCase("reset"))) {
                return visualConfigNames();
            }
        }
        if (!(arg0[0].equalsIgnoreCase("modules"))) break label_13;
        if (((arg0[1].equalsIgnoreCase("load")) || (arg0[1].equalsIgnoreCase("save"))) || (arg0[1].equalsIgnoreCase("reset"))) {
            return moduleStateNames();
        }
        label_13:
        if (arg0.length == 3 && arg0[0].equalsIgnoreCase("global")) {
            if (((arg0[2].equalsIgnoreCase("load")) || (arg0[2].equalsIgnoreCase("save"))) || (arg0[2].equalsIgnoreCase("reset"))) {
                return globalConfigNames(arg0[1]);
            }
        }
        return List.of();
    }

    private List savedConfigNames() {
        Object var2 = new File("Lightning/Configs");
        if (!(var2.exists())) {
            var2.mkdirs();
        }
        var tmp0 = var2.listFiles((arg0, arg1) -> arg1.endsWith(".json"));
        Object var1 = tmp0;
        if (tmp0 == null) return List.of();
        return Arrays.stream(var1).map(File::getName).map((arg0) -> arg0.substring(0, (arg0.length() - ".json".length()))).sorted().toList();
    }

    private List globalConfigNames(String arg0) {
        Object var3 = new File(("Lightning/GlobalConfigs/" + arg0));
        if (!(var3.exists())) {
            var3.mkdirs();
        }
        var tmp0 = var3.listFiles((arg0, arg1) -> arg1.endsWith(".json"));
        Object var2 = tmp0;
        if (tmp0 == null) return List.of();
        return Arrays.stream(var2).map(File::getName).map((arg0) -> arg0.substring(0, (arg0.length() - ".json".length()))).sorted().toList();
    }

    private List bindProfileNames() {
        Object var2 = new File("Lightning/Binds");
        if (!(var2.exists())) {
            var2.mkdirs();
        }
        var tmp0 = var2.listFiles((arg0, arg1) -> arg1.endsWith(".json"));
        Object var1 = tmp0;
        if (tmp0 == null) return List.of();
        return Arrays.stream(var1).map(File::getName).map((arg0) -> arg0.substring(0, (arg0.length() - ".json".length()))).sorted().toList();
    }

    private List visualConfigNames() {
        Object var2 = new File("Lightning/Visuals");
        if (!(var2.exists())) {
            var2.mkdirs();
        }
        var tmp0 = var2.listFiles((arg0, arg1) -> arg1.endsWith(".json"));
        Object var1 = tmp0;
        if (tmp0 == null) return List.of();
        return Arrays.stream(var1).map(File::getName).map((arg0) -> arg0.substring(0, (arg0.length() - ".json".length()))).sorted().toList();
    }

    private List moduleStateNames() {
        Object var2 = new File("Lightning/Modules");
        if (!(var2.exists())) {
            var2.mkdirs();
        }
        var tmp0 = var2.listFiles((arg0, arg1) -> arg1.endsWith(".json"));
        Object var1 = tmp0;
        if (tmp0 == null) return List.of();
        return Arrays.stream(var1).map(File::getName).map((arg0) -> arg0.substring(0, (arg0.length() - ".json".length()))).sorted().toList();
    }

    private List visualModules() {
        Object var1 = new ArrayList(LightningClient.MODULE_MANAGER.getModules(Module.Category.VISUALS));
        Object var2 = EXTRA_VISUAL_MODULES.iterator();
        while (var2.hasNext()) {
            Object var3 = ((Class) var2.next());
            Object var4 = LightningClient.MODULE_MANAGER.getModule(var3);
            if (var4 == null) {
                break label_0;
            }
            var1.add(var4);
        }
        return var1;
    }

    public void execute(String[] arg0) {
        if (arg0.length > 0 && arg0[0].equalsIgnoreCase("bind")) {
            if (arg0.length == 3) {
                Object var2 = arg0[1].toLowerCase();
                Object var3 = arg0[2];
                Object var4 = var2;
                int var5 = -1;
                switch (var4.hashCode()) {
                    case 3327206 -> { break label_1; }
                    case 3522941 -> { break label_0; }
                    case 108404047 -> { break label_2; }
                    default -> { break label_3; }
                }
                label_0:
                if (!(var4.equals("save"))) break label_3;
                var5 = 0;
                break label_3;
                label_1:
                if (!(var4.equals("load"))) break label_3;
                var5 = 1;
                break label_3;
                label_2:
                if (!(var4.equals("reset"))) break label_3;
                var5 = 2;
                label_3:
                switch (var5) {
                    case 0 -> { break label_4; }
                    case 1 -> { break label_5; }
                    case 2 -> { break label_6; }
                    default -> { break label_7; }
                }
                label_4:
                saveBindProfile(var3);
                break label_8;
                label_5:
                loadBindProfile(var3);
                break label_8;
                label_6:
                resetBindProfile(var3);
                break label_8;
                label_7:
                LightningClient.CHAT_MANAGER.info("config bind <load|save|reset> <name>");
                label_8:
            } else {
                LightningClient.CHAT_MANAGER.info("config bind <load|save|reset> <name>");
            }
            return;
        }
        if (arg0.length > 0 && arg0[0].equalsIgnoreCase("visual")) {
            if (arg0.length == 3) {
                var2 = arg0[1].toLowerCase();
                var3 = arg0[2];
                var4 = var2;
                var5 = -1;
                switch (var4.hashCode()) {
                    case 3327206 -> { break label_13; }
                    case 3522941 -> { break label_12; }
                    case 108404047 -> { break label_14; }
                    default -> { break label_15; }
                }
                label_12:
                if (!(var4.equals("save"))) break label_15;
                var5 = 0;
                break label_15;
                label_13:
                if (!(var4.equals("load"))) break label_15;
                var5 = 1;
                break label_15;
                label_14:
                if (!(var4.equals("reset"))) break label_15;
                var5 = 2;
                label_15:
                switch (var5) {
                    case 0 -> { break label_16; }
                    case 1 -> { break label_17; }
                    case 2 -> { break label_18; }
                    default -> { break label_19; }
                }
                label_16:
                saveVisualConfig(var3);
                break label_20;
                label_17:
                loadVisualConfig(var3);
                break label_20;
                label_18:
                resetVisualConfig(var3);
                break label_20;
                label_19:
                LightningClient.CHAT_MANAGER.info("config visual <load|save|reset> <name>");
                label_20:
            } else {
                LightningClient.CHAT_MANAGER.info("config visual <load|save|reset> <name>");
            }
            return;
        }
        if (arg0.length > 0 && arg0[0].equalsIgnoreCase("modules")) {
            if (arg0.length == 3) {
                var2 = arg0[1].toLowerCase();
                var3 = arg0[2];
                var4 = var2;
                var5 = -1;
                switch (var4.hashCode()) {
                    case 3327206 -> { break label_25; }
                    case 3522941 -> { break label_24; }
                    case 108404047 -> { break label_26; }
                    default -> { break label_27; }
                }
                label_24:
                if (!(var4.equals("save"))) break label_27;
                var5 = 0;
                break label_27;
                label_25:
                if (!(var4.equals("load"))) break label_27;
                var5 = 1;
                break label_27;
                label_26:
                if (!(var4.equals("reset"))) break label_27;
                var5 = 2;
                label_27:
                switch (var5) {
                    case 0 -> { break label_28; }
                    case 1 -> { break label_29; }
                    case 2 -> { break label_30; }
                    default -> { break label_31; }
                }
                label_28:
                saveModulesState(var3);
                break label_32;
                label_29:
                loadModulesState(var3);
                break label_32;
                label_30:
                resetModulesState(var3);
                break label_32;
                label_31:
                LightningClient.CHAT_MANAGER.info("config modules <load|save|reset> <name>");
                label_32:
            } else {
                LightningClient.CHAT_MANAGER.info("config modules <load|save|reset> <name>");
            }
            return;
        }
        if (arg0.length > 0 && arg0[0].equalsIgnoreCase("global")) {
            if (arg0.length == 4) {
                var2 = LightningClient.MODULE_MANAGER.getModule(arg0[1]);
                if (var2 == null) {
                    LightningClient.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
                    return;
                }
                var3 = arg0[2].toLowerCase();
                var4 = arg0[3];
                var5 = var3;
                int var6 = -1;
                switch (var5.hashCode()) {
                    case 3327206 -> { break label_38; }
                    case 3522941 -> { break label_37; }
                    case 108404047 -> { break label_39; }
                    default -> { break label_40; }
                }
                label_37:
                if (!(var5.equals("save"))) break label_40;
                var6 = 0;
                break label_40;
                label_38:
                if (!(var5.equals("load"))) break label_40;
                var6 = 1;
                break label_40;
                label_39:
                if (!(var5.equals("reset"))) break label_40;
                var6 = 2;
                label_40:
                switch (var6) {
                    case 0 -> { break label_41; }
                    case 1 -> { break label_42; }
                    case 2 -> { break label_43; }
                    default -> { break label_44; }
                }
                label_41:
                saveGlobalConfig(var2, var4);
                break label_45;
                label_42:
                loadGlobalConfig(var2, var4);
                break label_45;
                label_43:
                resetGlobalConfig(var2, var4);
                break label_45;
                label_44:
                LightningClient.CHAT_MANAGER.info("config global <module> <load|save|reset> <name>");
                label_45:
            } else {
                LightningClient.CHAT_MANAGER.info("config global <module> <load|save|reset> <name>");
            }
            return;
        }
        if (arg0.length != 2) break label_64;
        if (arg0[1].equalsIgnoreCase("reset")) {
            resetConfig(arg0[0]);
            return;
        }
        var2 = arg0[0].toLowerCase();
        var3 = -1;
        switch (var2.hashCode()) {
            case 3327206 -> { break label_50; }
            case 3522941 -> { break label_51; }
            case 108404047 -> { break label_52; }
            default -> { break label_53; }
        }
        label_50:
        if (!(var2.equals("load"))) break label_53;
        var3 = 0;
        break label_53;
        label_51:
        if (!(var2.equals("save"))) break label_53;
        var3 = 1;
        break label_53;
        label_52:
        if (!(var2.equals("reset"))) break label_53;
        var3 = 2;
        label_53:
        switch (var3) {
            case 0 -> { break label_54; }
            case 1 -> { break label_58; }
            case 2 -> { break label_61; }
            default -> { break label_62; }
        }
        label_54:
        if (!(FileUtils.fileExists(("Lightning/Configs/" + arg0[1] + ".json")))) {
            LightningClient.CHAT_MANAGER.tagged("The specified configuration does not exist.", getTag(), getName());
            return;
            try {
            }
            LightningClient.CONFIG_MANAGER.loadModules(arg0[1]);
            LightningClient.CHAT_MANAGER.tagged(("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + arg0[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration."), getTag(), getName());
        } catch (IOException ex57) {
            var4 = ex57;
            LightningClient.CHAT_MANAGER.tagged(("Failed to load the " + String.valueOf(ChatUtils.getPrimary()) + arg0[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration."), getTag(), getName());
            break label_63;
            try {
                label_58:
                LightningClient.CONFIG_MANAGER.saveModules(arg0[1]);
                LightningClient.CHAT_MANAGER.tagged(("Successfully saved the configuration to " + String.valueOf(ChatUtils.getPrimary()) + arg0[1] + ".json" + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
            } catch (IOException ex60) {
                var4 = ex60;
                LightningClient.CHAT_MANAGER.tagged(("Failed to save the " + String.valueOf(ChatUtils.getPrimary()) + arg0[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration."), getTag(), getName());
                break label_63;
                label_61:
                resetConfig(arg0[1]);
                break label_63;
                label_62:
                messageSyntax();
            }
        }
        label_63:
        break label_77;
        label_64:
        if (arg0.length == 1) {
            var2 = arg0[0].toLowerCase();
            var3 = -1;
            switch (var2.hashCode()) {
                case -934641255 -> { break label_65; }
                case 3522941 -> { break label_66; }
                case 108404047 -> { break label_67; }
                case 1126940025 -> { break label_68; }
                default -> { break label_69; }
            }
            label_65:
            if (!(var2.equals("reload"))) break label_69;
            var3 = 0;
            break label_69;
            label_66:
            if (!(var2.equals("save"))) break label_69;
            var3 = 1;
            break label_69;
            label_67:
            if (!(var2.equals("reset"))) break label_69;
            var3 = 2;
            break label_69;
            label_68:
            if (!(var2.equals("current"))) break label_69;
            var3 = 3;
            label_69:
            switch (var3) {
                case 0 -> { break label_70; }
                case 1 -> { break label_71; }
                case 2 -> { break label_72; }
                case 3 -> { break label_73; }
                default -> { break label_74; }
            }
            label_70:
            LightningClient.CONFIG_MANAGER.loadConfig();
            LightningClient.CHAT_MANAGER.tagged("Successfully reloaded the current configuration.", getTag(), getName());
            break label_75;
            label_71:
            LightningClient.CONFIG_MANAGER.saveConfig();
            LightningClient.CHAT_MANAGER.tagged("Successfully saved the current configuration.", getTag(), getName());
            break label_75;
            label_72:
            resetConfig(LightningClient.CONFIG_MANAGER.getCurrentConfig());
            break label_75;
            label_73:
            LightningClient.CHAT_MANAGER.tagged(("The client is currently using the " + String.valueOf(ChatUtils.getPrimary()) + LightningClient.CONFIG_MANAGER.getCurrentConfig() + String.valueOf(ChatUtils.getSecondary()) + " configuration."), getTag(), getName());
            break label_75;
            label_74:
            messageSyntax();
            label_75:
            break label_77;
        }
        messageSyntax();
        label_77:
    }

    private void resetConfig(String arg0) {
        Object var2 = LightningClient.MODULE_MANAGER.getModules().iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((Module) var2.next());
            var3.setToggled(false);
            var3.resetValues();
            break label_0;
            try {
            }
            LightningClient.CONFIG_MANAGER.saveModules(arg0);
            LightningClient.CHAT_MANAGER.tagged(("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + arg0 + String.valueOf(ChatUtils.getSecondary()) + " configuration to default."), getTag(), getName());
        } catch (IOException ex3) {
            var2 = ex3;
            LightningClient.CHAT_MANAGER.tagged(("Failed to reset the " + String.valueOf(ChatUtils.getPrimary()) + arg0 + String.valueOf(ChatUtils.getSecondary()) + " configuration."), getTag(), getName());
        }
    }

    private void resetGlobalConfig(Module arg0, String arg1) {
        arg0.resetValues();
        saveGlobalConfig(arg0, arg1);
    }

    private void resetBindProfile(String arg0) {
        Object var2 = LightningClient.MODULE_MANAGER.getModules().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Module) var2.next());
            var3.bind.resetValue();
            Object var4 = var3.getSettings().iterator();
            while (var4.hasNext()) {
                Object var5 = ((Setting) var4.next());
                if (!((var5 instanceof BindSetting))) {
                    break label_1;
                }
                Object var6 = ((BindSetting) var5);
                var6.resetValue();
            }
        }
        saveBindProfile(arg0);
    }

    private void resetVisualConfig(String arg0) {
        Object var2 = visualModules().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Module) var2.next());
            var3.resetValues();
        }
        saveVisualConfig(arg0);
    }

    private void resetModulesState(String arg0) {
        Object var2 = LightningClient.MODULE_MANAGER.getModules().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Module) var2.next());
            var3.setToggled(false);
            var3.resetValues();
        }
        saveModulesState(arg0);
    }

    private void saveGlobalConfig(Module arg0, String arg1) {
        try {
            Object var3 = LightningClient.CONFIG_MANAGER.serializeModuleJson(arg0);
            Object var4 = new File(("Lightning/GlobalConfigs/" + arg0.getName()));
            if (var4.exists()) break label_1;
            var4.mkdirs();
            label_1:
            Object var5 = new File(var4, (arg1 + ".json"));
            Object var6 = new FileWriter(var5);
            try {
                new GsonBuilder().setPrettyPrinting().create().toJson(var3, var6);
                var6.close();
            } catch (Throwable ex4) {
                Object var7 = ex4;
                try {
                    var6.close();
                } catch (Throwable ex7) {
                    Object var8 = ex7;
                    var7.addSuppressed(var8);
                }
                throw var7;
            }
            LightningClient.CHAT_MANAGER.tagged(("Successfully saved global config " + String.valueOf(ChatUtils.getPrimary()) + arg1 + String.valueOf(ChatUtils.getSecondary()) + " for module " + String.valueOf(ChatUtils.getPrimary()) + arg0.getName() + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
        } catch (Exception ex11) {
            var3 = ex11;
            LightningClient.CHAT_MANAGER.tagged(("Failed to save global config: " + var3.getMessage()), getTag(), getName());
            var3.printStackTrace();
        }
    }

    private void loadGlobalConfig(Module arg0, String arg1) {
        Object var3 = new File(("Lightning/GlobalConfigs/" + arg0.getName() + "/" + arg1 + ".json"));
        if (!(var3.exists())) {
            LightningClient.CHAT_MANAGER.tagged(("Global config " + String.valueOf(ChatUtils.getPrimary()) + arg1 + String.valueOf(ChatUtils.getSecondary()) + " does not exist for " + arg0.getName() + "."), getTag(), getName());
            return;
            try {
            }
            Object var4 = new FileReader(var3);
            try {
                Object var5 = JsonParser.parseReader(var4).getAsJsonObject();
                Object var6 = new File("Lightning/Configs");
                Object var7 = var6.listFiles((arg0, arg1) -> arg1.endsWith(".json"));
                if (var7 == null) break label_21;
                Object var8 = new GsonBuilder().setPrettyPrinting().create();
                Object var9 = var7;
                int var10 = var9.length;
                int var11 = 0;
                while (var11 < var10) {
                    Object var12 = var9[var11];
                    Object var14 = new FileReader(var12);
                    try {
                        Object var13 = JsonParser.parseReader(var14).getAsJsonObject();
                        var14.close();
                    } catch (Throwable ex5) {
                        Object var15 = ex5;
                        try {
                            var14.close();
                        } catch (Throwable ex8) {
                            Object var16 = ex8;
                            var15.addSuppressed(var16);
                        }
                        throw var15;
                    }
                    if (var13.has("Modules")) {
                    } else {
                    }
                    var14 = new JsonObject();
                    var14.add(arg0.getName(), var5);
                    var13.add("Modules", var14);
                    var15 = new FileWriter(var12);
                    try {
                        var8.toJson(var13, var15);
                        var15.close();
                    } catch (Throwable ex15) {
                        var16 = ex15;
                        try {
                            var15.close();
                        } catch (Throwable ex18) {
                            Object var17 = ex18;
                            var16.addSuppressed(var17);
                        }
                        throw var16;
                    }
                    var11++;
                }
                label_21:
                LightningClient.CONFIG_MANAGER.loadModules(LightningClient.CONFIG_MANAGER.getCurrentConfig());
                LightningClient.CHAT_MANAGER.tagged(("Successfully loaded global config " + String.valueOf(ChatUtils.getPrimary()) + arg1 + String.valueOf(ChatUtils.getSecondary()) + " for module " + String.valueOf(ChatUtils.getPrimary()) + arg0.getName() + String.valueOf(ChatUtils.getSecondary()) + " across all profiles."), getTag(), getName());
                var4.close();
            } catch (Throwable ex23) {
                var5 = ex23;
                try {
                    var4.close();
                } catch (Throwable ex26) {
                    var6 = ex26;
                    var5.addSuppressed(var6);
                }
                throw var5;
            }
        } catch (Exception ex29) {
            var4 = ex29;
            LightningClient.CHAT_MANAGER.tagged(("Failed to load global config: " + var4.getMessage()), getTag(), getName());
            var4.printStackTrace();
        }
    }

    private void saveBindProfile(String arg0) {
        try {
            Object var2 = new JsonObject();
            Object var3 = LightningClient.MODULE_MANAGER.getModules().iterator();
            while (var3.hasNext()) {
                Object var4 = ((Module) var3.next());
                Object var5 = new JsonObject();
                var5.add(var4.bind.getName(), new JsonPrimitive((var4.bind.getValue() + "," + var4.bind.getMode())));
                Object var6 = var4.getSettings().iterator();
                while (var6.hasNext()) {
                    Object var7 = ((Setting) var6.next());
                    if (var7 == var4.bind) break label_2;
                    if (!((var7 instanceof BindSetting))) {
                        break label_2;
                    }
                    Object var8 = ((BindSetting) var7);
                    var5.add(var8.getName(), new JsonPrimitive((var8.getValue() + "," + var8.getMode())));
                }
                var2.add(var4.getName(), var5);
            }
            var3 = new File("Lightning/Binds");
            if (!(var3.exists())) {
                var3.mkdirs();
            }
            var4 = new File(var3, (arg0 + ".json"));
            var5 = new FileWriter(var4);
            try {
                new GsonBuilder().setPrettyPrinting().create().toJson(var2, var5);
                var5.close();
            } catch (Throwable ex9) {
                var6 = ex9;
                try {
                    var5.close();
                } catch (Throwable ex12) {
                    var7 = ex12;
                    var6.addSuppressed(var7);
                }
                throw var6;
            }
            LightningClient.CHAT_MANAGER.tagged(("Successfully saved the current binds to " + String.valueOf(ChatUtils.getPrimary()) + arg0 + ".json" + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
        } catch (Exception ex16) {
            var2 = ex16;
            LightningClient.CHAT_MANAGER.tagged(("Failed to save bind profile: " + var2.getMessage()), getTag(), getName());
            var2.printStackTrace();
        }
    }

    private void loadBindProfile(String arg0) {
        Object var2 = new File(("Lightning/Binds/" + arg0 + ".json"));
        if (!(var2.exists())) {
            LightningClient.CHAT_MANAGER.tagged(("Bind profile " + String.valueOf(ChatUtils.getPrimary()) + arg0 + String.valueOf(ChatUtils.getSecondary()) + " does not exist."), getTag(), getName());
            return;
            try {
            }
            Object var3 = new FileReader(var2);
            try {
                Object var4 = JsonParser.parseReader(var3).getAsJsonObject();
                Object var5 = var4.keySet().iterator();
                while (var5.hasNext()) {
                    Object var6 = ((String) var5.next());
                    Object var7 = LightningClient.MODULE_MANAGER.getModule(var6);
                    if (var7 == null) {
                        break label_2;
                    }
                    Object var8 = var4.getAsJsonObject(var6);
                    Object var9 = var8.keySet().iterator();
                    while (var9.hasNext()) {
                        Object var10 = ((String) var9.next());
                        if (var10.equals(var7.bind.getName())) {
                        } else {
                        }
                        var tmp0 = findBindSetting(var7, var10);
                        Object var11 = tmp0;
                        Object var12 = tmp0;
                        if (var11 == null) {
                            break label_4;
                        }
                        Object var13 = var8.get(var10).getAsString().split(",", 2);
                        var11.setValue(Integer.parseInt(var13[0]));
                        if (var13.length <= 1) {
                            break label_4;
                        }
                        var11.setMode(var13[1]);
                    }
                }
                LightningClient.CHAT_MANAGER.tagged(("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + arg0 + String.valueOf(ChatUtils.getSecondary()) + " bind profile."), getTag(), getName());
                var3.close();
            } catch (Throwable ex12) {
                var4 = ex12;
                try {
                    var3.close();
                } catch (Throwable ex15) {
                    var5 = ex15;
                    var4.addSuppressed(var5);
                }
                throw var4;
            }
        } catch (Exception ex18) {
            var3 = ex18;
            LightningClient.CHAT_MANAGER.tagged(("Failed to load bind profile: " + var3.getMessage()), getTag(), getName());
            var3.printStackTrace();
        }
    }

    private BindSetting findBindSetting(Module arg0, String arg1) {
        Object var3 = arg0.getSettings().iterator();
        label_0:
        if (var3.hasNext()) {
            Object var4 = ((Setting) var3.next());
            if (!((var4 instanceof BindSetting))) break label_0;
            var tmp0 = ((BindSetting) var4);
            Object var5 = tmp0;
            if (!(tmp0.getName().equals(arg1))) {
                break label_0;
            }
            return var5;
        }
        return null;
    }

    private void saveVisualConfig(String arg0) {
        try {
            Object var2 = new JsonObject();
            Object var3 = visualModules().iterator();
            while (var3.hasNext()) {
                Object var4 = ((Module) var3.next());
                var2.add(var4.getName(), LightningClient.CONFIG_MANAGER.serializeModuleJson(var4));
            }
            var3 = new File("Lightning/Visuals");
            if (!(var3.exists())) {
                var3.mkdirs();
            }
            var4 = new File(var3, (arg0 + ".json"));
            Object var5 = new FileWriter(var4);
            try {
                new GsonBuilder().setPrettyPrinting().create().toJson(var2, var5);
                var5.close();
            } catch (Throwable ex6) {
                Object var6 = ex6;
                try {
                    var5.close();
                } catch (Throwable ex9) {
                    Object var7 = ex9;
                    var6.addSuppressed(var7);
                }
                throw var6;
            }
            LightningClient.CHAT_MANAGER.tagged(("Successfully saved the current visual settings to " + String.valueOf(ChatUtils.getPrimary()) + arg0 + ".json" + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
        } catch (Exception ex13) {
            var2 = ex13;
            LightningClient.CHAT_MANAGER.tagged(("Failed to save visual config: " + var2.getMessage()), getTag(), getName());
            var2.printStackTrace();
        }
    }

    private void loadVisualConfig(String arg0) {
        Object var2 = new File(("Lightning/Visuals/" + arg0 + ".json"));
        if (!(var2.exists())) {
            LightningClient.CHAT_MANAGER.tagged(("Visual config " + String.valueOf(ChatUtils.getPrimary()) + arg0 + String.valueOf(ChatUtils.getSecondary()) + " does not exist."), getTag(), getName());
            return;
        }
        Object var3 = ((Set) visualModules().stream().map(Module::getName).collect(Collectors.toSet()));
        try {
            Object var4 = new FileReader(var2);
            try {
                Object var5 = JsonParser.parseReader(var4).getAsJsonObject();
                Object var6 = var5.keySet().iterator();
                while (var6.hasNext()) {
                    Object var7 = ((String) var6.next());
                    Object var8 = LightningClient.MODULE_MANAGER.getModule(var7);
                    if (var8 == null) break label_3;
                    if (!(var3.contains(var8.getName()))) {
                        break label_3;
                    }
                    LightningClient.CONFIG_MANAGER.applyModuleJson(var8, var5.getAsJsonObject(var7));
                }
                LightningClient.CHAT_MANAGER.tagged(("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + arg0 + String.valueOf(ChatUtils.getSecondary()) + " visual config."), getTag(), getName());
                var4.close();
            } catch (Throwable ex7) {
                var5 = ex7;
                try {
                    var4.close();
                } catch (Throwable ex10) {
                    var6 = ex10;
                    var5.addSuppressed(var6);
                }
                throw var5;
            }
        } catch (Exception ex13) {
            var4 = ex13;
            LightningClient.CHAT_MANAGER.tagged(("Failed to load visual config: " + var4.getMessage()), getTag(), getName());
            var4.printStackTrace();
        }
    }

    private void saveModulesState(String arg0) {
        try {
            Object var2 = new JsonObject();
            Object var3 = LightningClient.MODULE_MANAGER.getModules().iterator();
            while (var3.hasNext()) {
                Object var4 = ((Module) var3.next());
                var2.add(var4.getName(), LightningClient.CONFIG_MANAGER.serializeModuleJson(var4, false));
            }
            var3 = new File("Lightning/Modules");
            if (!(var3.exists())) {
                var3.mkdirs();
            }
            var4 = new File(var3, (arg0 + ".json"));
            Object var5 = new FileWriter(var4);
            try {
                new GsonBuilder().setPrettyPrinting().create().toJson(var2, var5);
                var5.close();
            } catch (Throwable ex6) {
                Object var6 = ex6;
                try {
                    var5.close();
                } catch (Throwable ex9) {
                    Object var7 = ex9;
                    var6.addSuppressed(var7);
                }
                throw var6;
            }
            LightningClient.CHAT_MANAGER.tagged(("Successfully saved the current module states to " + String.valueOf(ChatUtils.getPrimary()) + arg0 + ".json" + String.valueOf(ChatUtils.getSecondary()) + "."), getTag(), getName());
        } catch (Exception ex13) {
            var2 = ex13;
            LightningClient.CHAT_MANAGER.tagged(("Failed to save module states: " + var2.getMessage()), getTag(), getName());
            var2.printStackTrace();
        }
    }

    private void loadModulesState(String arg0) {
        Object var2 = new File(("Lightning/Modules/" + arg0 + ".json"));
        if (!(var2.exists())) {
            LightningClient.CHAT_MANAGER.tagged(("Module state profile " + String.valueOf(ChatUtils.getPrimary()) + arg0 + String.valueOf(ChatUtils.getSecondary()) + " does not exist."), getTag(), getName());
            return;
            try {
            }
            Object var3 = new FileReader(var2);
            try {
                Object var4 = JsonParser.parseReader(var3).getAsJsonObject();
                Object var5 = var4.keySet().iterator();
                while (var5.hasNext()) {
                    Object var6 = ((String) var5.next());
                    Object var7 = LightningClient.MODULE_MANAGER.getModule(var6);
                    if (var7 == null) {
                        break label_2;
                    }
                    Object var8 = var4.get(var6);
                    if (var8.isJsonObject()) {
                        LightningClient.CONFIG_MANAGER.applyModuleJson(var7, var8.getAsJsonObject(), false);
                        break label_2;
                    }
                    if (!(var8.isJsonPrimitive())) break label_2;
                    if (!(var8.getAsJsonPrimitive().isBoolean())) {
                        break label_2;
                    }
                    var7.setToggled(var8.getAsBoolean());
                }
                LightningClient.CHAT_MANAGER.tagged(("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + arg0 + String.valueOf(ChatUtils.getSecondary()) + " module states."), getTag(), getName());
                var3.close();
            } catch (Throwable ex8) {
                var4 = ex8;
                try {
                    var3.close();
                } catch (Throwable ex11) {
                    var5 = ex11;
                    var4.addSuppressed(var5);
                }
                throw var4;
            }
        } catch (Exception ex14) {
            var3 = ex14;
            LightningClient.CHAT_MANAGER.tagged(("Failed to load module states: " + var3.getMessage()), getTag(), getName());
            var3.printStackTrace();
        }
    }

    static {
        EXTRA_VISUAL_MODULES = List.of(ColorModule.class, ClickGuiModule.class, HUDModule.class, CapesModule.class);
    }

}
