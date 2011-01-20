package fr.xebia.usiquizz.server.http.rest;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.persistence.UserRepository;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

public class JsonQuestionRestService extends RestService {

    private static final Logger logger = LoggerFactory.getLogger(JsonQuestionRestService.class);

    private Game game;

    private Map<String, ChannelHandlerContext> longPollingResponse = new ConcurrentHashMap<String, ChannelHandlerContext>();

    public JsonQuestionRestService(UserRepository userRepository, Game game) {
        this.game = game;
    }

    @Override
    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        HttpRequest request = (HttpRequest) e.getMessage();
        if (game.isGameStarted()) {
            writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        }
        // Get session_key
        String sessionKey = null;
        String cookieString = ((HttpRequest) e.getMessage()).getHeader(COOKIE);
        if (cookieString != null) {
            CookieDecoder cookieDecoder = new CookieDecoder();
            Set<Cookie> cookies = cookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                for (Cookie c : cookies) {
                    if (c.getName().equals("session_key")) {
                        sessionKey = c.getValue();
                    }
                }
            }
        }
        if (sessionKey == null) {
            logger.info("Player with no cookies... Rejected");
            writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
            return;
        }
        else {
            writeResponseWithoutClose(HttpResponseStatus.OK, ctx, e);
        }
        longPollingResponse.put(sessionKey, ctx);
        if (game.incrementPlayer()) {
            System.out.println(game.getUserConnected() + " player connected");
            game.startGame();
            startQuizz();
        }
        return;
    }

    private void startQuizz() {
        for (String sessionKey : longPollingResponse.keySet()) {
            endWritingResponseWithoutClose("{\n" +
                    "\"question\":\"La question 1\",\n" +
                    "\"answer_1\":\"La 1ere reponse à la question1\",\n" +
                    "\"answer_2\":\"La 2nd reponse à la question1\",\n" +
                    "\"answer_3\":\"La 3eme reponse à la question1\",\n" +
                    "\"answer_4\":\"La 4eme reponse à la question1\",\n" +
                    "\"score\":0\n" +
                    "} ", longPollingResponse.get(sessionKey));
        }
    }
}
