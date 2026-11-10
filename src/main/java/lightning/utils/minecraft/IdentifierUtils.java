package lightning.utils.minecraft;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;

public class IdentifierUtils {
    public static Item getItem(String arg0) {
        try {
            Object var1 = ((Item) getIdentifier(BuiltInRegistries.ITEM, arg0));
            if (var1 != null) return var1;
        } catch (Exception ex3) {
            var1 = ex3;
        }
        return null;
    }

    public static Block getBlock(String arg0) {
        try {
            Object var1 = ((Block) getIdentifier(BuiltInRegistries.BLOCK, arg0));
            if (var1 != null) return var1;
        } catch (Exception ex3) {
            var1 = ex3;
        }
        return null;
    }

    public static Potion getPotion(String arg0) {
        try {
            Object var1 = ((Potion) getIdentifier(BuiltInRegistries.POTION, arg0));
            if (var1 != null) return var1;
        } catch (Exception ex3) {
            var1 = ex3;
        }
        return null;
    }

    public static Object getIdentifier(Registry arg0, String arg1) {
        if ((arg1 == null) || (arg1.trim().isEmpty())) {
            return null;
        }
        var tmp0 = arg1.trim();
        arg1 = tmp0;
        if (tmp0.contains(":")) {
        } else {
        }
        Object var2 = Identifier.tryParse(("minecraft:" + arg1));
        if (var2 == null) return null;
        if (arg0.containsKey(var2)) return arg0.getValue(var2);
        return null;
    }

}
