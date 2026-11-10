package lightning.modules.impl.miscellaneous;

import java.awt.Color;
import lightning.LightningClient;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

@RegisterModule(name = "ShulkerInfo", description = "Renders a preview of the contents of the shulker you are hovering.", category = Module.Category.MISCELLANEOUS)
public class ShulkerInfoModule extends Module {
    public void renderInfo(GuiGraphicsExtractor arg0, int arg1, int arg2, ItemStack arg3) {
        try {
            Object var7 = ((ItemContainerContents) arg3.get(DataComponents.CONTAINER));
            Object var8 = arg3.getItem();
            Object var9 = Color.WHITE;
            if (!((var8 instanceof BlockItem))) break label_4;
            var tmp0 = ((BlockItem) var8);
            Object var5 = tmp0;
            var tmp1 = tmp0.getBlock();
            Object var6 = tmp1;
            if (!((tmp1 instanceof ShulkerBoxBlock))) break label_4;
            Object var10 = ((ShulkerBoxBlock) var6);
            try {
                var9 = new Color(var10.getColor().getTextureDiffuseColor());
            } catch (Exception ex3) {
                Object var11 = ex3;
            }
            label_4:
            arg1 += 4;
            var tmp2 = (arg2 - (62 + LightningClient.FONT_MANAGER.getHeight()));
            arg2 = tmp2;
            Renderer2D.renderQuad(arg0, ((float) arg1), ((float) tmp2), ((float) (arg1 + 164)), ((float) ((arg2 + 60) + LightningClient.FONT_MANAGER.getHeight())), new Color(0, 0, 0, 200));
            Renderer2D.renderOutline(arg0, ((float) arg1), ((float) arg2), ((float) (arg1 + 164)), ((float) ((arg2 + 60) + LightningClient.FONT_MANAGER.getHeight())), var9);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, arg3.getHoverName().getString(), (arg1 + 3), (arg2 + 3), Color.WHITE);
            var10 = 0;
            var11 = 0;
            Object var12 = var7.nonEmptyItemCopyStream().toList().iterator();
            while (var12.hasNext()) {
                Object var13 = ((ItemStack) var12.next());
                int var14 = ((arg1 + 1) + (var11 * 18));
                int var15 = (((arg2 + LightningClient.FONT_MANAGER.getHeight()) + 5) + (var10 * 18));
                arg0.item(var13, var14, var15);
                arg0.itemDecorations(mc.font, var13, var14, var15);
                var11++;
                if (var11 < 9) {
                    break label_5;
                }
                var11 = 0;
                var10++;
            }
        } catch (Exception ex8) {
            var5 = ex8;
        }
    }

    public boolean hasItems(ItemStack arg0) {
        Object var2 = ((ItemContainerContents) arg0.get(DataComponents.CONTAINER));
        if (var2 == null) break label_0;
        return !(var2.nonEmptyItemCopyStream().toList().isEmpty());
    }

}
