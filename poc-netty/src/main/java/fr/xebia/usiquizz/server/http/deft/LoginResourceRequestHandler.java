package fr.xebia.usiquizz.server.http.deft;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.persistence.User;
import fr.xebia.usiquizz.core.persistence.UserRepository;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.deftserver.util.HttpUtil;
import org.deftserver.web.handler.RequestHandler;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginResourceRequestHandler extends RestHandler {

    private static Logger logger = LoggerFactory.getLogger(LoginResourceRequestHandler.class);

    private UserRepository userRepository;

    private Game game;

    public LoginResourceRequestHandler(UserRepository userRepository, Game game) {
        this.userRepository = userRepository;
        this.game = game;
    }

    @Override
    public void post(HttpRequest request, HttpResponse response) {
        logger.debug("REST call for path " + request.getRequestedPath());
        logger.trace("Message : " + request.getBody());
        if (game.isGameStarted()) {
            response.setStatusCode(400);
            return;
        }
        try {
            String mail = null;
            String password = null;
            JsonParser jp = jsonFactory.createJsonParser(request.getBody());
            jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
                if ("mail".equals(fieldname)) { // contains an object
                    mail = jp.getText();
                }
                else if ("password".equals(fieldname)) {
                    password = jp.getText();
                }
                else {
                    throw new IllegalStateException("Unrecognized field '" + fieldname + "'!");
                }
            }
            if (mail != null && password != null) {
                User dbuser;
                if ((dbuser = userRepository.logUser(mail, password)) != null) {
                    initCookie(request, response);
                    response.setStatusCode(200);
                    response.write("logged");
                    return;
                }
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        // ERROR
        response.setStatusCode(400);
    }
}
