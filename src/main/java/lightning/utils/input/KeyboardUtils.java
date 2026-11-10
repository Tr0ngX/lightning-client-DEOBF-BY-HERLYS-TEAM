package lightning.utils.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

public class KeyboardUtils {
    public static String getKeyName(int arg0) {
        switch (arg0) {
            case -5 -> { break label_62; }
            case -4 -> { break label_61; }
            case -3 -> { break label_60; }
            case -2 -> { break label_59; }
            case -1 -> { break label_0; }
            case 0 -> { break label_63; }
            case 32 -> { break label_33; }
            case 39 -> { break label_24; }
            case 96 -> { break label_2; }
            case 161 -> { break label_3; }
            case 162 -> { break label_4; }
            case 256 -> { break label_1; }
            case 257 -> { break label_30; }
            case 258 -> { break label_13; }
            case 259 -> { break label_25; }
            case 260 -> { break label_7; }
            case 261 -> { break label_8; }
            case 262 -> { break label_23; }
            case 263 -> { break label_22; }
            case 264 -> { break label_21; }
            case 265 -> { break label_20; }
            case 266 -> { break label_10; }
            case 267 -> { break label_11; }
            case 268 -> { break label_9; }
            case 269 -> { break label_12; }
            case 280 -> { break label_26; }
            case 282 -> { break label_32; }
            case 283 -> { break label_5; }
            case 284 -> { break label_6; }
            case 290 -> { break label_34; }
            case 291 -> { break label_35; }
            case 292 -> { break label_36; }
            case 293 -> { break label_37; }
            case 294 -> { break label_38; }
            case 295 -> { break label_39; }
            case 296 -> { break label_40; }
            case 297 -> { break label_41; }
            case 298 -> { break label_42; }
            case 299 -> { break label_43; }
            case 300 -> { break label_44; }
            case 301 -> { break label_45; }
            case 302 -> { break label_46; }
            case 303 -> { break label_47; }
            case 304 -> { break label_48; }
            case 305 -> { break label_49; }
            case 306 -> { break label_50; }
            case 307 -> { break label_51; }
            case 308 -> { break label_52; }
            case 309 -> { break label_53; }
            case 310 -> { break label_54; }
            case 311 -> { break label_55; }
            case 312 -> { break label_56; }
            case 313 -> { break label_57; }
            case 314 -> { break label_58; }
            case 335 -> { break label_31; }
            case 340 -> { break label_18; }
            case 341 -> { break label_14; }
            case 342 -> { break label_16; }
            case 343 -> { break label_28; }
            case 344 -> { break label_19; }
            case 345 -> { break label_15; }
            case 346 -> { break label_17; }
            case 347 -> { break label_29; }
            case 348 -> { break label_27; }
            default -> { break label_64; }
        }
        label_0:
        break label_66;
        label_1:
        break label_66;
        label_2:
        break label_66;
        label_3:
        break label_66;
        label_4:
        break label_66;
        label_5:
        break label_66;
        label_6:
        break label_66;
        label_7:
        break label_66;
        label_8:
        break label_66;
        label_9:
        break label_66;
        label_10:
        break label_66;
        label_11:
        break label_66;
        label_12:
        break label_66;
        label_13:
        break label_66;
        label_14:
        break label_66;
        label_15:
        break label_66;
        label_16:
        break label_66;
        label_17:
        break label_66;
        label_18:
        break label_66;
        label_19:
        break label_66;
        label_20:
        break label_66;
        label_21:
        break label_66;
        label_22:
        break label_66;
        label_23:
        break label_66;
        label_24:
        break label_66;
        label_25:
        break label_66;
        label_26:
        break label_66;
        label_27:
        break label_66;
        label_28:
        break label_66;
        label_29:
        break label_66;
        label_30:
        break label_66;
        label_31:
        break label_66;
        label_32:
        break label_66;
        label_33:
        break label_66;
        label_34:
        break label_66;
        label_35:
        break label_66;
        label_36:
        break label_66;
        label_37:
        break label_66;
        label_38:
        break label_66;
        label_39:
        break label_66;
        label_40:
        break label_66;
        label_41:
        break label_66;
        label_42:
        break label_66;
        label_43:
        break label_66;
        label_44:
        break label_66;
        label_45:
        break label_66;
        label_46:
        break label_66;
        label_47:
        break label_66;
        label_48:
        break label_66;
        label_49:
        break label_66;
        label_50:
        break label_66;
        label_51:
        break label_66;
        label_52:
        break label_66;
        label_53:
        break label_66;
        label_54:
        break label_66;
        label_55:
        break label_66;
        label_56:
        break label_66;
        label_57:
        break label_66;
        label_58:
        break label_66;
        label_59:
        break label_66;
        label_60:
        break label_66;
        label_61:
        break label_66;
        label_62:
        break label_66;
        label_63:
        break label_66;
        label_64:
        Object var1 = GLFW.glfwGetKeyName(arg0, 0);
        if (var1 == null) {
            break label_66;
        }
        break label_66;
        label_66:
        return StringUtils.capitalize(var1);
    }

