package fr.xebia.usiquizz.server.http.rest;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.persistence.User;
import fr.xebia.usiquizz.core.persistence.UserRepository;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonLoginRestService extends RestService {

    private static Logger logger = LoggerFactory.getLogger(JsonLoginRestService.class);

    private UserRepository userRepository;

    private Game game;

    public JsonLoginRestService(UserRepository userRepository, Game game) {
        this.userRepository = userRepository;
        this.game = game;
    }

    @Override
    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        logger.debug("REST call for path " + path);
        logger.trace("Message : " + e.getMessage().toString());
        HttpRequest request = (HttpRequest) e.getMessage();
        if (game.isGameStarted()) {
            writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        }
        try {
            String mail = null;
            String password = null;
            JsonParser jp = jsonFactory.createJsonParser(request.getContent().array());
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
                    writeResponse("Logged", HttpResponseStatus.OK, ctx, e, true);
                    return;
                }
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        // ERROR
        writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
    }

}
