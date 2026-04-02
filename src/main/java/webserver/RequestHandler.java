package webserver;

import db.MemoryUserRepository;
import db.Repository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String startLine[] = br.readLine()
                    .split(" ");

            // Tomcat 구현 1단계 - 요구사항 1: index.html 반환하기
            if (startLine[0].equals("GET")) {
                handleGet(br, dos, startLine[1]);
                return;
            }

            // Tomcat 구현 1단계 - 요구사항 3: POST 방식으로 회원가입 하기
            if (startLine[0].equals("POST")) {
                handlePost(dos, startLine[1], br);
            }

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    // GET 방식의 요청 URL에 해당하는 HTML 파일을 읽어 응답으로 반환하는 메서드
    private void handleGet(BufferedReader br, DataOutputStream dos, String url) throws IOException {

        String path = "./webapp";
        if (url.equals("/")) {
            url = "/index.html";
        }

        // Tomcat 구현 1단계 - 요구사항 6: 사용자 목록 출력
        if (url.equals("/user/userList")) {
            log.log(Level.INFO, "Access User List ...");
            // 로그인이 안 되어있다고 가정, "/index.html"로 redirect
            url = "/index.html";
            boolean isCookie = false;

            Map<String, String> headers = parseRequestHeader(br);

            log.log(Level.INFO, "Cookie : " + headers.get("Cookie"));
            // 로그인이 되어있는 경우, "/user/list.html"로 redirect
            if (headers.get("Cookie") != null &&
                    headers.get("Cookie").contains("logined=true")) {  // cookie가 여러 개일 경우 처리
                url = "/user/list.html";
                isCookie = true;
            }

            response302Header(dos, url, isCookie);

            return;
        }

        path += url;
        byte[] body = Files.readAllBytes(Paths.get(path));

        // Tomcat 구현 1단계 - 요구사항 7: CSS 출력
        boolean isCss = false;
        if (url.endsWith(".css")) {
            log.log(Level.INFO, "CSS applied!");
            isCss = true;
        }
        response200Header(dos, body.length, isCss);
        responseBody(dos, body);
    }

    // POST 방식 로직 처리 메서드
    private void handlePost(DataOutputStream dos, String url, BufferedReader br) throws IOException {
        // 회원가입 로직
        if (url.equals("/user/signup")) {
            // body 읽어서 반환하는 메서드 호출
            Map<String, String> parameters = parseRequestBody(br);

            // memoryUserRepository에 사용자가 입력한 ID가 존재하지 않는 경우 새로 추가
            Repository repository = MemoryUserRepository.getInstance();

            if ((repository.findUserById(parameters.get("userId"))) == null) {
                User newUser = new User(parameters.get("userId"), parameters.get("password"),
                        parameters.get("name"), parameters.get("email"));
                repository.addUser(newUser);

                // 다시 index.html 화면 띄우기
                // HTTP Response message 의 status line을 "302 Found"로 설정
                response302Header(dos, "/index.html", false);
                log.log(Level.INFO, "New User created! ID: " + newUser.getUserId()
                        + ", PW: " + newUser.getPassword());
            }

            // 사용자가 입력한 ID가 존재하는 경우
            // ...

            return;
        }

        // Tomcat 구현 1단계 - 요구사항 5: 로그인하기
        if (url.equals("/user/login")) {
            log.log(Level.INFO, "Login attempt ...");
            // body 읽어서 반환하는 메서드 호출
            Map<String, String> parameters = parseRequestBody(br);

            String loginId = parameters.get("userId");
            String loginPassword = parameters.get("password");

            // MemoryUserRepository 불러오기
            Repository repository = MemoryUserRepository.getInstance();
            User user = repository.findUserById(loginId);

            // 로그인 성공 시
            if (user != null && (user.getPassword()).equals(loginPassword)) {
                log.log(Level.INFO, "Login successful! ID: " + loginId);
                response302Header(dos, "/index.html", true);  // "cookie: true": 쿠키 있음
                return;
            }

            // 로그인 실패 시
            log.log(Level.INFO, "Login failed. ID: " + loginId);
            response302Header(dos, "/user/login_failed.html", false);
        }
    }

    // POST 요청의 Request message 의 body 를 읽어서 Map으로 반환하는 메서드
    private Map<String, String> parseRequestBody(BufferedReader br) throws IOException {

        String queryString;
        int contentLength = 0;

        // Header Line이 끝나는 공백 다음부터 body이므로 해당 줄까지 넘김
        // + Content-Length 값 저장
        while (!(queryString = br.readLine()).isEmpty()) {
            if (queryString.contains("Content-Length")) {
                contentLength = Integer.parseInt(queryString.split(": ")[1].trim());
            }
        }

        // IOUtils, HttpRequestUtils 활용해 쿼리스트링 파싱
        queryString = IOUtils.readData(br, contentLength);

        return HttpRequestUtils.parseQueryParameter(queryString);
    }

    private Map<String, String> parseRequestHeader(BufferedReader br) throws IOException {

        String headerLine;
        Map<String, String> headers = new HashMap<>();

        while (!(headerLine = br.readLine()).isEmpty()) {
            String[] KeyValue = headerLine.split(": ");
            headers.put(KeyValue[0].trim(), KeyValue[1].trim());
        }

        return headers;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, boolean css) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            // .css 적용
            if (css)
                dos.writeBytes("Content-Type: text/css; charset=utf-8\r\n");
            else
                dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    // Tomcat 구현 1단계 - 요구사항 4: 302 status code 적용
    private void response302Header(DataOutputStream dos, String url, boolean cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            if (cookie)  // 쿠키 추가
                dos.writeBytes(("Set-Cookie: logined=true\r\n"));
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}