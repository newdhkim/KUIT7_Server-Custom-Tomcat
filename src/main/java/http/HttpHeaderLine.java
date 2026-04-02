package http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpHeaderLine {

    private final Map<String, String> headers = new HashMap<>();

    public HttpHeaderLine(List<String> headerLines) {
        for (String line : headerLines) {
            String[] keyValue = line.split(": ");
            headers.put(keyValue[0].trim(), keyValue[1].trim());
        }
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String get(String key) {
        return headers.get(key);
    }
}