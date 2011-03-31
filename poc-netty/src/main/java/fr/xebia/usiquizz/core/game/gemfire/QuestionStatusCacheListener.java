package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.RegionEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import fr.xebia.usiquizz.core.game.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuestionStatusCacheListener extends CacheListenerAdapter<Byte, Byte> {

    private static final Logger logger = LoggerFactory.getLogger(QuestionStatusCacheListener.class);

    private Game game;
    private ExecutorService executorService;

    public QuestionStatusCacheListener(Game distributedGame, ExecutorService executorService) {
        this.game = distributedGame;
        this.executorService = executorService;
    }


    @Override
    public void afterUpdate(EntryEvent<Byte, Byte> entryEvent) {
        logger.info("Question {} status change : {} --> {}", new Object[]{entryEvent.getKey(), entryEvent.getOldValue(), entryEvent.getNewValue()});
        if (entryEvent.getNewValue() == QuestionStatus.QUESTION_EN_COURS && entryEvent.getOldValue() != QuestionStatus.QUESTION_EN_COURS) {
            // L'appel doit être asynchrone pour liberer les thread gemfire
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    game.startCurrentLongPolling();
                }
            });
        }
    }

}
