package fr.xebia.usiquizz.server.http.netty.rest;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ResponseWriter {

    private static final String CONTENT_TYPE_VALUE = "application/json; charset=UTF-8";
    private static final String SESSION_KEY = "session_key";

    public ResponseWriter() {

    }

    public void writeResponse(final String content, final HttpResponseStatus httpResponseStatus, final ChannelHandlerContext ctx, final MessageEvent e, final String sessionKey) {

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, httpResponseStatus);
        if (content != null) {
            response.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
            response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        }

        if (sessionKey != null) {
            // Encode the cookie.
            CookieEncoder cookieEncoder = new CookieEncoder(true);
            cookieEncoder.addCookie(SESSION_KEY, sessionKey);
            response.addHeader(SET_COOKIE, cookieEncoder.encode());
        } else {
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
        //if (!isKeepAlive((HttpRequest)e.getMessage()) || response.getStatus().getCode() != 200) {
            future.addListener(ChannelFutureListener.CLOSE);
        //}

    }

    public void writeResponse(final String content, final HttpResponseStatus httpResponseStatus, final ChannelHandlerContext ctx, final MessageEvent e) {
        this.writeResponse(content, httpResponseStatus, ctx, e, null);
    }

    public void writeResponse(final HttpResponseStatus httpResponseStatus, final ChannelHandlerContext ctx, final MessageEvent e) {
        this.writeResponse(null, httpResponseStatus, ctx, e, null);
    }

    public void writeResponseWithoutClose(final HttpResponseStatus status, final ChannelHandlerContext ctx, final MessageEvent e) {

/*
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
        */
    }


    public void endWritingResponseWithoutClose(final String content, final String sessionKey, final ChannelHandlerContext ctx) {

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        // Encode the cookie.
        CookieEncoder cookieEncoder = new CookieEncoder(true);
            cookieEncoder.addCookie(SESSION_KEY, sessionKey);
            response.addHeader(SET_COOKIE, cookieEncoder.encode());
        response.setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));

        // Write the response.
        ChannelFuture future = ctx.getChannel().write(response);
        //if (!isKeepAlive((HttpRequest)e.getMessage()) || response.getStatus().getCode() != 200) {
        //    future.addListener(ChannelFutureListener.CLOSE);
        //}

        // Write the response.
        //ChannelFuture future = ctx.getChannel().write(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8));
        future.addListener(ChannelFutureListener.CLOSE);

    }
}
