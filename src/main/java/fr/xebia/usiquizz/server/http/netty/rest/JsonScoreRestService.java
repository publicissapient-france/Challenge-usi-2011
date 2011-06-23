package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.authentication.AdminAuthentication;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Score;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.core.persistence.Joueur;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

public class JsonScoreRestService extends AbstractRankingRestService {

    private static final Logger logger = LoggerFactory.getLogger(JsonScoreRestService.class);

    private static final String AUTHENTICATION_KEY = "authentication_key";
    private static final String USER_MAIL = "user_mail";


    protected JsonScoreRestService(Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
    }


    @Override
    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        try {
            HttpRequest request = (HttpRequest) e.getMessage();

            // Get authentication_key
            String authenticationKey = "";
            String userMail = "";
            QueryStringDecoder qsd = new QueryStringDecoder(request.getUri());
            List<String> auths = qsd.getParameters().get(AUTHENTICATION_KEY);
            if (auths != null && auths.size() > 0) {
                authenticationKey = auths.get(0);
            }
            List<String> mails = qsd.getParameters().get(USER_MAIL);
            if (mails != null && mails.size() > 0) {
                userMail = mails.get(0);
            }

            if (authenticationKey == null || !authenticationKey.equals(AdminAuthentication.key)) {
                logger.info("User with bad authentication ");
                responseWriter.writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                return;
            }


            String fakeSessionKey = Integer.valueOf(userMail.hashCode()).toString();
            // On a besoin du score courant
            // Du top 100
            // Des 50 precedents et 50 suivants
            responseWriter.writeResponse(constructJsonResponse(scoring.getCurrentScore(fakeSessionKey), scoring.getTop100(), scoring.get50Prec(fakeSessionKey), scoring.get50Suiv(fakeSessionKey)), HttpResponseStatus.OK, ctx, e, null);


        } catch (Exception exc) {
            logger.error("error during question rest service", exc);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        }
    }

}
