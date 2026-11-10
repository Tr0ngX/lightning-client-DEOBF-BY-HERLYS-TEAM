package lightning.utils.minecraft;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public final class EChestMemory {
    private static final EChestMemory INSTANCE;
    private static boolean registered;
    private static NonNullList items;
    private int trackedContainerId;

    private EChestMemory() {
        this.trackedContainerId = -1;
    }

    public static void init() {
        if (!(!registered)) return;
        registered = true;
        LightningClient.EVENT_HANDLER.subscribe(INSTANCE);
    }

    public static boolean hasItems() {
        return items != null;
    }

    public static NonNullList getItems() {
        return items;
    }

    @SubscribeEvent
    public void onPacket(PacketReceiveEvent arg0) {
        Object var3 = arg0.getPacket();
        if ((var3 instanceof ClientboundOpenScreenPacket)) {
            Object var2 = ((ClientboundOpenScreenPacket) var3);
            if (isLookingAtEnderChest()) {
                this.trackedContainerId = var2.getContainerId();
                items = NonNullList.withSize(27, ItemStack.EMPTY);
                break label_8;
            }
            this.trackedContainerId = -1;
            break label_8;
        }
        if (this.trackedContainerId == -1) break label_8;
        Object var4 = arg0.getPacket();
        if ((var4 instanceof ClientboundContainerSetContentPacket)) {
            var3 = ((ClientboundContainerSetContentPacket) var4);
        } else {
            Object var5 = arg0.getPacket();
            if (!((var5 instanceof ClientboundContainerSetSlotPacket))) break label_4;
            var4 = ((ClientboundContainerSetSlotPacket) var5);
            if (var4.getContainerId() != this.trackedContainerId) return;
            var5 = var4.getSlot();
            if (((items == null) || (var5 < 0)) || (var5 >= 27)) break label_4;
            items.set(var5, var4.getItem().copy());
            label_4:
            break label_8;
        }
        if (var3.containerId() != this.trackedContainerId) break label_8;
        var4 = NonNullList.withSize(27, ItemStack.EMPTY);
        var5 = 0;
        while (var5 < 27 && var5 < var3.items().size()) {
            var4.set(var5, ((ItemStack) var3.items().get(var5)).copy());
            var5++;
        }
        items = var4;
        label_8:
    }

    private boolean isLookingAtEnderChest() {
        Object var1 = Minecraft.getInstance();
        if (var1.level == null) return false;
        Object var2 = var1.hitResult;
        if (!((var2 instanceof BlockHitResult))) return false;
        Object var3 = ((BlockHitResult) var2);
        Object var4 = var3.getBlockPos();
        return var1.level.getBlockState(var4).is(Blocks.ENDER_CHEST);
    }

    static {
        INSTANCE = new EChestMemory();
        registered = false;
        items = null;
    }

}
