package fr.xebia.usiquizz.server.http.netty.rest;

import com.usi.Question;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.authentication.AdminAuthentication;
import fr.xebia.usiquizz.core.game.Game;
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

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

public class JsonAnswerRestService extends RestService {

    private static Logger logger = LoggerFactory.getLogger(JsonAnswerRestService.class);

    private static final String SESSION_KEY = "session_key";

    private Game game;

    public JsonAnswerRestService(Game game) {
        this.game = game;
    }

    @Override
    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        logger.debug("REST call for path {} ", path);
        logger.trace("Message : {}", e.getMessage().toString());
        HttpRequest request = (HttpRequest) e.getMessage();
        logger.debug("Parameters : {}", new String(request.getContent().array()));
        try {

            // Get session_key
            String sessionKey = null;
            String cookieString = ((HttpRequest) e.getMessage()).getHeader(COOKIE);
            if (cookieString != null) {
                CookieDecoder cookieDecoder = new CookieDecoder();
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
                writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                return;
            } else {
                writeResponseWithoutClose(HttpResponseStatus.OK, ctx, e);
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
                    writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                    logger.error("Unrecognized field '" + fieldname + "'!");
                    return;
                }
            }

            // FIXME
            // Create real service and finish 
            int answerNumber = Integer.parseInt(answer);
            Question question = game.getQuestion(0);
            writeResponse(createJsonResponse(answerNumber == question.getGoodchoice(), question.getChoice().get(question.getGoodchoice()), 0), HttpResponseStatus.OK, ctx, e, false);
        } catch (IOException e1) {
            e1.printStackTrace();
            writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        } catch (InvalidParameterFileException e2) {
            e2.printStackTrace();
            writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        } catch (Exception e3) {
            e3.printStackTrace();
            writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        }
        // ERROR
        writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
    }

    private String createJsonResponse(boolean isResponseGood, String goodAnswer, int currentScore) {
        StringTemplate jsonAnswer = new StringTemplate("{\"are_u_right\":\"$isResponseGood$\",\"good_answer\":\"$goodAnswer$\",\"score\":$score$}", DefaultTemplateLexer.class);
        jsonAnswer.setAttribute("isResponseGood", isResponseGood);
        jsonAnswer.setAttribute("goodAnswer", goodAnswer);
        jsonAnswer.setAttribute("score", currentScore + 1);
        return jsonAnswer.toString();
    }
}
