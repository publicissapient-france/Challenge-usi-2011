package fr.xebia.usiquizz.core.game;

import static fr.xebia.usiquizz.core.persistence.GemfireRepository.*;

import fr.xebia.usiquizz.core.game.gemfire.DistributedNodeScoreStore;
import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import fr.xebia.usiquizz.core.persistence.Joueur;
import fr.xebia.usiquizz.core.persistence.User;
import fr.xebia.usiquizz.core.sort.NodeSet;
import fr.xebia.usiquizz.core.sort.RBTree;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedScoring implements Scoring {

    private GemfireRepository gemfireRepository;

    private DistributedNodeScoreStore nodeStore;

    private RBTree<Joueur> tree;

    private List<Joueur> top100;

    // Uniquement pour debug... Nombre de reponse recu sur l'instance
    private ConcurrentHashMap<Byte, AtomicInteger> nbReponse = new ConcurrentHashMap<Byte, AtomicInteger>();

    public DistributedScoring(GemfireRepository gemfireRepository) {
        this.gemfireRepository = gemfireRepository;
        this.nodeStore = new DistributedNodeScoreStore(this.gemfireRepository.getScoreStoreRegion());
        this.tree = new RBTree<Joueur>(this.nodeStore);
    }

    @Override
    public void createScore(String sessionKey, User user) {

       // String email = gemfireRepository.getPlayerRegion().get(sessionKey);
        gemfireRepository.createScore(sessionKey, user);
        if (top100 != null){
            top100 = null;
        }
    }

    @Override
    public Score getCurrentScore(String sessionId) {
        return gemfireRepository.getScoreRegion().get(sessionId);
    }

    @Override
    public Score getCurrentScoreByEmail(String email) {
        return gemfireRepository.getScoreRegion().get(email.hashCode());
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
    public void calculRanking(){
         for (String sessionKey : gemfireRepository.getScoreRegion().keySet()) {
            Score score = gemfireRepository.getScoreRegion().get(sessionKey);
            tree.insert(new Joueur(score.getCurrentScore(), score.lname, score.fname, sessionKey));
        }
    }



    @Override
    public List<Joueur> getTop100() {

        if (top100 == null) {
            List<Joueur> res = new ArrayList<Joueur>();
            NodeSet<Joueur> set = tree.getMaxSet();
            Joueur joueur = null;
            int i = 0;
            while (i < 100 ){
                joueur = set.prev();
                if (joueur == null)
                    break;
                res.add(joueur);
                i++;
            }
            top100 = res;
        }
        return top100;
    }

    @Override
    public List<Joueur> get50Prec(String email) {

        Score score = gemfireRepository.getScoreRegion().get(email);
        Joueur joueur = new Joueur(score.getCurrentScore(), score.lname, score.fname, email);

        List<Joueur> res = new ArrayList<Joueur>();
        NodeSet<Joueur> set = tree.getSet(joueur);
        int i = 0;
        while (i < 50 ){
            joueur = set.prev();
            if (joueur == null)
                break;
            res.add(joueur);
            i++;
        }
        return res;
    }

    @Override
    public List<Joueur> get50Suiv(String email) {
        Score score = gemfireRepository.getScoreRegion().get(email);
        Joueur joueur = new Joueur(score.getCurrentScore(), score.lname, score.fname, email);

        List<Joueur> res = new ArrayList<Joueur>();
        NodeSet<Joueur> set = tree.getSet(joueur);
        int i = 0;
        while (i < 50 ){
            joueur = set.next();
            if (joueur == null)
                break;
            res.add(joueur);
            i++;
        }
        return res;
    }

    public byte[] getAnswers(String email) {
        return getCurrentScoreByEmail(email).getReponse();
    }

}
