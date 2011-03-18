package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.RegionEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import fr.xebia.usiquizz.core.game.Game;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuestionStatusCacheListener extends CacheListenerAdapter<Byte, Byte> {
    private Game game;
    private ExecutorService executorService;

    public QuestionStatusCacheListener(Game distributedGame, ExecutorService executorService) {
        this.game = distributedGame;
        this.executorService = executorService;
    }


    @Override
    public void afterCreate(EntryEvent<Byte, Byte> entryEvent) {
        if (entryEvent.getNewValue() == QuestionStatus.QUESTION_EN_COURS) {
            // L'appel doit être asynchrone pour liberer les thread gemfire
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    game.startCurrentLongPolling();
                }
            });
        }
    }

    @Override
    public void afterUpdate(EntryEvent<Byte, Byte> entryEvent) {
        if (entryEvent.getNewValue() == QuestionStatus.QUESTION_EN_COURS) {
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
