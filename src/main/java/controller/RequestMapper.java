package controller;

import enums.HttpMethod;
import enums.URL;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RequestMapper {

    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;
    private final Map<String, Controller> controllers = new HashMap<>();

    public RequestMapper(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;

        controllers.put(URL.ROOT.getURL(), new HomeController());
        controllers.put(URL.USER_SIGNUP.getURL(), new SignUpController());
        controllers.put(URL.USER_LOGIN.getURL(), new LoginController());
        controllers.put(URL.USER_LIST.getURL(), new UserListController());
    }

    public void proceed() throws IOException {

        String url = httpRequest.getStartLine()
                .getUrl();

        Controller controller = (controllers.get(url) != null) ?
                controllers.get(url) : new ForwardController();
        controller.execute(httpRequest, httpResponse);
    }
}
