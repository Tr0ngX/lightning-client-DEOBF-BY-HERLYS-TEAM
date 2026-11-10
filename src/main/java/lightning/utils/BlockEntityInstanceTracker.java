package lightning.utils;

import java.util.ArrayList;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BlockEntityInstanceTracker {
    private static BlockEntityInstanceTracker instance;
    private final ArrayList instances;

    private BlockEntityInstanceTracker() {
        this.instances = new ArrayList();
    }

    public static void initialize() {
        instance = new BlockEntityInstanceTracker();
    }

    public static void constructed(BlockEntity arg0) {
        if (instance != null) {
            instance.instances.add(arg0);
        }
    }

    public static void removed(BlockEntity arg0) {
        if (instance != null) {
            instance.instances.remove(arg0);
        }
    }

    public static void localPlayerConstructed() {
        if (instance != null) {
            instance.instances.clear();
        }
    }

    public static ArrayList snapshot() {
        return new ArrayList(instance.instances);
    }

}
