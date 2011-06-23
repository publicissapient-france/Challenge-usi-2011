package fr.xebia.usiquizz.core.sort;


import fr.xebia.usiquizz.core.persistence.Joueur;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JoueurSort {

    @Test
    public void testCompare() {
        Joueur j1 = new Joueur(1, "aa", "aa", "aa.aa");
        Joueur j2 = new Joueur(1, "zz", "aa", "zz.aa");
        Joueur j3 = new Joueur(2, "aa", "aa", "aa.aa");
        Joueur j4 = new Joueur(3, "aa", "aa", "aa.aa");
        Joueur j5 = new Joueur(4, "aa", "aa", "aa.aa");
        Joueur j6 = new Joueur(4, "aa", "zz", "aa.zz");
        Joueur j7 = new Joueur(4, "zz", "aa", "zz.aa");
        Joueur j8 = new Joueur(5, "aa", "aa", "aa.aa");

        LocalBTree<Joueur> tree = new LocalBTree<Joueur>();
        tree.insert(j1);
        tree.insert(j2);
        tree.insert(j3);
        tree.insert(j4);
        tree.insert(j5);
        tree.insert(j6);
        tree.insert(j7);
        tree.insert(j8);

        NodeSet<Joueur> set = tree.getSet(j4);
        System.out.println("Prev");
        List<Joueur> before = new ArrayList<Joueur>();
        before.add(set.prev());
        before.add(set.prev());
        before.add(set.prev());
        before.add(set.prev());
        Collections.reverse(before);
        for (Joueur j : before) {
            System.out.println(j);
        }

        set = tree.getSet(j4);
        System.out.println("Next");
        System.out.println(set.next());
        System.out.println(set.next());
        System.out.println(set.next());

    }
}
