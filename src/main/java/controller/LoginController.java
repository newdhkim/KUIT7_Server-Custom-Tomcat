package controller;

import db.MemoryUserRepository;
import db.Repository;
import enums.QueryKey;
import enums.URL;
import http.HttpRequest;
import http.HttpResponse;
import http.util.HttpRequestUtils;
import model.User;

import java.io.IOException;
import java.util.Map;

public class LoginController implements Controller {

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {

        String queryString = httpRequest.getBody();
        Map<String, String> parameters = HttpRequestUtils.parseQueryParameter(queryString);

        String loginId = parameters.get(QueryKey.USERID.getKey());
        String loginPassword = parameters.get(QueryKey.PASSWORD.getKey());

        Repository repository = MemoryUserRepository.getInstance();
        User user = repository.findUserById(loginId);

        // 로그인 성공 시
        if (user != null && (user.getPassword()).equals(loginPassword)) {
            httpResponse.redirect(URL.INDEX.getURL(), true);  // "cookie: true": 쿠키 있음
            return;
        }

        // 로그인 실패 시
        httpResponse.redirect(URL.USER_LOGIN_FAILED_HTML.getURL(), false);
    }
}
