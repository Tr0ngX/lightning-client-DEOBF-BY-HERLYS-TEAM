package lightning.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lightning.LightningClient;
import lightning.accounts.Account;
import lightning.accounts.AccountType;
import lightning.accounts.MicrosoftAuth;
import lightning.mixins.accessors.MinecraftClientAccessor;
import lightning.utils.IMinecraft;
import lombok.Generated;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;

public class AccountManager implements IMinecraft {
    private static final Gson GSON;
    private final List accounts;
    private Account currentAccount;

    public AccountManager() {
        this.accounts = new CopyOnWriteArrayList();
        loadAccounts();
    }

    public void loadAccounts() {
        try {
            Object var1 = getAccountsPath();
            if (!(Files.exists(var1, new LinkOption[0]))) {
                return;
                try {
                }
                Object var2 = Files.newBufferedReader(var1, StandardCharsets.UTF_8);
                try {
                    Object var3 = new AccountManager.1(this).getType();
                    Object var4 = ((List) GSON.fromJson(var2, var3));
                    if (var4 != null) {
                        this.accounts.clear();
                        this.accounts.addAll(var4);
                    }
                    if (var2 == null) break label_10;
                    var2.close();
                } catch (Throwable ex5) {
                    var3 = ex5;
                    if (var2 == null) break label_9;
                    try {
                        var2.close();
                    } catch (Throwable ex8) {
                        var4 = ex8;
                        var3.addSuppressed(var4);
                    }
                    label_9:
                    throw var3;
                }
                label_10:
            } catch (Exception ex11) {
                var1 = ex11;
                LightningClient.LOGGER.error("Failed to load accounts.json", var1);
            }
        }
    }

    public void saveAccounts() {
        try {
            Object var1 = getAccountsPath();
            if ((var1.getParent() == null) || (Files.exists(var1.getParent(), new LinkOption[0]))) break label_1;
            Files.createDirectories(var1.getParent(), new FileAttribute[0]);
            label_1:
            Object var2 = Files.newBufferedWriter(var1, StandardCharsets.UTF_8, new OpenOption[0]);
            try {
                GSON.toJson(this.accounts, var2);
                if (var2 == null) break label_9;
                var2.close();
            } catch (Throwable ex4) {
                Object var3 = ex4;
                if (var2 == null) break label_8;
                try {
                    var2.close();
                } catch (Throwable ex7) {
                    Object var4 = ex7;
                    var3.addSuppressed(var4);
                }
                label_8:
                throw var3;
            }
            label_9:
        } catch (Exception ex10) {
            var1 = ex10;
            LightningClient.LOGGER.error("Failed to save accounts.json", var1);
        }
    }

    private Path getAccountsPath() {
        var tmp0 = new String[1];
        tmp0[0] = "accounts.json";
        return Paths.get("Lightning", tmp0);
    }

    private void applySession(User arg0, String arg1) {
        Object var3 = ((MinecraftClientAccessor) mc);
        var3.setUser(arg0);
        try {
            if (arg1 != null && !(arg1.isEmpty())) {
                Object var4 = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
                Object var5 = var4.createUserApiService(arg1);
                var3.setUserApiService(var5);
                Object var6 = ProfileKeyPairManager.create(var5, arg0, mc.gameDirectory.toPath());
                var3.setProfileKeyPairManager(var6);
                var6.prepareKeyPair();
            } else {
                var3.setUserApiService(UserApiService.OFFLINE);
                var3.setProfileKeyPairManager(ProfileKeyPairManager.EMPTY_KEY_MANAGER);
            }
        } catch (Exception ex3) {
            var4 = ex3;
            LightningClient.LOGGER.warn(("Failed to initialize ProfileKeyPairManager for account: " + arg0.getName()), var4);
        }
    }

