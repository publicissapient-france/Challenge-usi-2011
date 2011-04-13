package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Score;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.core.persistence.Joueur;
import fr.xebia.usiquizz.server.http.netty.FastSessionKeyCookieDecoder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

public class JsonRankingRestService extends AbstractRankingRestService {

    private static final Logger logger = LoggerFactory.getLogger(JsonRankingRestService.class);


    protected JsonRankingRestService(Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
    }


    @Override
    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        try {
            HttpRequest request = (HttpRequest) e.getMessage();

            // Get session_key
            String sessionKey = FastSessionKeyCookieDecoder.findSessionKey(request.getHeader(COOKIE));

            if (sessionKey == null) {
                logger.info("Player with no cookies... Rejected");
                responseWriter.writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                return;
            }

            // On a besoin du score courant
            // Du top 100
            // Des 50 precedents et 50 suivants
            String email = game.getEmailFromSession(sessionKey);
            responseWriter.writeResponse(constructJsonResponse(scoring.getCurrentScoreByEmail(email), scoring.getTop100(), scoring.get50Prec(email), scoring.get50Suiv(email)), HttpResponseStatus.OK, ctx, e, null);


        } catch (Exception exc) {
            logger.error("error during question rest service", exc);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        }
    }


}
