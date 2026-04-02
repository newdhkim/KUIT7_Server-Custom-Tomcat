package enums;

public enum QueryKey {

    USERID("userId"),
    PASSWORD("password"),
    NAME("name"),
    EMAIL("email");

    private final String key;

    QueryKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
