package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerEndingGameListener extends CacheListenerAdapter<String, String> {

    private DistributedGame game;

    private AtomicBoolean noUser = new AtomicBoolean(true);

    public PlayerEndingGameListener(DistributedGame distributedGame) {
        this.game = distributedGame;
    }

    @Override
    public void afterCreate(EntryEvent<String, String> event) {
        // Si c'est le premier joueur on déclenche un timer d'une minutes + 10s...
        // Au bout d'une minute si plus de 80% des joueurs qui se sont loggué demande leur score on tweet un message
        if (noUser.compareAndSet(true, false)) {
            // start timers
            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {

                        @Override
                        public void run() {
                            if (game.countUserEndingGame() > (game.countUserConnected() * 0.8)) {
                                game.tweetResult();
                            }
                        }
                    }, 80, TimeUnit.SECONDS);
        }

        //if (game.countUserEndingGame() == game.countUserConnected()) {
        //    game.tweetResult();
        //}

    }

    public void init() {
        noUser.set(true);
    }
}
