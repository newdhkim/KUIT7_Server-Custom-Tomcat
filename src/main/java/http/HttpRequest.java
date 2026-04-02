package http;

import http.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    private HttpStartLine startLine;
    private HttpHeaderLine headers;
    private String body = null;

    public static HttpRequest from(BufferedReader br) throws IOException {

        HttpRequest request = new HttpRequest();
        request.parseHttpStartLine(br);
        request.parseHttpHeader(br);
        request.parseBody(br);
        return request;
    }

    private void parseHttpStartLine(BufferedReader br) throws IOException {

        String startLine = br.readLine();
        this.startLine = new HttpStartLine(startLine);
    }

    private void parseHttpHeader(BufferedReader br) throws IOException {

        List<String> headerLines = new ArrayList<String>();
        String line;
        while (!(line = br.readLine()).isEmpty()) {
            headerLines.add(line);
        }
        this.headers = new HttpHeaderLine(headerLines);
    }

    private void parseBody(BufferedReader br) throws IOException {

        Map<String, String> keyValue = headers.getHeaders();

        if (keyValue.get(enums.HttpHeader.CONTENT_LENGTH.getValue()) == null)
            return;

        int contentLength = Integer.parseInt(
                keyValue.get(enums.HttpHeader.CONTENT_LENGTH.getValue())
        );
        this.body = IOUtils.readData(br, contentLength);
    }

    public HttpStartLine getStartLine() {
        return startLine;
    }

    public HttpHeaderLine getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getBody() {
        return body;
    }
}
