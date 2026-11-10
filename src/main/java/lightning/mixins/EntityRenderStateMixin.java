package lightning.mixins;

import lightning.utils.graphics.Glint;
import lightning.utils.mixins.IChamsCapture;
import lightning.utils.mixins.ISelfState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements IChamsCapture, ISelfState {
    @Unique
    private boolean chamsFill;
    @Unique
    private int chamsFillColor;
    @Unique
    private boolean chamsOutline;
    @Unique
    private int chamsOutlineColor;
    @Unique
    private boolean chamsShine;
    @Unique
    private boolean chamsGlint;
    @Unique
    private int chamsGlintColor;
    @Unique
    private Glint.GlintParams chamsGlintParams;
    @Unique
    private boolean chamsSuppressReal;
    @Unique
    private float chamsRealAlpha;
    @Unique
    private float chamsYOffset;
    @Unique
    private boolean isSelf;

    public EntityRenderStateMixin() {
        this.chamsFill = false;
        this.chamsFillColor = 0;
        this.chamsOutline = false;
        this.chamsOutlineColor = 0;
        this.chamsShine = false;
        this.chamsGlint = false;
        this.chamsGlintColor = -1;
        this.chamsGlintParams = Glint.ENTITY;
        this.chamsSuppressReal = false;
        this.chamsRealAlpha = 1.0f;
        this.chamsYOffset = 0.0f;
        this.isSelf = false;
    }

    public float night$chamsYOffset() {
        return this.chamsYOffset;
    }

    public void night$setChamsYOffset(float arg0) {
        this.chamsYOffset = arg0;
    }

    public boolean night$isSelf() {
        return this.isSelf;
    }

    public void night$setSelf(boolean arg0) {
        this.isSelf = arg0;
    }

    public boolean night$chamsFill() {
        return this.chamsFill;
    }

    public int night$chamsFillColor() {
        return this.chamsFillColor;
    }

    public boolean night$chamsOutline() {
        return this.chamsOutline;
    }

    public int night$chamsOutlineColor() {
        return this.chamsOutlineColor;
    }

    public boolean night$chamsShine() {
        return this.chamsShine;
    }

    public boolean night$chamsGlint() {
        return this.chamsGlint;
    }

    public int night$chamsGlintColor() {
        return this.chamsGlintColor;
    }

    public Glint.GlintParams night$chamsGlintParams() {
        return this.chamsGlintParams;
    }

    public boolean night$chamsSuppressReal() {
        return this.chamsSuppressReal;
    }

    public float night$chamsRealAlpha() {
        return this.chamsRealAlpha;
    }

    public void night$setChamsRealAlpha(float arg0) {
        this.chamsRealAlpha = arg0;
    }

    public void night$setChamsGlint(boolean arg0, int arg1) {
        this.chamsGlint = arg0;
        this.chamsGlintColor = arg1;
        this.chamsGlintParams = Glint.ENTITY;
    }

    public void night$setChamsGlint(boolean arg0, int arg1, Glint.GlintParams arg2) {
        this.chamsGlint = arg0;
        this.chamsGlintColor = arg1;
        this.chamsGlintParams = (arg2 != null ? arg2 : Glint.ENTITY);
    }

    public void night$setChams(boolean arg0, int arg1, boolean arg2, int arg3, boolean arg4) {
        night$setChams(arg0, arg1, arg2, arg3, arg4, false);
    }

    public void night$setChams(boolean arg0, int arg1, boolean arg2, int arg3, boolean arg4, boolean arg5) {
        this.chamsFill = arg0;
        this.chamsFillColor = arg1;
        this.chamsOutline = arg2;
        this.chamsOutlineColor = arg3;
        this.chamsShine = arg4;
        this.chamsSuppressReal = arg5;
        this.chamsRealAlpha = 1.0f;
        this.chamsGlint = false;
        this.chamsGlintColor = -1;
        this.chamsGlintParams = Glint.ENTITY;
    }

}
