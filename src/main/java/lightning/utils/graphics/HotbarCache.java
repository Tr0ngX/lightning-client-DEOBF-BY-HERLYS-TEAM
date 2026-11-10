package lightning.utils.graphics;

import java.util.Objects;
import lightning.mixins.accessors.GuiGraphicsExtractorAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

public class HotbarCache {
    private static final HotbarCache.SlotEntry[] SLOTS;

    public static void clear() {
        Object var0 = SLOTS;
        int var1 = var0.length;
        int var2 = 0;
        while (var2 < var1) {
            Object var3 = var0[var2];
            var3.clear();
            var2++;
        }
    }

    public static void extractItem(GuiGraphicsExtractor arg0, LivingEntity arg1, ItemStack arg2, int arg3, int arg4, int arg5) {
        if (arg2.isEmpty()) return;
        Object var6 = Minecraft.getInstance();
        Object var7 = ((GuiGraphicsExtractorAccessor) arg0).night$getGuiRenderState();
        if (arg5 >= 1 && arg5 <= 10) {
        } else {
        }
        int var8 = -1;
        Object var9 = null;
        if (var8 >= 0 && var8 < SLOTS.length) {
            Object var10 = SLOTS[var8];
            if (var10.matches(arg2, arg5)) {
                var9 = var10.renderState;
            } else {
                var9 = new TrackingItemStackRenderState();
                if (var6.getItemModelResolver() != null && arg1 != null && arg1.level() != null) {
                    var6.getItemModelResolver().updateForTopItem(var9, arg2, ItemDisplayContext.GUI, arg1.level(), arg1, arg5);
                }
                var10.set(arg2, arg5, var9);
            }
            break label_7;
        }
        var9 = new TrackingItemStackRenderState();
        if (((var6.getItemModelResolver() == null) || (arg1 == null)) || (arg1.level() == null)) break label_7;
        var6.getItemModelResolver().updateForTopItem(var9, arg2, ItemDisplayContext.GUI, arg1.level(), arg1, arg5);
        label_7:
        if (var7 != null && var9 != null) {
            var10 = new Matrix3x2f(arg0.pose());
            var7.addItem(new GuiItemRenderState(var10, var9, arg3, arg4, ((ScreenRectangle) null)));
        }
    }

    static {
        SLOTS = new HotbarCache.SlotEntry[10];
        int var0 = 0;
        while (var0 < SLOTS.length) {
            SLOTS[var0] = new HotbarCache.SlotEntry();
            var0++;
        }
    }


    public class SlotEntry {
        public Item item;
        public int count;
        public int damage;
        public Object components;
        public int seed;
        public TrackingItemStackRenderState renderState;

        public boolean matches(ItemStack arg0, int arg1) {
            if (((arg0 == null) || (arg0.isEmpty())) || (this.renderState == null)) {
                return false;
            }
            if (this.renderState.isAnimated()) return false;
            if ((((arg0.getItem() != this.item) || (arg0.getCount() != this.count)) || (arg0.getDamageValue() != this.damage)) || (!(Objects.equals(arg0.getComponents(), this.components)))) break label_3;
            return this.seed == arg1;
        }

        public void set(ItemStack arg0, int arg1, TrackingItemStackRenderState arg2) {
            this.item = arg0.getItem();
            this.count = arg0.getCount();
            this.damage = arg0.getDamageValue();
            this.components = arg0.getComponents();
            this.seed = arg1;
            this.renderState = arg2;
        }

        public void clear() {
            this.item = null;
            this.count = 0;
            this.damage = 0;
            this.components = null;
            this.seed = 0;
            this.renderState = null;
        }

    }
}
