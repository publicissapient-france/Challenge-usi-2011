package fr.xebia.usiquizz.core.game;

import com.usi.Sessiontype;

public interface Game {

    public void init(Sessiontype st);

    // Acces direct au valeur
    public int getLongpollingduration();

    public int getNbusersthresold();

    public int getQuestiontimeframe();

    public int getNbquestions();

    public int getFlushusertable();

    public int getTrackeduseridmail();

    public boolean incrementPlayer();

    public int getUserConnected();

    boolean isGameStarted();

    void startGame();
}
