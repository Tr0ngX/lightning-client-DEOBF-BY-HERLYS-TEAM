package lightning.modules.api;

public interface IMotionCameraModule {
    boolean isToggled();

    boolean isSmoothPerspective();

    boolean on();

    boolean shouldBeDetached();

    double getDistance();

    double getFakeX();

    double getFakeY();

    double getFakeZ();

}
