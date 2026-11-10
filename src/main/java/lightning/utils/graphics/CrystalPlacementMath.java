package lightning.utils.graphics;

public final class CrystalPlacementMath {
    public static double progress(long arg0, int arg1) {
        return Math.max(0.0, Math.min(1.0, (((double) arg0) / (((double) Math.max(1, arg1)) * 1000000.0))));
    }

    public static double ease(double arg0) {
        arg0 = Math.max(0.0, Math.min(1.0, arg0));
        return (1.0 - Math.pow((1.0 - arg0), 3.0));
    }

    public static double opacity(double arg0) {
        arg0 = Math.max(0.0, Math.min(1.0, arg0));
        double var2 = Math.min(1.0, (arg0 / 0.12));
        var2 = ((var2 * var2) * (3.0 - (2.0 * var2)));
        return ((var2 * (1.0 - arg0)) * (1.0 - arg0));
    }

    public static double radius(double arg0, double arg1, double arg2) {
        return Math.max(0.01, ((arg0 * (0.72 + (0.28 * ease(arg2)))) + (arg1 * ease(arg2))));
    }

    public static double petal(double arg0) {
        return (1.0 + (0.12 * Math.cos((arg0 * 6.0))));
    }

}
