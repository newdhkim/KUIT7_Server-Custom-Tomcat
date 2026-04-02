package webserver;

import db.MemoryUserRepository;
import db.Repository;
import enums.*;
import http.HttpRequest;
import http.HttpHeaderLine;
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
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String CRLF = "\r\n";

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
            if (startLine[0].equals(HttpMethod.GET.name())) {
                handleGet(br, dos, startLine[1]);
                return;
            }

            // Tomcat 구현 1단계 - 요구사항 3: POST 방식으로 회원가입 하기
            if (startLine[0].equals(HttpMethod.POST.name())) {
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
            url = URL.INDEX.getURL();
        }

        // Tomcat 구현 1단계 - 요구사항 6: 사용자 목록 출력
        if (url.equals(URL.USER_LIST.getURL())) {
            log.log(Level.INFO, "Access User List ...");
            // 로그인이 안 되어있다고 가정, "/index.html"로 redirect
            url = URL.INDEX.getURL();
            boolean isCookie = false;

            Map<String, String> headers = parseRequestHeader(br);

            log.log(Level.INFO, HttpHeader.COOKIE.getValue() + " : " + headers.get(HttpHeader.COOKIE.getValue()));
            // 로그인이 되어있는 경우, "/user/list.html"로 redirect
            if (headers.get(HttpHeader.COOKIE.getValue()) != null &&
                    headers.get(HttpHeader.COOKIE.getValue()).contains("logined=true")) {  // cookie가 여러 개일 경우 처리
                url = URL.USER_LIST_HTML.getURL();
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
        if (url.equals(URL.USER_SIGNUP.getURL())) {
            // body 읽어서 반환하는 메서드 호출
            Map<String, String> parameters = parseRequestBody(br);

            // memoryUserRepository에 사용자가 입력한 ID가 존재하지 않는 경우 새로 추가
            Repository repository = MemoryUserRepository.getInstance();

            if ((repository.findUserById(parameters.get(QueryKey.USERID.getKey()))) == null) {
                User newUser = new User(parameters.get(QueryKey.USERID.getKey()), parameters.get(QueryKey.PASSWORD.getKey()),
                        parameters.get(QueryKey.NAME.getKey()), parameters.get(QueryKey.EMAIL.getKey()));
                repository.addUser(newUser);

                // 다시 index.html 화면 띄우기
                // HTTP Response message 의 status line을 "302 Found"로 설정
                response302Header(dos, URL.INDEX.getURL(), false);
                log.log(Level.INFO, "New User created! ID: " + newUser.getUserId()
                        + ", PW: " + newUser.getPassword());
            }

            // 사용자가 입력한 ID가 존재하는 경우
            // ...

            return;
        }

        // Tomcat 구현 1단계 - 요구사항 5: 로그인하기
        if (url.equals(URL.USER_LOGIN.getURL())) {
            log.log(Level.INFO, "Login attempt ...");
            // body 읽어서 반환하는 메서드 호출
            Map<String, String> parameters = parseRequestBody(br);

            String loginId = parameters.get(QueryKey.USERID.getKey());
            String loginPassword = parameters.get(QueryKey.PASSWORD.getKey());

            // MemoryUserRepository 불러오기
            Repository repository = MemoryUserRepository.getInstance();
            User user = repository.findUserById(loginId);

            // 로그인 성공 시
            if (user != null && (user.getPassword()).equals(loginPassword)) {
                log.log(Level.INFO, "Login successful! ID: " + loginId);
                response302Header(dos, URL.INDEX.getURL(), true);  // "cookie: true": 쿠키 있음
                return;
            }

            // 로그인 실패 시
            log.log(Level.INFO, "Login failed. ID: " + loginId);
            response302Header(dos, URL.USER_LOGIN_FAILED_HTML.getURL(), false);
        }
    }

    // POST 요청의 Request message 의 body 를 읽어서 Map으로 반환하는 메서드
    private Map<String, String> parseRequestBody(BufferedReader br) throws IOException {

        String queryString;
        int contentLength = 0;

        // Header Line이 끝나는 공백 다음부터 body이므로 해당 줄까지 넘김
        // + Content-Length 값 저장
        while (!(queryString = br.readLine()).isEmpty()) {
            if (queryString.contains(HttpHeader.CONTENT_LENGTH.getValue())) {
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
            dos.writeBytes(HTTP_VERSION + " " + StatusCode.OK.getCode() + " " + StatusCode.OK.getMessage() + CRLF);
            // .css 적용
            if (css)
                dos.writeBytes(HttpHeader.CONTENT_TYPE.getValue() + ": text/css; charset=utf-8" + CRLF);
            else
                dos.writeBytes(HttpHeader.CONTENT_TYPE.getValue() + ": text/html; charset=utf-8" + CRLF);
            dos.writeBytes(HttpHeader.CONTENT_LENGTH.getValue() + ": " + lengthOfBodyContent + CRLF);
            dos.writeBytes(CRLF);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    // Tomcat 구현 1단계 - 요구사항 4: 302 status code 적용
    private void response302Header(DataOutputStream dos, String url, boolean cookie) {
        try {
            dos.writeBytes(HTTP_VERSION + " " + StatusCode.FOUND.getCode() + " " + StatusCode.FOUND.getMessage() + CRLF);
            dos.writeBytes(HttpHeader.LOCATION.getValue() + ": " + url + CRLF);
            if (cookie)  // 쿠키 추가
                dos.writeBytes((HttpHeader.SET_COOKIE.getValue() + ": logined=true" + CRLF));
            dos.writeBytes(CRLF);
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