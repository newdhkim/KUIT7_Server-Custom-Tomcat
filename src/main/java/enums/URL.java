package enums;

public enum URL {

    ROOT("/"),
    INDEX("/index.html"),
    USER_LIST("/user/userList"),
    USER_LIST_HTML("/user/list.html"),
    USER_SIGNUP("/user/signup"),
    USER_LOGIN("/user/login"),
    USER_LOGIN_FAILED_HTML("/user/login_failed.html");

    private final String url;

    URL(String url) {
        this.url = url;
    }

    public String getURL() {
        return url;
    }
}
