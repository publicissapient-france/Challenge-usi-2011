package fr.xebia.usiquizz.server.http.deft;

import fr.xebia.usiquizz.core.game.Game;

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

import java.util.Set;


public class QuestionResourceRequestHandler extends RestHandler {
    private static final Logger logger = LoggerFactory.getLogger(QuestionResourceRequestHandler.class);

    private Game game;

    private LongPollingManager longPollingManager = new LongPollingManager();

    public QuestionResourceRequestHandler(Game game) {
        this.game = game;
    }

    @Override
    @Asynchronous
    public void get(HttpRequest request, final HttpResponse response) {
//        if (game.isGameStarted()) {
//            response.setStatusCode(200);
//        }
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
        else {
            //response.setStatusCode(200);
            //response.write("");
            //response.flush();
        	
        	IOLoopFactory.getLoopController().planifyResponse();
        	final IOLoopController loop = IOLoopFactory.getLoopController();
            // Don't close response
            longPollingManager.add(new AsyncCallback() {
                @Override
                public void onCallback() {
                    writeResponse(response);
                    loop.pushResponse(response);
                }
            });
        }
        if (game.incrementPlayer()) {
            System.out.println(game.getUserConnected() + " player connected");
            game.startGame();
            startQuizz();
        }else{
            if(game.getUserConnected() % 100 == 0){
                System.out.println(game.getUserConnected() + " player connected");
            }
        }
        return;
    }

    private void startQuizz() {
        longPollingManager.sendAllResponse();
    }

    private void writeResponse(HttpResponse response) {
    	
        response.write("{\n" +
                "\"question\":\"La question 1\",\n" +
                "\"answer_1\":\"La 1ere reponse à la question1\",\n" +
                "\"answer_2\":\"La 2nd reponse à la question1\",\n" +
                "\"answer_3\":\"La 3eme reponse à la question1\",\n" +
                "\"answer_4\":\"La 4eme reponse à la question1\",\n" +
                "\"score\":0\n" +
                "} ");
   //     response.finish();
    }

}
