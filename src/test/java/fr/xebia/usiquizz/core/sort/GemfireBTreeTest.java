package fr.xebia.usiquizz.core.sort;

import fr.xebia.usiquizz.core.persistence.Joueur;
import org.junit.Assert;
import org.junit.Test;

public class GemfireBTreeTest {

    @Test
    public void simpleInsertion() {
        LocalBTree<Joueur> tree = new LocalBTree<Joueur>();


        long start = System.currentTimeMillis();
        int size = 10000;
        int i = 0, j = size;
        while (i <= j) {
            tree.insert(new Joueur(i, "", "", ""));
            //  System.out.println("inserted Joueur "+i);
            tree.insert(new Joueur(j, "", "", ""));
            // System.out.println("inserted Joueur "+j);
            i++;
            j--;
        }
        long stop = System.currentTimeMillis();

        System.out.println("Took " + (stop - start) + " ms for " + size + " users ...");
        // check from max
        j = size;
        NodeSet<Joueur> set = tree.getMinSet();
        while (j != 0) {
            Assert.assertEquals("" + j, "" + set.next().getScore());

            j--;
        }

        // check from min
        j = 0;
        set = tree.getMaxSet();
        while (j != size) {
            Assert.assertEquals("" + j, "" + set.prev().getScore());
            j++;
        }


    }
}
