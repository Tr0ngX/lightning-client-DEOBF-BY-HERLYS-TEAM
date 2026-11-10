package lightning.modules.impl.visuals.chestscan;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class ChestScanChain {
    public static Map findEdges(Level arg0, Set arg1, BlockPos arg2, int arg3) {
        Object var4 = new HashMap();
        double var5 = (((double) arg3) * ((double) arg3));
        Object var7 = new ArrayDeque(arg1);
        Object var8 = new HashSet(arg1);
        while (!(var7.isEmpty())) {
            Object var11 = ((BlockPos) var7.poll());
            if (var11.distSqr(arg2) > var5) {
                break label_0;
            }
            Object var12 = var11.below();
            Object var13 = arg0.getBlockState(var12);
            if (!((var13.getBlock() instanceof HopperBlock))) break label_2;
            var tmp0 = ((Direction) var13.getValue(HopperBlock.FACING));
            Object var9 = tmp0;
            var tmp1 = var12.relative(tmp0);
            Object var10 = tmp1;
            if (!(isChest(arg0.getBlockState(tmp1)))) break label_2;
            var4.put(var11, var10);
            if (!(var8.add(var10))) break label_2;
            var7.add(var10);
            label_2:
            Object var14 = Direction.values();
            int var15 = var14.length;
            int var16 = 0;
            while (var16 < var15) {
                Object var17 = var14[var16];
                Object var19 = var11.relative(var17);
                Object var20 = arg0.getBlockState(var19);
                if ((!((var20.getBlock() instanceof HopperBlock))) || (var20.getValue(HopperBlock.FACING) != var17.getOpposite())) break label_6;
                var tmp2 = var19.above();
                Object var18 = tmp2;
                if (!(isChest(arg0.getBlockState(tmp2)))) {
                    break label_6;
                }
                var4.put(var18, var11);
                if (!(var8.add(var18))) {
                    break label_6;
                }
                var7.add(var18);
                label_6:
                var16++;
            }
        }
        return var4;
    }

    private static boolean isChest(BlockState arg0) {
        return (arg0.getBlock() instanceof ChestBlock);
    }

    public static Set inferEmpty(Map arg0, Map arg1) {
        Object var2 = new HashMap();
        Object var3 = arg0.entrySet().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Map.Entry) var3.next());
            ((List) var2.computeIfAbsent(((BlockPos) var4.getValue()), (arg0) -> new ArrayList())).add(((BlockPos) var4.getKey()));
        }
        var3 = new HashSet();
        var4 = new HashSet();
        Object var5 = arg1.entrySet().iterator();
        while (var5.hasNext()) {
            Object var6 = ((Map.Entry) var5.next());
            if (var6.getValue() == ChestScanStore.ChestStatus.FULL) break label_8;
            Object var7 = ((BlockPos) var6.getKey());
            if (!(var4.add(var7))) break label_8;
            Object var8 = new ArrayDeque();
            var8.add(var7);
            while (!(var8.isEmpty())) {
                Object var9 = ((BlockPos) var8.poll());
                Object var10 = ((List) var2.getOrDefault(var9, List.of())).iterator();
                while (var10.hasNext()) {
                    Object var11 = ((BlockPos) var10.next());
                    if (var4.add(var11)) {
                        if (!(arg1.containsKey(var11))) {
                            var3.add(var11);
                        }
                        var8.add(var11);
                    }
                }
            }
            label_8:
        }
        return var3;
    }

}
