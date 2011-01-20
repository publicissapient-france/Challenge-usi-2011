package fr.xebia.usiquizz.server.http.rest;

import com.usi.MockSessionType;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.GameLocalInstance;
import fr.xebia.usiquizz.core.persistence.UserRepository;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class RestRequestHandler {

    private Map<String, RestService> restMapping = new HashMap<String, RestService>();
    private UserRepository userRepository;
    private Game game = new GameLocalInstance();
    private GameParameterParser gameParameterParser = new GameParameterParser();

    public RestRequestHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
        // Create all reste resources
        restMapping.put("user", new JsonUserRestService(this.userRepository));
        restMapping.put("login", new JsonLoginRestService(this.userRepository, game));
        restMapping.put("game", new JsonGameRestService(gameParameterParser, game));
        restMapping.put("question", new JsonQuestionRestService(this.userRepository, game));
    }

    public void messageReceived(String path, ChannelHandlerContext ctx, MessageEvent e) {
        HttpRequest request = (HttpRequest) e.getMessage();
        // Define rest service to use
        String serviceToUse = path;
        if (path.indexOf("/") > 0) {
            serviceToUse = path.substring(0, path.indexOf("/"));
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
