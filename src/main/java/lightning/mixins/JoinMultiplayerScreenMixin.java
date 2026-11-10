package lightning.mixins;

import lightning.gui.components.AccountButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JoinMultiplayerScreen.class, priority = 10000)
public abstract class JoinMultiplayerScreenMixin extends Screen {
    protected JoinMultiplayerScreenMixin(Component arg0) {
        super(arg0);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo arg0) {
        int var4 = 75;
        int var5 = 20;
        Object var6 = null;
        Object var7 = children().iterator();
        label_0:
        if (!(var7.hasNext())) break label_5;
        Object var8 = ((GuiEventListener) var7.next());
        if (!((var8 instanceof AbstractWidget))) {
            break label_0;
        }
        Object var10 = ((AbstractWidget) var8);
        Object var11 = var10.getClass().getName().toLowerCase();
        if (var10.getMessage() != null) {
        } else {
        }
        Object var9 = "";
        Object var12 = "";
        if (!(var11.contains("viafabric")) && !(var9.contains("viafabric")) && !(var9.contains("viaversion"))) {
            break label_0;
        }
        var6 = var10;
        break label_5;
        label_5:
        if (var6 != null) {
            int var3 = ((var6.getX() - var4) - 4);
            int var2 = var6.getY();
            if (var6.getHeight() > 0) {
            } else {
            }
            var5 = 20;
        } else {
            var3 = ((this.width - var4) - 6);
            var2 = 5;
        }
        addRenderableWidget(AccountButton.create(var3, var2, var4, var5, this));
    }

}
