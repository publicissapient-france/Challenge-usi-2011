package fr.xebia.usiquizz.core.game;

import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.server.http.netty.rest.LongPollingQuestionManager;

import java.util.List;

public interface Game {

    void init(Sessiontype st);

    // Acces direct au parametrage du jeux
    int getLoginTimeout();

    int getNbusersthresold();

    int getQuestiontimeframe();

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
    void addPlayer(String sessionId, String email);

    /**
     * Return true si le joueur est deja connecté
     *
     * @param email
     * @return
     */
    boolean isAlreadyLogged(String email);

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
    int getCurrentQuestionIndex();

    void registerLongpollingCallback(QuestionLongpollingCallback callback);

    /**
     * Verification of flow
     * @param sessionKey
     * @param currentQuestion
     * @return
     */

    boolean isPlayerCanAnswer(String sessionKey, byte currentQuestion);

    boolean isPlayerCanAskQuestion(String sessionKey, byte questionNbr);
}
