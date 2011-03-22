package fr.xebia.usiquizz.core.game;

import fr.xebia.usiquizz.core.persistence.GemfireRepository;

public interface Scoring {

    void createScore(String sessionKey);

    Score getCurrentScore(String sessionId);

    byte addScore(String sessionId, boolean good, byte index);

    boolean isPlayerAlreadyAnswered(String sessionKey, byte currentQuestion);

    // temporary Called at the end of game 
    void calculRanking();
}
