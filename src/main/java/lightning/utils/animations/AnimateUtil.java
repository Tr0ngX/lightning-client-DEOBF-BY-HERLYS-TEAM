package lightning.utils.animations;

public class AnimateUtil {
    public static double animate(double arg0, double arg1, double arg2) {
        if (arg2 >= 1.0) return arg1;
        if (arg2 <= 0.0) return arg0;
        int var6 = (arg1 > arg0 ? 1 : 0);
        double var7 = Math.abs((arg1 - arg0));
        if (var7 <= 0.001) return arg1;
        double var9 = (var7 * arg2);
        if (!(!var6)) {
        } else {
        }
        return (var9 + -(var9));
    }

    public static float animate(float arg0, float arg1, float arg2) {
        if (arg2 >= 1.0f) return arg1;
        if (arg2 <= 0.0f) return arg0;
        int var3 = (arg1 > arg0 ? 1 : 0);
        float var4 = Math.abs((arg1 - arg0));
        if (var4 <= 0.001f) return arg1;
        float var5 = (var4 * arg2);
        if (!(!var3)) {
        } else {
        }
        return (var5 + -(var5));
    }

}
