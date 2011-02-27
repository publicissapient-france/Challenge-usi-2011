package fr.xebia.usiquizz.core.game.local;

import com.usi.Question;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.game.Game;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GameLocalInstance implements Game {

    private Sessiontype sessionType;

    private ConcurrentHashMap<String, Void> sessions = new ConcurrentHashMap<String, Void>();

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

    public boolean getFlushusertable() {
        return false;
    }

    public String getTrackeduseridmail() {
        return "";
    }

    public void addPlayer(String session) {
        sessions.put(session, null);
    }

    @Override
    public int getUserConnected() {
        return sessions.size();
    }

    @Override
    public Question getQuestion(int index) {
        return sessionType.getQuestions().get(index).getQuestion();
    }
}
