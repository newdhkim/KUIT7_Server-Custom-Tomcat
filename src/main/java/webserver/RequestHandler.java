package webserver;

import controller.*;
import db.MemoryUserRepository;
import db.Repository;
import enums.*;
import http.HttpRequest;
import http.HttpHeaderLine;
import http.HttpResponse;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    private final Repository repository;
    private Controller controller = new ForwardController();

    public RequestHandler(Socket connection) {
        this.repository = MemoryUserRepository.getInstance();
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            // Header 분석
            HttpRequest httpRequest = HttpRequest.from(br);
            HttpResponse httpResponse = new HttpResponse(out);

            String method = httpRequest.getStartLine()
                    .getMethod();
            String url = httpRequest.getStartLine()
                    .getUrl();

            // html 반환
            if (url.equals(URL.ROOT.getURL())) {
                log.log(Level.INFO, "Root URL");
                controller = new HomeController();
            }

            if (method.equals(HttpMethod.GET.name()) &&
                    (url.endsWith(".html") || url.endsWith(".css"))) {
                log.log(Level.INFO, "GET HTML");
                controller = new ForwardController();
            }

            // 회원가입 요청 처리
            if (url.equals(URL.USER_SIGNUP.getURL())) {
                log.log(Level.INFO, "Signup");
                controller = new SignUpController();
            }

            // 로그인 요청 처리
            if (url.equals(URL.USER_LOGIN.getURL())) {
                log.log(Level.INFO, "Login");
                controller = new LoginController();
            }

            // 사용자 목록 출력
            if (url.equals(URL.USER_LIST.getURL())) {
                log.log(Level.INFO, "List");
                controller = new UserListController();
            }

            log.log(Level.INFO, "Method: " + method + ", URL: " + url);
            log.log(Level.INFO, "Body: " + httpRequest.getBody());

            controller.execute(httpRequest, httpResponse);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }
}