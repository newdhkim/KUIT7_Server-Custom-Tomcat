package enums;

public enum StatusCode {

    OK("200", "OK"),
    FOUND("302", "Found");

    private final String code;
    private final String message;

    StatusCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public  String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
