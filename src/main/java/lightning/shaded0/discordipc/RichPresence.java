package lightning.shaded0.discordipc;

import com.google.gson.JsonObject;

public class RichPresence {
    private String details;
    private String state;
    private RichPresence.Assets assets;
    private RichPresence.Timestamps timestamps;

    public void setDetails(String arg0) {
        this.details = arg0;
    }

    public void setState(String arg0) {
        this.state = arg0;
    }

    public void setLargeImage(String arg0, String arg1) {
        if (this.assets == null) {
            this.assets = new RichPresence.Assets();
        }
        this.assets.large_image = arg0;
        this.assets.large_text = arg1;
    }

    public void setSmallImage(String arg0, String arg1) {
        if (this.assets == null) {
            this.assets = new RichPresence.Assets();
        }
        this.assets.small_image = arg0;
        this.assets.small_text = arg1;
    }

    public void setStart(long arg0) {
        if (this.timestamps == null) {
            this.timestamps = new RichPresence.Timestamps();
        }
        this.timestamps.start = arg0;
    }

    public void setEnd(long arg0) {
        if (this.timestamps == null) {
            this.timestamps = new RichPresence.Timestamps();
        }
        this.timestamps.end = arg0;
    }

    public JsonObject toJson() {
        Object var1 = new JsonObject();
        if (this.details != null) {
            var1.addProperty("details", this.details);
        }
        if (this.state != null) {
            var1.addProperty("state", this.state);
        }
        if (this.assets != null) {
            Object var2 = new JsonObject();
            if (this.assets.large_image != null) {
                var2.addProperty("large_image", this.assets.large_image);
            }
            if (this.assets.large_text != null) {
                var2.addProperty("large_text", this.assets.large_text);
            }
            if (this.assets.small_image != null) {
                var2.addProperty("small_image", this.assets.small_image);
            }
            if (this.assets.small_text != null) {
                var2.addProperty("small_text", this.assets.small_text);
            }
            var1.add("assets", var2);
        }
        if (this.timestamps != null) {
            var2 = new JsonObject();
            if (this.timestamps.start != null) {
                var2.addProperty("start", this.timestamps.start);
            }
            if (this.timestamps.end != null) {
                var2.addProperty("end", this.timestamps.end);
            }
            var1.add("timestamps", var2);
        }
        var1.addProperty("type", ((long) 2));
        return var1;
    }


    public class Timestamps {
        public Long start;
        public Long end;

        public Timestamps() {
        }

    }

    public class Assets {
        public String large_image;
        public String large_text;
        public String small_image;
        public String small_text;

        public Assets() {
        }

    }
}
