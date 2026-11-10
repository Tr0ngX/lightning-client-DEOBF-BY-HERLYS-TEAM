package lightning.commands;

import java.util.Arrays;
import java.util.List;
import lightning.LightningClient;
import lightning.utils.IMinecraft;
import lombok.Generated;

public abstract class Command implements IMinecraft {
    private String name;
    private String tag;
    private final String description;
    private final String syntax;
    private final List aliases;

    public Command() {
        Object var1 = ((RegisterCommand) getClass().getAnnotation(RegisterCommand.class));
        this.name = var1.name();
        if (var1.tag().isEmpty()) {
        } else {
        }
        var1.name().tag = var1.tag();
        this.description = var1.description();
        this.syntax = var1.syntax();
        this.aliases = Arrays.asList(var1.aliases());
    }

    public abstract void execute(String[] arg0);

    public List getSuggestions(String[] arg0) {
        return List.of();
    }

    protected List moduleNames() {
        return LightningClient.MODULE_MANAGER.getModules().stream().map((arg0) -> arg0.getName().toLowerCase()).toList();
    }

    public void messageSyntax() {
        LightningClient.CHAT_MANAGER.info((this.name + " " + this.syntax));
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getTag() {
        return this.tag;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public String getSyntax() {
        return this.syntax;
    }

    @Generated
    public List getAliases() {
        return this.aliases;
    }

    @Generated
    public void setName(String arg0) {
        this.name = arg0;
    }

    @Generated
    public void setTag(String arg0) {
        this.tag = arg0;
    }

}
