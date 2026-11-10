package lightning.utils.text;

import lombok.Generated;
import net.minecraft.util.StringRepresentable;

public enum CustomFormatting implements StringRepresentable {
    CLIENT,
    RAINBOW;

    private final char code;

    private CustomFormatting(char arg2) {
        this.code = arg2;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public static CustomFormatting byCode(char arg0) {
        int var1 = Character.toLowerCase(arg0);
        Object var2 = values();
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            if (var5.code != var1) {
            } else {
                return var5;
            }
            var4++;
        }
        return null;
    }

    public String toString() {
        return ("\u00a7" + this.code);
    }

    public String getSerializedName() {
        return null;
    }

    @Generated
    public char getCode() {
        return this.code;
    }

}
