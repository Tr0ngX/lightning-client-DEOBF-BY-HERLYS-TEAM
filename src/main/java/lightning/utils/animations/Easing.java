package lightning.utils.animations;

import net.minecraft.util.Mth;

public class Easing {
    public static float ease(float arg0, Easing.Method arg1) {
        switch (arg1.ordinal()) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            case 3 -> { break label_2; }
            case 4 -> { break label_3; }
            case 5 -> { break label_4; }
            case 6 -> { break label_5; }
            case 7 -> { break label_6; }
            case 8 -> { break label_7; }
            case 9 -> { break label_8; }
            case 10 -> { break label_9; }
            case 11 -> { break label_10; }
            case 12 -> { break label_11; }
            case 13 -> { break label_12; }
            case 14 -> { break label_13; }
            case 15 -> { break label_14; }
            default -> { break label_15; }
        }
        label_0:
        break label_16;
        label_1:
        break label_16;
        label_2:
        break label_16;
        label_3:
        break label_16;
        label_4:
        break label_16;
        label_5:
        break label_16;
        label_6:
        break label_16;
        label_7:
        break label_16;
        label_8:
        break label_16;
        label_9:
        break label_16;
        label_10:
        break label_16;
        label_11:
        break label_16;
        label_12:
        break label_16;
        label_13:
        break label_16;
        label_14:
        break label_16;
        label_15:
        label_16:
        return linear(arg0);
    }

    public static float toDelta(long arg0, int arg1) {
        return Mth.clamp((((float) toDelta(arg0)) / ((float) arg1)), 0.0f, 1.0f);
    }

    public static long toDelta(long arg0) {
        return (System.currentTimeMillis() - arg0);
    }

    private static float linear(float arg0) {
        return arg0;
    }

    private static float easeInSine(float arg0) {
        return ((float) (1.0 - Math.cos(((((double) arg0) * 3.141592653589793) / 2.0))));
    }

    private static float easeOutSine(float arg0) {
        return ((float) Math.sin(((((double) arg0) * 3.141592653589793) / 2.0)));
    }

    private static float easeInCubic(float arg0) {
        return ((arg0 * arg0) * arg0);
    }

    private static float easeOutCubic(float arg0) {
        return ((float) (1.0 - Math.pow(((double) (1.0f - arg0)), 3.0)));
    }

    private static float easeInQuad(float arg0) {
        return (arg0 * arg0);
    }

    private static float easeOutQuad(float arg0) {
        return (1.0f - ((1.0f - arg0) * (1.0f - arg0)));
    }

    private static float easeInQuart(float arg0) {
        return (((arg0 * arg0) * arg0) * arg0);
    }

    private static float easeOutQuart(float arg0) {
        return ((float) (1.0 - Math.pow(((double) (1.0f - arg0)), 4.0)));
    }

    private static float easeInOutSine(float arg0) {
        return ((float) (-((Math.cos((3.141592653589793 * ((double) arg0))) - 1.0)) / 2.0));
    }

    private static float easeInOutQuad(float arg0) {
        if (arg0 < 0.5f) {
        } else {
        }
        return ((float) (1.0 - (Math.pow(((double) ((-2.0f * arg0) + 2.0f)), 2.0) / 2.0)));
    }

    private static float easeInOutCubic(float arg0) {
        if (arg0 < 0.5f) {
        } else {
        }
        return ((float) (1.0 - (Math.pow(((double) ((-2.0f * arg0) + 2.0f)), 3.0) / 2.0)));
    }

    private static float easeInOutQuart(float arg0) {
        if (arg0 < 0.5f) {
        } else {
        }
        return ((float) (1.0 - (Math.pow(((double) ((-2.0f * arg0) + 2.0f)), 4.0) / 2.0)));
    }

    private static float easeInElastic(float arg0) {
        float var1 = 2.0943952f;
        if (arg0 == 0.0f) {
        } else {
            if (arg0 == 1.0f) {
            } else {
            }
        }
        return ((float) (-(Math.pow(2.0, ((double) ((10.0f * arg0) - 10.0f)))) * Math.sin(((((double) (arg0 * 10.0f)) - 10.75) * ((double) var1)))));
    }

    private static float easeOutElastic(float arg0) {
        float var1 = 2.0943952f;
        if (arg0 == 0.0f) {
        } else {
            if (arg0 == 1.0f) {
            } else {
            }
        }
        return ((float) ((Math.pow(2.0, ((double) (-10.0f * arg0))) * Math.sin(((((double) (arg0 * 10.0f)) - 0.75) * ((double) var1)))) + 1.0));
    }

    private static float easeInOutElastic(float arg0) {
        float var1 = 1.3962635f;
        if (arg0 == 0.0f) {
        } else {
            if (arg0 == 1.0f) {
                break label_3;
            }
            if (((double) arg0) < 0.5) {
                break label_3;
            }
            label_3:
        }
        return ((float) (((Math.pow(2.0, ((double) ((-20.0f * arg0) + 10.0f))) * Math.sin(((((double) (20.0f * arg0)) - 11.125) * ((double) var1)))) / 2.0) + 1.0));
    }


    public enum Method {
        LINEAR,
        EASE_IN_SINE,
        EASE_OUT_SINE,
        EASE_IN_CUBIC,
        EASE_OUT_CUBIC,
        EASE_IN_QUAD,
        EASE_OUT_QUAD,
        EASE_IN_QUART,
        EASE_OUT_QUART,
        EASE_IN_OUT_SINE,
        EASE_IN_OUT_QUAD,
        EASE_IN_OUT_CUBIC,
        EASE_IN_OUT_QUART,
        EASE_IN_ELASTIC,
        EASE_OUT_ELASTIC,
        EASE_IN_OUT_ELASTIC;


        private Method() {
        }

    }
}
