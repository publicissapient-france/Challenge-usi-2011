package fr.xebia.usiquizz.server.http.deft;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;

import java.util.UUID;

import org.codehaus.jackson.JsonFactory;
import org.deftserver.web.handler.RequestHandler;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;
import org.jboss.netty.handler.codec.http.CookieEncoder;

public abstract class RestHandler extends RequestHandler {

    protected JsonFactory jsonFactory = new JsonFactory();

    protected String initCookie(HttpRequest request, HttpResponse response) {
        CookieEncoder cookieEncoder = new CookieEncoder(true);
        String key = UUID.randomUUID().toString();
        cookieEncoder.addCookie("session_key", key);
        response.setHeader(SET_COOKIE, cookieEncoder.encode());
        return key;
    }

    protected void rewriteCookie(HttpRequest request, HttpResponse response) {
        response.setHeader(SET_COOKIE, request.getHeader(COOKIE));
    }
}
