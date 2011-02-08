package fr.xebia.usiquizz.server.http.deft;


import org.codehaus.jackson.JsonFactory;
import org.deftserver.web.handler.RequestHandler;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;
import org.jboss.netty.handler.codec.http.CookieEncoder;

import java.util.UUID;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;

public abstract class RestHandler extends RequestHandler {

    protected JsonFactory jsonFactory = new JsonFactory();

    protected void initCookie(HttpRequest request, HttpResponse response) {
        CookieEncoder cookieEncoder = new CookieEncoder(true);
        cookieEncoder.addCookie("session_key", UUID.randomUUID().toString());
        response.setHeader(SET_COOKIE, cookieEncoder.encode());
    }

    protected void rewriteCookie(HttpRequest request, HttpResponse response) {
        response.setHeader(SET_COOKIE, request.getHeader(COOKIE));
    }
}
