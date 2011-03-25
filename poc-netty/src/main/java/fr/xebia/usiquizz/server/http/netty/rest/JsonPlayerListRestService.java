package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

// Private API
public class JsonPlayerListRestService extends RestService {

    private static Logger logger = LoggerFactory.getLogger(JsonPlayerListRestService.class);


    public JsonPlayerListRestService(Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
    }

    @Override
    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        try {
            HttpRequest request = (HttpRequest) e.getMessage();
            Collection<String> playersMail = game.listPlayer();
            responseWriter.writeResponse(listPlayerToJson(playersMail), HttpResponseStatus.OK, ctx, e, null);
        } catch (Exception exc) {
            logger.error("error during question rest service", exc);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        }
    }

    private ChannelBuffer listPlayerToJson(Collection<String> playersMail) {
        StringBuilder sbSk = new StringBuilder();
        StringBuilder sbEmail = new StringBuilder();
        int i = 1;
        for (String s : playersMail) {
            sbSk.append("\"");
            sbSk.append(s.hashCode());
            sbSk.append("\"");

            sbEmail.append("\"");
            sbEmail.append(s);
            sbEmail.append("\"");
            if (i < playersMail.size()) {
                sbSk.append(",");
                sbEmail.append(",");
            }
            i++;

        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"session_key\":[");
        // session list
        sb.append(sbSk.toString());
        sb.append("],");

        sb.append("\"email\":[");
        // email list
        sb.append(sbEmail.toString());
        sb.append("]");
        sb.append("}");
        ChannelBuffer cb = ChannelBuffers.dynamicBuffer();
        cb.writeBytes(sb.toString().getBytes());
        return cb;
    }
}
