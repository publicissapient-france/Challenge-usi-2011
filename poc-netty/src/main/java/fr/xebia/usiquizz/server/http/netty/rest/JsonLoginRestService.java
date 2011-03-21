package fr.xebia.usiquizz.server.http.netty.rest;

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
import java.util.UUID;

public class JsonLoginRestService extends RestService {

    private static final String JSON_MAIL = "mail";
    private static final String JSON_PASSWORD = "password";

    private static Logger logger = LoggerFactory.getLogger(JsonLoginRestService.class);

    private UserRepository userRepository;

    private Game game;

    public JsonLoginRestService(UserRepository userRepository, Game game) {
        this.userRepository = userRepository;
        this.game = game;
    }

    @Override
    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        logger.debug("REST call for path {}", path);
        logger.trace("Message : {}", e.getMessage().toString());
        HttpRequest request = (HttpRequest) e.getMessage();
        // FIXME
        //if (game.isGameStarted()) {
        //    writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        //    return;
        // }
        try {
            String mail = null;
            String password = null;
            JsonParser jp = jsonFactory.createJsonParser(request.getContent().array());
            jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
                if (JSON_MAIL.equals(fieldname)) { // contains an object
                    mail = jp.getText();
                } else if (JSON_PASSWORD.equals(fieldname)) {
                    password = jp.getText();
                } else {
                    throw new IllegalStateException("Unrecognized field '" + fieldname + "'!");
                }
            }
            if (mail != null && password != null) {
                if ((userRepository.logUser(mail, password))) {
                    String sessionKey = UUID.randomUUID().toString();
                    game.addPlayer(sessionKey, mail);
                    responseWriter.writeResponse(null, HttpResponseStatus.OK, ctx, e, sessionKey);
                    return;
                }
            }
        } catch (IOException e1) {
            logger.error("Error ", e1);
        }
        // ERROR
        responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
    }

}
