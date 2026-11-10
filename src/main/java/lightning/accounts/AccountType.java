package lightning.accounts;

public enum AccountType {
    OFFLINE,
    MICROSOFT,
    SESSION;

    private final String displayName;

    private AccountType(String arg2) {
        this.displayName = arg2;
    }

    public String getDisplayName() {
        return this.displayName;
    }

}
