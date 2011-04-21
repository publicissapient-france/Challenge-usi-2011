package fr.xebia.usiquizz.core.game;

import static fr.xebia.usiquizz.core.persistence.GemfireRepository.*;

import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import fr.xebia.usiquizz.core.persistence.Joueur;
import fr.xebia.usiquizz.core.persistence.User;
import fr.xebia.usiquizz.core.sort.LocalBTree;
import fr.xebia.usiquizz.core.sort.NodeSet;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedScoring implements Scoring {

    private GemfireRepository gemfireRepository;

    private List<Joueur> top100;

    private ConcurrentSkipListSet<Joueur> tree;


    public DistributedScoring(GemfireRepository gemfireRepository) {
        this.gemfireRepository = gemfireRepository;
    }

    @Override
    public void createScore(String sessionKey, User user) {
        gemfireRepository.createScoreAsync(sessionKey, user);
    }

    @Override
    public Score getCurrentScore(String sessionId) {
        Score tmp = gemfireRepository.getScoreRegion().get(sessionId);
        if (tmp == null) {
            tmp = gemfireRepository.getScoreFinalRegion().get(sessionId);
        }
        return tmp;
    }

    @Override
    public Score getCurrentScoreByEmail(String email) {
        return getCurrentScore(Integer.toString(email.hashCode()));
    }

    @Override
    public byte addScore(String sessionId, byte choice, boolean good, byte index) {
        Score score = gemfireRepository.getScoreRegion().get(sessionId);
        score.addResponse(choice, good, index);
        gemfireRepository.writeAsyncScore(sessionId, score);
        return score.getCurrentScore();
    }

    @Override
    public boolean isPlayerAlreadyAnswered(String sessionKey, String currentQuestion) {
        return getCurrentScore(sessionKey).isAlreadyAnswer(currentQuestion);
    }


    @Override
    public void calculRanking() {
        for (String sessionKey : gemfireRepository.getScoreRegion().keySet()) {
            Score score = gemfireRepository.getScoreRegion().get(sessionKey);
            tree.add(new Joueur(score.getCurrentScore(), score.lname, score.fname, score.email));
        }
    }

    @Override
    public void reconstructRanking() {
        for (String sessionKey : gemfireRepository.getScoreFinalRegion().keySet()) {
            Score score = gemfireRepository.getScoreFinalRegion().get(sessionKey);
            tree.add(new Joueur(score.getCurrentScore(), score.lname, score.fname, score.email));
        }
    }


    @Override
    public List<Joueur> getTop100() {

        if (top100 == null) {
            List<Joueur> res = new ArrayList<Joueur>();
            Joueur joueur;
            Iterator<Joueur> itJoueur = tree.iterator();
            int i = 0;
            while (i < 100) {
                try {
                    joueur = itJoueur.next();
                } catch (NoSuchElementException e) {
                    break;
                }
                res.add(joueur);
                i++;
            }
            top100 = res;
        }
        return top100;
    }

    @Override
    public List<Joueur> get50Prec(String sessionKey) {

        Score score = getCurrentScore(sessionKey);
        Joueur joueur = new Joueur(score.getCurrentScore(), score.lname, score.fname, score.email);

        List<Joueur> res = new ArrayList<Joueur>();
        Iterator<Joueur> itJoueur = tree.headSet(joueur, true).descendingIterator();
        itJoueur.next();
        int i = 0;
        while (i < 5) {
            try {
                joueur = itJoueur.next();
            } catch (NoSuchElementException e) {
                break;
            }
            res.add(joueur);
            i++;
        }
        Collections.reverse(res);
        return res;
    }

    @Override
    public List<Joueur> get50Suiv(String sessionKey) {
        Score score = getCurrentScore(sessionKey);
        Joueur joueur = new Joueur(score.getCurrentScore(), score.lname, score.fname, score.email);

        List<Joueur> res = new ArrayList<Joueur>();
        Iterator<Joueur> itJoueur = tree.tailSet(joueur, true).iterator();
        itJoueur.next();
        int i = 0;
        while (i < 5) {
            try {
                joueur = itJoueur.next();
            } catch (NoSuchElementException e) {
                break;
            }
            res.add(joueur);
            i++;
        }
        return res;
    }

    public byte[] getAnswers(String sessionKey) {
        return getCurrentScore(sessionKey).getReponse();
    }


    @Override
    public void setTree(ConcurrentSkipListSet<Joueur> tree) {
        this.tree = tree;
    }

    @Override
    public void init(){
        top100 = null;
    }
}
