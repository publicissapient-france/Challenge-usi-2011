package fr.xebia.usiquizz.core.game;

import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import fr.xebia.usiquizz.core.persistence.Joueur;
import fr.xebia.usiquizz.core.persistence.User;
import fr.xebia.usiquizz.core.sort.LocalBTree;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public interface Scoring {

    void createScore(String sessionKey, User user);

    Score getCurrentScore(String sessionId);

    Score getCurrentScoreByEmail(String email);

    // Compute new score and store choice then return result
    byte addScore(String sessionId, byte choice, boolean good, byte index);

    byte[] getAnswers(String sessionKey);

    boolean isPlayerAlreadyAnswered(String sessionKey, String currentQuestion);

    // temporary Called at the end of game 
    void calculRanking();

    void reconstructRanking();

    List<Joueur> getTop100();

    List<Joueur> get50Prec(String sessionKey);

    List<Joueur> get50Suiv(String sessionKey);

    void setTree(ConcurrentSkipListSet<Joueur> tree);

    void init();
}
