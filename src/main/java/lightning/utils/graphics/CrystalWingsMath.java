package lightning.utils.graphics;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public final class CrystalWingsMath {
    public static final String[] STYLES;

    private static Path2D.Double path(double arg0, double arg1) {
        Object var4 = new Path2D.Double();
        var4.moveTo(arg0, arg1);
        return var4;
    }

    private static List flatten(Path2D arg0) {
        Object var1 = new ArrayList();
        Object var2 = new double[6];
        Object var3 = arg0.getPathIterator(null, 0.015);
        while (!(var3.isDone())) {
            int var4 = var3.currentSegment(var2);
            if (var4 != 4) {
                var tmp0 = new double[2];
                tmp0[0] = var2[0];
                tmp0[1] = var2[1];
                var1.add(tmp0);
            }
            var3.next();
        }
        if (var1.size() > 1 && Math.hypot((((double[]) var1.getFirst())[0] - ((double[]) var1.getLast())[0]), (((double[]) var1.getFirst())[1] - ((double[]) var1.getLast())[1])) < 1.0E-8) {
            var1.removeLast();
        }
        return List.copyOf(var1);
    }

    public static CrystalWingsMath.Mesh model(String arg0) {
        Object var1 = path(0.08, -0.32);
        Object var2 = new ArrayList();
        Object var3 = arg0;
        int var4 = -1;
        switch (var3.hashCode()) {
            case -1512922678 -> { break label_5; }
            case -371048597 -> { break label_3; }
            case 79847370 -> { break label_0; }
            case 80297676 -> { break label_2; }
            case 558407714 -> { break label_1; }
            case 2006435411 -> { break label_4; }
            default -> { break label_6; }
        }
        label_0:
        if (!(var3.equals("Sharp"))) break label_6;
        var4 = 0;
        break label_6;
        label_1:
        if (!(var3.equals("Outline"))) break label_6;
        var4 = 1;
        break label_6;
        label_2:
        if (!(var3.equals("Sweep"))) break label_6;
        var4 = 2;
        break label_6;
        label_3:
        if (!(var3.equals("SoftBat"))) break label_6;
        var4 = 3;
        break label_6;
        label_4:
        if (!(var3.equals("Butterfly"))) break label_6;
        var4 = 4;
        break label_6;
        label_5:
        if (!(var3.equals("MiniAngel"))) break label_6;
        var4 = 5;
        label_6:
        switch (var4) {
            case 0 -> { break label_7; }
            case 1 -> { break label_7; }
            case 2 -> { break label_8; }
            case 3 -> { break label_9; }
            case 4 -> { break label_14; }
            case 5 -> { break label_15; }
            default -> { break label_16; }
        }
        label_7:
        var1.curveTo(0.12, 0.25, 0.7, 0.48, 1.55, 1.08);
        var1.curveTo(1.44, 0.62, 1.22, 0.35, 0.79, 0.14);
        var1.lineTo(1.43, 0.56);
        var1.curveTo(1.34, 0.18, 1.05, 0.04, 0.59, -0.08);
        var1.lineTo(1.15, 0.19);
        var1.curveTo(0.96, -0.14, 0.72, -0.22, 0.38, -0.28);
        var1.curveTo(0.26, -0.29, 0.17, -0.31, 0.08, -0.32);
        break label_17;
        label_8:
        var1.curveTo(0.29, 0.1, 0.89, 0.14, 1.6, 0.65);
        var1.curveTo(1.4, 0.15, 0.93, -0.06, 0.42, -0.11);
        var1.lineTo(1.17, 0.12);
        var1.curveTo(0.95, -0.17, 0.57, -0.24, 0.08, -0.32);
        break label_17;
        label_9:
        var1.curveTo(0.15, 0.02, 0.28, 0.56, 0.38, 0.83);
        var1.curveTo(0.58, 0.6, 1.11, 0.48, 1.5, 0.05);
        var1.curveTo(1.26, 0.18, 1.2, -0.02, 1.17, -0.22);
        var1.curveTo(0.97, -0.05, 0.8, -0.14, 0.77, -0.39);
        var1.curveTo(0.58, -0.18, 0.32, -0.26, 0.08, -0.32);
        var tmp0 = new double[2];
        tmp0[0] = 0.77;
        tmp0[1] = 1.17;
        Object var5 = tmp0;
        int var6 = var5.length;
        int var7 = 0;
        while (var7 < var6) {
            double var8 = var5[var7];
            Object var10 = path(0.38, 0.83);
            var10.quadTo((var8 - 0.22), 0.15, var8, (var8 == 0.77 ? -0.39 : -0.22));
            var2.add(flatten(var10));
            var7++;
        }
        break label_17;
        label_14:
        var1.curveTo(0.27, 0.1, 0.83, 1.1, 1.32, 0.88);
        var1.curveTo(1.73, 0.67, 1.12, 0.1, 0.73, -0.06);
        var1.curveTo(1.28, -0.2, 1.09, -0.76, 0.67, -0.65);
        var1.curveTo(0.35, -0.61, 0.23, -0.47, 0.08, -0.32);
        var5 = path(0.22, -0.23);
        var5.quadTo(0.83, 0.12, 1.2, 0.63);
        var2.add(flatten(var5));
        break label_17;
        label_15:
        var1.curveTo(0.04, 0.19, 0.62, 0.3, 1.25, 0.86);
        var1.curveTo(1.46, 0.79, 1.13, 0.36, 0.77, 0.16);
        var1.curveTo(1.43, 0.66, 1.41, 0.2, 0.66, 0.04);
        var1.curveTo(1.27, 0.37, 1.25, -0.02, 0.52, -0.1);
        var1.curveTo(1.08, 0.04, 0.96, -0.29, 0.38, -0.22);
        var1.curveTo(0.79, -0.21, 0.56, -0.56, 0.08, -0.32);
        break label_17;
        label_16:
        var1.curveTo(-0.01, 0.03, 0.42, 0.22, 1.27, 0.83);
        var1.curveTo(1.65, 1.01, 1.65, 0.56, 1.04, 0.22);
        var1.curveTo(1.76, 0.5, 1.61, -0.04, 0.9, 0.0);
        var1.curveTo(1.55, -0.2, 1.16, -0.56, 0.63, -0.33);
        var1.curveTo(0.39, -0.57, 0.12, -0.57, 0.08, -0.32);
        if (!(arg0.equals("Curl"))) break label_17;
        var5 = path(0.21, -0.34);
        var5.curveTo(0.4, -0.47, 0.54, -0.24, 0.43, -0.1);
        var5.curveTo(0.32, 0.03, 0.22, -0.12, 0.32, -0.18);
        var2.add(flatten(var5));
        label_17:
        var1.closePath();
        var3 = flatten(var1);
        return new CrystalWingsMath.Mesh(var3, triangulate(var3), List.copyOf(var2));
    }

    private static double cross(double[] arg0, double[] arg1, double[] arg2) {
        return (((arg1[0] - arg0[0]) * (arg2[1] - arg0[1])) - ((arg1[1] - arg0[1]) * (arg2[0] - arg0[0])));
    }

    public static double area(double[][] arg0) {
        double var1 = 0.0;
        int var3 = 0;
        while (var3 < arg0.length) {
            Object var4 = arg0[((var3 + 1) % arg0.length)];
            var1 = (var1 + ((arg0[var3][0] * var4[1]) - (var4[0] * arg0[var3][1])));
            var3++;
        }
        return (Math.abs(var1) / 2.0);
    }

    private static List triangulate(List arg0) {
        Object var1 = new ArrayList(arg0);
        Object var2 = new ArrayList();
        double var3 = 0.0;
        int var5 = 0;
        while (var5 < var1.size()) {
            Object var6 = ((double[]) var1.get(var5));
            Object var7 = ((double[]) var1.get(((var5 + 1) % var1.size())));
            var3 = (var3 + ((var6[0] * var7[1]) - (var7[0] * var6[1])));
            var5++;
        }
        label_1:
        var5 = (var3 < 0.0 ? -1.0 : 1.0);
        while (var1.size() > 3) {
            var7 = 0;
            int var8 = 0;
            while (var8 < var1.size()) {
                Object var9 = ((double[]) var1.get((((var8 + var1.size()) - 1) % var1.size())));
                Object var10 = ((double[]) var1.get(var8));
                Object var11 = ((double[]) var1.get(((var8 + 1) % var1.size())));
                if ((var5 * cross(var9, var10, var11)) <= 1.0E-10) {
                    break label_12;
                }
                int var12 = 0;
                Object var13 = var1.iterator();
                while (var13.hasNext()) {
                    Object var14 = ((double[]) var13.next());
                    if ((var14 == var9) || (var14 == var10)) break label_7;
                    if (var14 == var11) {
                        break label_7;
                    }
                    if ((var5 * cross(var9, var10, var14)) >= -1.0E-10 && (var5 * cross(var10, var11, var14)) >= -1.0E-10 && (var5 * cross(var11, var9, var14)) >= -1.0E-10) {
                        var12 = 1;
                        break label_10;
                    }
                }
                label_10:
                if (!(!var12)) {
                    break label_12;
                }
                var tmp0 = new double[][3];
                tmp0[0] = var9;
                tmp0[1] = var10;
                tmp0[2] = var11;
                var2.add(tmp0);
                var1.remove(var8);
                var7 = 1;
                break label_13;
                label_12:
                var8++;
            }
            label_13:
            if (!var7) throw new IllegalArgumentException("Wing silhouette is not a simple polygon");
        }
        var2.add(((double[][]) var1.toArray((arg0) -> new double[][arg0])));
        return List.copyOf(var2);
    }

    public static double progress(long arg0, int arg1) {
        return Math.clamp((((double) arg0) / (((double) Math.max(1, arg1)) * 1000000.0)), 0.0, 1.0);
    }

    public static double ease(double arg0) {
        arg0 = Math.clamp(arg0, 0.0, 1.0);
        return (1.0 - Math.pow((1.0 - arg0), 3.0));
    }

    public static double appearScale(String arg0, double arg1, double arg2, double arg3) {
        if (arg0.equals("Grow")) return (arg2 + ((1.0 - arg2) * ease(arg1)));
        if (arg0.equals("Pop")) return ((arg2 + ((1.0 - arg2) * ease(arg1))) + (arg3 * Math.sin((3.141592653589793 * arg1))));
        return 1.0;
    }

    public static double flap(double arg0, double arg1, double arg2) {
        return (Math.sin((((arg0 * arg1) * 3.141592653589793) * 2.0)) * Math.toRadians(arg2));
    }

    static {
        var tmp0 = new String[8];
        tmp0[0] = "Cloud";
        tmp0[1] = "Curl";
        tmp0[2] = "Sharp";
        tmp0[3] = "MiniAngel";
        tmp0[4] = "Sweep";
        tmp0[5] = "SoftBat";
        tmp0[6] = "Butterfly";
        tmp0[7] = "Outline";
        STYLES = tmp0;
    }


    public record Mesh(List outline, List triangles, List seams) {

        public Mesh(List arg0, List arg1, List arg2) {
            this.outline = arg0;
            this.triangles = arg1;
            this.seams = arg2;
        }

        public List outline() {
            return this.outline;
        }

        public List triangles() {
            return this.triangles;
        }

        public List seams() {
            return this.seams;
        }

    }
}
