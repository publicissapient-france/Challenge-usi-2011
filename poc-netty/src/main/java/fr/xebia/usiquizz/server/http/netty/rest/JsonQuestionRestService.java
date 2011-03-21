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

    private LongPollingQuestionManager longPollingQuestionManager;

    public JsonQuestionRestService(UserRepository userRepository, Game game, LongPollingQuestionManager longPollingQuestionManager) {
        this.game = game;
        this.longPollingQuestionManager = longPollingQuestionManager;
    }

    @Override
    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        try {
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
                responseWriter.writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                return;
            }

            // Verify question asked... is active
            int questionNbr = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
            if (game.getCurrentQuestionIndex() != questionNbr) {
                // Bad player flow
                responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                return;
            }

            responseWriter.writeResponseWithoutClose(HttpResponseStatus.OK, ctx, e);
            longPollingQuestionManager.addPlayer(sessionKey, ctx, questionNbr);
        } catch (Exception exc) {
            logger.error("error during question rest service", exc);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        }
        return;

    }
}
