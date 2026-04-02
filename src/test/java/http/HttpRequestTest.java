package http;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

public class HttpRequestTest {

    private BufferedReader bufferedReaderFromFile(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(path))));
    }

    @Test
    void POST_요청_파싱_테스트() throws IOException {
        HttpRequest request = HttpRequest.from(bufferedReaderFromFile("src/test/resources/post_request.txt"));

        assertEquals("POST", request.getStartLine().getMethod());
        assertEquals("/user/create", request.getStartLine().getUrl());
        assertEquals("HTTP/1.1", request.getStartLine().getVersion());
        assertEquals("localhost:8080", request.getHeaders().getHeaders().get("Host"));
        assertEquals("40", request.getHeaders().getHeaders().get("Content-Length"));
        assertEquals("userId=jw&password=password&name=jungwoo", request.getBody());
    }
}
