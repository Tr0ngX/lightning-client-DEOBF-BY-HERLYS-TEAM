package lightning.settings.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lightning.settings.Setting;
import lombok.Generated;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;

public class WhitelistSetting extends Setting {
    private final WhitelistSetting.Type type;
    private final Set whitelist;

    public WhitelistSetting(String arg0, String arg1, WhitelistSetting.Type arg2) {
        super(arg0, arg0, arg1, new Setting.Visibility());
        this.whitelist = new HashSet();
        this.type = arg2;
    }

    public WhitelistSetting(String arg0, String arg1, String arg2, WhitelistSetting.Type arg3) {
        super(arg0, arg1, arg2, new Setting.Visibility());
        this.whitelist = new HashSet();
        this.type = arg3;
    }

    public WhitelistSetting(String arg0, String arg1, Setting.Visibility arg2, WhitelistSetting.Type arg3) {
        super(arg0, arg0, arg1, arg2);
        this.whitelist = new HashSet();
        this.type = arg3;
    }

    public WhitelistSetting(String arg0, String arg1, String arg2, Setting.Visibility arg3, WhitelistSetting.Type arg4) {
        super(arg0, arg1, arg2, arg3);
        this.whitelist = new HashSet();
        this.type = arg4;
    }

    public void add(Object arg0) {
        this.whitelist.add(arg0);
    }

    public void remove(Object arg0) {
        this.whitelist.remove(arg0);
    }

    public boolean isWhitelistContains(Object arg0) {
        return this.whitelist.contains(arg0);
    }

    public List getWhitelistIds() {
        return this.whitelist.stream().map((arg0) -> {
    if ((arg0 instanceof Item)) {
        Object var1 = ((Item) arg0);
        return BuiltInRegistries.ITEM.getKey(var1).toString();
    }
    if ((arg0 instanceof Block)) {
        var1 = ((Block) arg0);
        return BuiltInRegistries.BLOCK.getKey(var1).toString();
    }
    if ((arg0 instanceof Potion)) {
        var1 = ((Potion) arg0);
        return BuiltInRegistries.POTION.getKey(var1).toString();
    }
    return null;
}).toList();
    }

    public List getWhitelistedPotions() {
        return this.whitelist.stream().filter((arg0) -> (arg0 instanceof Potion)).map((arg0) -> ((Potion) arg0)).toList();
    }

    public void clear() {
        this.whitelist.clear();
    }

    @Generated
    public WhitelistSetting.Type getType() {
        return this.type;
    }

    @Generated
    public Set getWhitelist() {
        return this.whitelist;
    }


    public enum Type {
        ITEMS,
        BLOCKS,
        POTIONS;


        private Type() {
        }

    }
}
