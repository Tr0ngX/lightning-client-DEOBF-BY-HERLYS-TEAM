package lightning.modules.impl.player;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.visuals.BlockHighlightModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@RegisterModule(name = "AirPlace", description = "Lets you place blocks in the air in servers that allow it.", category = Module.Category.PLAYER)
public class AirPlaceModule extends Module {
    public BooleanSetting grim;
    private HitResult hitResult;

    public AirPlaceModule() {
        this.grim = new BooleanSetting("Grim", "Swaps the block into your offhand for just the instant of each placement -- GrimAC's whole scaffolding check group (AirLiquidPlace, RotationPlace, PositionPlace...) only runs for MAIN_HAND placements, verified against its own decompiled source.", false);
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        this.hitResult = mc.getCameraEntity().pick(mc.player.blockInteractionRange(), 0.0f, false);
        Object var3 = this.hitResult;
        if (!((var3 instanceof BlockHitResult))) {
            break label_2;
        }
        Object var2 = ((BlockHitResult) var3);
        if (!((mc.player.getMainHandItem().getItem() instanceof BlockItem))) break label_2;
        break label_3;
        label_2:
        return;
        label_3:
        if ((!(mc.options.keyUse.isDown())) || (!(mc.level.getBlockState(var2.getBlockPos()).isAir()))) break label_5;
        if (this.grim.getValue()) {
            var3 = mc.player.getInventory().getSelectedSlot();
            InventoryUtils.swapEquipment(var3, 45);
            WorldUtils.airPlaceBlock(var2.getBlockPos(), InteractionHand.OFF_HAND, "None", true);
            InventoryUtils.swapEquipment(var3, 45);
            break label_5;
        }
        WorldUtils.airPlaceBlock(var2.getBlockPos(), InteractionHand.MAIN_HAND, "None", true);
        label_5:
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (getNull()) return;
        Object var3 = ((BlockHighlightModule) LightningClient.MODULE_MANAGER.getModule(BlockHighlightModule.class));
        Object var4 = this.hitResult;
        if (!((var4 instanceof BlockHitResult))) {
            break label_2;
        }
        Object var2 = ((BlockHitResult) var4);
        if (!((mc.player.getMainHandItem().getItem() instanceof BlockItem))) break label_2;
        break label_3;
        label_2:
        return;
        label_3:
        if (mc.level.getBlockState(var2.getBlockPos()).isAir()) {
            var4 = new AABB(var2.getBlockPos());
            Renderer3D.renderBox(arg0.getMatrices(), var4, var3.fillColor.getColor());
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var4, var3.outlineColor.getColor());
        }
    }

}
