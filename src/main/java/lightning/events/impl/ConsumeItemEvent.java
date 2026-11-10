package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;

public class ConsumeItemEvent extends Event {
    private final ItemStack stack;

    @Generated
    public ItemStack getStack() {
        return this.stack;
    }

    @Generated
    public ConsumeItemEvent(ItemStack arg0) {
        this.stack = arg0;
    }

}
