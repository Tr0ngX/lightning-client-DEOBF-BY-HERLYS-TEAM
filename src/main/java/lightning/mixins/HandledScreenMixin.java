package lightning.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.modules.impl.core.NoMiddleClickModule;
import lightning.modules.impl.miscellaneous.ShulkerInfoModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.InventoryControlModule;
import lightning.modules.impl.player.ChestStealerModule;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen implements IMinecraft {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Shadow
    protected AbstractContainerMenu menu;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Shadow
    protected int imageWidth;
    private Slot night$lastDragSlot;

    @Shadow
    protected abstract void slotClicked(Slot arg0, int arg1, int arg2, ContainerInput arg3);

    protected HandledScreenMixin(Component arg0) {
        super(arg0);
        this.night$lastDragSlot = null;
    }

    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
    private void night$trimShulkerName(ItemStack arg0, CallbackInfoReturnable arg1) {
        Object var3 = ((ShulkerInfoModule) LightningClient.MODULE_MANAGER.getModule(ShulkerInfoModule.class));
        if (((var3 == null) || (!(var3.isToggled()))) || (!(var3.hasItems(arg0)))) {
            return;
        }
        Object var4 = ((List) arg1.getReturnValue());
        if ((var4 == null) || (var4.size() <= 1)) {
            return;
        }
        arg1.setReturnValue(new ArrayList(var4.subList(1, var4.size())));
    }

    @ModifyArgs(method = "extractTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"))
    private void night$pushVanillaTooltipBelowShulkerInfo(Args arg0) {
        Object var2 = ((ShulkerInfoModule) LightningClient.MODULE_MANAGER.getModule(ShulkerInfoModule.class));
        if ((var2 == null) || (!(var2.isToggled()))) {
            return;
        }
        if (((this.hoveredSlot == null) || (this.hoveredSlot.getItem().isEmpty())) || (!(var2.hasItems(this.hoveredSlot.getItem())))) {
            return;
        }
        int var3 = ((Integer) arg0.get(4)).intValue();
        arg0.set(4, (var3 + 20));
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void night$dragClick(MouseButtonEvent arg0, double arg1, double arg2, CallbackInfoReturnable arg3) {
        Object var8 = ((InventoryControlModule) LightningClient.MODULE_MANAGER.getModule(InventoryControlModule.class));
        if ((!(var8.isToggled())) || (!(var8.dragClick.getValue()))) {
            return;
        }
        if (!(InputConstants.isKeyDown(mc.getWindow(), 340))) {
            if (InputConstants.isKeyDown(mc.getWindow(), 344)) {
            }
        } else {
        }
        int var7 = 0;
        int var9 = 0;
        if ((arg0.button()) || (!var7)) {
            return;
        }
        if (((this.hoveredSlot == null) || (this.hoveredSlot.getItem().isEmpty())) || (this.hoveredSlot == this.night$lastDragSlot)) {
            return;
        }
        this.night$lastDragSlot = this.hoveredSlot;
        slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ContainerInput.QUICK_MOVE);
        arg3.setReturnValue(true);
        arg3.cancel();
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void night$dragClickReset(MouseButtonEvent arg0, CallbackInfoReturnable arg1) {
        this.night$lastDragSlot = null;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void night$chestStealerButtons(CallbackInfo arg0) {
        if (LightningClient.MODULE_MANAGER == null) break label_2;
        Object var2 = ((ChestStealerModule) LightningClient.MODULE_MANAGER.getModule(ChestStealerModule.class));
        if (((var2 == null) || (!(var2.isToggled()))) || (!(var2.buttonMode.getValue()))) break label_2;
        int var3 = (this instanceof InventoryScreen);
        int var4 = (this instanceof CraftingScreen);
        if (!(!var3)) {
            int var5 = ((this.leftPos + this.imageWidth) - 60);
            int var6 = (this.topPos - 22);
            addRenderableWidget(Button.builder(Component.literal("Drop"), (arg1) -> arg0.triggerDrop()).bounds(var5, var6, 60, 18).build());
            break label_2;
        }
        if (!(!var4)) {
            var5 = 46;
            var6 = 18;
            int var7 = 3;
            int var8 = ((this.leftPos + this.imageWidth) - var5);
            int var9 = (this.topPos - 22);
            addRenderableWidget(Button.builder(Component.literal("Drop"), (arg1) -> arg0.triggerDrop()).bounds(var8, var9, var5, var6).build());
            var8 = (var8 - (var5 + var7));
            addRenderableWidget(Button.builder(Component.literal("Dump"), (arg1) -> arg0.triggerDump()).bounds(var8, var9, var5, var6).build());
            var8 = (var8 - (var5 + var7));
            addRenderableWidget(Button.builder(Component.literal("Steal"), (arg1) -> arg0.triggerSteal()).bounds(var8, var9, var5, var6).build());
            break label_2;
        }
        var5 = ((this.leftPos + this.imageWidth) + 4);
        var6 = this.topPos;
        addRenderableWidget(Button.builder(Component.literal("Steal"), (arg1) -> arg0.triggerSteal()).bounds(var5, var6, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Dump"), (arg1) -> arg0.triggerDump()).bounds(var5, (var6 + 22), 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Drop"), (arg1) -> arg0.triggerDrop()).bounds(var5, (var6 + 44), 60, 20).build());
        label_2:
    }

    @Redirect(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack night$grimSwapVisual(Slot arg0) {
        Object var3 = arg0.getItem();
        if (LightningClient.MODULE_MANAGER == null) return var3;
        Object var4 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        int var5 = var4.getGrimParkedSlot();
        if (var5 == -1) return var3;
        if (arg0.index == 6) {
            var tmp0 = var4.getGrimHiddenElytra();
            Object var2 = tmp0;
            if (tmp0 != null) return var2;
        }
        if (arg0.index == InventoryUtils.indexToSlot(var5)) return var4.getGrimParkedDisplaced();
        return var3;
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void night$onSlotClicked(Slot arg0, int arg1, int arg2, ContainerInput arg3, CallbackInfo arg4) {
        if ((arg3 != ContainerInput.CLONE) || (LightningClient.MODULE_MANAGER == null)) break label_0;
        var tmp0 = ((NoMiddleClickModule) LightningClient.MODULE_MANAGER.getModule(NoMiddleClickModule.class));
        Object var6 = tmp0;
        if ((tmp0 == null) || (!(var6.isToggled()))) break label_0;
        arg4.cancel();
        label_0:
    }

}
