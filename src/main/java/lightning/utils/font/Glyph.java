package lightning.utils.font;

public record Glyph(GlyphMap parent, int u, int v, int width, int height, char value) {

    public Glyph(GlyphMap arg0, int arg1, int arg2, int arg3, int arg4, char arg5) {
        this.parent = arg0;
        this.u = arg1;
        this.v = arg2;
        this.width = arg3;
        this.height = arg4;
        this.value = arg5;
    }

    public GlyphMap parent() {
        return this.parent;
    }

    public int u() {
        return this.u;
    }

    public int v() {
        return this.v;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public char value() {
        return this.value;
    }

}
