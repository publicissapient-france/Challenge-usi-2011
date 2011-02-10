package fr.xebia.usiquizz.server.http.rest;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.jackson.JsonFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;

import java.util.Set;
import java.util.UUID;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public abstract class RestService {

    private static final String CONTENT_TYPE_VALUE = "text/plain; charset=UTF-8";
    private static final String SESSION_KEY = "session_key";


    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        throw new NotImplementedException(path);
    }

    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        throw new NotImplementedException(path);
    }

    protected JsonFactory jsonFactory = new JsonFactory();

    protected void writeResponse(String content, HttpResponseStatus httpResponseStatus, ChannelHandlerContext ctx, MessageEvent e, boolean resetCookie) {

        if (httpResponseStatus == null) {
            httpResponseStatus = OK;
        }
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, httpResponseStatus);
        if (content != null) {
            response.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
            response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        }

        if (resetCookie) {
            // Encode the cookie.
            CookieEncoder cookieEncoder = new CookieEncoder(true);
            cookieEncoder.addCookie(SESSION_KEY, UUID.randomUUID().toString());
            response.addHeader(SET_COOKIE, cookieEncoder.encode());
        }
        else {
            // Encode the cookie.
            String cookieString = ((HttpRequest) e.getMessage()).getHeader(COOKIE);
            if (cookieString != null) {
                CookieDecoder cookieDecoder = new CookieDecoder();
                Set<Cookie> cookies = cookieDecoder.decode(cookieString);
                if (!cookies.isEmpty()) {
                    // Reset the cookies if necessary.
                    CookieEncoder cookieEncoder = new CookieEncoder(true);
                    for (Cookie cookie : cookies) {
                        cookieEncoder.addCookie(cookie);
                    }
                    response.addHeader(SET_COOKIE, cookieEncoder.encode());
                }
            }
        }

        // Write the response.
        ChannelFuture future = ctx.getChannel().write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    protected void writeResponse(HttpResponseStatus httpResponseStatus, ChannelHandlerContext ctx, MessageEvent e) {
        this.writeResponse(null, httpResponseStatus, ctx, e, false);
    }

    protected HttpResponse writeResponseWithoutClose(String content, ChannelHandlerContext ctx, MessageEvent e) {

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        // Encode the cookie.
        String cookieString = ((HttpRequest) e.getMessage()).getHeader(COOKIE);
        if (cookieString != null) {
            CookieDecoder cookieDecoder = new CookieDecoder();
            Set<Cookie> cookies = cookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                CookieEncoder cookieEncoder = new CookieEncoder(true);
                for (Cookie cookie : cookies) {
                    cookieEncoder.addCookie(cookie);
                }
                response.addHeader(SET_COOKIE, cookieEncoder.encode());
            }
        }

        // Write the response.
        ChannelFuture future = ctx.getChannel().write(response);
        return response;
    }

    protected HttpResponse writeResponseWithoutClose(HttpResponseStatus status, ChannelHandlerContext ctx, MessageEvent e) {

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        // Encode the cookie.
        String cookieString = ((HttpRequest) e.getMessage()).getHeader(COOKIE);
        if (cookieString != null) {
            CookieDecoder cookieDecoder = new CookieDecoder();
            Set<Cookie> cookies = cookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                CookieEncoder cookieEncoder = new CookieEncoder(true);
                for (Cookie cookie : cookies) {
                    cookieEncoder.addCookie(cookie);
                }
                response.addHeader(SET_COOKIE, cookieEncoder.encode());
            }
        }

        // Write the response.
        ChannelFuture future = ctx.getChannel().write(response);
        return response;
    }

    protected void continueWritingResponseWithoutClose(String content, ChannelHandlerContext ctx) {

        // Write the response.
        ChannelFuture future = ctx.getChannel().write(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
    }

    protected void endWritingResponseWithoutClose(String content, ChannelHandlerContext ctx) {

        // Write the response.
        ChannelFuture future = ctx.getChannel().write(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
        future.addListener(ChannelFutureListener.CLOSE);
    }


}
