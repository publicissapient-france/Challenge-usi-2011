package fr.xebia.usiquizz.core.persistence;

import com.esotericsoftware.kryo.util.IntHashMap;
import com.gemstone.gemfire.cache.*;
import com.usi.Questiontype;
import fr.xebia.usiquizz.core.game.Score;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class GemfireRepository {

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
    private Region<String, String> playerRegion = cache.getRegion("player-region");
    private Region<String, String> currentQuestionRegion = cache.getRegion("current-question-region");
    private Region<Byte, Byte> questionStatusRegion;

    // Region for score
    // Cette région contient le score de manière session --> score
    // Utilie lors de l'envoie de la réponse pour donner rapidement son score à un joueur
    private Region<String, Score> scoreRegion = cache.getRegion("score-region");

    // Permet de trier pour un score donnée l'ensemble des joueurs.
    // L'intérer serait de 'partionner' sur les différents serveurs le tri par ordre alphabetique des ex-equo
    private Region<Integer, TreeSet<Joueur>> inverseScoreRegion = cache.getRegion("inverse-score-region");

    // Cette region contient le rang : session-id --> rang
    // Permet de rapidement retrouver le rang d'un joueur
    // Permet avec finalRankingRegion de répondre au requête de ranking
    private Region<String, Integer> ranking = cache.getRegion("ranking-region");

    // Cette region contient le rang : rang --> score;firstname;lastname;mail
    // Doit permettre de répondre tres vite au requête de ranking
    // La difficulté est de le remplir
    private Region<Integer, String> finalRankingRegion = cache.getRegion("final-ranking-region");

    public void initQestionStatusResgion(CacheListener questionStatusCacheListener) {
        AttributesFactory questionStatusAttribute = new AttributesFactory();
        questionStatusAttribute.setDataPolicy(DataPolicy.REPLICATE);
        questionStatusAttribute.addCacheListener(questionStatusCacheListener);
        RegionFactory rf = cache.createRegionFactory(questionStatusAttribute.create());
        questionStatusRegion = rf.create("question-status");
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

    public Region<Byte, Byte> getQuestionStatusRegion() {
        return questionStatusRegion;
    }
}
