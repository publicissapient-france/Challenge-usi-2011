package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import fr.xebia.usiquizz.core.game.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static fr.xebia.usiquizz.core.persistence.GemfireRepository.CURRENT_QUESTION_INDEX;

public class LoginCacheListener extends CacheListenerAdapter<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(QuestionStatusCacheListener.class);

    private Game game;
    private ExecutorService executorService;

    public LoginCacheListener(Game game, ExecutorService eventTaskExector) {
        this.executorService = eventTaskExector;
        this.game = game;
    }

    @Override
    public void afterCreate(EntryEvent<String, String> event) {
        if (game.isAllPlayerLogged()) {
            // FIXME BUG... C'est quand tous les joueurs sont loggué et qu'ils ont demandé la qestion que l'on peut envoyer les questions
            //game.startGame();
        }
    }
}