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

    void addPlayer(String sessionId);

    int getUserConnected();

    Question getQuestion(int index);
}
