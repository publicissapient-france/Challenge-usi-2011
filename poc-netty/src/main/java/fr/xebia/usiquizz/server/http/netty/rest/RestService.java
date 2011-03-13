package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
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
import java.util.concurrent.ExecutorService;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public abstract class RestService {

    protected ResponseWriter responseWriter;

    protected JsonFactory jsonFactory = new JsonFactory();

    protected ExecutorService executorService;

    protected Game game;

    protected Scoring scoring;

    protected RestService(Game game, Scoring scoring, ExecutorService executorService) {
        this.executorService = executorService;
        this.responseWriter = new ResponseWriter(executorService);
        this.game = game;
        this.scoring = scoring;
    }

    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        throw new NotImplementedException(path);
    }

    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        throw new NotImplementedException(path);
    }


}
