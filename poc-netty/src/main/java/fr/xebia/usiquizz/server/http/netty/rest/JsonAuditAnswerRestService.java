package fr.xebia.usiquizz.server.http.netty.rest;


import fr.xebia.usiquizz.core.authentication.AdminAuthentication;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class JsonAuditAnswerRestService extends RestService {

    private static final String AUTHENTICATION_KEY = "authentication_key";
    private static final String USER_MAIL = "user_mail";

    private static Logger logger = LoggerFactory.getLogger(JsonAuditAnswerRestService.class);


    public JsonAuditAnswerRestService(Game game, Scoring scoring, ExecutorService executorService) {
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

            // On regarde si on demande une question en particulier
            // On sépare la query string du path
            String requestPath = path.substring(0, path.indexOf("?"));

            try {
                byte questionNbr = Byte.parseByte(requestPath.substring(requestPath.lastIndexOf("/") + 1));
                // audit d'une question
                //auditQuestion(questionNbr);
            } catch (NumberFormatException ex) {
                // audit de tout
                //auditAllQuestion();
            }


        } catch (Exception exc) {
            logger.error("error during question rest service", exc);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        }
    }
}
