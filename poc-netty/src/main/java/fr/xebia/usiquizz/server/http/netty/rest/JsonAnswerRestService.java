package fr.xebia.usiquizz.server.http.netty.rest;

import com.usi.Question;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.server.http.netty.FastSessionKeyCookieDecoder;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

public class JsonAnswerRestService extends RestService {

    private static Logger logger = LoggerFactory.getLogger(JsonAnswerRestService.class);

    private static final String SESSION_KEY = "session_key";

    private static final String JSON_ANSWER_ATTRIBUTE = "answer";

    private static final CookieDecoder cookieDecoder = new CookieDecoder();

    private final Map<Byte, Question> questionLocalCache = new HashMap<Byte, Question>();

    public JsonAnswerRestService(Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
    }

    @Override
    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        HttpRequest request = (HttpRequest) e.getMessage();
        try {

            // currentQuestion
            String questionNbr = path.substring(path.lastIndexOf("/") + 1);
            Byte questionNbrByte = Byte.parseByte(questionNbr);
            // Get session_key
            String sessionKey = FastSessionKeyCookieDecoder.findSessionKey(request.getHeader(COOKIE));
                        
            if (sessionKey == null) {
                logger.info("Player with no cookies... Rejected");
                responseWriter.writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                return;
            }

            // Verifie que l'on est encore dans la bonne fenetre de réponse.
            if (!game.isPlayerCanAnswer(sessionKey, questionNbr)) {
                //logger.info("Player {} outside windows answer of question {}", sessionKey, questionNbr);
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
                if (JSON_ANSWER_ATTRIBUTE.equals(fieldname)) { // contains an object
                    answer = jp.getText();
                } else {
                    responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                    logger.info("Unrecognized field '" + fieldname + "'!");
                    return;
                }
            }

            byte answerNumber = Byte.parseByte(answer);
            Question question = getQuestion(questionNbrByte);
            // Verify is answerd is correction
            boolean answerIsCorrect = question.getGoodchoice() == answerNumber;
            // update score
            byte newScore = scoring.addScore(sessionKey, answerNumber, answerIsCorrect, questionNbrByte);
            responseWriter.writeResponse(AnswerJsonWriter.createJsonResponse(answerIsCorrect, question.getGoodAnswer(), newScore), HttpResponseStatus.OK, ctx, e, sessionKey);
            return;
        } catch (Exception e3) {
            logger.error("Error ", e3);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        }
    }

    private Question getQuestion(byte questionNbr) {
        Question q = questionLocalCache.get(questionNbr);
        if (q == null) {
            synchronized (this) {
                q = questionLocalCache.get(questionNbr);
                if (q == null) {
                    q = game.getQuestion(questionNbr);
                    questionLocalCache.put(questionNbr, q);
                }
            }
        }
        return q;
    }


    static class AnswerJsonWriter {

        private final static byte[] ARE_U_RIGHT_BA = "{\"are_u_right\":\"".getBytes();
        private final static byte[] GOOD_ANSWER_BA = "\",\"good_answer\":\"".getBytes();
        private final static byte[] SCORE_BA = "\",\"score\":".getBytes();
        private final static byte[] END_BA = "}".getBytes();
        private final static byte[] TRUE_BA = Boolean.TRUE.toString().getBytes();
        private final static byte[] FALSE_BA = Boolean.FALSE.toString().getBytes();


        private static ChannelBuffer createJsonResponse(boolean isResponseGood, byte[] goodAnswer, int currentScore) {

            if (isResponseGood) {
                return ChannelBuffers.wrappedBuffer(ARE_U_RIGHT_BA, TRUE_BA, GOOD_ANSWER_BA, goodAnswer, SCORE_BA, Integer.toString(currentScore).getBytes(), END_BA);
            } else {
                return ChannelBuffers.wrappedBuffer(ARE_U_RIGHT_BA, FALSE_BA, GOOD_ANSWER_BA, goodAnswer, SCORE_BA, Integer.toString(currentScore).getBytes(), END_BA);
            }

        }
    }
}