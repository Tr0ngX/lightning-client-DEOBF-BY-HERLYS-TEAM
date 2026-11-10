package lightning.gui.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

public final class DescriptionLayout {
    public static List wrap(String arg0, int arg1, ToIntFunction arg2) {
        if (arg0.isEmpty()) return List.of();
        if (arg1 < 1) throw new IllegalArgumentException("No text width");
        Object var3 = new ArrayList();
        Object var4 = arg0.split("\\R", -1);
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            Object var7 = var4[var6];
            Object var8 = "";
            Object var9 = var7.strip().split("\\s+");
            int var10 = var9.length;
            int var11 = 0;
            while (var11 < var10) {
                Object var12 = var9[var11];
                if (var12.isEmpty()) {
                    break label_12;
                }
                if (var8.isEmpty()) {
                } else {
                }
                Object var13 = (var8 + " " + var12);
                if (arg2.applyAsInt(var13) <= arg1) {
                    var8 = var13;
                    break label_12;
                }
                if (!(var8.isEmpty())) {
                    var3.add(var8);
                    var8 = "";
                }
                int var14 = 0;
                while (var14 < var12.length()) {
                    int var15 = var12.codePointAt(var14);
                    Object var16 = new String(Character.toChars(var15));
                    if (arg2.applyAsInt(var16) > arg1) throw new IllegalArgumentException("Glyph exceeds text width");
                    if (arg2.applyAsInt((var8 + var16)) > arg1) {
                        var3.add(var8);
                        var8 = "";
                    }
                    var8 = (var8 + var16);
                    var14 = (var14 + Character.charCount(var15));
                }
                label_12:
                var11++;
            }
            var3.add(var8);
            var6++;
        }
        return List.copyOf(var3);
    }

    public static DescriptionLayout.Panel create(String arg0, String arg1, String arg2, String arg3, int arg4, int arg5, int arg6, int arg7, int arg8, int arg9, int arg10, int arg11, List arg12, ToIntFunction arg13) {
        int var14 = Math.max(arg6, (arg5 + 4));
        int var15 = ((arg13.applyAsInt(arg2) + arg13.applyAsInt(arg3)) + (arg4 * 3));
        if ((arg10 < var15) || (arg11 < (var14 + (arg4 * 2)))) {
            return null;
        }
        Object var16 = new LinkedHashSet();
        var16.add(Math.min(arg10, Math.max(var15, arg7)));
        int var17 = (((Integer) var16.iterator().next()).intValue() + 24);
        while (var17 < arg10) {
            var16.add(var17);
            var17 += 24;
        }
        var16.add(arg10);
        var17 = arg12.iterator();
        while (var17.hasNext()) {
            Object var18 = ((DescriptionLayout.Rect) var17.next());
            if (var18.x >= var15 && var18.x <= arg10) {
                var16.add(var18.x);
            }
            int var19 = ((arg10 - var18.x) - var18.width);
            if (var19 >= var15 && var19 <= arg10) {
                var16.add(var19);
            }
            Object var20 = arg12.iterator();
            while (var20.hasNext()) {
                Object var21 = ((DescriptionLayout.Rect) var20.next());
                int var22 = ((var21.x - var18.x) - var18.width);
                if (var22 >= var15 && var22 <= arg10) {
                    var16.add(var22);
                }
            }
        }
        var17 = var16.iterator();
        while (var17.hasNext()) {
            var18 = ((Integer) var17.next()).intValue();
            try {
                var19 = wrap(arg0, (var18 - (arg4 * 2)), arg13);
                var20 = wrap(arg1, (var18 - (arg4 * 2)), arg13);
            } catch (IllegalArgumentException ex14) {
                var21 = ex14;
                break label_11;
            }
            var21 = ((var14 + (arg4 * 2)) + ((var19.size() + var20.size()) * arg5));
            if (var21 > arg11) {
                break label_11;
            }
            var22 = new LinkedHashSet();
            Object var23 = new LinkedHashSet();
            var22.add(Math.clamp(((long) arg8), 0, (arg10 - var18)));
            var23.add(Math.clamp(((long) arg9), 0, (arg11 - var21)));
            var22.add(0);
            var22.add((arg10 - var18));
            var23.add(0);
            var23.add((arg11 - var21));
            Object var24 = arg12.iterator();
            while (var24.hasNext()) {
                Object var25 = ((DescriptionLayout.Rect) var24.next());
                var22.add((var25.x - var18));
                var22.add((var25.x + var25.width));
                var23.add((var25.y - var21));
                var23.add((var25.y + var25.height));
            }
            var24 = var23.iterator();
            while (var24.hasNext()) {
                var25 = ((Integer) var24.next()).intValue();
                Object var26 = var22.iterator();
                while (var26.hasNext()) {
                    int var27 = ((Integer) var26.next()).intValue();
                    if (((var27 < 0) || (var25 < 0)) || ((var27 + var18) > arg10)) break label_20;
                    if ((var25 + var21) > arg11) {
                        break label_20;
                    }
                    Object var28 = new DescriptionLayout.Rect(var27, var25, var18, var21);
                    Objects.requireNonNull(var28);
                    if (arg12.stream().noneMatch(var28::intersects)) return new DescriptionLayout.Panel(var28, var14, var19, var20);
                }
            }
        }
        return null;
    }


    public record Panel(DescriptionLayout.Rect bounds, int headerHeight, List english, List vietnamese) {

        public Panel(DescriptionLayout.Rect arg0, int arg1, List arg2, List arg3) {
            this.bounds = arg0;
            this.headerHeight = arg1;
            this.english = arg2;
            this.vietnamese = arg3;
        }

        public int totalHeight() {
            return this.bounds.height();
        }

        public DescriptionLayout.Rect bounds() {
            return this.bounds;
        }

        public int headerHeight() {
            return this.headerHeight;
        }

        public List english() {
            return this.english;
        }

        public List vietnamese() {
            return this.vietnamese;
        }

    }

    public record Rect(int x, int y, int width, int height) {

        public Rect(int arg0, int arg1, int arg2, int arg3) {
            this.x = arg0;
            this.y = arg1;
            this.width = arg2;
            this.height = arg3;
        }

        public boolean intersects(DescriptionLayout.Rect arg0) {
            if (((this.x >= (arg0.x + arg0.width)) || ((this.x + this.width) <= arg0.x)) || (this.y >= (arg0.y + arg0.height))) break label_0;
            return (this.y + this.height) > arg0.y;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }

    }
}
