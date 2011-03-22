package fr.xebia.usiquizz.core.game;

import static fr.xebia.usiquizz.core.persistence.GemfireRepository.*;

import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import fr.xebia.usiquizz.core.persistence.Joueur;

import java.util.TreeMap;
import java.util.TreeSet;

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

    @Override
    public void calculRanking() {
        TreeSet<Joueur> set = new TreeSet<Joueur>(new Joueur.JoueurComparator());
        for (String sessionId : gemfireRepository.getScoreRegion().keySet()) {
            Score score = gemfireRepository.getScoreRegion().get(sessionId);
            set.add(new Joueur(score.getCurrentScore(), "", "", gemfireRepository.getPlayerRegion().get(sessionId), sessionId));
        }

        int i = 1;
        for (Joueur j : set) {
            gemfireRepository.getFinalRankingRegion().put(i, j);
            gemfireRepository.getRanking().put(j.getSessionId(), i);
            i++;
        }
    }


}
