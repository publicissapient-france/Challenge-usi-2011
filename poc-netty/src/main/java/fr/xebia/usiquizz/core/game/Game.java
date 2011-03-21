package fr.xebia.usiquizz.core.game;

import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.server.http.netty.rest.LongPollingQuestionManager;

import java.util.List;

public interface Game {

    void init(Sessiontype st);

    // Acces direct au parametrage du jeux
    int getLongpollingduration();

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
    void addPlayerForQuestion(String sessionId, int questionIndex);

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
}
