
package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.core.persistence.UserAlreadyExists;
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
import java.util.concurrent.ExecutorService;

public class JsonUserRestService extends RestService {

    private static final Logger logger = LoggerFactory.getLogger(JsonUserRestService.class);

    private static final String JSON_MAIL = "mail";
    private static final String JSON_PASSWORD = "password";
    private static final String JSON_FIRSTNAME = "firstname";
    private static final String JSON_LASTNAME = "lastname";

    private UserRepository userRepository;

    public JsonUserRestService(UserRepository userRepository, Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
        this.userRepository = userRepository;
    }

    @Override
    public void post(String path, ChannelHandlerContext ctx, MessageEvent event) {
        HttpRequest request = (HttpRequest) event.getMessage();
        String email = null;
        String password = null;
        String firstname = null;
        String lastname = null;

        JsonParser jp = null;
        try {
            jp = jsonFactory.createJsonParser(request.getContent().array());
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken();
                if (JSON_MAIL.equals(fieldname)) {
                    email = jp.getText();
                } else if (JSON_PASSWORD.equals(fieldname)) {
                    password = jp.getText();
                } else if (JSON_FIRSTNAME.equals(fieldname)) {
                    firstname = jp.getText();
                } else if (JSON_LASTNAME.equals(fieldname)) {
                    lastname = jp.getText();
                } else {
                    throw new IllegalStateException("Unrecognized field '" + fieldname + "'!");
                }
            }
            try {
                userRepository.insertUser(email, password, firstname, lastname);
            } catch (UserAlreadyExists uae) {
                responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, event);
            }
        } catch (IOException e1) {
            logger.error("Error ", e1);
        }

        responseWriter.writeResponse(HttpResponseStatus.OK, ctx, event);
    }
}
