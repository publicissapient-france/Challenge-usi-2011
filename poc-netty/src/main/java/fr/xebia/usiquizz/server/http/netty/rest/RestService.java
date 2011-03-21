package fr.xebia.usiquizz.server.http.netty.rest;

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

    protected ResponseWriter responseWriter = new ResponseWriter();

    protected JsonFactory jsonFactory = new JsonFactory();

    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        throw new NotImplementedException(path);
    }

    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        throw new NotImplementedException(path);
    }


}
