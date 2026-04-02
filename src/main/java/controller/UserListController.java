package controller;

import enums.HttpHeader;
import enums.URL;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class UserListController implements Controller {

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {

        String cookieValue = httpRequest.getHeader(HttpHeader.COOKIE.getValue());

        // 로그인이 안 되어있다고 가정, "/index.html"로 redirect
        String url = URL.INDEX.getURL();
        boolean isCookie = false;

        // 로그인이 되어있는 경우, "/user/list.html"로 redirect
        if (cookieValue != null && cookieValue.contains("logined=true")) {  // cookie가 여러 개일 경우 처리
            url = URL.USER_LIST_HTML.getURL();
            isCookie = true;
        }

        httpResponse.redirect(url, isCookie);
    }
}
