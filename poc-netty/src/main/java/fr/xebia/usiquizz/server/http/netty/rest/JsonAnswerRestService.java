package fr.xebia.usiquizz.server.http.netty.rest;

import com.usi.Question;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.authentication.AdminAuthentication;
import fr.xebia.usiquizz.core.game.AsyncGame;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import fr.xebia.usiquizz.core.xml.InvalidParameterFileException;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

public class JsonAnswerRestService extends RestService {

    private static Logger logger = LoggerFactory.getLogger(JsonAnswerRestService.class);

    private static final String SESSION_KEY = "session_key";

    private static final CookieDecoder cookieDecoder = new CookieDecoder();

    public JsonAnswerRestService(Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
    }

    @Override
    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        logger.debug("REST call for path {} ", path);
        logger.trace("Message : {}", e.getMessage().toString());
        HttpRequest request = (HttpRequest) e.getMessage();
        logger.debug("Parameters : {}", new String(request.getContent().array()));
        try {

            // currentQuestion
            byte questionNbr = Byte.parseByte(path.substring(path.lastIndexOf("/") + 1));
            // Get session_key
            String sessionKey = null;
            String cookieString = ((HttpRequest) e.getMessage()).getHeader(COOKIE);
            if (cookieString != null) {

                Set<Cookie> cookies = cookieDecoder.decode(cookieString);
                if (!cookies.isEmpty()) {
                    for (Cookie c : cookies) {
                        if (c.getName().equals(SESSION_KEY)) {
                            sessionKey = c.getValue();
                        }
                    }
                }
            }
            if (sessionKey == null) {
                logger.info("Player with no cookies... Rejected");
                responseWriter.writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                return;
            }

            // Verifie que l'on est encore dans la bonne fenetre de réponse.
            if (!game.isPlayerCanAnswer(sessionKey, questionNbr)) {
                logger.info("Player {} outside windows answer of question {}", sessionKey, questionNbr);
                responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                return;
            }

            // Verifie que le joueur n'a pas deja répondu.
            if (scoring.isPlayerAlreadyAnswered(sessionKey, questionNbr)) {
                logger.info("Player {} has already answer question {}", sessionKey, questionNbr);
                responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                return;
            }

            String answer = null;
            JsonParser jp = jsonFactory.createJsonParser(request.getContent().array());
            jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
                if ("answer".equals(fieldname)) { // contains an object
                    answer = jp.getText();
                } else {
                    responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                    logger.info("Unrecognized field '" + fieldname + "'!");
                    return;
                }
            }

            int answerNumber = Integer.parseInt(answer);
            Question question = game.getQuestion(questionNbr);
            // Verify is answerd is correction
            boolean answerIsCorrect = question.getGoodchoice() == answerNumber;
            // update score
            scoring.addScore(sessionKey, answerIsCorrect, questionNbr);

            responseWriter.writeResponse(createJsonResponse(answerIsCorrect, question.getChoice().get(question.getGoodchoice()), 0), HttpResponseStatus.OK, ctx, e);
            return;
        } catch (Exception e3) {
            logger.error("Error ", e3);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        }
    }


    private String createJsonResponse(boolean isResponseGood, String goodAnswer, int currentScore) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"are_u_right\":\"");
        sb.append(isResponseGood);
        sb.append("\",\"good_answer\":\"");
        sb.append(goodAnswer);
        sb.append("\",\"score\":");
        sb.append(currentScore);
        sb.append("}");
        return sb.toString();
    }
}
