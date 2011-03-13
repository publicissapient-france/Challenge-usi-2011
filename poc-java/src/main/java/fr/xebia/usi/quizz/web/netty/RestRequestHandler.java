package fr.xebia.usi.quizz.web.netty;


import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

import fr.xebia.usi.quizz.web.netty.rest.RestService;
import fr.xebia.usi.quizz.web.netty.rest.UserRestService;
import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.JsonMapperImpl;
import fr.xebia.usi.quizz.service.UserManager;
import fr.xebia.usi.quizz.service.UserManagerMongoImpl;

import java.util.HashMap;
import java.util.Map;

public class RestRequestHandler {

    private Map<String, RestService> restMapping = new HashMap<String, RestService>();
	private final UserManager manager;
	private final JsonMapper mapper;

    public RestRequestHandler() {
    	mapper = new JsonMapperImpl();
    	manager = new UserManagerMongoImpl();
        // Create all reste resources
        restMapping.put("user", new UserRestService(manager, mapper));
//        restMapping.put("login", new JsonLoginRestService(this.userRepository, game));
//        restMapping.put("game", new JsonGameRestService(gameParameterParser, game));
//        restMapping.put("question", new JsonQuestionRestService(this.userRepository, game));
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
