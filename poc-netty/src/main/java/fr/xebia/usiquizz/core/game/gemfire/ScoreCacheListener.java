package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import fr.xebia.usiquizz.core.game.Score;
import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import fr.xebia.usiquizz.core.persistence.Joueur;
import fr.xebia.usiquizz.core.sort.LocalBTree;
import fr.xebia.usiquizz.core.sort.RBTree;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: slm
 * Date: 31/03/11
 * Time: 22:55
 * To change this template use File | Settings | File Templates.
 */
public class ScoreCacheListener extends CacheListenerAdapter<String, Score> {

    private LocalBTree<Joueur> tree;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    public ScoreCacheListener(LocalBTree<Joueur> tree) {
        this.tree = tree;
    }

    /**
     * Compute final ranking after score has been added to final score region
     * if and only if we own the score lock
     *
     * @param entryEvent
     */
    @Override
    public void afterCreate(final EntryEvent<String, Score> entryEvent) {
        // Don't block gemfire thread
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                    Score score = entryEvent.getNewValue();
                    tree.insert(new Joueur(score.getCurrentScore(), score.lname, score.fname, score.email));
            }
        });

    }
}
