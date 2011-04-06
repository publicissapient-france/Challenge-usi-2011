package fr.xebia.usiquizz.core.sort;

import fr.xebia.usiquizz.core.game.gemfire.DistributedNodeScoreStore;
import fr.xebia.usiquizz.core.persistence.GemfireRepository;
import fr.xebia.usiquizz.core.persistence.Joueur;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: slm
 * Date: 06/04/11
 * Time: 00:55
 * To change this template use File | Settings | File Templates.
 */
public class GemfireBTreeTest {

        @Test
    public void simpleInsertion(){
            GemfireRepository repository = new GemfireRepository();
            DistributedNodeScoreStore nodeStore = new DistributedNodeScoreStore(repository.getScoreStoreRegion());
            RBTree<Joueur> tree = new RBTree<Joueur>(nodeStore);



        long start = System.currentTimeMillis();
        int size = 10000;
        int i = 0, j = size;
        while (i <= j){
            tree.insert(new Joueur(i, "", "", ""));
          //  System.out.println("inserted Joueur "+i);
            tree.insert(new Joueur(j, "", "", ""));
           // System.out.println("inserted Joueur "+j);
            i++;
            j--;
        }
        long stop = System.currentTimeMillis();

        System.out.println("Took "+ (stop - start) + " ms for "+size+" users ...");
        // check from max
        j = size;
        NodeSet<Joueur> set =  tree.getMaxSet();
        while(j != 0){
            Assert.assertEquals("" + j, "" + set.prev().getScore());

            j--;
        }

        // check from min
        j = 0;
        set =  tree.getMinSet();
        while(j != size){
            Assert.assertEquals(""+j, ""+set.next().getScore());
            j++;
        }


    }
}
