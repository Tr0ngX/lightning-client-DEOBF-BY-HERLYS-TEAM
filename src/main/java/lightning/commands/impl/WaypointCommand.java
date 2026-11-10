package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.managers.WaypointManager;
import lightning.utils.chat.ChatUtils;
import net.minecraft.world.phys.Vec3;

@RegisterCommand(name = "waypoint", tag = "Waypoint", description = "Allows you to manage the client's custom waypoints.", syntax = "<add|del> <[x, y, z]|[x, z]> | <clear|list>", aliases = "w")
public class WaypointCommand extends Command {
    public List getSuggestions(String[] arg0) {
        if (arg0.length == 0) return List.of("add", "del", "clear", "list");
        if (arg0.length == 1 && arg0[0].equalsIgnoreCase("del")) {
            Object var2 = new ArrayList();
            Object var3 = LightningClient.WAYPOINT_MANAGER.getWaypoints().iterator();
            while (var3.hasNext()) {
                Object var4 = ((WaypointManager.Waypoint) var3.next());
                var2.add(var4.getName());
            }
            return var2;
        }
        return List.of();
    }

    public void execute(String[] arg0) {
        if (arg0.length == 1) {
            if (arg0[0].equalsIgnoreCase("clear")) {
                LightningClient.WAYPOINT_MANAGER.clear();
                LightningClient.CHAT_MANAGER.tagged("Successfully cleared your custom waypoints.", getTag(), (getName() + "-list"));
                break label_25;
            }
            if (arg0[0].equalsIgnoreCase("list")) {
                Object var2 = LightningClient.WAYPOINT_MANAGER.getWaypoints();
                if (!(var2.isEmpty())) break label_1;
                LightningClient.CHAT_MANAGER.tagged("You currently have no custom waypoints set.", getTag());
                break label_6;
                label_1:
                Object var3 = new StringBuilder();
                int var4 = 0;
                Object var5 = var2.iterator();
                while (var5.hasNext()) {
                    Object var6 = ((WaypointManager.Waypoint) var5.next());
                    var4++;
                    var3.append(ChatUtils.getSecondary()).append(var6.getName()).append((var4 == var2.size() ? "" : ", "));
                }
                LightningClient.CHAT_MANAGER.message(("Custom waypoints " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + var2.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(var3)), (getName() + "-list"));
                label_6:
                break label_25;
            }
            messageSyntax();
            break label_25;
        }
        if (arg0.length != 2 && arg0.length != 4) {
            if (arg0.length != 5) break label_24;
            try {
            }
            if (arg0.length == 2) {
            } else {
            }
            var3 = Integer.parseInt(arg0[2]);
            if (arg0.length != 2) {
                if (arg0.length == 4) {
                }
            } else {
            }
            var tmp0 = Integer.parseInt(arg0[3]);
            var2 = tmp0;
            var4 = tmp0;
            if (arg0.length == 2) {
                break label_17;
            }
            if (arg0.length == 4) {
                break label_17;
            }
            label_17:
            var5 = Integer.parseInt(arg0[4]);
            var6 = new Vec3(((double) var3), ((double) var2), ((double) var5));
            if (arg0[0].equalsIgnoreCase("add")) {
                LightningClient.WAYPOINT_MANAGER.add(arg0[1], var6);
                LightningClient.CHAT_MANAGER.tagged(("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + arg0[1] + " [" + ((int) var6.x) + ", " + ((int) var6.y) + ", " + ((int) var6.z) + "]" + String.valueOf(ChatUtils.getSecondary()) + " to your custom waypoints."), getTag(), getName());
                break label_25;
                try {
                }
                if (arg0[0].equalsIgnoreCase("del")) {
                    LightningClient.WAYPOINT_MANAGER.remove(arg0[1]);
                    LightningClient.CHAT_MANAGER.tagged(("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + arg0[1] + String.valueOf(ChatUtils.getSecondary()) + " to your custom waypoints."), getTag(), getName());
                    break label_25;
                    try {
                    }
                    messageSyntax();
                } catch (NumberFormatException ex23) {
                    var2 = ex23;
                    LightningClient.CHAT_MANAGER.tagged(("Please input valid " + String.valueOf(ChatUtils.getPrimary()) + "integer" + String.valueOf(ChatUtils.getSecondary()) + " numbers for the coordinates."), getTag(), getName());
                    break label_25;
                    label_24:
                    messageSyntax();
                }
                label_25:
            }
        }
    }

}
