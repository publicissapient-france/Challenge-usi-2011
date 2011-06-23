package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import fr.xebia.usiquizz.core.game.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;


public class LoginCacheListener extends CacheListenerAdapter<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(QuestionStatusCacheListener.class);

    private Game game;
    private ExecutorService executorService;

    public LoginCacheListener(Game game, ExecutorService eventTaskExector) {
        this.executorService = eventTaskExector;
        this.game = game;
    }

}
