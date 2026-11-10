package lightning.managers;

import java.util.ArrayList;
import lightning.LightningClient;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.WorldUtils;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;

public class WaypointManager implements IMinecraft {
    private final ArrayList waypoints;

    public WaypointManager() {
        this.waypoints = new ArrayList();
    }

    public boolean contains(String arg0) {
        return this.waypoints.stream().anyMatch((arg1) -> arg1.getName().equalsIgnoreCase(arg0));
    }

    public void add(String arg0, Vec3 arg1) {
        add(arg0, arg1, WorldUtils.getDimension(), LightningClient.SERVER_MANAGER.getServer());
    }

    public void add(String arg0, Vec3 arg1, String arg2, String arg3) {
        if (contains(arg0)) return;
        this.waypoints.add(new WaypointManager.Waypoint(arg0, arg1, arg2, arg3));
    }

    public void remove(String arg0) {
        this.waypoints.removeIf((arg1) -> arg1.getName().equalsIgnoreCase(arg0));
    }

    public void clear() {
        this.waypoints.clear();
    }

    @Generated
    public ArrayList getWaypoints() {
        return this.waypoints;
    }


    public class Waypoint {
        private final String name;
        private final Vec3 pos;
        private final String dimension;
        private final String server;

        public Waypoint(String arg0, Vec3 arg1, String arg2, String arg3) {
            this.name = arg0;
            this.pos = arg1;
            this.dimension = arg2;
            this.server = arg3;
        }

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public Vec3 getPos() {
            return this.pos;
        }

        @Generated
        public String getDimension() {
            return this.dimension;
        }

        @Generated
        public String getServer() {
            return this.server;
        }

    }
}
