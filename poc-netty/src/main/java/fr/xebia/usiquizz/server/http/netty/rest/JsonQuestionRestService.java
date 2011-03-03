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
                writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                return;
            }

            // Verify question asked... is active
            int questionNbr = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
            if (game.getCurrentQuestionIndex() != questionNbr) {
                // Bad player flow
                writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                return;
            }

            writeResponseWithoutClose(HttpResponseStatus.OK, ctx, e);
            longPollingResponse.put(sessionKey, ctx);
            game.addPlayerForCurrentQuestion(sessionKey);
            // FIXME Change with a callback from game instance..
            // FIXME cannot work in distributed
            if (game.allPlayerReadyForQuestion()) {
                logger.info(game.countUserForCurrentQuestion() + " player ready for question " + questionNbr);
                //game.startGame();
                sendQuestions(game.getCurrentQuestionIndex());
                //All question sended
                // Empty long pooling map
                longPollingResponse.clear();
                game.emptyCurrentQuestion();
                game.setCurrentQuestionIndex(game.getCurrentQuestionIndex() + 1);
            } else {
                if (game.countUserForCurrentQuestion() % 100 == 0) {
                    logger.info(game.countUserForCurrentQuestion() + " player request question " + questionNbr);
                }
            }

        } catch (Exception exc) {
            logger.error("error during question rest service", exc);
            writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        }
        return;

    }

    private void sendQuestions(int questionNumber) {
        for (String sessionKey : longPollingResponse.keySet()) {
            endWritingResponseWithoutClose(game.getQuestion(questionNumber).getLabel(), longPollingResponse.get(sessionKey));
        }
    }
}
