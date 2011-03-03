package fr.xebia.usiquizz.core.game;

import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;

import java.util.List;

public interface Game {

    void init(Sessiontype st);

    // Acces direct au valeur
    int getLongpollingduration();

    int getNbusersthresold();

    int getQuestiontimeframe();

    int getNbquestions();

    boolean getFlushusertable();

    String getTrackeduseridmail();

    void addPlayer(String sessionId, String email);

    int countUserConnected();

    void addPlayerForCurrentQuestion(String sessionId);

    int countUserForCurrentQuestion();

    void emptyCurrentQuestion();

    Question getQuestion(int index);

    boolean allPlayerReadyForQuestion();

    int getCurrentQuestionIndex();

    void setCurrentQuestionIndex(int index);
}
