package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class ForwardController implements Controller {

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {

        String url = httpRequest.getStartLine()
                .getUrl();
        httpResponse.forward(url);
    }
}
