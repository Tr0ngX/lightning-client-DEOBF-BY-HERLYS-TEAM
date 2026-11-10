package lightning.utils.minecraft;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantmentUtils {
    public static int getEnchantmentLevel(ItemStack arg0, ResourceKey arg1) {
        if ((arg0 == null) || (arg0.isEmpty())) {
            return 0;
        }
        Object var2 = ((ItemEnchantments) arg0.get(DataComponents.ENCHANTMENTS));
        if ((var2 == null) || (var2.isEmpty())) {
            return 0;
        }
        Object var3 = var2.entrySet().iterator();
        label_4:
        if (var3.hasNext()) {
            Object var4 = ((Object2IntMap.Entry) var3.next());
            Object var5 = ((Holder) var4.getKey()).unwrapKey();
            if (!(var5.isPresent())) break label_4;
            if (!(((ResourceKey) var5.get()).equals(arg1))) {
                break label_4;
            }
            return var4.getIntValue();
        }
        return 0;
    }

}
