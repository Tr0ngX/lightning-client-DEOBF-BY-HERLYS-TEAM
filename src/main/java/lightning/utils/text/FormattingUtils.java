package lightning.utils.text;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.mixins.accessors.StyleAccessor;
import lightning.mixins.accessors.TextColorAccessor;
import lightning.utils.color.ColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public class FormattingUtils {
    public static String[] FORMATS;

    public static Style withExclusiveFormatting(Style arg0, CustomFormatting arg1) {
        Object var2 = arg0.getColor();
        if (arg1 == CustomFormatting.CLIENT) {
            var2 = TextColorAccessor.create(ColorUtils.getGlobalColor().getRGB(), "CLIENT");
            break label_1;
        }
        if (arg1 != CustomFormatting.RAINBOW) break label_1;
        var2 = TextColorAccessor.create(ColorUtils.getGlobalColor().getRGB(), "RAINBOW");
        label_1:
        return StyleAccessor.create(var2, null, false, false, false, false, false, arg0.getClickEvent(), arg0.getHoverEvent(), arg0.getInsertion(), arg0.getFont());
    }

    public static Object getFormatting(String arg0) {
        Object var1 = arg0.toLowerCase();
        int var2 = -1;
        switch (var1.hashCode()) {
            case -1924313178 -> { break label_10; }
            case -1910830810 -> { break label_6; }
            case -1910805820 -> { break label_2; }
            case -1910651699 -> { break label_14; }
            case -1357712437 -> { break label_15; }
            case -734239628 -> { break label_11; }
            case 112785 -> { break label_7; }
            case 3002044 -> { break label_5; }
            case 3027034 -> { break label_1; }
            case 3178592 -> { break label_12; }
            case 3181155 -> { break label_13; }
            case 93818879 -> { break label_0; }
            case 98619139 -> { break label_3; }
            case 899342809 -> { break label_4; }
            case 973576630 -> { break label_16; }
            case 1252881926 -> { break label_9; }
            case 1739491559 -> { break label_8; }
            default -> { break label_17; }
        }
        label_0:
        if (!(var1.equals("black"))) break label_17;
        var2 = 0;
        break label_17;
        label_1:
        if (!(var1.equals("blue"))) break label_17;
        var2 = 1;
        break label_17;
        label_2:
        if (!(var1.equals("dark blue"))) break label_17;
        var2 = 2;
        break label_17;
        label_3:
        if (!(var1.equals("green"))) break label_17;
        var2 = 3;
        break label_17;
        label_4:
        if (!(var1.equals("dark green"))) break label_17;
        var2 = 4;
        break label_17;
        label_5:
        if (!(var1.equals("aqua"))) break label_17;
        var2 = 5;
        break label_17;
        label_6:
        if (!(var1.equals("dark aqua"))) break label_17;
        var2 = 6;
        break label_17;
        label_7:
        if (!(var1.equals("red"))) break label_17;
        var2 = 7;
        break label_17;
        label_8:
        if (!(var1.equals("dark red"))) break label_17;
        var2 = 8;
        break label_17;
        label_9:
        if (!(var1.equals("light purple"))) break label_17;
        var2 = 9;
        break label_17;
        label_10:
        if (!(var1.equals("dark purple"))) break label_17;
        var2 = 10;
        break label_17;
        label_11:
        if (!(var1.equals("yellow"))) break label_17;
        var2 = 11;
        break label_17;
        label_12:
        if (!(var1.equals("gold"))) break label_17;
        var2 = 12;
        break label_17;
        label_13:
        if (!(var1.equals("gray"))) break label_17;
        var2 = 13;
        break label_17;
        label_14:
        if (!(var1.equals("dark gray"))) break label_17;
        var2 = 14;
        break label_17;
        label_15:
        if (!(var1.equals("client"))) break label_17;
        var2 = 15;
        break label_17;
        label_16:
        if (!(var1.equals("rainbow"))) break label_17;
        var2 = 16;
        label_17:
        switch (var2) {
            case 0 -> { break label_18; }
            case 1 -> { break label_19; }
            case 2 -> { break label_20; }
            case 3 -> { break label_21; }
            case 4 -> { break label_22; }
            case 5 -> { break label_23; }
            case 6 -> { break label_24; }
            case 7 -> { break label_25; }
            case 8 -> { break label_26; }
            case 9 -> { break label_27; }
            case 10 -> { break label_28; }
            case 11 -> { break label_29; }
            case 12 -> { break label_30; }
            case 13 -> { break label_31; }
            case 14 -> { break label_32; }
            case 15 -> { break label_33; }
            case 16 -> { break label_34; }
            default -> { break label_35; }
        }
        label_18:
        break label_36;
        label_19:
        break label_36;
        label_20:
        break label_36;
        label_21:
        break label_36;
        label_22:
        break label_36;
        label_23:
        break label_36;
        label_24:
        break label_36;
        label_25:
        break label_36;
        label_26:
        break label_36;
        label_27:
        break label_36;
        label_28:
        break label_36;
        label_29:
        break label_36;
        label_30:
        break label_36;
        label_31:
        break label_36;
        label_32:
        break label_36;
        label_33:
        break label_36;
        label_34:
        break label_36;
        label_35:
        label_36:
        return ChatFormatting.WHITE;
    }

    public static List wrapText(String arg0, int arg1) {
        Object var2 = new ArrayList();
        Object var3 = arg0.split(" ");
        Object var4 = "";
        Object var5 = var3;
        int var6 = var5.length;
        int var7 = 0;
        while (var7 < var6) {
            Object var8 = var5[var7];
            if ((LightningClient.FONT_MANAGER.getWidth(((String) var4)) + LightningClient.FONT_MANAGER.getWidth(var8)) <= arg1) {
                var4 = (((String) var4) + var8 + " ");
            } else {
                var2.add(((String) var4));
                var4 = (var8 + " ");
            }
            var7++;
        }
        if (LightningClient.FONT_MANAGER.getWidth(((String) var4)) > 0) {
            var2.add(((String) var4));
        }
        return var2;
    }

    public static String[] formatSeconds(long arg0) {
        var tmp0 = new Object[1];
        tmp0[0] = (arg0 / 3600L);
        Object var2 = String.format("%02d", tmp0);
        var tmp1 = new Object[1];
        tmp1[0] = ((arg0 % 3600L) / 60L);
        Object var3 = String.format("%02d", tmp1);
        var tmp2 = new Object[1];
        tmp2[0] = (arg0 % 60L);
        Object var4 = String.format("%02d", tmp2);
        var tmp3 = new String[3];
        tmp3[0] = var2;
        tmp3[1] = var3;
        tmp3[2] = var4;
        return tmp3;
    }

    static {
        var tmp0 = new String[18];
        tmp0[0] = "White";
        tmp0[1] = "Black";
        tmp0[2] = "Blue";
        tmp0[3] = "Dark Blue";
        tmp0[4] = "Green";
        tmp0[5] = "Dark Green";
        tmp0[6] = "Aqua";
        tmp0[7] = "Dark Aqua";
        tmp0[8] = "Red";
        tmp0[9] = "Dark Red";
        tmp0[10] = "Light Purple";
        tmp0[11] = "Dark Purple";
        tmp0[12] = "Yellow";
        tmp0[13] = "Gold";
        tmp0[14] = "Gray";
        tmp0[15] = "Dark Gray";
        tmp0[16] = "Client";
        tmp0[17] = "Rainbow";
        FORMATS = tmp0;
    }

}
