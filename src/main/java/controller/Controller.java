package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public interface Controller {

    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException;

}