    public boolean login(Account arg0) {
        if (arg0 == null) {
            return false;
            try {
            }
            if (arg0.getType() == AccountType.OFFLINE) {
                Object var2 = new User(arg0.getName(), arg0.getProfileId(), "", Optional.empty(), Optional.empty());
                applySession(var2, "");
                this.currentAccount = arg0;
                arg0.setLastUsed(System.currentTimeMillis());
                saveAccounts();
                return true;
                try {
                }
                if (arg0.getType() == AccountType.MICROSOFT) {
                    if ((arg0.getRefreshToken() == null) || (arg0.getRefreshToken().isEmpty())) break label_5;
                    var tmp0 = MicrosoftAuth.refreshAccount(arg0.getRefreshToken());
                    var2 = tmp0;
                    if (!(tmp0.success())) break label_5;
                    arg0.setName(var2.username());
                    arg0.setUuid(var2.uuid());
                    arg0.setAccessToken(var2.accessToken());
                    if (var2.refreshToken() != null && !(var2.refreshToken().isEmpty())) {
                        arg0.setRefreshToken(var2.refreshToken());
                    }
                    Object var3 = new User(arg0.getName(), arg0.getProfileId(), arg0.getAccessToken(), Optional.empty(), Optional.empty());
                    applySession(var3, arg0.getAccessToken());
                    this.currentAccount = arg0;
                    arg0.setLastUsed(System.currentTimeMillis());
                    saveAccounts();
                    return true;
                    try {
                        label_5:
                        if (arg0.getAccessToken() != null && !(arg0.getAccessToken().isEmpty())) {
                            var3 = new User(arg0.getName(), arg0.getProfileId(), arg0.getAccessToken(), Optional.empty(), Optional.empty());
                            applySession(var3, arg0.getAccessToken());
                            this.currentAccount = arg0;
                            arg0.setLastUsed(System.currentTimeMillis());
                            saveAccounts();
                            return true;
                            try {
                            }
                            return false;
                            try {
                            }
                            if (arg0.getType() == AccountType.SESSION) {
                                if (arg0.getAccessToken() != null) {
                                } else {
                                }
                                var2 = new User;
                                applySession(var2, arg0.getAccessToken());
                                this.currentAccount = arg0;
                                arg0.setLastUsed(System.currentTimeMillis());
                                saveAccounts();
                                return true;
                            }
                        } catch (Exception ex15) {
                            var2 = ex15;
                            LightningClient.LOGGER.error(("Failed to login to account: " + arg0.getName()), var2);
                            return false;
                        }
                        return false;
                    }
                }
            }
        }
    }

    public Account loginOffline(String arg0) {
        if ((arg0 == null) || (arg0.trim().isEmpty())) {
            return null;
        }
        arg0 = arg0.trim();
        Object var2 = null;
        Object var3 = this.accounts.iterator();
        label_2:
        if (!(var3.hasNext())) break label_4;
        Object var4 = ((Account) var3.next());
        if (var4.getType() != AccountType.OFFLINE) break label_2;
        if (!(var4.getName().equalsIgnoreCase(arg0))) {
            break label_2;
        }
        var2 = var4;
        break label_4;
        label_4:
        if (var2 == null) {
            var3 = UUID.nameUUIDFromBytes(("OfflinePlayer:" + arg0).getBytes(StandardCharsets.UTF_8));
            var2 = new Account(arg0, var3.toString(), AccountType.OFFLINE);
            this.accounts.add(var2);
        }
        login(var2);
        return var2;
    }

    public void addAccount(Account arg0) {
        if (arg0 == null) return;
        this.accounts.removeIf((arg1) -> {
    if (!(arg1.getName().equalsIgnoreCase(arg0.getName()))) break label_0;
    return arg1.getType() == arg0.getType();
});
        this.accounts.add(arg0);
        saveAccounts();
    }

    public void removeAccount(Account arg0) {
        if (arg0 == null) return;
        this.accounts.remove(arg0);
        if (this.currentAccount == arg0) {
            this.currentAccount = null;
        }
        saveAccounts();
    }

    public String getCurrentName() {
        if (mc.getUser() != null) return mc.getUser().getName();
        if (this.currentAccount != null) {
        } else {
        }
        return "Unknown";
    }

    @Generated
    public List getAccounts() {
        return this.accounts;
    }

    @Generated
    public Account getCurrentAccount() {
        return this.currentAccount;
    }

    @Generated
    public void setCurrentAccount(Account arg0) {
        this.currentAccount = arg0;
    }

    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }


    class 1 extends TypeToken {
        1(AccountManager arg0) {
            Objects.requireNonNull(arg0);
        }

    }
}
