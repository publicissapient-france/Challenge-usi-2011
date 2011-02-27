package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.game.Game;

import java.util.List;

public class DistributedGame implements Game {

    private static final String LONG_POOLING_DURATION = "long-pooling-duration";
    private static final String NB_USERS_THRESOLD = "nb-users-thresold";
    private static final String QUESTION_TIME_FRAME = "question-time-frame";
    private static final String NB_QUESTIONS = "nb-questions";
    private static final String FLUSH_USER_TABLE = "flush-user-table";
    private static final String TRACKED_USER_IDMAIL = "tracked-user-idmail";
    private static final String QUESTION_LIST = "question_list";

    Cache cache = new CacheFactory()
            .set("cache-xml-file", "gemfire/cache.xml")
            .create();
    Region<String, Object> gameRegion = cache.getRegion("game-region");
    Region<String, List<Questiontype>> questionRegion = cache.getRegion("question-region");
    Region<String, String> playerRegion = cache.getRegion("player-region");


    @Override
    public void init(Sessiontype st) {
        gameRegion.put(LONG_POOLING_DURATION, st.getParameters().getLongpollingduration());
        gameRegion.put(NB_USERS_THRESOLD, st.getParameters().getNbusersthresold());
        gameRegion.put(QUESTION_TIME_FRAME, st.getParameters().getQuestiontimeframe());
        gameRegion.put(NB_QUESTIONS, st.getParameters().getNbquestions());
        gameRegion.put(FLUSH_USER_TABLE, st.getParameters().isFlushusertable());
        gameRegion.put(TRACKED_USER_IDMAIL, st.getParameters().getTrackeduseridmail());
        questionRegion.put(QUESTION_LIST, st.getQuestions());
    }

    @Override
    public int getLongpollingduration() {
        return ((Integer)gameRegion.get(LONG_POOLING_DURATION)).intValue();
    }

    @Override
    public int getNbusersthresold() {
        return ((Integer)gameRegion.get(NB_USERS_THRESOLD)).intValue();
    }

    @Override
    public int getQuestiontimeframe() {
        return ((Integer)gameRegion.get(QUESTION_TIME_FRAME)).intValue();
    }

    @Override
    public int getNbquestions() {
        return ((Integer)gameRegion.get(NB_QUESTIONS)).intValue();
    }

    @Override
    public boolean getFlushusertable() {
        return ((Boolean)gameRegion.get(FLUSH_USER_TABLE)).booleanValue();
    }

    @Override
    public String getTrackeduseridmail() {
        return ((String)gameRegion.get(TRACKED_USER_IDMAIL));
    }

    @Override
    public void addPlayer(String sessionId) {
        playerRegion.put(sessionId, sessionId);
    }

    @Override
    public int getUserConnected() {
        return playerRegion.size();
    }

    @Override
    public Question getQuestion(int index) {
        return questionRegion.get(QUESTION_LIST).get(index).getQuestion();
    }
}
