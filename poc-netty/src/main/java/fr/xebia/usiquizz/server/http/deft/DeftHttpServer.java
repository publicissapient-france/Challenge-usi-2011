package fr.xebia.usiquizz.server.http.deft;


import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.GameLocalInstance;
import fr.xebia.usiquizz.core.persistence.UserRepository;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import org.deftserver.io.IOLoop;
import org.deftserver.io.timeout.Timeout;
import org.deftserver.web.Application;
import org.deftserver.web.AsyncCallback;
import org.deftserver.web.HttpServer;
import org.deftserver.web.handler.RequestHandler;
import org.deftserver.web.http.HttpServerDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DeftHttpServer {

	private static final Logger LOG = LoggerFactory.getLogger("deft-http");
	
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        Game game = new GameLocalInstance();
        GameParameterParser gameParameterParser = new GameParameterParser();

        Map<String, RequestHandler> handlers = new HashMap<String, RequestHandler>();

        handlers.put("/api/user", new UserResourceRequestHandler(userRepository));
        handlers.put("/api/login", new LoginResourceRequestHandler(userRepository, game));
        handlers.put("/api/game", new GameResourceRequestHandler(gameParameterParser, game));
        handlers.put("/api/question/([0-9]+)", new QuestionResourceRequestHandler(game));
        handlers.put("/", new StaticResourceRequestHandler());
        
        HttpServerDescriptor.KEEP_ALIVE_TIMEOUT = 600000;
		HttpServerDescriptor.READ_BUFFER_SIZE = 1500;			// 1500 bytes 
		HttpServerDescriptor.WRITE_BUFFER_SIZE = 1500;			// 1500 bytes 
        HttpServer server = new HttpServer(new Application(handlers));
        server.listen(8080);
        IOLoop.INSTANCE.start();
     
    }
}
