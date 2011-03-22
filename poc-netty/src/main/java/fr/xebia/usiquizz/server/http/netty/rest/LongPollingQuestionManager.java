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
import java.util.concurrent.atomic.AtomicBoolean;

public class LongPollingQuestionManager implements QuestionLongpollingCallback {

    private static final Logger logger = LoggerFactory.getLogger(LongPollingQuestionManager.class);

    private Map<String, ChannelHandlerContext> longPollingResponse = new ConcurrentHashMap<String, ChannelHandlerContext>();

    private Game game;

    private ResponseWriter responseWriter;

    private ExecutorService executorService;

    private AtomicBoolean sendQuestionStarted = new AtomicBoolean(false);

    private byte currentQuestionIndex = 1;

    public LongPollingQuestionManager(Game game, ResponseWriter responseWriter, ExecutorService executorService) {
        this.game = game;
        this.responseWriter = responseWriter;
        this.executorService = executorService;
        this.game.registerLongpollingCallback(this);
    }

    public void initNewQuestion(byte currentQuestionIndex) {
        this.longPollingResponse.clear();
        this.currentQuestionIndex = currentQuestionIndex;
        sendQuestionStarted.set(false);
    }

    public void addPlayer(String sessionKey, ChannelHandlerContext ctx, byte questionNbr) {
        longPollingResponse.put(sessionKey, ctx);
        game.addPlayerForQuestion(sessionKey, questionNbr);
    }

    public void sendQuestionToAllPlayer() {
        if (!sendQuestionStarted.getAndSet(true)) {
            logger.info("Send all question to player");

            // On incrémente l'index de question attendu
            game.setCurrentQuestionIndex((byte) (currentQuestionIndex + 1));

            // On demande la réinitialisation
            game.resetPlayerAskedQuestion();

            for (final String sessionKey : longPollingResponse.keySet()) {
                //executorService.submit(new Runnable() {
                //    @Override
                //    public void run() {
                responseWriter.endWritingResponseWithoutClose(game.createQuestionInJson(currentQuestionIndex, sessionKey), sessionKey, longPollingResponse.get(sessionKey));
                //    }
                //});

            }
            // On démarre un timer locale...
            // On le démarre apres avoir envoyer toute les questions histoire que tous les clients est au moins questiontimeframe pour répondre.
            // Et ca nous arrange....
            game.startQuestionTimeframe(currentQuestionIndex);
        }
        //longPollingResponse.clear();
    }

    @Override
    public void startSendAll() {
        sendQuestionToAllPlayer();
    }
}
