package fr.xebia.usiquizz.core.game;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.usi.Question;
import com.usi.Sessiontype;

public class GameLocalInstance implements Game {

    private Sessiontype sessionType;

    private AtomicInteger nbJoueurLogged;

    private AtomicBoolean gameStarted;

    public void init(Sessiontype sessiontype) {
        this.sessionType = sessiontype;
        nbJoueurLogged = new AtomicInteger(0);
        gameStarted = new AtomicBoolean(false);
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

    @Override
    public boolean getFlushusertable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getTrackeduseridmail() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean incrementPlayer() {
        return nbJoueurLogged.incrementAndGet() >= getNbusersthresold();
    }

    public AtomicInteger getNbJoueurLogged() {
        return nbJoueurLogged;
    }

    @Override
    public void addPlayer(String sessionId, String email) {
        // TODO Auto-generated method stub

    }

    @Override
    public int countUserConnected() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void addPlayerForCurrentQuestion(String sessionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public int countUserForCurrentQuestion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void emptyCurrentQuestion() {
        // TODO Auto-generated method stub

    }

    @Override
    public Question getQuestion(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean allPlayerReadyForQuestion() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getCurrentQuestionIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setCurrentQuestionIndex(int index) {
        // TODO Auto-generated method stub

    }
}
