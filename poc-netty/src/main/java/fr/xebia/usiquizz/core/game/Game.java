package fr.xebia.usiquizz.core.game;

import com.usi.Question;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.game.exception.LoginPhaseEndedException;
import fr.xebia.usiquizz.core.persistence.User;
import org.jboss.netty.buffer.ChannelBuffer;

import java.util.Collection;
import java.util.List;

public interface Game {

    void init(Sessiontype st);

    // Acces direct au parametrage du jeux
    int getLoginTimeout();

    int getNbusersthresold();

    int getQuestiontimeframe();

    int getSynchrotime();

    int getNbquestions();

    boolean getFlushusertable();

    String getTrackeduseridmail();

    Question getQuestion(int index);

    List<Question> getQuestionList();

    // Gestion des joueurs

    /**
     * Ajoute un joueur à la partie courante
     *
     * @param sessionId
     * @param email
     */
    void logPlayerToApplication(String sessionId, String email) throws LoginPhaseEndedException;

    /**
     * Return true si le joueur est deja connecté
     *
     * @param sessionKey
     * @return
     */
    boolean isAlreadyLogged(String sessionKey);

    /**
     * Retourne le nombre de joueur connecté à la partir (phase de login passé)
     *
     * @return
     */
    int countUserConnected();

    /**
     * Ajoute le joueur a la liste de ce qui ont demandé une question question
     *
     * @param sessionId
     */
    void addPlayerForQuestion(String sessionId, String questionIndex);

    /**
     * Retourne le nombre de joueur ayant demandé une question
     *
     * @return
     */
    int countUserForQuestion(String questionIndex);

    /**
     * Retourne l'index de question courant
     *
     * @return
     */
    String getCurrentQuestionIndex();

    /**
     * Retourne l'index de la réponse courante
     * @return
     */
    String getCurrentAnswerIndex();

    void setCurrentQuestionIndex(String newIndex);

    void setCurrentAnswerIndex(String newIndex);

    void registerLongpollingCallback(QuestionLongpollingCallback callback);

    void startCurrentLongPolling();

    /**
     * Verification of flow
     * @param sessionKey
     * @param currentQuestion
     * @return
     */

    boolean isPlayerCanAnswer(String sessionKey, String currentQuestion);

    boolean isPlayerCanAskQuestion(String sessionKey, String questionNbr);

    void startQuestionTimeframe(byte currentQuestionIndex);

    void resetPlayerAskedQuestion();

    ChannelBuffer createQuestionInJson(byte currentQuestionIndex, String sessionKey);

    String getEmailFromSession(String sessionKey);

    Collection<String> listPlayer();

    boolean isAllPlayerLogged();

    void startGame();

    List<User> userList(int count);
}
