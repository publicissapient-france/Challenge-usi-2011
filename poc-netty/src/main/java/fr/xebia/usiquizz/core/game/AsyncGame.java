package fr.xebia.usiquizz.core.game;

import com.usi.Question;
import com.usi.Sessiontype;

// Async version of game
// Must be manually synchrnized with Game interface
public interface AsyncGame extends Game {

    void init(Sessiontype st, GameCallback<Void> callback);

    // Acces direct au parametrage du jeux
    void getLoginTimeout(GameCallback<Integer> callback);

    void getNbusersthresold(GameCallback<Integer> callback);

    void getQuestiontimeframe(GameCallback<Integer> callback);

    void getNbquestions(GameCallback<Integer> callback);

    void getFlushusertable(GameCallback<Boolean> callback);

    void getTrackeduseridmail(GameCallback<String> callback);

    void getQuestion(int index, GameCallback<Question> callback);

    // Gestion des joueurs

    /**
     * Ajoute un joueur à la partie courante
     *
     * @param sessionId
     * @param email
     */
    void addPlayer(String sessionId, String email, GameCallback<Void> callback);

    /**
     * Return true si le joueur est deja connecté
     *
     * @param email
     * @return
     */
    void isAlreadyLogged(String email, GameCallback<Boolean> callback);

    /**
     * Retourne le nombre de joueur connecté à la partir (phase de login passé)
     *
     * @return
     */
    void countUserConnected(GameCallback<Integer> callback);

    /**
     * Ajoute le joueur a la liste de ce qui ont demandé une question question
     *
     * @param sessionId
     */
    void addPlayerForQuestion(String sessionId, byte questionIndex, GameCallback<Void> callback);

    /**
     * Retourne le nombre de joueur ayant demandé une question
     *
     * @return
     */
    void countUserForQuestion(int questionIndex, GameCallback<Integer> callback);

    /**
     * Retourne l'index de question courant
     *
     * @return
     */
    void getCurrentQuestionIndex(GameCallback<Integer> callback);

    void registerLongpollingCallback(QuestionLongpollingCallback callback, GameCallback<Void> rescallback);
}