    public static int getKeyNumber(String arg0) {
        Object var1 = arg0.toLowerCase();
        int var2 = -1;
        switch (var1.hashCode()) {
            case -2000208463 -> { break label_31; }
            case -1883398382 -> { break label_30; }
            case -1335458389 -> { break label_7; }
            case -1183792455 -> { break label_6; }
            case -1093611626 -> { break label_17; }
            case -1093217649 -> { break label_27; }
            case -1079639749 -> { break label_59; }
            case -990290151 -> { break label_10; }
            case -979651536 -> { break label_4; }
            case -936493994 -> { break label_58; }
            case -921836720 -> { break label_18; }
            case -921442743 -> { break label_28; }
            case -782084385 -> { break label_2; }
            case -782084384 -> { break label_3; }
            case -50927732 -> { break label_25; }
            case 3211 -> { break label_33; }
            case 3212 -> { break label_34; }
            case 3213 -> { break label_35; }
            case 3214 -> { break label_36; }
            case 3215 -> { break label_37; }
            case 3216 -> { break label_38; }
            case 3217 -> { break label_39; }
            case 3218 -> { break label_40; }
            case 3219 -> { break label_41; }
            case 3739 -> { break label_19; }
            case 99589 -> { break label_42; }
            case 99590 -> { break label_43; }
            case 99591 -> { break label_44; }
            case 99592 -> { break label_45; }
            case 99593 -> { break label_46; }
            case 99594 -> { break label_47; }
            case 99595 -> { break label_48; }
            case 99596 -> { break label_49; }
            case 99597 -> { break label_50; }
            case 99598 -> { break label_51; }
            case 99620 -> { break label_52; }
            case 99621 -> { break label_53; }
            case 99622 -> { break label_54; }
            case 99623 -> { break label_55; }
            case 99624 -> { break label_56; }
            case 99625 -> { break label_57; }
            case 100571 -> { break label_11; }
            case 100725 -> { break label_0; }
            case 114581 -> { break label_12; }
            case 3089570 -> { break label_20; }
            case 3208415 -> { break label_8; }
            case 3314109 -> { break label_15; }
            case 3317767 -> { break label_21; }
            case 3347807 -> { break label_26; }
            case 3387192 -> { break label_62; }
            case 3439314 -> { break label_9; }
            case 3492855 -> { break label_16; }
            case 96667352 -> { break label_29; }
            case 98615813 -> { break label_1; }
            case 102804695 -> { break label_13; }
            case 106440182 -> { break label_5; }
            case 108345821 -> { break label_14; }
            case 108511772 -> { break label_22; }
            case 109637894 -> { break label_32; }
            case 138429999 -> { break label_23; }
            case 241352513 -> { break label_60; }
            case 241352514 -> { break label_61; }
            case 1353507967 -> { break label_24; }
            default -> { break label_63; }
        }
        label_0:
        if (!(var1.equals("esc"))) break label_63;
        var2 = 0;
        break label_63;
        label_1:
        if (!(var1.equals("grave"))) break label_63;
        var2 = 1;
        break label_63;
        label_2:
        if (!(var1.equals("world1"))) break label_63;
        var2 = 2;
        break label_63;
        label_3:
        if (!(var1.equals("world2"))) break label_63;
        var2 = 3;
        break label_63;
        label_4:
        if (!(var1.equals("prtscr"))) break label_63;
        var2 = 4;
        break label_63;
        label_5:
        if (!(var1.equals("pause"))) break label_63;
        var2 = 5;
        break label_63;
        label_6:
        if (!(var1.equals("insert"))) break label_63;
        var2 = 6;
        break label_63;
        label_7:
        if (!(var1.equals("delete"))) break label_63;
        var2 = 7;
        break label_63;
        label_8:
        if (!(var1.equals("home"))) break label_63;
        var2 = 8;
        break label_63;
        label_9:
        if (!(var1.equals("pgup"))) break label_63;
        var2 = 9;
        break label_63;
        label_10:
        if (!(var1.equals("pgdown"))) break label_63;
        var2 = 10;
        break label_63;
        label_11:
        if (!(var1.equals("end"))) break label_63;
        var2 = 11;
        break label_63;
        label_12:
        if (!(var1.equals("tab"))) break label_63;
        var2 = 12;
        break label_63;
        label_13:
        if (!(var1.equals("lctrl"))) break label_63;
        var2 = 13;
        break label_63;
        label_14:
        if (!(var1.equals("rctrl"))) break label_63;
        var2 = 14;
        break label_63;
        label_15:
        if (!(var1.equals("lalt"))) break label_63;
        var2 = 15;
        break label_63;
        label_16:
        if (!(var1.equals("ralt"))) break label_63;
        var2 = 16;
        break label_63;
        label_17:
        if (!(var1.equals("lshift"))) break label_63;
        var2 = 17;
        break label_63;
        label_18:
        if (!(var1.equals("rshift"))) break label_63;
        var2 = 18;
        break label_63;
        label_19:
        if (!(var1.equals("up"))) break label_63;
        var2 = 19;
        break label_63;
        label_20:
        if (!(var1.equals("down"))) break label_63;
        var2 = 20;
        break label_63;
        label_21:
        if (!(var1.equals("left"))) break label_63;
        var2 = 21;
        break label_63;
        label_22:
        if (!(var1.equals("right"))) break label_63;
        var2 = 22;
        break label_63;
        label_23:
        if (!(var1.equals("apostrophe"))) break label_63;
        var2 = 23;
        break label_63;
        label_24:
        if (!(var1.equals("backspace"))) break label_63;
        var2 = 24;
        break label_63;
        label_25:
        if (!(var1.equals("capslock"))) break label_63;
        var2 = 25;
        break label_63;
        label_26:
        if (!(var1.equals("menu"))) break label_63;
        var2 = 26;
        break label_63;
        label_27:
        if (!(var1.equals("lsuper"))) break label_63;
        var2 = 27;
        break label_63;
        label_28:
        if (!(var1.equals("rsuper"))) break label_63;
        var2 = 28;
        break label_63;
        label_29:
        if (!(var1.equals("enter"))) break label_63;
        var2 = 29;
        break label_63;
        label_30:
        if (!(var1.equals("numenter"))) break label_63;
        var2 = 30;
        break label_63;
        label_31:
        if (!(var1.equals("numlock"))) break label_63;
        var2 = 31;
        break label_63;
        label_32:
        if (!(var1.equals("space"))) break label_63;
        var2 = 32;
        break label_63;
        label_33:
        if (!(var1.equals("f1"))) break label_63;
        var2 = 33;
        break label_63;
        label_34:
        if (!(var1.equals("f2"))) break label_63;
        var2 = 34;
        break label_63;
        label_35:
        if (!(var1.equals("f3"))) break label_63;
        var2 = 35;
        break label_63;
        label_36:
        if (!(var1.equals("f4"))) break label_63;
        var2 = 36;
        break label_63;
        label_37:
        if (!(var1.equals("f5"))) break label_63;
        var2 = 37;
        break label_63;
        label_38:
        if (!(var1.equals("f6"))) break label_63;
        var2 = 38;
        break label_63;
        label_39:
        if (!(var1.equals("f7"))) break label_63;
        var2 = 39;
        break label_63;
        label_40:
        if (!(var1.equals("f8"))) break label_63;
        var2 = 40;
        break label_63;
        label_41:
        if (!(var1.equals("f9"))) break label_63;
        var2 = 41;
        break label_63;
        label_42:
        if (!(var1.equals("f10"))) break label_63;
        var2 = 42;
        break label_63;
        label_43:
        if (!(var1.equals("f11"))) break label_63;
        var2 = 43;
        break label_63;
        label_44:
        if (!(var1.equals("f12"))) break label_63;
        var2 = 44;
        break label_63;
        label_45:
        if (!(var1.equals("f13"))) break label_63;
        var2 = 45;
        break label_63;
        label_46:
        if (!(var1.equals("f14"))) break label_63;
        var2 = 46;
        break label_63;
        label_47:
        if (!(var1.equals("f15"))) break label_63;
        var2 = 47;
        break label_63;
        label_48:
        if (!(var1.equals("f16"))) break label_63;
        var2 = 48;
        break label_63;
        label_49:
        if (!(var1.equals("f17"))) break label_63;
        var2 = 49;
        break label_63;
        label_50:
        if (!(var1.equals("f18"))) break label_63;
        var2 = 50;
        break label_63;
        label_51:
        if (!(var1.equals("f19"))) break label_63;
        var2 = 51;
        break label_63;
        label_52:
        if (!(var1.equals("f20"))) break label_63;
        var2 = 52;
        break label_63;
        label_53:
        if (!(var1.equals("f21"))) break label_63;
        var2 = 53;
        break label_63;
        label_54:
        if (!(var1.equals("f22"))) break label_63;
        var2 = 54;
        break label_63;
        label_55:
        if (!(var1.equals("f23"))) break label_63;
        var2 = 55;
        break label_63;
        label_56:
        if (!(var1.equals("f24"))) break label_63;
        var2 = 56;
        break label_63;
        label_57:
        if (!(var1.equals("f25"))) break label_63;
        var2 = 57;
        break label_63;
        label_58:
        if (!(var1.equals("rclick"))) break label_63;
        var2 = 58;
        break label_63;
        label_59:
        if (!(var1.equals("mclick"))) break label_63;
        var2 = 59;
        break label_63;
        label_60:
        if (!(var1.equals("button3"))) break label_63;
        var2 = 60;
        break label_63;
        label_61:
        if (!(var1.equals("button4"))) break label_63;
        var2 = 61;
        break label_63;
        label_62:
        if (!(var1.equals("none"))) break label_63;
        var2 = 62;
        label_63:
        switch (var2) {
            case 0 -> { break label_64; }
            case 1 -> { break label_65; }
            case 2 -> { break label_66; }
            case 3 -> { break label_67; }
            case 4 -> { break label_68; }
            case 5 -> { break label_69; }
            case 6 -> { break label_70; }
            case 7 -> { break label_71; }
            case 8 -> { break label_72; }
            case 9 -> { break label_73; }
            case 10 -> { break label_74; }
            case 11 -> { break label_75; }
            case 12 -> { break label_76; }
            case 13 -> { break label_77; }
            case 14 -> { break label_78; }
            case 15 -> { break label_79; }
            case 16 -> { break label_80; }
            case 17 -> { break label_81; }
            case 18 -> { break label_82; }
            case 19 -> { break label_83; }
            case 20 -> { break label_84; }
            case 21 -> { break label_85; }
            case 22 -> { break label_86; }
            case 23 -> { break label_87; }
            case 24 -> { break label_88; }
            case 25 -> { break label_89; }
            case 26 -> { break label_90; }
            case 27 -> { break label_91; }
            case 28 -> { break label_92; }
            case 29 -> { break label_93; }
            case 30 -> { break label_94; }
            case 31 -> { break label_95; }
            case 32 -> { break label_96; }
            case 33 -> { break label_97; }
            case 34 -> { break label_98; }
            case 35 -> { break label_99; }
            case 36 -> { break label_100; }
            case 37 -> { break label_101; }
            case 38 -> { break label_102; }
            case 39 -> { break label_103; }
            case 40 -> { break label_104; }
            case 41 -> { break label_105; }
            case 42 -> { break label_106; }
            case 43 -> { break label_107; }
            case 44 -> { break label_108; }
            case 45 -> { break label_109; }
            case 46 -> { break label_110; }
            case 47 -> { break label_111; }
            case 48 -> { break label_112; }
            case 49 -> { break label_113; }
            case 50 -> { break label_114; }
            case 51 -> { break label_115; }
            case 52 -> { break label_116; }
            case 53 -> { break label_117; }
            case 54 -> { break label_118; }
            case 55 -> { break label_119; }
            case 56 -> { break label_120; }
            case 57 -> { break label_121; }
            case 58 -> { break label_122; }
            case 59 -> { break label_123; }
            case 60 -> { break label_124; }
            case 61 -> { break label_125; }
            case 62 -> { break label_126; }
            default -> { break label_127; }
        }
        label_64:
        int var4 = 256;
        int var3 = 256;
        break label_130;
        label_65:
        var4 = 96;
        var3 = 96;
        break label_130;
        label_66:
        var4 = 161;
        var3 = 161;
        break label_130;
        label_67:
        var4 = 162;
        var3 = 162;
        break label_130;
        label_68:
        var4 = 283;
        var3 = 283;
        break label_130;
        label_69:
        var4 = 284;
        var3 = 284;
        break label_130;
        label_70:
        var4 = 260;
        var3 = 260;
        break label_130;
        label_71:
        var4 = 261;
        var3 = 261;
        break label_130;
        label_72:
        var4 = 268;
        var3 = 268;
        break label_130;
        label_73:
        var4 = 266;
        var3 = 266;
        break label_130;
        label_74:
        var4 = 267;
        var3 = 267;
        break label_130;
        label_75:
        var4 = 269;
        var3 = 269;
        break label_130;
        label_76:
        var4 = 258;
        var3 = 258;
        break label_130;
        label_77:
        var4 = 341;
        var3 = 341;
        break label_130;
        label_78:
        var4 = 345;
        var3 = 345;
        break label_130;
        label_79:
        var4 = 342;
        var3 = 342;
        break label_130;
        label_80:
        var4 = 346;
        var3 = 346;
        break label_130;
        label_81:
        var4 = 340;
        var3 = 340;
        break label_130;
        label_82:
        var4 = 344;
        var3 = 344;
        break label_130;
        label_83:
        var4 = 265;
        var3 = 265;
        break label_130;
        label_84:
        var4 = 264;
        var3 = 264;
        break label_130;
        label_85:
        var4 = 263;
        var3 = 263;
        break label_130;
        label_86:
        var4 = 262;
        var3 = 262;
        break label_130;
        label_87:
        var4 = 39;
        var3 = 39;
        break label_130;
        label_88:
        var4 = 259;
        var3 = 259;
        break label_130;
        label_89:
        var4 = 280;
        var3 = 280;
        break label_130;
        label_90:
        var4 = 348;
        var3 = 348;
        break label_130;
        label_91:
        var4 = 343;
        var3 = 343;
        break label_130;
        label_92:
        var4 = 347;
        var3 = 347;
        break label_130;
        label_93:
        var4 = 257;
        var3 = 257;
        break label_130;
        label_94:
        var4 = 335;
        var3 = 335;
        break label_130;
        label_95:
        var4 = 282;
        var3 = 282;
        break label_130;
        label_96:
        var4 = 32;
        var3 = 32;
        break label_130;
        label_97:
        var4 = 290;
        var3 = 290;
        break label_130;
        label_98:
        var4 = 291;
        var3 = 291;
        break label_130;
        label_99:
        var4 = 292;
        var3 = 292;
        break label_130;
        label_100:
        var4 = 293;
        var3 = 293;
        break label_130;
        label_101:
        var4 = 294;
        var3 = 294;
        break label_130;
        label_102:
        var4 = 295;
        var3 = 295;
        break label_130;
        label_103:
        var4 = 296;
        var3 = 296;
        break label_130;
        label_104:
        var4 = 297;
        var3 = 297;
        break label_130;
        label_105:
        var4 = 298;
        var3 = 298;
        break label_130;
        label_106:
        var4 = 299;
        var3 = 299;
        break label_130;
        label_107:
        var4 = 300;
        var3 = 300;
        break label_130;
        label_108:
        var4 = 301;
        var3 = 301;
        break label_130;
        label_109:
        var4 = 302;
        var3 = 302;
        break label_130;
        label_110:
        var4 = 303;
        var3 = 303;
        break label_130;
        label_111:
        var4 = 304;
        var3 = 304;
        break label_130;
        label_112:
        var4 = 305;
        var3 = 305;
        break label_130;
        label_113:
        var4 = 306;
        var3 = 306;
        break label_130;
        label_114:
        var4 = 307;
        var3 = 307;
        break label_130;
        label_115:
        var4 = 308;
        var3 = 308;
        break label_130;
        label_116:
        var4 = 309;
        var3 = 309;
        break label_130;
        label_117:
        var4 = 310;
        var3 = 310;
        break label_130;
        label_118:
        var4 = 311;
        var3 = 311;
        break label_130;
        label_119:
        var4 = 312;
        var3 = 312;
        break label_130;
        label_120:
        var4 = 313;
        var3 = 313;
        break label_130;
        label_121:
        var4 = 314;
        var3 = 314;
        break label_130;
        label_122:
        var tmp0 = -2;
        var4 = tmp0;
        var3 = tmp0;
        break label_130;
        label_123:
        var tmp1 = -3;
        var4 = tmp1;
        var3 = tmp1;
        break label_130;
        label_124:
        var tmp2 = -4;
        var4 = tmp2;
        var3 = tmp2;
        break label_130;
        label_125:
        var tmp3 = -5;
        var4 = tmp3;
        var3 = tmp3;
        break label_130;
        label_126:
        var4 = 0;
        var3 = 0;
        break label_130;
        try {
            label_127:
            var tmp4 = InputConstants.getKey(("key.keyboard." + arg0)).getValue();
            var4 = tmp4;
            var3 = tmp4;
        } catch (NumberFormatException ex129) {
            var4 = ex129;
            int var5 = 0;
            var3 = 0;
            break label_130;
        }
        label_130:
        return var3;
    }

    public static boolean isBindDown(int arg0) {
        if (arg0 == 0) return false;
        Object var1 = Minecraft.getInstance();
        if (var1.gui != null && var1.gui.screen() != null) return false;
        if (var1.getWindow() == null) return false;
        if (arg0 >= 0) return InputConstants.isKeyDown(var1.getWindow(), arg0);
        int var2 = (-(arg0) - 1);
        return GLFW.glfwGetMouseButton(var1.getWindow().handle(), var2) == 1;
    }

}
