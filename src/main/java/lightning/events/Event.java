package lightning.events;

import lombok.Generated;

public class Event {
    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    @Generated
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Generated
    public void setCancelled(boolean arg0) {
        this.cancelled = arg0;
    }

    @Generated
    public boolean equals(Object arg0) {
        if (arg0 == this) return true;
        if (!((arg0 instanceof Event))) return false;
        Object var2 = ((Event) arg0);
        if (!(var2.canEqual(this))) return false;
        return isCancelled() == var2.isCancelled();
    }

    @Generated
    protected boolean canEqual(Object arg0) {
        return (arg0 instanceof Event);
    }

    @Generated
    public int hashCode() {
        int var1 = 59;
        int var2 = 1;
        var2 = ((var2 * 59) + (isCancelled() ? 79 : 97));
        return var2;
    }

    @Generated
    public String toString() {
        return ("Event(cancelled=" + isCancelled() + ")");
    }

}
