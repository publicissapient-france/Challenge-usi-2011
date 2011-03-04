package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.persistence.UserRepository;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class RestRequestHandler {

    private static final String USER_REST_SERVICE = "user";
    private static final String LOGIN_REST_SERVICE = "login";
    private static final String GAME_REST_SERVICE = "game";
    private static final String QUESTION_REST_SERVICE = "question";
    private static final String ANSWER_REST_SERVICE = "answer";

    private static final String PATH_SEPARATOR = "/";

    private Map<String, RestService> restMapping = new HashMap<String, RestService>();
    private UserRepository userRepository;
    private Game game;
    private GameParameterParser gameParameterParser = new GameParameterParser();


    public RestRequestHandler(UserRepository userRepository, Game game, LongPollingQuestionManager longPollingQuestionManager) {
        this.userRepository = userRepository;
        this.game = game;
        // Create all reste resources
        restMapping.put(USER_REST_SERVICE, new JsonUserRestService(this.userRepository));
        restMapping.put(LOGIN_REST_SERVICE, new JsonLoginRestService(this.userRepository, game));
        restMapping.put(GAME_REST_SERVICE, new JsonGameRestService(gameParameterParser, game));
        restMapping.put(QUESTION_REST_SERVICE, new JsonQuestionRestService(this.userRepository, game, longPollingQuestionManager));
        restMapping.put(ANSWER_REST_SERVICE, new JsonAnswerRestService(game));
    }

    public void messageReceived(String path, ChannelHandlerContext ctx, MessageEvent e) {
        HttpRequest request = (HttpRequest) e.getMessage();
        // Define rest service to use
        String serviceToUse = path;
        if (path.indexOf(PATH_SEPARATOR) > 0) {
            serviceToUse = path.substring(0, path.indexOf(PATH_SEPARATOR));
        }
        if (request.getMethod().equals(HttpMethod.GET)) {
            restMapping.get(serviceToUse).get(path, ctx, e);
        }
        else if (request.getMethod().equals(HttpMethod.POST)) {
            restMapping.get(serviceToUse).post(path, ctx, e);
        }
        else if (request.getMethod().equals(HttpMethod.PUT)) {
            //restMapping.get("user").get(path, e);
        }
        else if (request.getMethod().equals(HttpMethod.DELETE)) {
            //   restMapping.get("user").get(path, e);
        }
    }
}
