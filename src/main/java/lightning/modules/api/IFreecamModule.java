package lightning.modules.api;

public interface IFreecamModule {
    boolean isToggled();

    boolean isRotate();

    void onMouseTurn(double arg0, double arg1);

    float getFreeYaw();

    float getFreePitch();

    double getFreeX();

    double getFreeY();

    double getFreeZ();

}
