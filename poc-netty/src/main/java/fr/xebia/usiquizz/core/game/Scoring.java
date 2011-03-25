package fr.xebia.usiquizz.core.game;

import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import fr.xebia.usiquizz.core.persistence.Joueur;

import java.util.List;

public interface Scoring {

    void createScore(String sessionKey);

    Score getCurrentScore(String sessionId);

    Score getCurrentScoreByEmail(String email);

    // Compute new score and store choice then return result
    byte addScore(String sessionId, byte choice, boolean good, byte index);

    byte[] getAnswers(String email);

    boolean isPlayerAlreadyAnswered(String sessionKey, byte currentQuestion);

    // temporary Called at the end of game 
    void calculRanking();

    List<Joueur> getTop100();

    List<Joueur> get50Prec(String email);

    List<Joueur> get50Suiv(String email);
}
