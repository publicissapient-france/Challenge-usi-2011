package fr.xebia.usiquizz.server.http.netty.rest;


import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.ExecutorService;

public class HeartbeatRestService extends RestService {

    private static final byte[] OK_STRING = "OK".getBytes();

    protected HeartbeatRestService(Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
    }

    @Override
    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        responseWriter.writeResponse(ChannelBuffers.wrappedBuffer(OK_STRING), HttpResponseStatus.OK, ctx, e, null);
    }
}
