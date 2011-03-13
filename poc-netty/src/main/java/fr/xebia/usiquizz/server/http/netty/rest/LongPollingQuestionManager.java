package fr.xebia.usiquizz.server.http.netty.rest;

import fr.xebia.usiquizz.core.game.AsyncGame;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.QuestionLongpollingCallback;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class LongPollingQuestionManager implements QuestionLongpollingCallback {

    private static final Logger logger = LoggerFactory.getLogger(LongPollingQuestionManager.class);

    private Map<String, ChannelHandlerContext> longPollingResponse = new ConcurrentHashMap<String, ChannelHandlerContext>();

    private Game game;

    private ResponseWriter responseWriter;

    private ExecutorService executorService;

    public LongPollingQuestionManager(Game game, ResponseWriter responseWriter) {
        this.game = game;
        this.responseWriter = responseWriter;
        this.executorService = executorService;
        this.game.registerLongpollingCallback(this);
    }

    public void addPlayer(String sessionKey, ChannelHandlerContext ctx, byte questionNbr) {
        longPollingResponse.put(sessionKey, ctx);
        game.addPlayerForQuestion(sessionKey, questionNbr);
    }

    public void sendQuestionToAllPlayer() {
        logger.info("Send all question to player");
        final String question = game.getQuestion(game.getCurrentQuestionIndex()).getLabel();
        for (final String sessionKey : longPollingResponse.keySet()) {
            responseWriter.endWritingResponseWithoutClose(question, longPollingResponse.get(sessionKey));
        }
        //longPollingResponse.clear();
    }

    @Override
    public void startSendAll() {
        sendQuestionToAllPlayer();
    }
}
