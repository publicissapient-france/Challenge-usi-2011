package fr.xebia.usiquizz.core.game;

import static fr.xebia.usiquizz.core.persistence.GemfireRepository.*;

import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import fr.xebia.usiquizz.core.persistence.Joueur;

import java.util.ArrayList;
import java.util.List;
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
        String email = gemfireRepository.getPlayerRegion().get(sessionKey);
        gemfireRepository.getScoreRegion().put(email, new Score(((Integer) gemfireRepository.getGameRegion().get(NB_QUESTIONS)).byteValue()));
    }

    @Override
    public Score getCurrentScore(String sessionId) {
        String email = gemfireRepository.getPlayerRegion().get(sessionId);
        return gemfireRepository.getScoreRegion().get(email);
    }

    @Override
    public Score getCurrentScoreByEmail(String email) {
        return gemfireRepository.getScoreRegion().get(email);
    }

    @Override
    public byte addScore(String sessionId, byte choice, boolean good, byte index) {
        String email = gemfireRepository.getPlayerRegion().get(sessionId);
        Score score = gemfireRepository.getScoreRegion().get(email);
        score.addResponse(choice, good, index);
        gemfireRepository.getScoreRegion().put(email, score);
        return score.getCurrentScore();
    }

    @Override
    public boolean isPlayerAlreadyAnswered(String sessionKey, byte currentQuestion) {
        return getCurrentScore(sessionKey).isAlreadyAnswer(currentQuestion);
    }

    @Override
    public void calculRanking() {
        TreeSet<Joueur> set = new TreeSet<Joueur>(new Joueur.JoueurComparator());
        for (String email : gemfireRepository.getScoreRegion().keySet()) {
            Score score = gemfireRepository.getScoreRegion().get(email);
            set.add(new Joueur(score.getCurrentScore(), "", "", email));
        }

        int i = 1;
        for (Joueur j : set) {
            gemfireRepository.getFinalRankingRegion().put(i, j);
            gemfireRepository.getRanking().put(j.getEmail(), i);
            i++;
        }
    }

    @Override
    public List<Joueur> getTop100() {
        List<Joueur> res = new ArrayList<Joueur>();
        for (int i = 1; i <= 100; i++) {
            Joueur j = gemfireRepository.getFinalRankingRegion().get(i);
            if (j != null) {
                res.add(j);
            } else {
                break;
            }
        }
        return res;
    }

    @Override
    public List<Joueur> get50Prec(String email) {

        int ranking = gemfireRepository.getRanking().get(email);
        List<Joueur> res = new ArrayList<Joueur>();

        for (int i = ranking - 1; i >= ranking - 50; i--) {
            Joueur j = gemfireRepository.getFinalRankingRegion().get(i);
            if (j == null) {
                break;
            }
            res.add(j);
        }
        return res;
    }

    @Override
    public List<Joueur> get50Suiv(String email) {
        int ranking = gemfireRepository.getRanking().get(email);
        List<Joueur> res = new ArrayList<Joueur>();

        for (int i = ranking + 1; i <= ranking + 50; i++) {
            Joueur j = gemfireRepository.getFinalRankingRegion().get(i);
            if (j == null) {
                break;
            }
            res.add(j);
        }
        return res;
    }

    public byte[] getAnswers(String email){
        return getCurrentScoreByEmail(email).getReponse();
    }

}
