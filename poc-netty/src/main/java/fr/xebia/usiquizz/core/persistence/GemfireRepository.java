package fr.xebia.usiquizz.core.persistence;

import com.esotericsoftware.kryo.util.IntHashMap;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.usi.Questiontype;
import fr.xebia.usiquizz.core.game.Score;

import java.util.List;
import java.util.Map;

public class GemfireRepository {

    private Cache cache = new CacheFactory()
            .set("cache-xml-file", "gemfire/cache.xml")
            .create();

    // Region for user storage
    private Region<String, byte[]> userRegion = cache.getRegion("user-region");

    // Region for the current game
    private Region<String, Object> gameRegion = cache.getRegion("game-region");
    private Region<String, Questiontype> questionRegion = cache.getRegion("question-region");
    private Region<String, String> playerRegion = cache.getRegion("player-region");
    private Region<String, String> currentQuestionRegion = cache.getRegion("current-question-region");
    private Map<Byte, Byte> questionStatusRegion = cache.getRegion("question-status");

    // Region for score
    private Region<String, Score> scoreRegion = cache.getRegion("score-region");

    public Cache getCache() {
        return cache;
    }

    public Region<String, Object> getGameRegion() {
        return gameRegion;
    }

    public Region<String, Questiontype> getQuestionRegion() {
        return questionRegion;
    }

    public Region<String, String> getPlayerRegion() {
        return playerRegion;
    }

    public Region<String, String> getCurrentQuestionRegion() {
        return currentQuestionRegion;
    }

    public Region<String, byte[]> getUserRegion() {
        return userRegion;
    }

    public Region<String, Score> getScoreRegion() {
        return scoreRegion;
    }

    public Map<Byte, Byte> getQuestionStatusRegion() {
        return questionStatusRegion;
    }
}
