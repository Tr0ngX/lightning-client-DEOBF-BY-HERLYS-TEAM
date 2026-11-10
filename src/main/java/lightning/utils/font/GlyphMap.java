package lightning.utils.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import lightning.mixins.accessors.AbstractTextureAccessor;
import lightning.mixins.accessors.NativeImageAccessor;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

public class GlyphMap {
    private static final AtomicInteger PAGE_SEQ;
    private DynamicTexture texture;
    private Identifier textureId;
    private final Font[] fonts;
    private int width;
    private int height;
    private final char include;
    private final char exclude;
    private final int padding;
    private final Char2ObjectArrayMap glyphs;
    boolean generated;

    public GlyphMap(Font[] arg0, char arg1, char arg2, int arg3) {
        this.glyphs = new Char2ObjectArrayMap();
        this.generated = false;
        this.fonts = arg0;
        this.include = arg1;
        this.exclude = arg2;
        this.padding = arg3;
    }

    public void generate() {
        Object var1 = this;
        Object var2 = var1;
        // synchronized (var1)
        privateGenerate();
        break label_4;
        Object var3 = null;
        throw var3;
        label_4:
    }

    private void privateGenerate() {
        if (!this.generated) {
            int var1 = ((this.exclude - this.include) - 1);
            int var2 = ((int) (Math.ceil(Math.sqrt(((double) var1))) * 1.5));
            int var3 = 0;
            int var4 = 0;
            int var5 = 0;
            int var6 = 0;
            int var7 = 0;
            int var8 = 0;
            int var9 = 0;
            Object var10 = new ArrayList();
            Object var11 = new AffineTransform();
            Object var12 = new FontRenderContext(var11, true, true);
            while (var3 <= var1) {
                int var13 = ((char) (this.include + var3));
                Object var14 = getFontForGlyph(var13);
                Object var15 = var14.getStringBounds(String.valueOf(var13), var12);
                int var16 = ((int) Math.ceil(var15.getWidth()));
                int var17 = ((int) Math.ceil(var15.getHeight()));
                var3++;
                var5 = Math.max(var5, (var7 + var16));
                var6 = Math.max(var6, (var8 + var17));
                if (var4 < var2) break label_1;
                var7 = 0;
                var8 = (var8 + (var9 + this.padding));
                var4 = 0;
                var9 = 0;
                label_1:
                var9 = Math.max(var9, var17);
                var10.add(new Glyph(this, var7, var8, var16, var17, var13));
                var7 = (var7 + (var16 + this.padding));
                var4++;
            }
            var13 = new BufferedImage(Math.max((var5 + this.padding), 1), Math.max((var6 + this.padding), 1), 2);
            this.width = var13.getWidth();
            this.height = var13.getHeight();
            var14 = var13.createGraphics();
            var14.setColor(new Color(255, 255, 255, 0));
            var14.fillRect(0, 0, this.width, this.height);
            var14.setColor(Color.WHITE);
            var14.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            var14.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            var14.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            var14.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            var14.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            var15 = var10.iterator();
            while (var15.hasNext()) {
                var16 = ((Glyph) var15.next());
                var14.setFont(getFontForGlyph(var16.value()));
                var17 = var14.getFontMetrics();
                var14.drawString(String.valueOf(var16.value()), var16.u(), (var16.v() + var17.getAscent()));
                this.glyphs.put(var16.value(), var16);
            }
            var15 = new NativeImage(NativeImage.Format.RGBA, var13.getWidth(), var13.getHeight(), false);
            var16 = MemoryUtil.memIntBuffer(((NativeImageAccessor) var15).getPointer(), (var15.getWidth() * var15.getHeight()));
            var17 = var13.getRaster();
            Object var18 = var13.getColorModel();
            int var19 = var17.getNumBands();
            int var20 = var17.getDataBuffer().getDataType();
            switch (var20) {
                case 0 -> { break label_5; }
                case 1 -> { break label_6; }
                case 2 -> { break label_7; }
                case 3 -> { break label_8; }
                case 4 -> { break label_9; }
                case 5 -> { break label_10; }
                default -> { break label_7; }
            }
            label_5:
            break label_11;
            label_6:
            break label_11;
            label_7:
            throw new IllegalArgumentException(("Unknown data buffer type: " + var20));
            label_8:
            break label_11;
            label_9:
            break label_11;
            label_10:
            label_11:
            Object var21 = new double[var19];
            int var22 = 0;
            while (var22 < var13.getHeight()) {
                int var23 = 0;
                while (var23 < var13.getWidth()) {
                    var17.getDataElements(var23, var22, var21);
                    var16.put(((((var18.getAlpha(var21) << 24) | (var18.getBlue(var21) << 16)) | (var18.getGreen(var21) << 8)) | var18.getRed(var21)));
                    var23++;
                }
                var22++;
            }
            var22 = PAGE_SEQ.getAndIncrement();
            var23 = new DynamicTexture(() -> ("lightning/glyph_page_" + arg0), var15);
            this.texture = var23;
            this.textureId = Identifier.fromNamespaceAndPath("lightning", ("glyph_page_" + var22));
            Minecraft.getInstance().getTextureManager().register(this.textureId, var23);
            ((AbstractTextureAccessor) var23).setSampler(RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.empty()));
            this.generated = true;
        }
    }

    public Glyph getGlyph(char arg0) {
        Object var2 = this;
        Object var3 = var2;
        // synchronized (var2)
        if (!this.generated) {
            privateGenerate();
        }
        return ((Glyph) this.glyphs.get(arg0));
        Object var4 = null;
        throw var4;
    }

    public void destroy() {
        Object var1 = this;
        Object var2 = var1;
        // synchronized (var1)
        this.generated = false;
        if (this.textureId != null) {
            Minecraft.getInstance().getTextureManager().release(this.textureId);
            this.textureId = null;
        }
        if (this.texture != null) {
            this.texture.close();
        }
        this.glyphs.clear();
        this.width = -1;
        this.height = -1;
        break label_6;
        Object var3 = null;
        throw var3;
        label_6:
    }

    public boolean contains(char arg0) {
        if (arg0 < this.include) break label_0;
        return arg0 < this.exclude;
    }

    private Font getFontForGlyph(char arg0) {
        Object var2 = this.fonts;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            if (!(var5.canDisplay(arg0))) {
            } else {
                return var5;
            }
            var4++;
        }
        return this.fonts[0];
    }

    @Generated
    public DynamicTexture getTexture() {
        return this.texture;
    }

    @Generated
    public Identifier getTextureId() {
        return this.textureId;
    }

    @Generated
    public Font[] getFonts() {
        return this.fonts;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public char getInclude() {
        return this.include;
    }

    @Generated
    public char getExclude() {
        return this.exclude;
    }

    @Generated
    public int getPadding() {
        return this.padding;
    }

    @Generated
    public Char2ObjectArrayMap getGlyphs() {
        return this.glyphs;
    }

    @Generated
    public boolean isGenerated() {
        return this.generated;
    }

    static {
        PAGE_SEQ = new AtomicInteger();
    }

}
