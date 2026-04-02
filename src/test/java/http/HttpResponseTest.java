package http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpResponseTest {

    private OutputStream outputStreamToFile(String path) throws IOException {
        return Files.newOutputStream(Paths.get(path));
    }

    private String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    @Test
    void forward_테스트() throws IOException {
        HttpResponse response = new HttpResponse(outputStreamToFile("src/test/resources/forward_response.txt"));
        response.forward("/index.html");

        String result = readFile("src/test/resources/forward_response.txt");
        assertTrue(result.contains("HTTP/1.1 200 OK"));
        assertTrue(result.contains("Content-Type: text/html"));
        assertTrue(result.contains("Content-Length:"));
    }

    @Test
    void redirect_테스트() throws IOException {
        HttpResponse response = new HttpResponse(outputStreamToFile("src/test/resources/redirect_response.txt"));
        response.redirect("/index.html", false);

        String result = readFile("src/test/resources/redirect_response.txt");
        assertTrue(result.contains("HTTP/1.1 302 Found"));
        assertTrue(result.contains("Location: /index.html"));
    }

    @Test
    void redirect_쿠키_테스트() throws IOException {
        HttpResponse response = new HttpResponse(outputStreamToFile("src/test/resources/redirect_response.txt"));
        response.redirect("/index.html", true);

        String result = readFile("src/test/resources/redirect_response.txt");
        assertTrue(result.contains("HTTP/1.1 302 Found"));
        assertTrue(result.contains("Location: /index.html"));
        assertTrue(result.contains("Set-Cookie: logined=true"));
    }
}
