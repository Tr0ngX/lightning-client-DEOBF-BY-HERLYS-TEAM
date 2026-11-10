package lightning.commands.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.Module;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.color.ColorUtils;
import lightning.utils.input.KeyboardUtils;
import lightning.utils.minecraft.IdentifierUtils;
import lightning.utils.miscellaneous.ListUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@RegisterCommand(name = "module", tag = "Module", description = "Allows you to view and change this module's settings.", syntax = "<[setting]> <[...]> | <list|reset>")
public class ModuleCommand extends Command {
    private final Module module;

    public ModuleCommand(Module arg0) {
        this.module = arg0;
        setName(arg0.getName().toLowerCase());
        setTag(arg0.getName());
    }

    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) {
            Object var2 = new ArrayList();
            var2.add("list");
            var2.add("reset");
            Object var3 = this.module.getSettings().iterator();
            while (var3.hasNext()) {
                Object var4 = ((Setting) var3.next());
                if ((var4 instanceof CategorySetting)) break label_1;
                var2.add(var4.getName().toLowerCase());
                label_1:
            }
            return var2;
        }
        if (arg0.length == 1) {
            var2 = this.module.getSetting(arg0[0]);
            if (var2 == null) return List.of();
            Objects.requireNonNull(var2);
            var3 = var2;
            var4 = 0;
            switch (typeSwitch(var3, var4)) {
                case 0 -> { break label_6; }
                case 1 -> { break label_7; }
                case 2 -> { break label_8; }
                case 3 -> { break label_9; }
                case 4 -> { break label_10; }
                case 5 -> { break label_11; }
                case 6 -> { break label_12; }
                default -> { break label_13; }
            }
            label_6:
            Object var5 = ((BooleanSetting) var3);
            break label_14;
            label_7:
            Object var6 = ((NumberSetting) var3);
            break label_14;
            label_8:
            Object var7 = ((ModeSetting) var3);
            Object var8 = new ArrayList(var7.getModes());
            var8.add("reset");
            var8.add("list");
            break label_14;
            label_9:
            var8 = ((StringSetting) var3);
            break label_14;
            label_10:
            Object var9 = ((BindSetting) var3);
            break label_14;
            label_11:
            Object var10 = ((ColorSetting) var3);
            break label_14;
            label_12:
            Object var11 = ((WhitelistSetting) var3);
            break label_14;
            label_13:
            label_14:
            return List.of();
        }
        if (arg0.length == 2 && (this.module.getSetting(arg0[0]) instanceof ColorSetting)) {
            var2 = arg0[1].toLowerCase();
            if ((var2.equals("sync")) || (var2.equals("rainbow"))) {
                return List.of("true", "false");
            }
        }
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length < 1) break label_120;
        if (arg0[0].equalsIgnoreCase("list")) {
            if (this.module.getSettings().isEmpty()) {
                LightningClient.CHAT_MANAGER.tagged("This module currently has no registered settings.", this.module.getName(), ("module-cmd-" + getName() + "-list"));
                break label_121;
            }
            Object var2 = new StringBuilder();
            int var3 = 0;
            Object var4 = this.module.getSettings().iterator();
            while (var4.hasNext()) {
                Object var5 = ((Setting) var4.next());
                if ((var5 instanceof CategorySetting)) break label_11;
                var2.append(ChatUtils.getSecondary()).append(var5.getName()).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary());
                if (!((var5 instanceof BooleanSetting))) break label_2;
                var2.append(((BooleanSetting) var5).getValue());
                label_2:
                if (!((var5 instanceof NumberSetting))) break label_3;
                var2.append(((NumberSetting) var5).getValue());
                label_3:
                if (!((var5 instanceof ModeSetting))) break label_4;
                var2.append(((ModeSetting) var5).getValue());
                label_4:
                if (!((var5 instanceof StringSetting))) break label_5;
                var2.append(((StringSetting) var5).getValue());
                label_5:
                if (!((var5 instanceof BindSetting))) break label_6;
                var2.append(KeyboardUtils.getKeyName(((BindSetting) var5).getValue()).toUpperCase());
                label_6:
                if (!((var5 instanceof WhitelistSetting))) break label_7;
                var2.append("...");
                label_7:
                if (!((var5 instanceof ColorSetting))) break label_8;
                var2.append("RGBA(").append(((ColorSetting) var5).getColor().getRed()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting) var5).getColor().getGreen()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting) var5).getColor().getBlue()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting) var5).getColor().getAlpha()).append(")").append(ChatUtils.getPrimary()).append(",").append(ChatUtils.getSecondary()).append(" SyRa(").append(((ColorSetting) var5).isSync()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting) var5).isRainbow()).append(")");
                label_8:
                var2.append(ChatUtils.getPrimary()).append("]").append(ChatUtils.getSecondary()).append(((var3 + 1) == this.module.getSettings().size() ? "" : ", "));
                var3++;
                label_11:
            }
            label_12:
            LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getSecondary()) + this.module.getName() + " " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + this.module.getSettings().size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var2)), ("module-cmd-" + getName() + "-list"));
            break label_121;
        }
        if (arg0[0].equalsIgnoreCase("reset")) {
            this.module.resetValues();
            LightningClient.CHAT_MANAGER.tagged("Successfully reset all of the module's settings.", this.module.getName(), ("module-cmd-" + getName()));
            break label_121;
        }
        var2 = this.module.getSetting(arg0[0]);
        if (var2 == null) {
            LightningClient.CHAT_MANAGER.tagged("Could not find the setting specified.", this.module.getName(), ("module-cmd-" + getName()));
            return;
        }
        arg0 = ((String[]) Arrays.copyOfRange(arg0, 1, arg0.length));
        Objects.requireNonNull(var2);
        var3 = var2;
        var4 = 0;
        switch (typeSwitch(var3, var4)) {
            case 0 -> { break label_17; }
            case 1 -> { break label_22; }
            case 2 -> { break label_32; }
            case 3 -> { break label_51; }
            case 4 -> { break label_59; }
            case 5 -> { break label_67; }
            case 6 -> { break label_89; }
            default -> { break label_118; }
        }
        label_17:
        var5 = ((BooleanSetting) var3);
        if (var5 == this.module.chatNotify) {
            LightningClient.CHAT_MANAGER.tagged(("This setting is the module's main ChatNotify setting. Please use the " + String.valueOf(ChatUtils.getPrimary()) + "chatnotify" + String.valueOf(ChatUtils.getSecondary()) + " command instead."), this.module.getName(), getName());
            return;
        }
        if (var5 == this.module.drawn) {
            LightningClient.CHAT_MANAGER.tagged(("This setting is the module's main Drawn setting. Please use the " + String.valueOf(ChatUtils.getPrimary()) + "drawn" + String.valueOf(ChatUtils.getSecondary()) + " command instead."), this.module.getName(), getName());
            return;
        }
        if (arg0.length == 1) {
            if (arg0[0].equalsIgnoreCase("reset")) {
                var5.resetValue();
                LightningClient.CHAT_MANAGER.tagged(("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + var5.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting."), this.module.getName(), ("module-cmd-" + getName()));
                break label_119;
            }
            var5.setValue(Boolean.parseBoolean(arg0[0]));
            LightningClient.CHAT_MANAGER.tagged(("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + var5.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + var5.getValue() + String.valueOf(ChatUtils.getSecondary()) + "."), this.module.getName(), ("module-cmd-" + getName()));
            break label_119;
        }
        LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var5.getName().toLowerCase() + " <[value]|reset>"));
        break label_119;
        label_22:
        Object var6 = ((NumberSetting) var3);
        if (arg0.length != 1) break label_31;
        if (arg0[0].equalsIgnoreCase("reset")) {
            var6.resetValue();
            LightningClient.CHAT_MANAGER.tagged(("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + var6.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting."), this.module.getName(), ("module-cmd-" + getName()));
            break label_119;
            try {
            }
            switch (ModuleCommand.1.$SwitchMap$night$settings$impl$NumberSetting$Type[var6.getType().ordinal()]) {
                case 1 -> { break label_24; }
                case 2 -> { break label_25; }
                case 3 -> { break label_26; }
                default -> { break label_27; }
            }
            label_24:
            var6.setValue(Long.parseLong(arg0[0]));
            break label_28;
            label_25:
            var6.setValue(Double.parseDouble(arg0[0]));
            break label_28;
            label_26:
            var6.setValue(Float.parseFloat(arg0[0]));
            break label_28;
            label_27:
            var6.setValue(Integer.parseInt(arg0[0]));
            label_28:
            LightningClient.CHAT_MANAGER.tagged(("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + var6.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + String.valueOf(var6.getValue()) + String.valueOf(ChatUtils.getSecondary()) + "."), this.module.getName(), ("module-cmd-" + getName()));
        } catch (NumberFormatException ex30) {
            Object var7 = ex30;
            LightningClient.CHAT_MANAGER.tagged(("Please input a valid " + String.valueOf(ChatUtils.getPrimary()) + var6.getType().name().toLowerCase() + String.valueOf(ChatUtils.getSecondary()) + " number."), this.module.getName(), ("module-cmd-" + getName()));
            break label_119;
            label_31:
            LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var6.getName().toLowerCase() + " <[value]|reset>"));
            break label_119;
            label_32:
            var7 = ((ModeSetting) var3);
            if (arg0.length >= 1) {
                if (arg0[0].equalsIgnoreCase("reset")) {
                    if (var7.getModes().stream().noneMatch("reset"::equalsIgnoreCase)) break label_49;
                }
                if (arg0[0].equalsIgnoreCase("force-reset")) break label_49;
                if (arg0[0].equalsIgnoreCase("list")) {
                    if (var7.getModes().stream().noneMatch("list"::equalsIgnoreCase)) break label_43;
                }
                if (arg0[0].equalsIgnoreCase("force-list")) break label_43;
                if (arg0[0].equalsIgnoreCase("reset") && var7.getModes().stream().anyMatch("reset"::equalsIgnoreCase)) {
                    LightningClient.CHAT_MANAGER.info(("If you would like to reset this setting's value, write \"" + String.valueOf(ChatUtils.getPrimary()) + "force-reset" + String.valueOf(ChatUtils.getSecondary()) + "\" instead."));
                }
                if (arg0[0].equalsIgnoreCase("list") && var7.getModes().stream().anyMatch("list"::equalsIgnoreCase)) {
                    LightningClient.CHAT_MANAGER.info(("If you would like to view a list of valid values for this setting, write \"" + String.valueOf(ChatUtils.getPrimary()) + "force-list" + String.valueOf(ChatUtils.getSecondary()) + "\" instead."));
                }
                Object var8 = new StringBuilder();
                int var9 = 0;
                Object var10 = arg0;
                int var11 = var10.length;
                int var12 = 0;
                while (var12 < var11) {
                    Object var13 = var10[var12];
                    var8.append(var13).append(((var9 + 1) == arg0.length ? "" : " "));
                    var9++;
                    var12++;
                }
                var tmp0 = var8.toString();
                Objects.requireNonNull(tmp0);
                if (var7.getModes().stream().anyMatch(tmp0::equalsIgnoreCase)) {
                    var7.setValue(((String) var7.getModes().get(ListUtils.getIndex(var7.getModes(), var8.toString()))));
                    LightningClient.CHAT_MANAGER.tagged(("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + var7.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + var7.getValue() + String.valueOf(ChatUtils.getSecondary()) + "."), this.module.getName(), ("module-cmd-" + getName()));
                } else {
                    LightningClient.CHAT_MANAGER.tagged("Please input a valid value for this setting.", this.module.getName(), ("module-cmd-" + getName()));
                }
                break label_119;
                label_43:
                if (var7.getModes().isEmpty()) {
                    LightningClient.CHAT_MANAGER.tagged((String.valueOf(ChatUtils.getPrimary()) + var7.getName() + String.valueOf(ChatUtils.getSecondary()) + " currently has no values registered."), this.module.getName(), ("module-cmd-" + getName()));
                    break label_119;
                }
                var8 = new StringBuilder();
                var9 = 0;
                var10 = var7.getModes().iterator();
                while (var10.hasNext()) {
                    var11 = ((String) var10.next());
                    var8.append(ChatUtils.getSecondary()).append(var11).append(((var9 + 1) == var7.getModes().size() ? "" : ", "));
                    var9++;
                }
                LightningClient.CHAT_MANAGER.tagged((String.valueOf(ChatUtils.getSecondary()) + var7.getName() + " " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + var7.getModes().size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var8)), this.module.getName(), ("module-cmd-" + getName()));
                break label_119;
                label_49:
                var7.resetValue();
                LightningClient.CHAT_MANAGER.tagged(("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + var7.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting."), this.module.getName(), ("module-cmd-" + getName()));
                break label_119;
            }
            LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var7.getName().toLowerCase() + " <[value]|reset|list>"));
            break label_119;
            label_51:
            var8 = ((StringSetting) var3);
            if (arg0.length >= 1) {
                if (arg0[0].equalsIgnoreCase("force-reset")) {
                    var8.setValue(var8.getDefaultValue());
                    LightningClient.CHAT_MANAGER.tagged(("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + var8.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting."), this.module.getName(), ("module-cmd-" + getName()));
                    break label_119;
                }
                if (arg0[0].equalsIgnoreCase("reset")) {
                    LightningClient.CHAT_MANAGER.info(("If you would like to reset this setting's value, write \"" + String.valueOf(ChatUtils.getPrimary()) + "force-reset" + String.valueOf(ChatUtils.getSecondary()) + "\" instead."));
                }
                var9 = new StringBuilder();
                var10 = 0;
                var11 = arg0;
                var12 = var11.length;
                var13 = 0;
                while (var13 < var12) {
                    Object var14 = var11[var13];
                    var9.append(var14).append(((var10 + 1) == arg0.length ? "" : " "));
                    var10++;
                    var13++;
                }
                var8.setValue(var9.toString());
                LightningClient.CHAT_MANAGER.tagged(("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + var8.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + var8.getValue() + String.valueOf(ChatUtils.getSecondary()) + "."), this.module.getName(), ("module-cmd-" + getName()));
                break label_119;
            }
            LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var8.getName().toLowerCase() + " <[value]|reset>"));
            break label_119;
            label_59:
            var9 = ((BindSetting) var3);
            if (var2 == this.module.bind) {
                LightningClient.CHAT_MANAGER.tagged(("This setting is the module's main toggle keybind. Please use the " + String.valueOf(ChatUtils.getPrimary()) + "bind" + String.valueOf(ChatUtils.getSecondary()) + " command instead."), this.module.getName(), ("module-cmd-" + getName()));
                return;
            }
            if (arg0.length != 1) break label_66;
            if (arg0[0].equalsIgnoreCase("reset")) {
                var9.resetValue();
                LightningClient.CHAT_MANAGER.tagged(("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + var9.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting."), this.module.getName(), ("module-cmd-" + getName()));
                break label_119;
            }
            var10 = 0;
            try {
                var10 = KeyboardUtils.getKeyNumber(arg0[0]);
            } catch (IllegalArgumentException ex64) {
                var11 = ex64;
            }
            var9.setValue(var10);
            LightningClient.CHAT_MANAGER.tagged(("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + var9.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(var9.getValue()).toUpperCase() + String.valueOf(ChatUtils.getSecondary()) + "."), this.module.getName(), ("module-cmd-" + getName()));
            break label_119;
            label_66:
            LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var9.getName().toLowerCase() + " <[value]|reset>"));
            break label_119;
            label_67:
            var10 = ((ColorSetting) var3);
            if (arg0.length != 2) break label_86;
            var11 = 0;
            if (!(arg0[0].equalsIgnoreCase("red")) && !(arg0[0].equalsIgnoreCase("green")) && !(arg0[0].equalsIgnoreCase("blue"))) {
                if (!(arg0[0].equalsIgnoreCase("alpha"))) break label_71;
                try {
                }
                var11 = Math.clamp(((long) Integer.parseInt(arg0[1])), 0, 255);
            } catch (NumberFormatException ex70) {
                var12 = ex70;
                LightningClient.CHAT_MANAGER.tagged(("Please input a valid number for the " + String.valueOf(ChatUtils.getPrimary()) + arg0[0].toLowerCase() + String.valueOf(ChatUtils.getSecondary()) + " value."), this.module.getName());
                return;
            }
            label_71:
            var12 = "";
            var13 = "";
            if (arg0[0].equalsIgnoreCase("red")) {
                var10.setColor(new Color(var11, var10.getValue().getColor().getGreen(), var10.getValue().getColor().getBlue(), var10.getValue().getColor().getAlpha()));
                var12 = "red";
                var13 = String.valueOf(var10.getValue().getColor().getRed());
                break label_85;
            }
            if (arg0[0].equalsIgnoreCase("green")) {
                var10.setColor(new Color(var10.getValue().getColor().getRed(), var11, var10.getValue().getColor().getBlue(), var10.getValue().getColor().getAlpha()));
                var12 = "green";
                var13 = String.valueOf(var10.getValue().getColor().getGreen());
                break label_85;
            }
            if (arg0[0].equalsIgnoreCase("blue")) {
                var10.setColor(new Color(var10.getValue().getColor().getRed(), var10.getValue().getColor().getGreen(), var11, var10.getValue().getColor().getAlpha()));
                var12 = "blue";
                var13 = String.valueOf(var10.getValue().getColor().getBlue());
                break label_85;
            }
            if (arg0[0].equalsIgnoreCase("alpha")) {
                var10.setColor(new Color(var10.getValue().getColor().getRed(), var10.getValue().getColor().getGreen(), var10.getValue().getColor().getBlue(), var11));
                var12 = "alpha";
                var13 = String.valueOf(var10.getValue().getColor().getAlpha());
                break label_85;
            }
            if (arg0[0].equalsIgnoreCase("sync")) {
                var10.setSync(Boolean.parseBoolean(arg0[1]));
                var12 = "sync";
                var13 = String.valueOf(var10.isSync());
                break label_85;
            }
            if (arg0[0].equalsIgnoreCase("rainbow")) {
                var10.setRainbow(Boolean.parseBoolean(arg0[1]));
                var12 = "rainbow";
                var13 = String.valueOf(var10.isRainbow());
                break label_85;
            }
            if (!(arg0[0].equalsIgnoreCase("code"))) break label_84;
            if (!(ColorUtils.isValidColorCode(arg0[1]))) {
                LightningClient.CHAT_MANAGER.tagged("Please input a valid color code.", this.module.getName(), ("module-cmd-" + getName()));
                return;
                try {
                }
                var14 = Color.decode(((arg0[1].startsWith("#") ? "" : "#") + arg0[1]));
                var10.setColor(new Color(var14.getRed(), var14.getGreen(), var14.getBlue()));
            } catch (NumberFormatException ex82) {
                var14 = ex82;
                LightningClient.CHAT_MANAGER.tagged("Please input a valid color code.", this.module.getName(), ("module-cmd-" + getName()));
                return;
            }
            var12 = "color";
            var13 = ("rgba(" + var10.getValue().getColor().getRed() + ", " + var10.getValue().getColor().getGreen() + ", " + var10.getValue().getColor().getBlue() + ", " + var10.getValue().getColor().getAlpha() + ")");
            break label_85;
            label_84:
            LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var10.getName().toLowerCase() + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>"));
            label_85:
            LightningClient.CHAT_MANAGER.tagged(("Successfully set the " + String.valueOf(ChatUtils.getPrimary()) + var12 + String.valueOf(ChatUtils.getSecondary()) + " value to " + String.valueOf(ChatUtils.getPrimary()) + var13 + String.valueOf(ChatUtils.getSecondary()) + "."), this.module.getName(), ("module-cmd-" + getName()));
            break label_119;
            label_86:
            if (arg0.length == 1) {
                if (arg0[0].equalsIgnoreCase("reset")) {
                    var10.resetValue();
                    LightningClient.CHAT_MANAGER.tagged(("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + var10.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting."), this.module.getName(), ("module-cmd-" + getName()));
                    break label_119;
                }
                LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var10.getName().toLowerCase() + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>"));
                break label_119;
            }
            LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var10.getName().toLowerCase() + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>"));
            break label_119;
            label_89:
            var11 = ((WhitelistSetting) var3);
            if (arg0.length == 2) {
                if (arg0[0].equalsIgnoreCase("add")) {
                    if (var11.getType() == WhitelistSetting.Type.ITEMS) {
                        var12 = IdentifierUtils.getItem(arg0[1]);
                        if (var12 == null) {
                            LightningClient.CHAT_MANAGER.tagged("Please input a valid item ID.", this.module.getName(), ("module-cmd-" + getName()));
                            return;
                        }
                        if (var11.isWhitelistContains(var12)) {
                            LightningClient.CHAT_MANAGER.tagged((String.valueOf(ChatUtils.getPrimary()) + var12.getName(var12.getDefaultInstance()).getString() + String.valueOf(ChatUtils.getSecondary()) + " is already on the whitelist."), this.module.getName(), ("module-cmd-" + getName()));
                        } else {
                            var11.add(var12);
                            LightningClient.CHAT_MANAGER.tagged(("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + var12.getName(var12.getDefaultInstance()).getString() + String.valueOf(ChatUtils.getSecondary()) + " to the whitelist."), this.module.getName(), ("module-cmd-" + getName()));
                        }
                        break label_119;
                    }
                    if (var11.getType() == WhitelistSetting.Type.BLOCKS) {
                        var12 = IdentifierUtils.getBlock(arg0[1]);
                        if (var12 == null) {
                            LightningClient.CHAT_MANAGER.tagged("Please input a valid block ID.", this.module.getName(), ("module-cmd-" + getName()));
                            return;
                        }
                        if (var11.isWhitelistContains(var12)) {
                            LightningClient.CHAT_MANAGER.tagged((String.valueOf(ChatUtils.getPrimary()) + var12.getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " is already on the whitelist."), this.module.getName(), ("module-cmd-" + getName()));
                        } else {
                            var11.add(var12);
                            LightningClient.CHAT_MANAGER.tagged(("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + var12.getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " to the whitelist."), this.module.getName(), ("module-cmd-" + getName()));
                        }
                        break label_119;
                    }
                    LightningClient.CHAT_MANAGER.error("Something went wrong while detecting the setting's type.");
                    break label_119;
                }
                if (arg0[0].equalsIgnoreCase("del")) {
                    if (var11.getType() != WhitelistSetting.Type.ITEMS) break label_99;
                    var tmp1 = IdentifierUtils.getItem(arg0[1]);
                    var12 = tmp1;
                    if ((tmp1 == null) || (!(var11.isWhitelistContains(var12)))) break label_99;
                    var11.remove(var12);
                    LightningClient.CHAT_MANAGER.tagged(("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + var12.getName(var12.getDefaultInstance()).getString() + String.valueOf(ChatUtils.getSecondary()) + " from the whitelist."), this.module.getName(), ("module-cmd-" + getName()));
                    break label_119;
                    label_99:
                    if (var11.getType() != WhitelistSetting.Type.BLOCKS) break label_100;
                    var tmp2 = IdentifierUtils.getBlock(arg0[1]);
                    var13 = tmp2;
                    if ((tmp2 == null) || (!(var11.isWhitelistContains(var13)))) break label_100;
                    var11.remove(var13);
                    LightningClient.CHAT_MANAGER.tagged(("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + var13.getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " from the whitelist."), this.module.getName(), ("module-cmd-" + getName()));
                    break label_119;
                    label_100:
                    LightningClient.CHAT_MANAGER.tagged((String.valueOf(ChatUtils.getPrimary()) + arg0[1] + String.valueOf(ChatUtils.getSecondary()) + " is not on the whitelist."), this.module.getName(), ("module-cmd-" + getName()));
                    break label_119;
                }
                LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var11.getName().toLowerCase() + " <add|del> <[id]> | <list|clear>"));
                break label_119;
            }
            if (arg0.length == 1) {
                if (arg0[0].equalsIgnoreCase("clear")) {
                    var11.getWhitelist().clear();
                    LightningClient.CHAT_MANAGER.tagged(("Successfully cleared the " + String.valueOf(ChatUtils.getPrimary()) + var11.getName() + String.valueOf(ChatUtils.getSecondary()) + " whitelist."), this.module.getName(), ("module-cmd-" + getName()));
                    break label_119;
                }
                if (arg0[0].equalsIgnoreCase("list")) {
                    if (var11.getWhitelist().isEmpty()) {
                        LightningClient.CHAT_MANAGER.tagged(("There are currently no " + (var11.getType() == WhitelistSetting.Type.ITEMS ? "items" : "blocks") + " on the whitelist."), this.module.getName(), ("module-cmd-" + getName()));
                        break label_119;
                    }
                    var12 = new StringBuilder();
                    var13 = 0;
                    var14 = var11.getWhitelist().iterator();
                    while (var14.hasNext()) {
                        Object var15 = var14.next();
                        var12.append(ChatUtils.getSecondary());
                        Objects.requireNonNull(var15);
                        Object var16 = var15;
                        int var17 = 0;
                        switch (typeSwitch(var16, var17)) {
                            case 0 -> { break label_109; }
                            case 1 -> { break label_110; }
                            default -> { break label_111; }
                        }
                        label_109:
                        Object var18 = ((Block) var16);
                        var12.append(var18.getName().getString());
                        break label_112;
                        label_110:
                        Object var19 = ((Item) var16);
                        var12.append(var19.getName(var19.getDefaultInstance()).getString());
                        break label_112;
                        label_111:
                        var12.append("Invalid");
                        label_112:
                        var12.append(ChatUtils.getPrimary()).append(((var13 + 1) == var11.getWhitelist().size() ? "" : ", "));
                        var13++;
                    }
                    LightningClient.CHAT_MANAGER.message((String.valueOf(ChatUtils.getSecondary()) + var11.getName() + " " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + var11.getWhitelist().size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var12)), ("module-cmd-" + getName()));
                    break label_119;
                }
                LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var11.getName().toLowerCase() + " <add|del> <[id]> | <clear|list>"));
                break label_119;
            }
            LightningClient.CHAT_MANAGER.info((this.module.getName().toLowerCase() + " " + var11.getName().toLowerCase() + " <add|del> <[id]> | <clear|list>"));
            break label_119;
            label_118:
            messageSyntax();
        }
        label_119:
        break label_121;
        label_120:
        messageSyntax();
        label_121:
    }


    class 1 {

        static {
            $SwitchMap$night$settings$impl$NumberSetting$Type = new int[NumberSetting.Type.values().length];
            try {
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.LONG.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.DOUBLE.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.FLOAT.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
            }
        }

    }
}
