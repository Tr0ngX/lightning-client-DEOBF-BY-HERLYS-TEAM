package lightning.commands.impl;

import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.impl.player.RekitModule;

@RegisterCommand(name = "kit", tag = "Kit", description = "Manages Rekit's saved kits.", syntax = "<save|load|delete> <name> | <list|active>")
public class KitCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) return List.of("save", "load", "delete", "list", "active");
        if (arg0.length == 1) {
            if ((arg0[0].equalsIgnoreCase("load")) || (arg0[0].equalsIgnoreCase("delete"))) {
                return ((RekitModule) LightningClient.MODULE_MANAGER.getModule(RekitModule.class)).getKitNames();
            }
        }
        return List.of();
    }

    public void execute(String[] arg0) {
        Object var2 = ((RekitModule) LightningClient.MODULE_MANAGER.getModule(RekitModule.class));
        if (arg0.length == 0) {
            messageSyntax();
            return;
        }
        Object var3 = arg0[0].toLowerCase();
        int var4 = -1;
        switch (var3.hashCode()) {
            case -1422950650 -> { break label_4; }
            case -1335458389 -> { break label_3; }
            case 3322014 -> { break label_5; }
            case 3327206 -> { break label_2; }
            case 3522941 -> { break label_1; }
            default -> { break label_6; }
        }
        label_1:
        if (!(var3.equals("save"))) break label_6;
        var4 = 0;
        break label_6;
        label_2:
        if (!(var3.equals("load"))) break label_6;
        var4 = 1;
        break label_6;
        label_3:
        if (!(var3.equals("delete"))) break label_6;
        var4 = 2;
        break label_6;
        label_4:
        if (!(var3.equals("active"))) break label_6;
        var4 = 3;
        break label_6;
        label_5:
        if (!(var3.equals("list"))) break label_6;
        var4 = 4;
        label_6:
        switch (var4) {
            case 0 -> { break label_7; }
            case 1 -> { break label_9; }
            case 2 -> { break label_11; }
            case 3 -> { break label_13; }
            case 4 -> { break label_14; }
            default -> { break label_15; }
        }
        label_7:
        if (arg0.length < 2) {
            messageSyntax();
            return;
        }
        var2.saveKit(arg0[1]);
        break label_16;
        label_9:
        if (arg0.length < 2) {
            var2.listKits();
            break label_16;
        }
        var2.loadKit(arg0[1]);
        break label_16;
        label_11:
        if (arg0.length < 2) {
            messageSyntax();
            return;
        }
        var2.deleteKit(arg0[1]);
        break label_16;
        label_13:
        var2.showActiveKit();
        break label_16;
        label_14:
        var2.listKits();
        break label_16;
        label_15:
        messageSyntax();
        label_16:
    }

}
