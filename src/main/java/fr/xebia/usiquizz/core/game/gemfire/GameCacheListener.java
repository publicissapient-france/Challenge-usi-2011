package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import fr.xebia.usiquizz.core.persistence.GemfireAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Needed for reset local resource
 */
public class GameCacheListener extends CacheListenerAdapter<String, Object> {

    private static final Logger logger = LoggerFactory.getLogger(GameCacheListener.class);

    private DistributedGame game;

    public GameCacheListener(DistributedGame game) {
        this.game = game;
    }

    @Override
    public void afterCreate(EntryEvent<String, Object> event) {
        if (event.getKey().equals(GemfireAttribute.LOGIN_PHASE_STATUS)) {
            if (event.getNewValue().equals(GemfireAttribute.LOGIN_PHASE_NON_COMMENCER)) {
                logger.warn("New game reinit local resources");
                // Reinit d'un jeu
                game.resetLocalGameData();
            } else if (event.getNewValue().equals(GemfireAttribute.LOGIN_PHASE_EN_COURS)) {
                game.setLoginPhaseBegin();
            } else if (event.getNewValue().equals(GemfireAttribute.LOGIN_PHASE_TERMINER)) {
                game.setLoginPhaseEnded();
            }
        } else if (event.getKey().equals(GemfireAttribute.CURRENT_QUESTION_INDEX)) {
            game.setLocalCurrentQuestionIndex((String) event.getNewValue());
        } else if (event.getKey().equals(GemfireAttribute.CURRENT_ANSWER_INDEX)) {
            game.setLocalCurrentAnswerIndex((String) event.getNewValue());
        }
    }

    @Override
    public void afterUpdate(EntryEvent<String, Object> event) {
        if (event.getKey().equals(GemfireAttribute.LOGIN_PHASE_STATUS)) {
            if (event.getNewValue().equals(GemfireAttribute.LOGIN_PHASE_NON_COMMENCER)) {
                logger.warn("New game reinit local resources");
                // Reinit d'un jeu
                game.resetLocalGameData();
            } else if (event.getNewValue().equals(GemfireAttribute.LOGIN_PHASE_EN_COURS)) {
                game.setLoginPhaseBegin();
            } else if (event.getNewValue().equals(GemfireAttribute.LOGIN_PHASE_TERMINER)) {
                game.setLoginPhaseEnded();
            }
        } else if (event.getKey().equals(GemfireAttribute.CURRENT_QUESTION_INDEX)) {
            game.setLocalCurrentQuestionIndex((String) event.getNewValue());
        } else if (event.getKey().equals(GemfireAttribute.CURRENT_ANSWER_INDEX)) {
            game.setLocalCurrentAnswerIndex((String) event.getNewValue());
        }
    }
}
