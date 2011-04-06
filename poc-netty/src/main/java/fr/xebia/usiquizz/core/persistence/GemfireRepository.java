package fr.xebia.usiquizz.core.persistence;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.distributed.DistributedLockService;
import com.usi.Questiontype;
import fr.xebia.usiquizz.core.game.Score;
import fr.xebia.usiquizz.core.game.gemfire.ScoreCacheListener;
import fr.xebia.usiquizz.core.sort.Node;
import fr.xebia.usiquizz.core.sort.NodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GemfireRepository {


    private static final Logger LOG = LoggerFactory.getLogger(GemfireRepository.class);

    private final ExecutorService asyncScoreWritingOperation = new ThreadPoolExecutor(2, 2, 1, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

                private int counter = 0;

                @Override
                public Thread newThread
                        (Runnable
                                 r) {
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
                public Thread newThread
                        (Runnable
                                 r) {
                    Thread t = new Thread(r);
                    t.setName("Async player gemfire writing : " + counter++);
                    return t;
                }
            }

    );

    private final ExecutorService asyncScore = Executors.newSingleThreadExecutor();

    // PARAMETRE DU JEU
    public static final String LOGIN_TIMEOUT = "login-timeout";
    public static final String SYNCHROTIME = "synchrotime";
    public static final String NB_USERS_THRESOLD = "nb-users-thresold";
    public static final String QUESTION_TIME_FRAME = "question-time-frame";
    public static final String NB_QUESTIONS = "nb-questions";
    public static final String FLUSH_USER_TABLE = "flush-user-table";
    public static final String TRACKED_USER_IDMAIL = "tracked-user-idmail";

    // QUESTION
    public static final String QUESTION_LIST = "question_list";
    // Response
    public static final String GOOD_RESPONSE = "good-response";


    // ETAT COURANT DU JEU
    public static final String CURRENT_QUESTION_INDEX = "current-question-index";
    public static final String CURRENT_ANSWER_INDEX = "current-answer-index";
    public static final String LOGIN_PHASE_STATUS = "login-phase-status";

    private Cache cache = new CacheFactory()
            .set("cache-xml-file", "gemfire/cache.xml")
            .create();

    // Region for user storage
    private Region<String, byte[]> userRegion = cache.getRegion("user-region");

    // Region for the current game
    private Region<String, Object> gameRegion = cache.getRegion("game-region");
    private Region<String, Questiontype> questionRegion = cache.getRegion("question-region");
    // FIXME add index on value....
    // Some search on email
    private Region<String, String> playerRegion;
    private Region<String, String> currentQuestionRegion;

    // TODO : This is a simple stupid region test to implement Ranking tree NodeStore
    private Region<Joueur, Node<Joueur>> scoreStoreRegion = cache.getRegion("score-store-region");
    
    private Region<String, Byte> questionStatusRegion;

    // Region for score
    // Cette région contient le score de manière email --> score
    // Utilie lors de l'envoie de la réponse pour donner rapidement son score à un joueur
    private Region<String, Score> scoreRegion = cache.getRegion("score-region");

    // cette region contient les score finaux des utilisateurs qui ont répondus
    // Elle associe email -> Score
    private Region<String, Score> scoreFinalRegion;


    DistributedLockService dls = DistributedLockService.create("ScoreLockService", cache.getDistributedSystem());

    // Tells wether this instance owns the lock or not
    private AtomicBoolean ownScoreLock = new AtomicBoolean(false);

    public GemfireRepository() {

      

        // Try to get the lock indefinitely
        // if server owning the lock crash we can recover

        asyncScore.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    boolean locked = dls.lock("finalScoring", -1, -1);
                    LOG.info("Granted to final scoring distributed lock {}", locked);
                    ownScoreLock.set(locked);
                    while (locked){
                        try {
                            Thread.currentThread().sleep(500);
                        } catch (InterruptedException e) {
                            LOG.warn("Interruption while sleeping before lock access", e);
                           locked = false;
                        }
                    }
                    dls.freeResources("finalScoring");
                 }

            }
        });
    }

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
        AttributesFactory scoreAttribute = new AttributesFactory();
        scoreAttribute.setDataPolicy(DataPolicy.REPLICATE);
        scoreAttribute.setScope(Scope.DISTRIBUTED_ACK);
        scoreAttribute.addCacheListener(finalScoreListener);
        RegionFactory rf = cache.createRegionFactory(scoreAttribute.create());
        scoreFinalRegion = rf.create("final-score-region");
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


    public void addPlayerToGameAsync(final String sessionId, final String email) {
        asyncPlayerWritingOperation.submit(new Runnable() {
            @Override
            public void run() {
                getPlayerRegion().put(sessionId, email);
            }
        });

    }

    public Region<Joueur, Node<Joueur>> getScoreStoreRegion() {
        return scoreStoreRegion;
    }


    public boolean hasFinalScoreLock() {
        return ownScoreLock.get();
    }
}
