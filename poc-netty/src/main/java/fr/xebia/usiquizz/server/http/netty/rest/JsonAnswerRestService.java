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
                responseWriter.writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
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
                    logger.error("Unrecognized field '" + fieldname + "'!");
                    return;
                }
            }

            // FIXME
            // Create real service and finish 
            int answerNumber = Integer.parseInt(answer);
            Question question = game.getQuestion(1);
            responseWriter.writeResponse(createJsonResponse(answerNumber == question.getGoodchoice(), question.getChoice().get(question.getGoodchoice() - 1), 0), HttpResponseStatus.OK, ctx, e);
            return;
        } catch (IOException e1) {
            logger.error("Error ", e1);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        } catch (InvalidParameterFileException e2) {
            logger.error("Error ", e2);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
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
