package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.RegionEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerEndingGameListener extends CacheListenerAdapter<String, String> {

    private DistributedGame game;

    private AtomicBoolean noUser = new AtomicBoolean(true);

    private static Logger logger = LoggerFactory.getLogger(PlayerEndingGameListener.class);

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
                            } else {
                                logger.warn("No tweet, only {} end the game on {}", game.countUserEndingGame(), game.countUserConnected());
                            }
                            // Very BAD PRACTICE.... but avoid full gc during game....
                            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {

                                        @Override
                                        public void run() {
                                            System.gc();
                                        }
                                    }, 5, TimeUnit.SECONDS);
                        }
                    }, 80, TimeUnit.SECONDS);


        }

        //if (game.countUserEndingGame() == game.countUserConnected()) {
        //    game.tweetResult();
        //}

    }

    @Override
    public void afterRegionClear(RegionEvent<String, String> event) {
        noUser.set(true);
    }

    public void init() {
        noUser.set(true);
    }
}
