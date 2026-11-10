package lightning.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import lightning.LightningClient;
import lightning.commands.impl.ModuleCommand;
import lightning.core.JarClassScanner;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ChatInputEvent;
import lightning.modules.Module;
import lombok.Generated;

public class CommandManager {
    private final ArrayList commands;
    private String prefix;
    private final CommandDispatcher dispatcher;

    public CommandManager() {
        this.commands = new ArrayList();
        this.prefix = ".";
        this.dispatcher = new CommandDispatcher();
        LightningClient.EVENT_HANDLER.subscribe(this);
        try {
            Object var1 = JarClassScanner.findSubtypesOf(Command.class, LightningClient.class).iterator();
            while (var1.hasNext()) {
                Object var2 = ((Class) var1.next());
                if (var2.getAnnotation(RegisterCommand.class) == null) break label_1;
                if (var2 == ModuleCommand.class) {
                    break label_1;
                }
                this.commands.add(((Command) var2.getDeclaredConstructor(new Class[0]).newInstance(new Object[0])));
            }
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException ex4) {
            var1 = ex4;
            LightningClient.LOGGER.error("Failed to register the client's modules!", var1);
        }
        var1 = new HashSet();
        var2 = this.commands.iterator();
        while (var2.hasNext()) {
            Object var3 = ((Command) var2.next());
            var1.add(var3.getName().toLowerCase());
            Object var4 = var3.getAliases().iterator();
            while (var4.hasNext()) {
                Object var5 = ((String) var4.next());
                var1.add(var5.toLowerCase());
            }
        }
        var2 = LightningClient.MODULE_MANAGER.getModules().iterator();
        while (var2.hasNext()) {
            var3 = ((Module) var2.next());
            if (var1.contains(var3.getName().toLowerCase())) {
                break label_10;
            }
            this.commands.add(new ModuleCommand(var3));
        }
        var2 = this.commands.iterator();
        while (var2.hasNext()) {
            var3 = ((Command) var2.next());
            register(var3);
        }
    }

    private void register(Command arg0) {
        Object var2 = new ArrayList();
        var2.add(arg0.getName());
        var2.addAll(arg0.getAliases());
        Object var3 = var2.iterator();
        while (var3.hasNext()) {
            Object var4 = ((String) var3.next());
            Object var5 = LiteralArgumentBuilder.literal(var4);
            var5.executes((arg1) -> {
    arg0.execute(new String[0]);
    return 1;
});
            var5.then(RequiredArgumentBuilder.argument("args", StringArgumentType.greedyString()).suggests((arg1, arg2) -> {
    Object var3 = arg2.getRemaining();
    Object var4 = var3.split(" ", -1);
    Object var5 = var4[(var4.length - 1)].toLowerCase();
    Object var6 = ((String[]) Arrays.copyOf(var4, (var4.length - 1)));
    Object var7 = arg2.createOffset(((arg2.getStart() + var3.length()) - var5.length()));
    Object var8 = arg0.getSuggestions(var6).iterator();
    while (var8.hasNext()) {
        Object var9 = ((String) var8.next());
        if (!(var9.toLowerCase().startsWith(var5))) {
            break label_0;
        }
        var7.suggest(var9);
    }
    return var7.buildFuture();
}).executes((arg1) -> {
    Object var2 = StringArgumentType.getString(arg1, "args");
    if (var2.isEmpty()) {
    } else {
    }
    new String[0].execute(var2.split(" "));
    return 1;
}));
            this.dispatcher.register(var5);
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent arg0) {
        Object var2 = arg0.getMessage();
        if (!(var2.startsWith(this.prefix))) return;
        arg0.setCancelled(true);
        execute(var2);
    }

    public void execute(String arg0) {
        try {
            this.dispatcher.execute(arg0.substring(this.prefix.length()), this);
        } catch (CommandSyntaxException ex2) {
            Object var2 = ex2;
            LightningClient.CHAT_MANAGER.warn("Could not find the command specified.");
        }
    }

    public Command getCommand(String arg0) {
        return ((Command) this.commands.stream().filter((arg1) -> {
    if (!(arg1.getName().equalsIgnoreCase(arg0.toLowerCase()))) {
        if (arg1.getAliases().contains(arg0.toLowerCase())) {
        }
    } else {
    }
    return false;
}).findFirst().orElse(null));
    }

    @Generated
    public ArrayList getCommands() {
        return this.commands;
    }

    @Generated
    public String getPrefix() {
        return this.prefix;
    }

    @Generated
    public void setPrefix(String arg0) {
        this.prefix = arg0;
    }

    @Generated
    public CommandDispatcher getDispatcher() {
        return this.dispatcher;
    }

}
