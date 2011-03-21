package fr.xebia.usiquizz.server.http.deft;

import java.util.Set;

import org.deftserver.io.IOLoopController;
import org.deftserver.io.IOLoopFactory;
import org.deftserver.web.AsyncCallback;
import org.deftserver.web.Asynchronous;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.xebia.usiquizz.core.game.Game;

public class QuestionResourceRequestHandler extends RestHandler {
    private static final Logger logger = LoggerFactory
            .getLogger(QuestionResourceRequestHandler.class);

    private Game game;

    private LongPollingManager longPollingManager = new LongPollingManager();

    public QuestionResourceRequestHandler(Game game) {
        this.game = game;
    }

    @Override
    @Asynchronous
    public void get(HttpRequest request, final HttpResponse response) {
        // if (game.isGameStarted()) {
        // response.setStatusCode(200);
        // }
        // Get session_key
        String sessionKey = null;
        String cookieString = request.getHeader("Cookie");
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
            response.setStatusCode(401);
            response.finish();
            return;
        }

        // Verify question asked... is active
        final int questionNbr = Integer.parseInt(request.getRequestedPath().substring(
                request.getRequestedPath().lastIndexOf("/") + 1));
        if (game.getCurrentQuestionIndex() != questionNbr) {
            // Bad player flow
            response.setStatusCode(400);
            response.write("Not current question !");
            return;
        }

        // response.setStatusCode(200);
        // response.write("");
        // response.flush();

        IOLoopFactory.getLoopController().planifyResponse();
        final IOLoopController loop = IOLoopFactory.getLoopController();
        // Don't close response
        longPollingManager.add(new AsyncCallback() {
            @Override
            public void onCallback() {
                writeResponse(response, questionNbr);
                loop.pushResponse(response);
            }
        });

        game.addPlayerForCurrentQuestion(sessionKey);

        if (game.allPlayerReadyForQuestion()) {
            System.out.println(game.countUserConnected() + " player connected");
            // game.startGame();
            startQuizz();
            longPollingManager.clear();
            game.emptyCurrentQuestion();
            game.setCurrentQuestionIndex(game.getCurrentQuestionIndex() + 1);
        } else {
            if (game.countUserForCurrentQuestion() % 100 == 0) {
                System.out.println(game.countUserConnected() + " player connected");
            }
        }
        return;
    }

    private void startQuizz() {
        longPollingManager.sendAllResponse();
    }

    private void writeResponse(HttpResponse response, int nb) {

        response.write(game.getQuestion(nb).getLabel());

        // response.finish();
    }

}
