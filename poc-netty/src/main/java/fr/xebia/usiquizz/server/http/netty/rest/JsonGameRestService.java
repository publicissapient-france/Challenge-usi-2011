
package fr.xebia.usiquizz.server.http.netty.rest;

import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.authentication.AdminAuthentication;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import fr.xebia.usiquizz.core.xml.InvalidParameterFileException;
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

public class JsonGameRestService extends RestService {

    private static Logger logger = LoggerFactory.getLogger(JsonGameRestService.class);

    private GameParameterParser gameParameterParser;

    public JsonGameRestService(GameParameterParser gameParameterParser, Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
        this.gameParameterParser = gameParameterParser;
    }

    @Override
    public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {
        logger.debug("REST call for path {} ", path);
        logger.trace("Message : {}", e.getMessage().toString());
        HttpRequest request = (HttpRequest) e.getMessage();
        logger.info("Parameters : {}", new String(request.getContent().array()));
        try {
            String authenticationKey = null;
            String parameters = null;
            JsonParser jp = jsonFactory.createJsonParser(request.getContent().array());
            jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
                if ("authentication_key".equals(fieldname)) { // contains an object
                    authenticationKey = jp.getText();
                } else if ("parameters".equals(fieldname)) {
                    parameters = jp.getText();
                } else {
                    responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
                    logger.error("Unrecognized field '" + fieldname + "'!");
                    return;
                }
            }
            if (authenticationKey != null && parameters != null) {
                if (!authenticationKey.equals(AdminAuthentication.key)) {
                    logger.info("User with bad authentication ");
                    responseWriter.writeResponse(HttpResponseStatus.UNAUTHORIZED, ctx, e);
                    return;
                }
                Sessiontype sessionType = gameParameterParser.parseXmlParameter(parameters);
                game.init(sessionType);
                responseWriter.writeResponse(HttpResponseStatus.CREATED, ctx, e);
                return;
            }
        } catch (IOException e1) {
            logger.error("Error ", e1);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        } catch (InvalidParameterFileException e2) {
            logger.error("Error ", e2);
            responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
            return;
        }
        // ERROR
        responseWriter.writeResponse(HttpResponseStatus.BAD_REQUEST, ctx, e);
    }
}
