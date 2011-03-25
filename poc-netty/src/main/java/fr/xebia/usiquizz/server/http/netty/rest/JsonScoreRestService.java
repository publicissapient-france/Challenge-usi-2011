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

public class JsonScoreRestService extends RestService {

    private static final Logger logger = LoggerFactory.getLogger(JsonScoreRestService.class);

    private static final String SESSION_KEY = "session_key";
    private static final String AUTHENTICATION_KEY = "authentication_key";
    private static final String USER_MAIL = "user_mail";

    private static final CookieDecoder cookieDecoder = new CookieDecoder();


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


            // On a besoin du score courant
            // Du top 100
            // Des 50 precedents et 50 suivants
            responseWriter.writeResponse(constructJsonResponse(scoring.getCurrentScoreByEmail(userMail), scoring.getTop100(), scoring.get50Prec(userMail), scoring.get50Suiv(userMail)), HttpResponseStatus.OK, ctx, e, null);


        } catch (Exception exc) {
            logger.error("error during question rest service", exc);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        }
    }

    public ChannelBuffer constructJsonResponse(Score score, List<Joueur> top100, List<Joueur> prec, List<Joueur> suiv) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"score\":");
        sb.append(score.getCurrentScore());
        sb.append(",");
        sb.append("\"top_scores\":{");
        // boucle des top scores
        createJsonListJoueur(top100, sb);
        sb.append("},");

        if (prec.size() > 0) {
            sb.append("\"before\":{");
            // boucle des before scores
            createJsonListJoueur(prec, sb);
            sb.append("},");
        }

        if (suiv.size() > 0) {
            sb.append("\"after\":{");
            // boucle des after scores
            createJsonListJoueur(suiv, sb);
            sb.append("}");
        }
        sb.append("}");

        ChannelBuffer cb = ChannelBuffers.dynamicBuffer(20000);
        cb.writeBytes(sb.toString().getBytes());
        return cb;
    }

    private void createJsonListJoueur(List<Joueur> list, StringBuilder sb) {
        int i = 0;
        StringBuilder sbMail = new StringBuilder();
        StringBuilder sbScores = new StringBuilder();
        StringBuilder sbFirstName = new StringBuilder();
        StringBuilder sbLastName = new StringBuilder();
        sbMail.append("\"mail\":[");
        sbScores.append("\"scores\":[");
        sbFirstName.append("\"firstname\":[");
        sbLastName.append("\"lastname\":[");
        for (Joueur j : list) {
            // Mail
            sbMail.append("\"");
            sbMail.append(j.getEmail());
            sbMail.append("\"");
            if (i < list.size()) {
                sbMail.append(",");
            }

            // scores
            sbScores.append(j.getScore());
            if (i < list.size()) {
                sbScores.append(",");
            }

            // firstname
            sbFirstName.append("\"");
            sbFirstName.append(j.getFirstName());
            sbFirstName.append("\"");
            if (i < list.size()) {
                sbFirstName.append(",");
            }

            // lastname
            sbLastName.append("\"");
            sbLastName.append(j.getLastName());
            sbLastName.append("\"");
            if (i < list.size()) {
                sbLastName.append(",");
            }

            i++;
        }
        sbMail.append("],");
        sbScores.append("],");
        sbFirstName.append("],");
        sbLastName.append("],");

        sb.append(sbMail.toString());
        sb.append(sbScores.toString());
        sb.append(sbFirstName.toString());
        sb.append(sbLastName.toString());
    }
}
