package lightning.utils.color;

import java.awt.Color;
import lightning.LightningClient;
import lightning.modules.impl.core.ColorModule;
import lightning.settings.impl.ColorSetting;
import net.minecraft.ChatFormatting;

public class ColorUtils {
    public static ColorSetting.Color getDefaultColor() {
        return new ColorSetting.Color(new Color(160, 120, 255), true, false);
    }

    public static ColorSetting.Color getDefaultFillColor() {
        return new ColorSetting.Color(new Color(160, 120, 255, 40), true, false);
    }

    public static ColorSetting.Color getDefaultOutlineColor() {
        return new ColorSetting.Color(new Color(160, 120, 255, 120), true, false);
    }

    public static Color getGlobalColor() {
        return getGlobalColor(255, 0L);
    }

    public static Color getGlobalColor(int arg0) {
        return getGlobalColor(arg0, 0L);
    }

    public static Color getGlobalColor(int arg0, long arg1) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var3 = null;
        Object var4 = null;
        if (var3 == null) return new Color(160, 120, 255, arg0);
        if (var3.color.isRainbow()) return getRainbow(var3.rainbowSpeed.getValue().longValue(), (var3.rainbowSaturation.getValue().floatValue() / 100.0f), (var3.rainbowBrightness.getValue().floatValue() / 100.0f), arg0, arg1);
        return getColor(var3.color.getValue().getColor(), arg0);
    }

    public static Color getColor(Color arg0, int arg1) {
        return new Color(arg0.getRed(), arg0.getGreen(), arg0.getBlue(), arg1);
    }

    public static Color getRainbow() {
        return getRainbow(255);
    }

    public static Color getOffsetRainbow(long arg0) {
        return getOffsetRainbow(255, arg0);
    }

    public static Color getRainbow(int arg0) {
        return getOffsetRainbow(arg0, 0L);
    }

    public static Color getOffsetRainbow(int arg0, long arg1) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var3 = null;
        if (var3 != null) {
        } else {
        }
        long var4 = 6L;
        if (var3 != null) {
        } else {
        }
        float var6 = 1.0f;
        if (var3 != null) {
        } else {
        }
        float var7 = 1.0f;
        if (var3 != null && var3.rainbowScale != null) {
        } else {
        }
        float var8 = 1.0f;
        return getRainbow(var4, var6, var7, arg0, ((long) (((float) arg1) * var8)));
    }

    public static Color getRainbow(long arg0, float arg1, float arg2, int arg3) {
        return getRainbow(arg0, arg1, arg2, arg3, 0L);
    }

    public static Color getRainbow(long arg0, float arg1, float arg2, int arg3, long arg4) {
        arg0 = ((long) Math.clamp(arg0, 1, 20));
        double var9 = (10500.0 - (500.0 * ((double) arg0)));
        double var11 = (((double) (System.currentTimeMillis() % ((long) var9))) / var9);
        double var7 = (((double) arg4) * 0.0035);
        float var13 = ((float) ((var11 + (((double) arg4) * 0.0035)) % 1.0));
        if (var13 < 0.0f) {
            var13 = (var13 + 1.0f);
        }
        Object var14 = new Color(Color.HSBtoRGB(Math.clamp(var13, 0.0f, 1.0f), Math.clamp(arg1, 0.0f, 1.0f), Math.clamp(arg2, 0.0f, 1.0f)));
        return new Color(var14.getRed(), var14.getGreen(), var14.getBlue(), arg3);
    }

    public static Color getOffsetWave(Color arg0, long arg1) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var3 = null;
        if (var3 != null) {
        } else {
        }
        long var4 = 6L;
        return getWave(arg0, var4, 255, arg1);
    }

    public static Color getWave(Color arg0, long arg1, int arg2, long arg3) {
        arg1 = Math.max(1L, Math.min(arg1, 20L));
        Object var6 = new float[3];
        Color.RGBtoHSB(arg0.getRed(), arg0.getGreen(), arg0.getBlue(), var6);
        double var7 = (10500.0 - (500.0 * ((double) arg1)));
        double var9 = (((double) (System.currentTimeMillis() % ((long) var7))) / var7);
        double var11 = (((double) arg3) * 0.0035);
        double var13 = ((var9 + var11) % 1.0);
        if (var13 < 0.0) {
            var13 = (var13 + 1.0);
        }
        float var15 = ((float) Math.abs((((var13 * 2.0) % 2.0) - 1.0)));
        var6[2] = (0.5f + (0.5f * var15));
        Object var16 = new Color(Color.HSBtoRGB(var6[0], var6[1], var6[2]));
        return new Color(var16.getRed(), var16.getGreen(), var16.getBlue(), arg2);
    }

    public static Color getPulse(Color arg0) {
        return getPulse(arg0, 15L);
    }

    public static Color getPulse(Color arg0, long arg1) {
        arg1 = Math.max(1L, Math.min(arg1, 20L));
        double var3 = Math.sin(((6.283185307179586 * ((double) (((float) arg1) / 20.0f))) * ((double) (((float) (System.currentTimeMillis() - LightningClient.UPTIME)) / 1000.0f))));
        double var5 = ((var3 + 1.0) / 2.0);
        return new Color(arg0.getRed(), arg0.getGreen(), arg0.getBlue(), ((int) (((double) arg0.getAlpha()) * var5)));
    }

    public static Color getHashColor(String arg0) {
        int var1 = arg0.hashCode();
        return new Color(((var1 & 16711680) >> 16), ((var1 & 65280) >> 8), (var1 & 255));
    }

    public static boolean isValidColorCode(String arg0) {
        if (arg0.startsWith("#")) {
            arg0 = arg0.substring(1);
        }
        if (arg0.length() != 3 && arg0.length() != 6) return false;
        int var1 = 0;
        while (var1 < arg0.length()) {
            if (arg0.charAt(var1) >= 48) {
                if (arg0.charAt(var1) <= 9) break label_6;
            }
            if (arg0.charAt(var1) >= 97) {
                if (arg0.charAt(var1) <= 102) break label_6;
            }
            if (arg0.charAt(var1) >= 65) break label_6;
            if (arg0.charAt(var1) <= 70) {
                break label_6;
            }
            return false;
            label_6:
            var1++;
        }
        return true;
    }

    public static ChatFormatting getHealthColor(double arg0) {
        if (arg0 > 18.0) return ChatFormatting.GREEN;
        if (arg0 > 16.0) return ChatFormatting.DARK_GREEN;
        if (arg0 > 12.0) return ChatFormatting.YELLOW;
        if (arg0 > 8.0) return ChatFormatting.GOLD;
        if (arg0 > 5.0) return ChatFormatting.RED;
        return ChatFormatting.DARK_RED;
    }

    public static ChatFormatting getTotemColor(int arg0) {
        if (arg0 == 1) return ChatFormatting.GREEN;
        if (arg0 == 2) return ChatFormatting.DARK_GREEN;
        if (arg0 == 3) return ChatFormatting.YELLOW;
        if (arg0 == 4) return ChatFormatting.GOLD;
        if (arg0 == 5) return ChatFormatting.RED;
        return ChatFormatting.DARK_RED;
    }

}
