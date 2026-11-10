package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.Module;

@RegisterCommand(name = "all", tag = "All", description = "Mass-toggles modules. 'off' disables all enabled modules; 'off restore' re-enables them.", syntax = "off | off restore")
public class AllCommand extends Command {
    private static final List lastDisabled;

    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) return List.of("off");
        if (arg0.length == 1 && arg0[0].equalsIgnoreCase("off")) return List.of("restore");
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length != 0) {
            if (arg0[0].equalsIgnoreCase("off")) break label_1;
        }
        messageSyntax();
        return;
        label_1:
        if (arg0.length == 2 && arg0[1].equalsIgnoreCase("restore")) {
            if (lastDisabled.isEmpty()) {
                LightningClient.CHAT_MANAGER.tagged("Nothing to restore.", getTag(), getName());
                return;
            }
            int var2 = 0;
            Object var3 = lastDisabled.iterator();
            while (var3.hasNext()) {
                Object var4 = ((Module) var3.next());
                if (var4.isToggled()) {
                    break label_3;
                }
                var4.setToggled(true, false);
                var2++;
            }
            lastDisabled.clear();
            LightningClient.CHAT_MANAGER.tagged(("Restored " + var2 + " module(s)."), getTag(), getName());
            return;
        }
        if (arg0.length == 1) {
            lastDisabled.clear();
            var2 = 0;
            var3 = LightningClient.MODULE_MANAGER.getModules().iterator();
            while (var3.hasNext()) {
                var4 = ((Module) var3.next());
                if (var4.isPersistent()) break label_7;
                if (!(var4.isToggled())) {
                    break label_7;
                }
                var4.setToggled(false, false);
                lastDisabled.add(var4);
                var2++;
            }
            LightningClient.CHAT_MANAGER.tagged(("Disabled " + var2 + " module(s). Use '.all off restore' to bring them back."), getTag(), getName());
            return;
        }
        messageSyntax();
    }

    static {
        lastDisabled = new ArrayList();
    }

}
