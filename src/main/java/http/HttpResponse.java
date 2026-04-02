package http;

import enums.HttpHeader;
import enums.StatusCode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

public class HttpResponse {

    private final DataOutputStream dos;
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String CRLF = "\r\n";

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(String path) throws IOException {

        path = "./webapp" + path;
        byte[] body = Files.readAllBytes(Paths.get(path));

        // Status Line
        dos.writeBytes(HTTP_VERSION + " " + StatusCode.OK.getCode() + " "
                + StatusCode.OK.getMessage() + CRLF);

        // Header
        if (path.endsWith(".css"))
            dos.writeBytes(HttpHeader.CONTENT_TYPE.getValue() + ": text/css; charset=utf-8" + CRLF);
        else
            dos.writeBytes(HttpHeader.CONTENT_TYPE.getValue() + ": text/html; charset=utf-8" + CRLF);
        dos.writeBytes(HttpHeader.CONTENT_LENGTH.getValue() + ": " + body.length + CRLF);
        dos.writeBytes(CRLF);

        // Body
        dos.write(body, 0, body.length);
        dos.flush();
    }

    public void redirect(String path, boolean cookie) throws IOException {

        // Status Line
        dos.writeBytes(HTTP_VERSION + " " + StatusCode.FOUND.getCode() + " "
                + StatusCode.FOUND.getMessage() + CRLF);

        // Header
        dos.writeBytes(HttpHeader.LOCATION.getValue() + ": " + path + CRLF);
        if (cookie)  // 쿠키 추가
            dos.writeBytes((HttpHeader.SET_COOKIE.getValue() + ": logined=true" + CRLF));
        dos.writeBytes(CRLF);

        dos.flush();
    }
}
