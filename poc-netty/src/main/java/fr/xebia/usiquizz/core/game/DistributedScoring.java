package fr.xebia.usiquizz.core.game;

import static fr.xebia.usiquizz.core.persistence.GemfireRepository.*;

import fr.xebia.usiquizz.core.persistence.GemfireRepository;

public class DistributedScoring implements Scoring {

    private GemfireRepository gemfireRepository;

    public DistributedScoring(GemfireRepository gemfireRepository) {
        this.gemfireRepository = gemfireRepository;
    }

    @Override
    public void createScore(String sessionKey) {
        // FIXME pass nb question
        gemfireRepository.getScoreRegion().put(sessionKey, new Score(((Integer) gemfireRepository.getGameRegion().get(NB_QUESTIONS)).byteValue()));
    }

    @Override
    public Score getCurrentScore(String sessionId) {
        return gemfireRepository.getScoreRegion().get(sessionId);
    }

    @Override
    public byte addScore(String sessionId, boolean good, byte index) {
        Score score = gemfireRepository.getScoreRegion().get(sessionId);
        score.addResponse(good, index);
        gemfireRepository.getScoreRegion().put(sessionId, score);
        return score.getCurrentScore();
    }

    @Override
    public boolean isPlayerAlreadyAnswered(String sessionKey, byte currentQuestion) {
        return getCurrentScore(sessionKey).isAlreadyAnswer(currentQuestion);
    }


}
