package fr.xebia.usiquizz.core.game;


import fr.xebia.usiquizz.core.persistence.GemfireRepository;

public class DistributedScoring implements Scoring {

    private GemfireRepository gemfireRepository;

    public DistributedScoring(GemfireRepository gemfireRepository) {
        this.gemfireRepository = gemfireRepository;
    }

    @Override
    public void createScore(String sessionKey) {
        // FIXME pass nb question
        gemfireRepository.getScoreRegion().put(sessionKey, new Score((byte) 1));
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
