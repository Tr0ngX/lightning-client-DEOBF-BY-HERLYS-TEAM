package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class KeyInputEvent extends Event {
    private final int key;
    private final int modifiers;

    @Generated
    public boolean equals(Object arg0) {
        if (arg0 == this) return true;
        if (!((arg0 instanceof KeyInputEvent))) return false;
        Object var2 = ((KeyInputEvent) arg0);
        if (!(var2.canEqual(this))) return false;
        if (!(super.equals(arg0))) return false;
        if (getKey() != var2.getKey()) return false;
        return getModifiers() == var2.getModifiers();
    }

    @Generated
    protected boolean canEqual(Object arg0) {
        return (arg0 instanceof KeyInputEvent);
    }

    @Generated
    public int hashCode() {
        int var1 = 59;
        int var2 = super.hashCode();
        var2 = ((var2 * 59) + getKey());
        var2 = ((var2 * 59) + getModifiers());
        return var2;
    }

    @Generated
    public KeyInputEvent(int arg0, int arg1) {
        this.key = arg0;
        this.modifiers = arg1;
    }

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public int getModifiers() {
        return this.modifiers;
    }

    @Generated
    public String toString() {
        return ("KeyInputEvent(key=" + getKey() + ", modifiers=" + getModifiers() + ")");
    }

}
