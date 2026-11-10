package lightning.managers;

import java.util.ArrayList;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.utils.IMinecraft;

public class TaskManager implements IMinecraft {
    private final ArrayList tasks;

    public TaskManager() {
        this.tasks = new ArrayList();
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (!(this.tasks.isEmpty())) {
            ((Runnable) this.tasks.getFirst()).run();
            this.tasks.removeFirst();
        }
    }

    public void submit(Runnable arg0) {
        this.tasks.add(arg0);
    }

}
