package lightning.utils.mixins;

import lightning.utils.graphics.Glint;

public interface IChamsCapture {
    boolean night$chamsFill();

    int night$chamsFillColor();

    boolean night$chamsOutline();

    int night$chamsOutlineColor();

    boolean night$chamsShine();

    boolean night$chamsGlint();

    int night$chamsGlintColor();

    Glint.GlintParams night$chamsGlintParams();

    boolean night$chamsSuppressReal();

    float night$chamsRealAlpha();

    void night$setChamsRealAlpha(float arg0);

    float night$chamsYOffset();

    void night$setChamsYOffset(float arg0);

    void night$setChams(boolean arg0, int arg1, boolean arg2, int arg3, boolean arg4);

    void night$setChams(boolean arg0, int arg1, boolean arg2, int arg3, boolean arg4, boolean arg5);

    void night$setChamsGlint(boolean arg0, int arg1);

    void night$setChamsGlint(boolean arg0, int arg1, Glint.GlintParams arg2);

}
