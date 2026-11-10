package lightning.commands.impl;

import java.io.File;
import lightning.LightningClient;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import net.minecraft.util.Util;

@RegisterCommand(name = "folder", description = "Opens the clients folder.")
public class FolderCommand extends Command {
    public void execute(String[] arg0) {
        Object var2 = new File("Lightning");
        if (var2.exists()) {
            Util.getPlatform().openFile(var2);
        } else {
            LightningClient.CHAT_MANAGER.info("Could not find the client's configuration folder.");
        }
    }

}
