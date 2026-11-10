package lightning.accounts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Account {
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("uuid")
    private String uuid;
    @Expose
    @SerializedName("type")
    private AccountType type;
    @Expose
    @SerializedName("refreshToken")
    private String refreshToken;
    @Expose
    @SerializedName("accessToken")
    private String accessToken;
    @Expose
    @SerializedName("lastUsed")
    private long lastUsed;

    public Account(String arg0, String arg1, AccountType arg2) {
        this.name = arg0;
        this.uuid = arg1;
        this.type = arg2;
        this.lastUsed = System.currentTimeMillis();
    }

    public Account(String arg0, String arg1, AccountType arg2, String arg3, String arg4) {
        this.name = arg0;
        this.uuid = arg1;
        this.type = arg2;
        this.refreshToken = arg3;
        this.accessToken = arg4;
        this.lastUsed = System.currentTimeMillis();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String arg0) {
        this.name = arg0;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String arg0) {
        this.uuid = arg0;
    }

    public AccountType getType() {
        return this.type;
    }

    public void setType(AccountType arg0) {
        this.type = arg0;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String arg0) {
        this.refreshToken = arg0;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String arg0) {
        this.accessToken = arg0;
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public void setLastUsed(long arg0) {
        this.lastUsed = arg0;
    }

    public UUID getProfileId() {
        try {
            if (this.uuid != null && !(this.uuid.isEmpty())) {
                if (this.uuid.contains("-")) {
                    return UUID.fromString(this.uuid);
                    try {
                    }
                    return UUID.fromString(this.uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})", "$1-$2-$3-$4-$5"));
                }
            } catch (Exception ex5) {
                Object var1 = ex5;
            }
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.name).getBytes(StandardCharsets.UTF_8));
        }
    }

}
