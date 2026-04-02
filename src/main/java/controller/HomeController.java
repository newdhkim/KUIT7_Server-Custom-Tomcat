package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public class HomeController implements Controller {

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {

        String url = "/index.html";
        httpResponse.forward(url);
    }
}
