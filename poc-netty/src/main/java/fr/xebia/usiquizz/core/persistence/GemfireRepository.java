package fr.xebia.usiquizz.core.persistence;

import static fr.xebia.usiquizz.core.persistence.GemfireAttribute.*;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.distributed.DistributedLockService;
import com.usi.Questiontype;
import fr.xebia.usiquizz.core.game.Score;
import fr.xebia.usiquizz.core.game.gemfire.ScoreCacheListener;
import fr.xebia.usiquizz.core.sort.LocalBTree;
import fr.xebia.usiquizz.core.sort.Node;
import fr.xebia.usiquizz.core.sort.NodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GemfireRepository {


    private static final Logger LOG = LoggerFactory.getLogger(GemfireRepository.class);

    private final ExecutorService asyncScoreWritingOperation = new ThreadPoolExecutor(2, 2, 1, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

                private int counter = 0;

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("Async score gemfire writing : " + counter++);
                    return t;
                }
            }

    );

    private final ExecutorService asyncPlayerWritingOperation = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

                private int counter = 0;

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("Async player gemfire writing : " + counter++);
                    return t;
                }
            }

    );

    private final ExecutorService asyncScore = Executors.newSingleThreadExecutor();


    private Cache cache = new CacheFactory()
            .set("cache-xml-file", "gemfire/cache.xml")
            .create();

    // Region for user storage
    private Region<String, byte[]> userRegion = cache.getRegion("user-region");

    // Region for the current game
    private Region<String, Object> gameRegion;
    private Region<String, Questiontype> questionRegion = cache.getRegion("question-region");
    // FIXME add index on value....
    // Some search on email
    private Region<String, String> playerRegion;
    private Region<String, String> currentQuestionRegion;

    private Region<String, Byte> questionStatusRegion;

    // Region for score
    // Cette région contient le score de manière email --> score
    // Utilie lors de l'envoie de la réponse pour donner rapidement son score à un joueur
    private Region<String, Score> scoreRegion = cache.getRegion("score-region");

    private Region<String, Score> scoreFinalRegion;

    public void initQuestionStatusRegion(CacheListener questionStatusCacheListener) {
        AttributesFactory questionStatusAttribute = new AttributesFactory();
        questionStatusAttribute.setDataPolicy(DataPolicy.REPLICATE);
        questionStatusAttribute.addCacheListener(questionStatusCacheListener);
        RegionFactory rf = cache.createRegionFactory(questionStatusAttribute.create());
        questionStatusRegion = rf.create("question-status");
    }

    public void initCurrentQuestionRegion() {
        AttributesFactory questionAttribute = new AttributesFactory();
        questionAttribute.setDataPolicy(DataPolicy.REPLICATE);
        questionAttribute.setScope(Scope.DISTRIBUTED_NO_ACK);
        RegionFactory rf = cache.createRegionFactory(questionAttribute.create());
        currentQuestionRegion = rf.create("current-question-region");
    }

    public void initLoginRegion(CacheListener loginCacheListener) {
        AttributesFactory loginAttribute = new AttributesFactory();
        loginAttribute.setDataPolicy(DataPolicy.REPLICATE);
        loginAttribute.setScope(Scope.DISTRIBUTED_NO_ACK);
        loginAttribute.addCacheListener(loginCacheListener);
        RegionFactory rf = cache.createRegionFactory(loginAttribute.create());
        playerRegion = rf.create("player-region");
    }

    public void initFinalScoreRegion(CacheListener finalScoreListener) {
        // Creation score disk store
        DiskStoreFactory scoreStoreFactory = cache.createDiskStoreFactory();
        scoreStoreFactory.setDiskDirs(new File[]{new File("gemfire-persistence/score-oplogDir1"), new File("gemfire-persistence/score-oplogDir2"), new File("gemfire-persistence/score-dbDir")});
        scoreStoreFactory.setMaxOplogSize(10);
        scoreStoreFactory.setQueueSize(100);
        scoreStoreFactory.create("final-score");

        AttributesFactory scoreAttribute = new AttributesFactory();
        scoreAttribute.setDataPolicy(DataPolicy.PERSISTENT_REPLICATE);
        scoreAttribute.setDiskStoreName("final-score");
        scoreAttribute.setDiskSynchronous(false);
        scoreAttribute.setScope(Scope.DISTRIBUTED_NO_ACK);
        scoreAttribute.addCacheListener(finalScoreListener);
        RegionFactory rf = cache.createRegionFactory(scoreAttribute.create());
        scoreFinalRegion = rf.create("final-score-region");
    }

    public void initGameRegion(CacheListener gameListener) {
        AttributesFactory gameAttribute = new AttributesFactory();
        gameAttribute.setDataPolicy(DataPolicy.REPLICATE);
        gameAttribute.setScope(Scope.DISTRIBUTED_ACK);
        gameAttribute.addCacheListener(gameListener);
        RegionFactory rf = cache.createRegionFactory(gameAttribute.create());
        gameRegion = rf.create("game-region");
    }

    public Cache getCache() {
        return cache;
    }

    public Region<String, Object> getGameRegion() {
        return gameRegion;
    }

    public Region<String, Questiontype> getQuestionRegion() {
        return questionRegion;
    }

    public Region<String, String> getPlayerRegion() {
        return playerRegion;
    }

    public Region<String, String> getCurrentQuestionRegion() {
        return currentQuestionRegion;
    }

    public Region<String, byte[]> getUserRegion() {
        return userRegion;
    }

    public Region<String, Score> getScoreRegion() {
        return scoreRegion;
    }

    public Region<String, Score> getScoreFinalRegion() {
        return scoreFinalRegion;
    }

    public Region<String, Byte> getQuestionStatusRegion() {
        return questionStatusRegion;
    }


    // put to score region in other thread
    public void writeAsyncScore(final String sessionKey, final Score score) {
        asyncScoreWritingOperation.submit(new Runnable() {
            @Override
            public void run() {
                if (!score.isAlreadyAnswer(((Integer) getGameRegion().get(NB_QUESTIONS)).intValue())) {
                    // Maj du score standard
                    getScoreRegion().put(sessionKey, score);
                } else {
                    // Ajout du score dans la region final et suppression de scoreRegion
                    getScoreRegion().remove(sessionKey);
                    getScoreFinalRegion().put(sessionKey, score);
                }
            }
        });
    }

    public void writeAsyncPlayerForQuestion(final String sessionId) {
        asyncPlayerWritingOperation.submit(new Runnable() {
            @Override
            public void run() {
                getCurrentQuestionRegion().put(sessionId, "");
            }
        });
    }

    public void createScoreAsync(final String sessionKey, final User user) {
        asyncScoreWritingOperation.submit(new Runnable() {
            @Override
            public void run() {
                getScoreRegion().put(sessionKey, new Score(((Integer) getGameRegion().get(NB_QUESTIONS)).byteValue(), user));
            }
        });

    }

    public void addPlayerToGameAsync(final String sessionId, final String email) {
        asyncPlayerWritingOperation.submit(new Runnable() {
            @Override
            public void run() {
                getPlayerRegion().put(sessionId, email);
            }
        });

    }

    /**
     * Clears all region used at game time
     */
    public void clearGameCaches() {
        this.currentQuestionRegion.clear();
        this.playerRegion.clear();
        this.scoreFinalRegion.clear();
        this.scoreRegion.clear();
        this.gameRegion.clear();
    }

}
