package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.game.AsyncGame;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.core.persistence.UserRepository;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class RestRequestHandler {

    private static final String USER_REST_SERVICE = "user";
    private static final String LOGIN_REST_SERVICE = "login";
    private static final String GAME_REST_SERVICE = "game";
    private static final String QUESTION_REST_SERVICE = "question";
    private static final String ANSWER_REST_SERVICE = "answer";
    private static final String PARAMETER_REST_SERVICE = "parameter";
    private static final String RANKING_REST_SERVICE = "ranking";
    private static final String SCORE_REST_SERVICE = "score";
    private static final String LIST_USER_REST_SERVICE = "player-list";
    private static final String AUDIT_REST_SERVICE = "audit";
    private static final String USER_LIST_REST_SERVICE = "user-list";
    private static final String HEARTBEAT_REST_SERVICE = "ok";

    private static final String PATH_SEPARATOR = "/";
    private static final String QUERY_SEPARATOR = "?";

    private Map<String, RestService> restMapping = new HashMap<String, RestService>();
    private UserRepository userRepository;
    private Game game;
    private Scoring scoring;
    private GameParameterParser gameParameterParser = new GameParameterParser();


    public RestRequestHandler(UserRepository userRepository, Game game, Scoring scoring, LongPollingQuestionManager longPollingQuestionManager, ExecutorService executorService) {
        this.userRepository = userRepository;
        this.game = game;
        this.scoring = scoring;
        // Create all reste resources
        restMapping.put(USER_REST_SERVICE, new JsonUserRestService(this.userRepository, game, scoring, executorService));
        restMapping.put(LOGIN_REST_SERVICE, new JsonLoginRestService(this.userRepository, game, scoring, executorService));
        restMapping.put(GAME_REST_SERVICE, new JsonGameRestService(gameParameterParser, game, scoring, executorService));
        restMapping.put(QUESTION_REST_SERVICE, new JsonQuestionRestService(this.userRepository, longPollingQuestionManager, game, scoring, executorService));
        restMapping.put(ANSWER_REST_SERVICE, new JsonAnswerRestService(game, scoring, executorService));
        restMapping.put(PARAMETER_REST_SERVICE, new JsonParameterRestService(game, scoring, executorService));
        restMapping.put(RANKING_REST_SERVICE, new JsonRankingRestService(game, scoring, executorService));
        restMapping.put(SCORE_REST_SERVICE, new JsonScoreRestService(game, scoring, executorService));
        restMapping.put(LIST_USER_REST_SERVICE, new JsonPlayerListRestService(game, scoring, executorService));
        restMapping.put(AUDIT_REST_SERVICE, new JsonAuditAnswerRestService(game, scoring, executorService));
        restMapping.put(USER_LIST_REST_SERVICE, new JsonUserListRestService(game, scoring, executorService));
        restMapping.put(HEARTBEAT_REST_SERVICE, new HeartbeatRestService(game, scoring, executorService));
    }

    public void messageReceived(String path, ChannelHandlerContext ctx, MessageEvent e) {
        HttpRequest request = (HttpRequest) e.getMessage();
        // Define rest service to use
        String serviceToUse = path;
        if (path.indexOf(PATH_SEPARATOR) > 0) {
            serviceToUse = path.substring(0, path.indexOf(PATH_SEPARATOR));
        } else if (path.indexOf(QUERY_SEPARATOR) > 0) {
            // Gestion du cas avec un ?
            serviceToUse = path.substring(0, path.indexOf(QUERY_SEPARATOR));
        }

        if (request.getMethod().equals(HttpMethod.GET)) {
            restMapping.get(serviceToUse).get(path, ctx, e);
        } else if (request.getMethod().equals(HttpMethod.POST)) {
            restMapping.get(serviceToUse).post(path, ctx, e);
        } else if (request.getMethod().equals(HttpMethod.PUT)) {
            //restMapping.get("user").get(path, e);
        } else if (request.getMethod().equals(HttpMethod.DELETE)) {
            //   restMapping.get("user").get(path, e);
        }
    }
}
