package fr.xebia.usiquizz.core.game;

import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.game.exception.LoginPhaseEndedException;
import fr.xebia.usiquizz.server.http.netty.rest.LongPollingQuestionManager;

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
    void addPlayerForQuestion(String sessionId, byte questionIndex);

    /**
     * Retourne le nombre de joueur ayant demandé une question
     *
     * @return
     */
    int countUserForQuestion(int questionIndex);

    /**
     * Retourne l'index de question courant
     *
     * @return
     */
    byte getCurrentQuestionIndex();

    /**
     * Retourne l'index de la réponse courante
     * @return
     */
    byte getCurrentAnswerIndex();

    void setCurrentQuestionIndex(byte newIndex);

    void setCurrentAnswerIndex(byte newIndex);

    void registerLongpollingCallback(QuestionLongpollingCallback callback);

    void startCurrentLongPolling();

    /**
     * Verification of flow
     * @param sessionKey
     * @param currentQuestion
     * @return
     */

    boolean isPlayerCanAnswer(String sessionKey, byte currentQuestion);

    boolean isPlayerCanAskQuestion(String sessionKey, byte questionNbr);

    void startQuestionTimeframe(byte currentQuestionIndex);

    void resetPlayerAskedQuestion();

    String createQuestionInJson(byte currentQuestionIndex, String sessionKey);

    String getEmailFromSession(String sessionKey);

    Collection<String> listPlayer();
}
