package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.usi.Question;
import com.usi.Questiontype;
import com.usi.Sessiontype;
import fr.xebia.usiquizz.core.game.Game;
import fr.xebia.usiquizz.core.persistence.GemfireRepository;

import java.util.List;

public class DistributedGame implements Game {

    private static final String LONG_POOLING_DURATION = "long-pooling-duration";
    private static final String NB_USERS_THRESOLD = "nb-users-thresold";
    private static final String QUESTION_TIME_FRAME = "question-time-frame";
    private static final String NB_QUESTIONS = "nb-questions";
    private static final String FLUSH_USER_TABLE = "flush-user-table";
    private static final String TRACKED_USER_IDMAIL = "tracked-user-idmail";
    private static final String QUESTION_LIST = "question_list";
    private static final String CURRENT_QUESTION_INDEX = "current-question-index";


    private GemfireRepository gemfireRepository;

    public DistributedGame(GemfireRepository gemfireRepository) {
        this.gemfireRepository = gemfireRepository;
    }

    @Override
    public void init(Sessiontype st) {
        gemfireRepository.getGameRegion().put(LONG_POOLING_DURATION, st.getParameters().getLongpollingduration());
        gemfireRepository.getGameRegion().put(NB_USERS_THRESOLD, st.getParameters().getNbusersthresold());
        gemfireRepository.getGameRegion().put(QUESTION_TIME_FRAME, st.getParameters().getQuestiontimeframe());
        gemfireRepository.getGameRegion().put(NB_QUESTIONS, st.getParameters().getNbquestions());
        gemfireRepository.getGameRegion().put(FLUSH_USER_TABLE, st.getParameters().isFlushusertable());
        gemfireRepository.getGameRegion().put(TRACKED_USER_IDMAIL, st.getParameters().getTrackeduseridmail());
        gemfireRepository.getGameRegion().put(CURRENT_QUESTION_INDEX, 1);
        gemfireRepository.getQuestionRegion().put(QUESTION_LIST, st.getQuestions());
    }

    @Override
    public int getLongpollingduration() {
        return ((Integer) gemfireRepository.getGameRegion().get(LONG_POOLING_DURATION)).intValue();
    }

    @Override
    public int getNbusersthresold() {
        return ((Integer) gemfireRepository.getGameRegion().get(NB_USERS_THRESOLD)).intValue();
    }

    @Override
    public int getQuestiontimeframe() {
        return ((Integer) gemfireRepository.getGameRegion().get(QUESTION_TIME_FRAME)).intValue();
    }

    @Override
    public int getNbquestions() {
        return ((Integer) gemfireRepository.getGameRegion().get(NB_QUESTIONS)).intValue();
    }

    @Override
    public boolean getFlushusertable() {
        return ((Boolean) gemfireRepository.getGameRegion().get(FLUSH_USER_TABLE)).booleanValue();
    }

    @Override
    public String getTrackeduseridmail() {
        return ((String) gemfireRepository.getGameRegion().get(TRACKED_USER_IDMAIL));
    }

    @Override
    public void addPlayer(String sessionId, String email) {
        gemfireRepository.getPlayerRegion().put(sessionId, email);
    }

    @Override
    public int countUserConnected() {
        return gemfireRepository.getPlayerRegion().size();
    }

    @Override
    public void addPlayerForCurrentQuestion(String sessionId) {
        gemfireRepository.getCurrentQuestionRegion().put(sessionId, "");
    }

    @Override
    public int countUserForCurrentQuestion() {
        return gemfireRepository.getCurrentQuestionRegion().size();
    }

    @Override
    public void emptyCurrentQuestion() {
        gemfireRepository.getCurrentQuestionRegion().clear();
    }

    @Override
    public Question getQuestion(int index) {
        // -1 difference between spec and list implementation
        return gemfireRepository.getQuestionRegion().get(QUESTION_LIST).get(index - 1).getQuestion();
    }

    @Override
    public boolean allPlayerReadyForQuestion() {
        return gemfireRepository.getCurrentQuestionRegion().size() >= getNbusersthresold();
    }

    @Override
    public int getCurrentQuestionIndex() {
        return ((Integer) gemfireRepository.getGameRegion().get(CURRENT_QUESTION_INDEX)).intValue();
    }

    @Override
    public void setCurrentQuestionIndex(int index) {
        gemfireRepository.getGameRegion().put(CURRENT_QUESTION_INDEX, index);
    }
}
