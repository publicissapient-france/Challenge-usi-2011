package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import fr.xebia.usiquizz.core.game.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public class CurrentQuestionCacheListener extends CacheListenerAdapter<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(QuestionStatusCacheListener.class);

    private Game game;
    private ExecutorService executorService;

    public CurrentQuestionCacheListener(Game game, ExecutorService eventTaskExector) {
        this.executorService = eventTaskExector;
        this.game = game;
    }

    @Override
    public void afterCreate(EntryEvent<String, String> event) {
        if (game.getCurrentQuestionIndex().equals("1") && game.countUserForCurrentQuestion() == game.countUserConnected()) {
            logger.info("All player asked question 1 ---> Start Game");
            game.startGame();
        }
    }
}
