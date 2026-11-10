package lightning.modules.api;

public interface ISprintModule {
    boolean isToggled();

    boolean shouldSprint();

    boolean isGrimCompensating();

    int getGrimStrafe();

    float getGrimYaw();

    boolean isInstantMode();

}
