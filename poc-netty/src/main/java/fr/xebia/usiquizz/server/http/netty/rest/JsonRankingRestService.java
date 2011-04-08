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

public class JsonRankingRestService extends RestService {

    private static final Logger logger = LoggerFactory.getLogger(JsonRankingRestService.class);

    private static final String SESSION_KEY = "session_key";

    private static final CookieDecoder cookieDecoder = new CookieDecoder();


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

    public ChannelBuffer constructJsonResponse(Score score, List<Joueur> top100, List<Joueur> prec, List<Joueur> suiv) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"score\":");
        sb.append(score.getCurrentScore());
        sb.append(",");
        sb.append("\"top_scores\":{");
        // boucle des top scores
        createJsonListJoueur(top100, sb);
        sb.append("},");

        sb.append("\"before\":{");

            // boucle des before scores
        createJsonListJoueur(prec, sb);


        sb.append("},");
        sb.append("\"after\":{");

            // boucle des after scores
        createJsonListJoueur(suiv, sb);

        
        sb.append("}");
        sb.append("}");
        ChannelBuffer cb = ChannelBuffers.dynamicBuffer(20000);
        cb.writeBytes(sb.toString().getBytes());
        return cb;
    }

    private void createJsonListJoueur(List<Joueur> list, StringBuilder sb) {
        int i = 1;
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
        sbLastName.append("]");

        sb.append(sbMail.toString());
        sb.append(sbScores.toString());
        sb.append(sbFirstName.toString());
        sb.append(sbLastName.toString());
    }
}
