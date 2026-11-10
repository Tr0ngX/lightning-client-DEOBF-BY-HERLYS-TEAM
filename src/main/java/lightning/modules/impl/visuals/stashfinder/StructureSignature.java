package lightning.modules.impl.visuals.stashfinder;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureSignature {
    public static boolean isNatural(Minecraft arg0, LevelChunk arg1, Map arg2) {
        if (arg0.level == null) return false;
        int var6 = ((Integer) arg2.getOrDefault("Shulkers", 0)).intValue();
        int var7 = ((Integer) arg2.getOrDefault("Chests", 0)).intValue();
        int var8 = ((Integer) arg2.getOrDefault("Hoppers", 0)).intValue();
        int var9 = ((Integer) arg2.getOrDefault("Barrels", 0)).intValue();
        int var10 = ((Integer) arg2.getOrDefault("Crafters", 0)).intValue();
        int var11 = ((Integer) arg2.getOrDefault("Ender Chests", 0)).intValue();
        int var12 = ((Integer) arg2.getOrDefault("Donkey", 0)).intValue();
        int var13 = ((Integer) arg2.getOrDefault("Chest Boat", 0)).intValue();
        if ((((((((var6 >= 8) || (var7 >= 20)) || (var8 >= 8)) || (var9 >= 16)) || (var10 >= 6)) || (var11 >= 6)) || (var12 >= 4)) || (var13 >= 3)) {
            return false;
        }
        if ((!(arg0.hasSingleplayerServer())) || (arg0.getSingleplayerServer() == null)) break label_9;
        var tmp0 = arg0.getSingleplayerServer().getLevel(arg0.level.dimension());
        Object var5 = tmp0;
        if (tmp0 == null) break label_9;
        var tmp1 = arg1.getPos();
        Object var3 = tmp1;
        var tmp2 = var5.getChunk(tmp1.x(), var3.z());
        Object var4 = tmp2;
        if (tmp2 == null) break label_9;
        Object var14 = var4.getAllStarts().entrySet().iterator();
        label_3:
        if (var14.hasNext()) {
            Object var15 = ((Map.Entry) var14.next());
            if (var15.getValue() == null) break label_3;
            if (!(((StructureStart) var15.getValue()).isValid())) {
                break label_3;
            }
            return true;
        }
        var14 = var4.getAllReferences().values().iterator();
        label_6:
        if (var14.hasNext()) {
            var15 = ((LongSet) var14.next());
            if (var15 == null) break label_6;
            if (var15.isEmpty()) {
                break label_6;
            }
            return true;
        }
        return false;
        label_9:
        return matchesMultiplayerHeuristic(arg0, arg1);
    }

    private static boolean matchesMultiplayerHeuristic(Minecraft arg0, LevelChunk arg1) {
        if (arg0.level != null) {
            break label_1;
        }
        label_1:
        Object var2 = Level.OVERWORLD;
        Object var3 = new int[StructureSignature.Sig.values().length];
        Object var4 = arg1.getSections();
        int var5 = 0;
        while (var5 < var4.length) {
            Object var6 = var4[var5];
            if (var6 == null) break label_14;
            if (var6.hasOnlyAir()) {
                break label_14;
            }
            int var7 = (arg1.getSectionYFromSectionIndex(var5) << 4);
            int var8 = 0;
            while (var8 < 16) {
                int var9 = (var7 + var8);
                int var10 = 0;
                while (var10 < 16) {
                    int var11 = 0;
                    while (var11 < 16) {
                        Object var12 = var6.getBlockState(var10, var8, var11).getBlock();
                        Object var13 = StructureSignature.Sig.values();
                        int var14 = var13.length;
                        int var15 = 0;
                        while (var15 < var14) {
                            Object var16 = var13[var15];
                            if (var16.dimension != null) {
                                if (var16.dimension != var2) break label_10;
                            }
                            if ((var9 < var16.yMin) || (var9 > var16.yMax)) break label_10;
                            if (!(var16.blocks.contains(var12))) {
                                break label_10;
                            }
                            int var17 = var16.ordinal();
                            var3[var17] = (var3[var17] + 1);
                            label_10:
                            var15++;
                        }
                        var11++;
                    }
                    var10++;
                }
                var8++;
            }
            label_14:
            var5++;
        }
        var5 = StructureSignature.Sig.values();
        var6 = var5.length;
        var7 = 0;
        while (var7 < var6) {
            var8 = var5[var7];
            if (var8.dimension != null) {
                if (var8.dimension != var2) break label_19;
            }
            if (var3[var8.ordinal()] < var8.threshold) break label_19;
            if (var8.biomeTag != null && !(chunkHasBiome(arg1, var8))) {
                break label_19;
            }
            return true;
            label_19:
            var7++;
        }
        return false;
    }

    private static boolean chunkHasBiome(LevelChunk arg0, StructureSignature.Sig arg1) {
        if (arg1.biomeTag == null) return true;
        Object var3 = arg0.getPos();
        int var4 = Math.clamp(((long) ((arg1.yMin + arg1.yMax) / 2)), arg0.getMinY(), arg0.getMaxY());
        int var5 = (var4 >> 2);
        int var6 = (var3.getMinBlockX() >> 2);
        int var7 = (var3.getMinBlockZ() >> 2);
        var tmp0 = new int[][5];
        var tmp1 = new int[2];
        tmp1[0] = 0;
        tmp1[1] = 0;
        tmp0[0] = tmp1;
        var tmp2 = new int[2];
        tmp2[0] = 3;
        tmp2[1] = 0;
        tmp0[1] = tmp2;
        var tmp3 = new int[2];
        tmp3[0] = 0;
        tmp3[1] = 3;
        tmp0[2] = tmp3;
        var tmp4 = new int[2];
        tmp4[0] = 3;
        tmp4[1] = 3;
        tmp0[3] = tmp4;
        var tmp5 = new int[2];
        tmp5[0] = 2;
        tmp5[1] = 2;
        tmp0[4] = tmp5;
        Object var2 = tmp0;
        Object var8 = tmp0;
        int var9 = var8.length;
        int var10 = 0;
        while (var10 < var9) {
            Object var11 = var8[var10];
            Object var12 = arg0.getNoiseBiome((var6 + var11[0]), var5, (var7 + var11[1]));
            if (var12 == null) break label_3;
            if (!(var12.is(arg1.biomeTag))) {
                break label_3;
            }
            return true;
            label_3:
            var10++;
        }
        return false;
    }


    enum Sig {
        TRIAL_CHAMBERS,
        ANCIENT_CITY,
        END_CITY,
        NETHER_FORTRESS,
        BASTION_REMNANT,
        OCEAN_MONUMENT,
        WOODLAND_MANSION,
        DESERT_PYRAMID,
        JUNGLE_TEMPLE,
        VILLAGE,
        MINESHAFT,
        STRONGHOLD,
        SHIPWRECK,
        PILLAGER_OUTPOST,
        DUNGEON,
        RUINED_PORTAL;

        final Set blocks;
        final int threshold;
        final TagKey biomeTag;
        final ResourceKey dimension;
        final int yMin;
        final int yMax;

        private Sig(Set arg2, int arg3, TagKey arg4, ResourceKey arg5, int arg6, int arg7) {
            this.blocks = arg2;
            this.threshold = arg3;
            this.biomeTag = arg4;
            this.dimension = arg5;
            this.yMin = arg6;
            this.yMax = arg7;
        }

    }
}
