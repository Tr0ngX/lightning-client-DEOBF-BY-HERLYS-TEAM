package lightning.events.impl;

import lightning.events.Event;
import lightning.modules.Module;
import lightning.utils.rotations.Rotation;
import lombok.Generated;

public class ClientRotationEvent extends Event {
    private final Rotation rotation;
    private Module owner;

    public ClientRotationEvent(Rotation arg0) {
        this.rotation = arg0;
    }

    public void setYaw(float arg0) {
        setCancelled(true);
        this.rotation.setYaw(arg0);
    }

    public void setPitch(float arg0) {
        setCancelled(true);
        this.rotation.setPitch(arg0);
    }

    @Generated
    public Rotation getRotation() {
        return this.rotation;
    }

    @Generated
    public Module getOwner() {
        return this.owner;
    }

    @Generated
    public void setOwner(Module arg0) {
        this.owner = arg0;
    }

}
