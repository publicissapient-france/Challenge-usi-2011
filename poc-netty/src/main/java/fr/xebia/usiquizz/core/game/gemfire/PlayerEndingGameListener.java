package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;

public class PlayerEndingGameListener extends CacheListenerAdapter<String, String> {

    private DistributedGame game;

    public PlayerEndingGameListener(DistributedGame distributedGame) {
        this.game = distributedGame;
    }

    @Override
    public void afterCreate(EntryEvent<String, String> event) {
        if (game.countUserEndingGame() == game.countUserConnected()) {
            game.tweetResult();
        }

    }
}
