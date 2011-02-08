package fr.xebia.usiquizz.server.http.deft;

import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.xml.GameParameterParser;
import fr.xebia.usiquizz.core.xml.InvalidParameterFileException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GameResourceRequestHandler extends RestHandler {
    private static Logger logger = LoggerFactory.getLogger(GameResourceRequestHandler.class);


    private GameParameterParser gameParameterParser;

    private Game game;


    public GameResourceRequestHandler(GameParameterParser gameParameterParser, Game game) {
        this.game = game;
        this.gameParameterParser = gameParameterParser;
    }

    @Override
    public void post(HttpRequest request, HttpResponse response) {
        logger.debug("REST call for path " + request.getRequestedPath());
        logger.trace("Message : " + request.getBody());
        try {
            String authenticationKey = null;
            String parameters = null;
            JsonParser jp = jsonFactory.createJsonParser(request.getBody());
            jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
                if ("authentication_key".equals(fieldname)) { // contains an object
                    authenticationKey = jp.getText();
                }
                else if ("parameters".equals(fieldname)) {
                    parameters = jp.getText();
                }
                else {
                    response.setStatusCode(400);
                    logger.error("Unrecognized field '" + fieldname + "'!");
                    return;
                }
            }
            if (authenticationKey != null && parameters != null) {
                Sessiontype sessionType = gameParameterParser.parseXmlParameter(parameters);
                // FIXME Fill the mock for the moment
                game.init(sessionType);
                response.setStatusCode(200);
                return;
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
            response.setStatusCode(400);
        }
        catch (InvalidParameterFileException e2) {
            e2.printStackTrace();
            response.setStatusCode(400);
        }
        // ERROR
        response.setStatusCode(400);
    }
}
