package fr.xebia.usiquizz.core.persistence;

import static fr.xebia.usiquizz.core.persistence.GemfireAttribute.*;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.distributed.DistributedLockService;
import com.usi.Questiontype;
import fr.xebia.usiquizz.core.game.Score;
import fr.xebia.usiquizz.core.game.gemfire.ScoreCacheListener;
import fr.xebia.usiquizz.core.persistence.serialization.UserSerializer;
import fr.xebia.usiquizz.core.sort.LocalBTree;
import fr.xebia.usiquizz.core.sort.Node;
import fr.xebia.usiquizz.core.sort.NodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.management.counter.Units;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

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


    private Cache cache = new CacheFactory()
            .set("cache-xml-file", "gemfire/cache.xml")
            .create();

    // Region for user storage
    private Region<String, byte[]> userRegion;

    // Region for the current game
    private Region<String, Object> gameRegion;
    // FIXME add index on value....
    // Some search on email
    private Region<String, String> playerRegion;
    private Region<String, String> currentQuestionRegion;
    private Region<String, String> playerEndingGameRegion;

    private Region<String, Byte> questionStatusRegion;

    // Region for score
    // Cette région contient le score de manière email --> score
    // Utilie lors de l'envoie de la réponse pour donner rapidement son score à un joueur
    private Region<String, Score> scoreRegion = cache.getRegion("score-region");

    private Region<String, Score> scoreFinalRegion;

    public void initQuestionStatusRegion(CacheListener questionStatusCacheListener) {
        AttributesFactory questionStatusAttribute = new AttributesFactory();
        questionStatusAttribute.setDataPolicy(DataPolicy.REPLICATE);
        questionStatusAttribute.setScope(Scope.GLOBAL);
        questionStatusAttribute.addCacheListener(questionStatusCacheListener);
        RegionFactory rf = cache.createRegionFactory(questionStatusAttribute.create());
        questionStatusRegion = rf.create("question-status");
    }

    public void initUserRegion() {
        // Creation score disk store
        DiskStoreFactory userStoreFactory = cache.createDiskStoreFactory();
        userStoreFactory.setDiskDirs(new File[]{new File("gemfire-persistence/user-oplogDir1"), new File("gemfire-persistence/user-oplogDir2"), new File("gemfire-persistence/user-dbDir")});
        userStoreFactory.setMaxOplogSize(10);
        userStoreFactory.create("user-persistence");

        PartitionAttributesFactory<String, byte[]> userPartitionAttributeFactory = new PartitionAttributesFactory<String, byte[]>();
        userPartitionAttributeFactory.setRedundantCopies(1);
        PartitionAttributes<String, byte[]> userPartitionAttribute = userPartitionAttributeFactory.create();

        AttributesFactory userAttribute = new AttributesFactory();
        userAttribute.setDataPolicy(DataPolicy.PERSISTENT_PARTITION);
        userAttribute.setDiskStoreName("user-persistence");
        userAttribute.setDiskSynchronous(true);
        userAttribute.setPartitionAttributes(userPartitionAttribute);
        RegionFactory rf = cache.createRegionFactory(userAttribute.create());
        userRegion = rf.create("user-region");
    }

    public void initCurrentQuestionRegion(CacheListener<String, String> currentQuestionCacheListener) {
        AttributesFactory questionAttribute = new AttributesFactory();
        questionAttribute.setDataPolicy(DataPolicy.REPLICATE);
        questionAttribute.setScope(Scope.DISTRIBUTED_NO_ACK);
        RegionFactory rf = cache.createRegionFactory(questionAttribute.create());
        currentQuestionRegion = rf.create("current-question-region");
    }

    public void initPlayerEndingGameRegion(CacheListener endingGameCacheListener) {
        AttributesFactory endingGameAttribute = new AttributesFactory();
        endingGameAttribute.setDataPolicy(DataPolicy.REPLICATE);
        endingGameAttribute.setScope(Scope.DISTRIBUTED_NO_ACK);
        endingGameAttribute.addCacheListener(endingGameCacheListener);
        RegionFactory rf = cache.createRegionFactory(endingGameAttribute.create());
        playerEndingGameRegion = rf.create("player-ending-region");
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
        scoreStoreFactory.create("final-score");

        AttributesFactory scoreAttribute = new AttributesFactory();
        scoreAttribute.setDataPolicy(DataPolicy.PERSISTENT_REPLICATE);
        scoreAttribute.setDiskStoreName("final-score");
        scoreAttribute.setDiskSynchronous(true);
        scoreAttribute.setScope(Scope.DISTRIBUTED_NO_ACK);
        scoreAttribute.addCacheListener(finalScoreListener);
        RegionFactory rf = cache.createRegionFactory(scoreAttribute.create());
        scoreFinalRegion = rf.create("final-score-region");
    }

    public void initGameRegion(CacheListener gameListener) {
        // Creation score disk store
        DiskStoreFactory scoreStoreFactory = cache.createDiskStoreFactory();
        scoreStoreFactory.setDiskDirs(new File[]{new File("gemfire-persistence/game-dbDir")});
        scoreStoreFactory.setMaxOplogSize(10);
        scoreStoreFactory.create("game");

        AttributesFactory gameAttribute = new AttributesFactory();
        gameAttribute.setDataPolicy(DataPolicy.PERSISTENT_REPLICATE);
        gameAttribute.setDiskStoreName("game");
        gameAttribute.setScope(Scope.DISTRIBUTED_ACK);
        gameAttribute.addCacheListener(gameListener);
        RegionFactory rf = cache.createRegionFactory(gameAttribute.create());
        gameRegion = rf.create("game-region");
    }

    public Cache getCache() {
        return cache;
    }


    protected Region<String, Object> getGameRegion() {
        return gameRegion;
    }

    protected Region<String, String> getPlayerRegion() {
        return playerRegion;
    }

    protected Region<String, String> getCurrentQuestionRegion() {
        return currentQuestionRegion;
    }

    protected Region<String, byte[]> getUserRegion() {
        return userRegion;
    }

    public Region<String, Score> getScoreRegion() {
        return scoreRegion;
    }

    public Region<String, Score> getScoreFinalRegion() {
        return scoreFinalRegion;
    }

    protected Region<String, Byte> getQuestionStatusRegion() {
        return questionStatusRegion;
    }

    protected Region<String, String> getPlayerEndingGameRegion() {
        return playerEndingGameRegion;
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

    public void writePlayerEndGame(final String sessionKey) {
        asyncPlayerWritingOperation.submit(new Runnable() {
            @Override
            public void run() {
                getPlayerEndingGameRegion().put(sessionKey, "");
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
        this.playerEndingGameRegion.clear();
    }


    public int getScoreFinalRegionSize() {
        return scoreFinalRegion.size();
    }

    public void clearUserRegion() {
        userRegion.clear();
    }

    public void putInGameRegion(String gameKey, Object value) {
        gameRegion.put(gameKey, value);
    }

    /**
     * Change le statut pour une question
     * Ce changement doit être transactionnel. 2 noeud ne doivent pas pouvoir changer la valeur systématiquement sinon il y a un risque que le timer questiontimeframe ne démarre pas
     *
     * @param questionIndexInString
     * @param questionStatut
     */
    public void putQuestionStatus(String questionIndexInString, byte questionStatut) {
        Lock lock = questionStatusRegion.getDistributedLock(questionIndexInString);
        lock.lock();
        try {
            questionStatusRegion.put(questionIndexInString, questionStatut);
        } finally {
            lock.unlock();
        }
    }

    public Byte getQuestionStatus(String questionIndexInString) {
        return questionStatusRegion.get(questionIndexInString);
    }

    public Object getFromGameRegion(String gameKey) {
        return gameRegion.get(gameKey);
    }


    public List<User> listUser(int count) {
        UserSerializer us = new UserSerializer();
        Set<String> key = userRegion.keySet();
        Iterator<String> keyIt = key.iterator();
        List<User> res = new ArrayList<User>();
        for (int i = 0; i < count; i++) {
            res.add(us.deserializeUser(userRegion.get(keyIt.next())));
        }
        return res;
    }

    public int getPlayerRegionSize() {
        return playerRegion.size();
    }


    public boolean isPlayerExists(String sessionKey) {
        return playerRegion.containsKey(sessionKey);
    }

    public Collection<String> listPlayer() {
        return playerRegion.keySet();
    }

    public int getCurrentQuestionRegionSize() {
        return currentQuestionRegion.size();
    }

    public boolean isPlayerAlreadyAskQuestion(String sessionKey) {
        return currentQuestionRegion.containsKey(sessionKey);
    }

    public void clearCurrentQuestionRegion() {
        currentQuestionRegion.clear();
    }

    public Score getScore(String session_key) {
        return scoreRegion.get(session_key);
    }

    public int getPlayerEndingGameRegionSize() {
        return playerEndingGameRegion.size();
    }

    public void shutdown() {
        cache.close();
    }
}
