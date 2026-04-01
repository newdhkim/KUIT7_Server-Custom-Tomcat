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
                responseHtmlFile(dos, startLine[1]);
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
    private void responseHtmlFile(DataOutputStream dos, String url) throws IOException {

        String path = "./webapp";
        if (url.equals("/")) {
            url = "/index.html";
        }
        path += url;
        byte[] body = Files.readAllBytes(Paths.get(path));
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    // POST 방식 로직 처리 메서드
    private void handlePost(DataOutputStream dos, String url, BufferedReader br) throws IOException {
        // 회원가입 로직
        if (url.equals("/user/signup")) {
            String queryString;
            int contentLength = 0;

            // Header Line이 끝나는 공백 다음부터 body이므로 해당 줄까지 넘김
            // + Content-Length 값 저장
            while (!(queryString = br.readLine()).isEmpty()) {
                if (queryString.split(": ")[0].equals("Content-Length")) {
                    contentLength = Integer.parseInt(queryString.split(": ")[1]);
                }
            }

            // IOUtils, HttpRequestUtils 활용해 쿼리스트링 파싱
            queryString = IOUtils.readData(br, contentLength);
            Map<String, String> parameters = HttpRequestUtils.parseQueryParameter(queryString);

            // memoryUserRepository에 사용자가 입력한 ID가 존재하지 않는 경우 새로 추가
            Repository repository = MemoryUserRepository.getInstance();

            if ((repository.findUserById(parameters.get("userId"))) == null) {
                User newUser = new User(parameters.get("userId"), parameters.get("password"),
                        parameters.get("name"), parameters.get("email"));
                repository.addUser(newUser);

                // 다시 index.html 화면 띄우기
                responseHtmlFile(dos, "/index.html");
            }

            // 사용자가 입력한 ID가 존재하는 경우
            // ...
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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