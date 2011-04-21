package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.core.game.exception.LoginPhaseEndedException;
import fr.xebia.usiquizz.core.persistence.User;
import fr.xebia.usiquizz.core.persistence.UserRepository;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonLoginRestService extends RestService {

    private static final String JSON_MAIL = "mail";
    private static final String JSON_PASSWORD = "password";

    private Pattern pattern = Pattern.compile("\\{\"mail\".*\"(.*)\".*\"password\".*\"(.*)\"\\}");


    private static Logger logger = LoggerFactory.getLogger(JsonLoginRestService.class);

    private UserRepository userRepository;

    public JsonLoginRestService(UserRepository userRepository, Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
        this.userRepository = userRepository;
    }

    @Override
    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        HttpRequest request = (HttpRequest) e.getMessage();
        // FIXME
        //if (game.isGameStarted()) {
        //    writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
        //    return;
        // }
        try {
            String mail = null;
            String password = null;
            /*
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
                    //throw new IllegalStateException("Unrecognized field '" + fieldname + "'!");
                    logger.info("Bad field : {}, login refused", fieldname);
                    responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                    return;
                }
            }
            */

            Matcher m = pattern.matcher(new String(request.getContent().array()));
            if (m.matches()) {
                mail = m.group(1);
                password = m.group(2);
            } else {
                //throw new IllegalStateException("Unrecognized field '" + fieldname + "'!");
                logger.info("json not valid : {}, login refused", new String(request.getContent().array()));
                responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                return;
            }

            String sessionKey = Integer.toString(mail.hashCode());

            if (mail != null && !mail.isEmpty() && password != null && !password.isEmpty()) {
                User user = userRepository.logUser(mail, password);
                if (user != null) {

                    if (game.isAlreadyLogged(sessionKey)) {
                        logger.info("user {} already logged with sessionKey : {}", mail, sessionKey);
                        responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                        return;
                    }
                    // Add player as logged
                    try {
                        game.logPlayerToApplication(sessionKey, mail);
                    } catch (LoginPhaseEndedException e1) {
                        // Trop tard pour se logguer ...
                        responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                        return;
                    }
                    // Add a score object to player
                    scoring.createScore(sessionKey, user);

                    responseWriter.writeResponse(null, HttpResponseStatus.CREATED, ctx, e, sessionKey);
                    return;
                } else {
                    logger.warn("Authentication refused {}", mail);
                    responseWriter.writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                    return;
                }
            }
        } catch (Exception e1) {
            logger.error("Error ", e1);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        }
        // ERROR
        logger.error("Flow not supported");
        responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
    }

}
