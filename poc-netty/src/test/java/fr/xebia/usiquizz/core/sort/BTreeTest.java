package fr.xebia.usiquizz.core.sort;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * Test the Btree
 *
 * User: slm
 * Date: 28/03/11
 * Time: 20:14
 */
public class BTreeTest {

    @Test
    public void simpleInsertion(){
        BTree<Integer> tree = new BTree<Integer>();
        tree.insert(10);
        tree.insert(12);
        tree.insert(1);
        tree.insert(14);
        tree.insert(5);
        tree.insert(42);
        tree.insert(43);
        tree.insert(13);


        Assert.assertEquals("Should be 8", 8, tree.size());
        NodeSet<Integer> set =  tree.getMaxSet();

        Assert.assertEquals("43", ""+set.prev());

        Assert.assertEquals("42", ""+set.prev());

        Assert.assertEquals("14", ""+set.prev());

        Assert.assertEquals("13", ""+set.prev());

        Assert.assertEquals("12", ""+set.prev());

        Assert.assertEquals("10", ""+set.prev());

        Assert.assertEquals("5", ""+set.prev());

        Assert.assertEquals("1", ""+set.prev());


        tree.delete(13);
        tree.delete(5);
        tree.delete(43);

        Assert.assertEquals("Should be 5", 5, tree.size());
        set =  tree.getMaxSet();

        Assert.assertEquals("42", ""+set.prev());

        Assert.assertEquals("14", ""+set.prev());

        Assert.assertEquals("12", ""+set.prev());

        Assert.assertEquals("10", ""+set.prev());

        Assert.assertEquals("1", ""+set.prev());
        Assert.assertNull(set.prev());

        tree.insert(13);
        tree.insert(5);
        tree.insert(45);
        tree.insert(6);
        tree.insert(9);

        set = tree.getMinSet();
        Assert.assertEquals("1", ""+set.next());

        Assert.assertEquals("5", ""+set.next());
        Assert.assertEquals("6", ""+set.next());
        Assert.assertEquals("9", ""+set.next());
        Assert.assertEquals("10", ""+set.next());

        Assert.assertEquals("12", ""+set.next());

        Assert.assertEquals("13", ""+set.next());

        Assert.assertEquals("14", ""+set.next());
        Assert.assertEquals("42", ""+set.next());
        Assert.assertEquals("45", ""+set.next());
        Assert.assertNull(set.next());
    }

    /**
     * Test 10k melted insertions
     */
    @Test
    public void insertionLoadTest(){
        BTree<Integer> tree = new BTree<Integer>();
        int size = 10000;
        int i = 0, j = size;
        while (i != j){
            tree.insert(i);
            tree.insert(j);
            i++;
            j--;
        }

        // check from max
        j = size;
        NodeSet<Integer> set =  tree.getMaxSet();
        while(j != 0){
            Assert.assertEquals(""+j, ""+set.prev());

            j--;
        }

        // check from min
        j = 0;
        set =  tree.getMinSet();
        while(j != size){
            Assert.assertEquals(""+j, ""+set.next());
            j++;
        }

    }

    /**
     * Test 1250 deletion in a 10k tree
     */
    @Test
    public void insertionDeletionLoadTest(){
        BTree<Integer> tree = new BTree<Integer>();
        int size = 10000;
        int i = 0, j = size;
        while (i <= size){
            tree.insert(i);
            i++;
        }

        i = 250;
        while (i <= 1500){
            tree.delete(i);
            i++;
        }

        // check from max
        j = 10000;
        NodeSet<Integer> set =  tree.getMaxSet();
        while(j > 1500 ){
            Assert.assertEquals(""+j, ""+set.prev());
            j--;
        }

        j= 249;
        while(j >= 0){
            Assert.assertEquals(""+j, ""+set.prev());
            j--;
        }

    }
}
