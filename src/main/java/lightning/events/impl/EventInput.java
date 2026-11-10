package lightning.events.impl;

import lightning.events.Event;

public class EventInput extends Event {
    public boolean forward;
    public boolean backward;
    public boolean left;
    public boolean right;
    public boolean jumping;
    public boolean sneaking;
    public boolean sprinting;

    public EventInput(boolean arg0, boolean arg1, boolean arg2, boolean arg3, boolean arg4, boolean arg5, boolean arg6) {
        this.forward = arg0;
        this.backward = arg1;
        this.left = arg2;
        this.right = arg3;
        this.jumping = arg4;
        this.sneaking = arg5;
        this.sprinting = arg6;
    }

}
