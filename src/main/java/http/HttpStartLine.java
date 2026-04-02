package http;

public class HttpStartLine {

    private final String method;
    private final String url;
    private final String version;

    public HttpStartLine(String startLine) {
        String[] tokens = startLine.split(" ");
        this.method = tokens[0];
        this.url = tokens[1];
        this.version = tokens[2];
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }
}
