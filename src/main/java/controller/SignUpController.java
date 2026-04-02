package controller;

import db.MemoryUserRepository;
import db.Repository;
import enums.HttpHeader;
import enums.QueryKey;
import enums.URL;
import http.HttpRequest;
import http.HttpResponse;
import http.util.HttpRequestUtils;
import model.User;

import java.io.IOException;
import java.util.Map;

public class SignUpController implements Controller {

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {

        String queryString = httpRequest.getBody();
        Map<String, String> userInfo = HttpRequestUtils.parseQueryParameter(queryString);

        // 사용자가 입력한 ID가 존재하지 않는 경우 새로 추가
        Repository repository = MemoryUserRepository.getInstance();

        if ((repository.findUserById(userInfo.get(QueryKey.USERID.getKey()))) == null) {
            User newUser = new User(
                    userInfo.get(QueryKey.USERID.getKey()),
                    userInfo.get(QueryKey.PASSWORD.getKey()),
                    userInfo.get(QueryKey.NAME.getKey()),
                    userInfo.get(QueryKey.EMAIL.getKey())
            );  // builder 패턴 적용 가능

            repository.addUser(newUser);

            httpResponse.redirect(URL.INDEX.getURL(), false);
        }

        // 사용자가 입력한 ID가 존재하는 경우
        // ...
    }
}
