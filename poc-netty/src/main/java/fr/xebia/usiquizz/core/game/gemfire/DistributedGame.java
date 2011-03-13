package fr.xebia.usiquizz.core.game.gemfire;

import com.usi.Question;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.QuestionLongpollingCallback;
import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DistributedGame implements Game {

    private static final Logger logger = LoggerFactory.getLogger(DistributedGame.class);

    private static final String LOGIN_TIMEOUT = "login-timeout";
    private static final String SYNCHROTIME = "synchrotime";
    private static final String NB_USERS_THRESOLD = "nb-users-thresold";
    private static final String QUESTION_TIME_FRAME = "question-time-frame";
    private static final String NB_QUESTIONS = "nb-questions";
    private static final String FLUSH_USER_TABLE = "flush-user-table";
    private static final String TRACKED_USER_IDMAIL = "tracked-user-idmail";
    private static final String QUESTION_LIST = "question_list";
    private static final String CURRENT_QUESTION_INDEX = "current-question-index";

    private static final byte NON_JOUEE = 10;
    private static final byte EN_COURS = 11;
    private static final byte JOUEE = 12;


    private GemfireRepository gemfireRepository;
    private QuestionLongpollingCallback longpollingCallback;
    private boolean firstForQuestion = true;
    private boolean firstLogin = true;

    public DistributedGame(GemfireRepository gemfireRepository) {
        this.gemfireRepository = gemfireRepository;
    }

    @Override
    public void init(Sessiontype st) {
        // Parametre du jeu
        gemfireRepository.getGameRegion().put(LOGIN_TIMEOUT, st.getParameters().getLogintimeout());
        gemfireRepository.getGameRegion().put(SYNCHROTIME, st.getParameters().getSynchrotime());
        gemfireRepository.getGameRegion().put(NB_USERS_THRESOLD, st.getParameters().getNbusersthreshold());
        gemfireRepository.getGameRegion().put(QUESTION_TIME_FRAME, st.getParameters().getQuestiontimeframe());
        gemfireRepository.getGameRegion().put(NB_QUESTIONS, st.getParameters().getNbquestions());
        gemfireRepository.getGameRegion().put(FLUSH_USER_TABLE, st.getParameters().isFlushusertable());
        gemfireRepository.getGameRegion().put(TRACKED_USER_IDMAIL, st.getParameters().getTrackeduseridmail());
        gemfireRepository.getGameRegion().put(CURRENT_QUESTION_INDEX, 1);

        // les questions
        gemfireRepository.getQuestionRegion().put(QUESTION_LIST, st.getQuestions());

        // Les status de chaque question (non jouée, en cours, jouée)
        for (byte currentIndex = 1; currentIndex <= st.getParameters().getNbquestions(); currentIndex++) {
            gemfireRepository.getQuestionStatusRegion().put(currentIndex, NON_JOUEE);
        }
    }

    @Override
    public int getLoginTimeout() {
        return ((Integer) gemfireRepository.getGameRegion().get(LOGIN_TIMEOUT)).intValue();
    }

    @Override
    public int getNbusersthresold() {
        return ((Integer) gemfireRepository.getGameRegion().get(NB_USERS_THRESOLD)).intValue();
    }

    @Override
    public int getQuestiontimeframe() {
        return ((Integer) gemfireRepository.getGameRegion().get(QUESTION_TIME_FRAME)).intValue();
    }

    @Override
    public int getNbquestions() {
        return ((Integer) gemfireRepository.getGameRegion().get(NB_QUESTIONS)).intValue();
    }

    @Override
    public boolean getFlushusertable() {
        return ((Boolean) gemfireRepository.getGameRegion().get(FLUSH_USER_TABLE)).booleanValue();
    }

    @Override
    public String getTrackeduseridmail() {
        return ((String) gemfireRepository.getGameRegion().get(TRACKED_USER_IDMAIL));
    }

    @Override
    public void addPlayer(String sessionId, String email) {
        gemfireRepository.getPlayerRegion().put(sessionId, email);
        // FIXME EXPERIMENTATION Start timer
        if (firstLogin) {
            firstLogin = false;
            logger.info("Start timers for login timeout");
            //Executors.newSingleThreadScheduledExecutor().schedule((new Runnable() {
            //    @Override
            //    public void run() {
            //        startCurrentLongPolling((byte) 1);
            //    }
            //}), getLoginTimeout(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean isAlreadyLogged(String email) {
        return gemfireRepository.getPlayerRegion().containsKey(email);
    }

    @Override
    public int countUserConnected() {
        return gemfireRepository.getPlayerRegion().size();
    }

    @Override
    public void addPlayerForQuestion(final String sessionId, final byte questionIndex) {
        gemfireRepository.getCurrentQuestionRegion().put(sessionId, "");
        // FIXME EXPERIMENTATION Start timer
        if (firstForQuestion) {
            firstForQuestion = false;
            logger.info("Start timers for longpolling");
            Executors.newSingleThreadScheduledExecutor().schedule((new Runnable() {
                @Override
                public void run() {
                    startCurrentLongPolling(questionIndex);
                }
            }), getQuestiontimeframe(), TimeUnit.MILLISECONDS);

        }
    }

    @Override
    public int countUserForQuestion(int questionIndex) {
        return gemfireRepository.getCurrentQuestionRegion().size();
    }

    @Override
    public Question getQuestion(int index) {
        // -1 difference between spec and list implementation
        return gemfireRepository.getQuestionRegion().get(QUESTION_LIST).getQuestion().get(index);
    }

    @Override
    public int getCurrentQuestionIndex() {
        Integer i =  (Integer) gemfireRepository.getGameRegion().get(CURRENT_QUESTION_INDEX);
        return i;
    }

    @Override
    public void registerLongpollingCallback(QuestionLongpollingCallback callback) {
        this.longpollingCallback = callback;
    }

    @Override
    public boolean isPlayerCanAnswer(String sessionKey, byte currentQuestion) {
        // L'index de la question doit être la question courante.
        if (currentQuestion != getCurrentQuestionIndex()) {
            return false;
        }

        // On doit être encore dans la bonne timeframe
        // FIXME implements...

        // Le joueur ne doit pas déja avoir répondu
        // FIXME A voir si reèlement obligatoire
        return true;
    }

    @Override
    public boolean isPlayerCanAskQuestion(String sessionKey, byte questionNbr) {
        // L'index de la question doit être la question courante.
        if (questionNbr != getCurrentQuestionIndex()) {
            return false;
        }

        // On doit être encore dans la bonne timeframe
        // FIXME implements...

        // Le joueur ne doit pas déja avoir demandé la question
        return !gemfireRepository.getCurrentQuestionRegion().containsKey(sessionKey);


    }

    private void startCurrentLongPolling(byte questionIndex) {
        // On lance le longpolling uniquement si la question n'est pas encore joue...
        if (gemfireRepository.getQuestionStatusRegion().get(questionIndex) == NON_JOUEE) {
            gemfireRepository.getQuestionStatusRegion().put(questionIndex, EN_COURS);
            longpollingCallback.startSendAll();
            gemfireRepository.getQuestionStatusRegion().put(questionIndex, JOUEE);
        }
    }
}
