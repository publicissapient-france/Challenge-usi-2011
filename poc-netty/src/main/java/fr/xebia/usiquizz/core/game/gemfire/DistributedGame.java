package fr.xebia.usiquizz.core.game.gemfire;

import static fr.xebia.usiquizz.core.persistence.GemfireAttribute.*;

import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.game.QuestionLongpollingCallback;
import fr.xebia.usiquizz.core.game.Scoring;
import fr.xebia.usiquizz.core.game.exception.LoginPhaseEndedException;
import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import fr.xebia.usiquizz.core.persistence.Joueur;
import fr.xebia.usiquizz.core.persistence.User;
import fr.xebia.usiquizz.core.persistence.serialization.UserSerializer;
import fr.xebia.usiquizz.core.twitter.UsiXebiaTwitter;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class DistributedGame implements Game {

    private static final Logger logger = LoggerFactory.getLogger(DistributedGame.class);

    private ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(4, new ThreadFactory() {

        private int i = 1;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("Game schedule ThreadFactory " + i++);
            return thread;
        }
    });

    private ExecutorService eventTaskExector = Executors.newCachedThreadPool(new ThreadFactory() {

        private int i = 1;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("Event task executor " + i++);
            return thread;
        }
    });


    private GemfireRepository gemfireRepository;
    private QuestionLongpollingCallback longpollingCallback;
    private Scoring scoring;
    private JsonQuestionWriter jsonQuestionWriter = new JsonQuestionWriter();
    private ConcurrentSkipListSet<Joueur> bTree;
    private UsiXebiaTwitter usiXebiaTwitter = new UsiXebiaTwitter();

    // local state
    private boolean loginPhaseEnded = false;
    private boolean firstUserLogged = false;
    private String currentQuestionIndex = "1";
    private String currentAnswerIndex = "1";

    // listener
    PlayerEndingGameListener playerEndingGameListener = new PlayerEndingGameListener(this);

    public DistributedGame(GemfireRepository gemfireRepository, Scoring scoring) {
        this.gemfireRepository = gemfireRepository;
        this.scoring = scoring;
        gemfireRepository.initUserRegion();
        gemfireRepository.initQuestionStatusRegion(new QuestionStatusCacheListener(this, eventTaskExector));
        gemfireRepository.initCurrentQuestionRegion(new CurrentQuestionCacheListener(this, eventTaskExector));
        gemfireRepository.initPlayerEndingGameRegion(playerEndingGameListener);
        gemfireRepository.initLoginRegion(new LoginCacheListener(this, eventTaskExector));
        this.bTree = new ConcurrentSkipListSet<Joueur>();
        gemfireRepository.initFinalScoreRegion(new ScoreCacheListener(this.bTree));
        gemfireRepository.initGameRegion(new GameCacheListener(this));

        scoring.setTree(this.bTree);
        // if repository contains final score data, reconstruct scoring tree (Organisation can stop application and consult result after restart application)
        if (gemfireRepository.getScoreFinalRegionSize() > 0) {
            logger.info("Reconstruct scoring btree");
            scoring.reconstructRanking();
            logger.info("Reconstruct scoring btree ended");
        }

    }

    @Override
    public void init(Sessiontype st) {
        // Clean all cache
        gemfireRepository.clearGameCaches();
        // Clean scoring
        scoring.init();
        // Clean user for twitter
        playerEndingGameListener.init();
        // Flush user Table when requested
        if (st.getParameters().isFlushusertable()) {
            gemfireRepository.clearUserRegion();
        }

        // Parametre du jeu
        gemfireRepository.putInGameRegion(LOGIN_TIMEOUT, st.getParameters().getLogintimeout());
        gemfireRepository.putInGameRegion(SYNCHROTIME, st.getParameters().getSynchrotime());
        gemfireRepository.putInGameRegion(NB_USERS_THRESOLD, st.getParameters().getNbusersthreshold());
        gemfireRepository.putInGameRegion(QUESTION_TIME_FRAME, st.getParameters().getQuestiontimeframe());
        gemfireRepository.putInGameRegion(NB_QUESTIONS, st.getParameters().getNbquestions());
        gemfireRepository.putInGameRegion(FLUSH_USER_TABLE, st.getParameters().isFlushusertable());
        gemfireRepository.putInGameRegion(TRACKED_USER_IDMAIL, st.getParameters().getTrackeduseridmail());
        gemfireRepository.putInGameRegion(CURRENT_QUESTION_INDEX, "1");
        gemfireRepository.putInGameRegion(CURRENT_ANSWER_INDEX, "1");

        // Status du jeux
        gemfireRepository.putInGameRegion(LOGIN_PHASE_STATUS, LOGIN_PHASE_NON_COMMENCER);

        // les questions
        gemfireRepository.putInGameRegion(QUESTION_LIST, st.getQuestions());


        // Les status de chaque question (non jouée, en cours, jouée)
        for (byte currentIndex = 1; currentIndex <= st.getParameters().getNbquestions(); currentIndex++) {
            gemfireRepository.putQuestionStatus(Byte.toString(currentIndex), QuestionStatus.QUESTION_NON_JOUEE);
        }


    }

    public void resetLocalGameData() {
        longpollingCallback.reset();
        loginPhaseEnded = false;
        firstUserLogged = false;
        // Clear all caches used in the party
        bTree.clear();
    }

    @Override
    public int getLoginTimeout() {
        return ((Integer) gemfireRepository.getFromGameRegion(LOGIN_TIMEOUT)).intValue();
    }

    @Override
    public int getNbusersthresold() {
        return ((Integer) gemfireRepository.getFromGameRegion(NB_USERS_THRESOLD)).intValue();
    }

    @Override
    public int getQuestiontimeframe() {
        return ((Integer) gemfireRepository.getFromGameRegion(QUESTION_TIME_FRAME)).intValue();
    }

    @Override
    public int getNbquestions() {
        return ((Integer) gemfireRepository.getFromGameRegion(NB_QUESTIONS)).intValue();
    }

    @Override
    public boolean getFlushusertable() {
        return ((Boolean) gemfireRepository.getFromGameRegion(FLUSH_USER_TABLE)).booleanValue();
    }

    @Override
    public String getTrackeduseridmail() {
        return ((String) gemfireRepository.getFromGameRegion(TRACKED_USER_IDMAIL));
    }

    @Override
    public int getSynchrotime() {
        return ((Integer) gemfireRepository.getFromGameRegion(SYNCHROTIME));
    }

    @Override
    public List<Question> getQuestionList() {
        return ((Questiontype) gemfireRepository.getFromGameRegion(QUESTION_LIST)).getQuestion();
    }

    @Override
    public List<User> userList(int count) {
        return gemfireRepository.listUser(count);
    }

    public void setLocalCurrentQuestionIndex(String currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public void setLocalCurrentAnswerIndex(String currentAnswerIndex) {
        this.currentAnswerIndex = currentAnswerIndex;
    }

    public String getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public String getCurrentAnswerIndex() {
        return currentAnswerIndex;
    }



    @Override
    public void logPlayerToApplication(String sessionId, String email) throws LoginPhaseEndedException {
        // Si c'est le premier joueur, alors on démarre un timer
        // FIXME Le status doit bien être synchrone entre tous les serveurs.. Bien verifier la conf de cette région
        if (loginPhaseEnded) {
            throw new LoginPhaseEndedException();
        } else {
            if (!firstUserLogged) {
                synchronized (this) {
                    if (!firstUserLogged) {
                        logger.info("Start timers login timeout");
                        scheduleExecutor.schedule((new Runnable() {
                            @Override
                            public void run() {
                                logger.info("Login timer ended");
                                gemfireRepository.putInGameRegion(LOGIN_PHASE_STATUS, LOGIN_PHASE_TERMINER);
                                logger.info("{} player logged for game", gemfireRepository.getPlayerRegionSize());

                                startGame();
                            }
                        }), getLoginTimeout(), TimeUnit.SECONDS);
                        gemfireRepository.putInGameRegion(LOGIN_PHASE_STATUS, LOGIN_PHASE_EN_COURS);
                    }
                }
            }
        }
        gemfireRepository.addPlayerToGameAsync(sessionId, email);
    }

    @Override
    public void setLoginPhaseBegin() {
        firstUserLogged = true;
    }

    @Override
    public void setLoginPhaseEnded() {
        loginPhaseEnded = true;
    }

    @Override
    public void startGame() {
        // On change le status de la première question.
        // Un listener permet, si nécessaire (pas tous les joueurs de loggué) de démarrer l'envoie de la premiere question
        // Si la question n'est pas encore en cours on la passe en cours. (cas de tous les joueurs loggué avant la fin du timer)
        logger.warn("Start game");
        if (gemfireRepository.getQuestionStatus("1").intValue() == QuestionStatus.QUESTION_NON_JOUEE) {
            gemfireRepository.putQuestionStatus("1", QuestionStatus.QUESTION_EN_COURS);
        }
    }

    @Override
    public boolean isAllPlayerLogged() {
        return gemfireRepository.getPlayerRegionSize() == ((Integer) gemfireRepository.getFromGameRegion(NB_USERS_THRESOLD)).intValue();
    }

    @Override
    public boolean isAlreadyLogged(String sessionKey) {
        return gemfireRepository.isPlayerExists(sessionKey);
    }


    @Override
    public Collection<String> listPlayer() {
        return gemfireRepository.listPlayer();
    }

    @Override
    public int countUserConnected() {
        return gemfireRepository.getPlayerRegionSize();
    }

    @Override
    public void addPlayerForQuestion(final String sessionId, final String questionIndex) {
        gemfireRepository.writeAsyncPlayerForQuestion(sessionId);
    }

    @Override
    public int countUserForCurrentQuestion() {
        return gemfireRepository.getCurrentQuestionRegionSize();
    }

    @Override
    public Question getQuestion(int index) {
        // -1 difference between spec and list implementation
        return ((Questiontype) gemfireRepository.getFromGameRegion(QUESTION_LIST)).getQuestion().get(index - 1);
    }

    @Override
    public void setCurrentQuestionIndex(String newIndex) {
        gemfireRepository.putInGameRegion(CURRENT_QUESTION_INDEX, newIndex);
    }

    @Override
    public void setCurrentAnswerIndex(String newIndex) {
        gemfireRepository.putInGameRegion(CURRENT_ANSWER_INDEX, newIndex);
    }

    @Override
    public void registerLongpollingCallback(QuestionLongpollingCallback callback) {
        this.longpollingCallback = callback;
    }

    @Override
    public boolean isPlayerCanAnswer(String sessionKey, String currentQuestion) {
        // L'index de la question doit correspondre au reponse que l'on attend
        if (!currentQuestion.equals(getCurrentAnswerIndex())) {
            logger.info("Player {} outside windows answer of question {} current answer {}", new Object[]{sessionKey, currentQuestion, getCurrentAnswerIndex()});
            return false;
        }

        // On doit être encore dans la bonne timeframe
        // Pour cela on regarde le statut de la question courante.
        // Elle doit être de en QUESTION_EN_COURS
        if (gemfireRepository.getQuestionStatus(currentQuestion) != QuestionStatus.QUESTION_EN_COURS) {
            logger.info("Player {} outside windows answer of question {} current statut {} (should be 11)", new Object[]{sessionKey, currentQuestion, gemfireRepository.getQuestionStatus(currentQuestion)});
            return false;
        }

        // Le joueur ne doit pas déja avoir répondu
        // FIXME A voir si reèlement obligatoire
        return true;
    }

    @Override
    public boolean isPlayerCanAskQuestion(String sessionKey, String questionNbr) {
        // L'index de la question doit être la question courante.
        if (!questionNbr.equals(getCurrentQuestionIndex())) {
            return false;
        }

        // On doit être encore dans la bonne timeframe
        // FIXME implements...

        // Le joueur ne doit pas déja avoir demandé la question
        return !gemfireRepository.isPlayerAlreadyAskQuestion(sessionKey);


    }

    @Override
    public void startQuestionTimeframe(final byte currentQuestionIndex) {
        logger.info("Start question timeframe for question {}", currentQuestionIndex);
        longpollingCallback.initNewQuestion((byte) (currentQuestionIndex + 1));

        scheduleExecutor.schedule((new Runnable() {
            @Override
            public void run() {
                logger.info("end question timeframe for question {}", currentQuestionIndex);

                // Si c'est la dernière question fin du jeu
                if (currentQuestionIndex >= getNbquestions()) {
                    // End of game
                    logger.info("END OF GAME");
                    // Create final ranking only when we own the score lock
                    scoring.calculRanking();
                } else {
                    // Sinon On déclenche le synchrotime...
                    startSynchroTime();
                }
            }
        }), getQuestiontimeframe(), TimeUnit.SECONDS);
    }

    @Override
    public void resetPlayerAskedQuestion() {
        gemfireRepository.clearCurrentQuestionRegion();
    }

    @Override
    public ChannelBuffer createQuestionInJson(byte currentQuestionIndex, String session_key) {
        return jsonQuestionWriter.createQuestionInJson(currentQuestionIndex, gemfireRepository.getScore(session_key).getCurrentScore());

    }


    private void startSynchroTime() {
        final String currentAnswerIndex = getCurrentAnswerIndex();
        final String currentQuestionIndex = getCurrentQuestionIndex();
        logger.info("fin REPONSE {}", currentAnswerIndex);
        gemfireRepository.putQuestionStatus(currentAnswerIndex, QuestionStatus.QUESTION_JOUEE);
        logger.info("Start synchro time");

        scheduleExecutor.schedule((new Runnable() {
            @Override
            public void run() {
                logger.info("end synchro time");
                // Fin de la fenetre de reponse de la question prec
                // On met le status de la question en QUESTION_JOUEE

                // On change le statut de la question
                gemfireRepository.putQuestionStatus(currentQuestionIndex, QuestionStatus.QUESTION_EN_COURS);
            }
        }), getSynchrotime(), TimeUnit.SECONDS);
    }

    public void startCurrentLongPolling() {
        longpollingCallback.startSendAll();
    }

    public void playerEndGame(String sessionKey) {
        gemfireRepository.writePlayerEndGame(sessionKey);
    }

    public int countUserEndingGame() {
        return gemfireRepository.getPlayerEndingGameRegionSize();
    }

    public void tweetResult() {
        usiXebiaTwitter.tweetNbUserSupportedByAppli(countUserEndingGame());
    }



    private class JsonQuestionWriter {

        private final byte[] END_BA = "\"}".getBytes();

        private Map<Byte, byte[]> questionCache = new ConcurrentHashMap<Byte, byte[]>();

        public ChannelBuffer createQuestionInJson(byte currentQuestionIndex, byte currentScore) {
            byte[] questionBa = questionCache.get(currentQuestionIndex);
            if (questionBa == null) {
                synchronized (this) {
                    questionBa = questionCache.get(currentQuestionIndex);
                    if (questionBa == null) {
                        // construct question
                        Question question = getQuestion(currentQuestionIndex);
                        StringBuffer sb = new StringBuffer();
                        sb.append("{\"question\":\"");
                        sb.append(question.getLabel());
                        sb.append("\",\"answer_1\":\"");
                        sb.append(question.getChoice().get(0));
                        sb.append("\",\"answer_2\":\"");
                        sb.append(question.getChoice().get(1));
                        sb.append("\",\"answer_3\":\"");
                        sb.append(question.getChoice().get(2));
                        sb.append("\",\"answer_4\":\"");
                        sb.append(question.getChoice().get(3));
                        sb.append("\",\"score\":\"");
                        questionBa = sb.toString().getBytes();
                        questionCache.put(currentQuestionIndex, questionBa);
                    }
                }
            }
            return ChannelBuffers.wrappedBuffer(questionBa, Byte.toString(currentScore).getBytes(), END_BA);
        }

    }
}