package fr.xebia.usiquizz.server.http.netty.rest;

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

    private static final String SESSION_KEY = "session_key";

    private Game game;

    private Map<String, ChannelHandlerContext> longPollingResponse = new ConcurrentHashMap<String, ChannelHandlerContext>();

    public JsonQuestionRestService(UserRepository userRepository, Game game) {
        this.game = game;
    }

    @Override
    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        HttpRequest request = (HttpRequest) e.getMessage();

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
        longPollingResponse.put(sessionKey, ctx);
        game.addPlayer(sessionKey);
        if (game.getUserConnected() >= game.getNbusersthresold()) {
            System.out.println(game.getUserConnected() + " player connected");
            //game.startGame();
            startQuizz();
        } else {
            if (game.getUserConnected() % 100 == 0) {
                System.out.println(game.getUserConnected() + " player connected");
            }
        }
        return;
    }

    private void startQuizz() {
        for (String sessionKey : longPollingResponse.keySet()) {
            endWritingResponseWithoutClose(game.getQuestion(0).getLabel(), longPollingResponse.get(sessionKey));
        }
    }
}
