package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.Scoring;
import org.antlr.stringtemplate.StringTemplate;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

//Non requis par le challenge permet au client web de récupérer les param de conf
public class JsonParameterRestService extends RestService {

    private static Logger logger = LoggerFactory.getLogger(JsonGameRestService.class);


    protected JsonParameterRestService(Game game, Scoring scoring, ExecutorService executorService) {
        super(game, scoring, executorService);
    }

    @Override
    public void get(String path, ChannelHandlerContext ctx, MessageEvent e) {
        game.getQuestiontimeframe();
        responseWriter.writeResponse(constructJsonResponse(game.getNbquestions(), game.getQuestiontimeframe(), game.getSynchrotime()), HttpResponseStatus.OK, ctx, e);
    }

    private String constructJsonResponse(int nbQuestion, int questionTimeFrame, int synchrotime) {
        StringTemplate st = new StringTemplate("{ \n" +
                "\"nbQuestion\" : \"$nbQuestion$\", \n" +
                "\"questionTimeFrame\" : \"$questionTimeFrame$\", \n" +
                "\"synchrotime\" : \"$synchrotime$\",\n" +
                "}");
        st.setAttribute("nbQuestion", nbQuestion);
        st.setAttribute("questionTimeFrame", questionTimeFrame);
        st.setAttribute("synchrotime", synchrotime);
        return st.toString();
    }
}
