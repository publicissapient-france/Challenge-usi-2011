package fr.xebia.usiquizz.core.game;

import com.usi.Sessiontype;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GameLocalInstance implements Game {

    private Sessiontype sessionType;

    private AtomicInteger nbJoueurLogged = new AtomicInteger(0);

    private AtomicBoolean gameStarted = new AtomicBoolean(false);

    public void init(Sessiontype sessiontype) {
        this.sessionType = sessiontype;
    }

    public int getLongpollingduration() {
        return sessionType.getParameters().getLongpollingduration();
    }

    public int getNbusersthresold() {
        return sessionType.getParameters().getNbusersthresold();
    }

    public int getQuestiontimeframe() {
        return sessionType.getParameters().getQuestiontimeframe();
    }

    public int getNbquestions() {
        return sessionType.getParameters().getNbquestions();
    }

    public int getFlushusertable() {
        return 0;
    }

    public int getTrackeduseridmail() {
        return 0;
    }

    public boolean incrementPlayer() {
        return nbJoueurLogged.incrementAndGet() >= getNbusersthresold();
    }

    @Override
    public int getUserConnected() {
        return nbJoueurLogged.get();
    }

    @Override
    public boolean isGameStarted() {
        return gameStarted.get();
    }

    @Override
    public void startGame() {
        gameStarted.set(true);
    }
}
