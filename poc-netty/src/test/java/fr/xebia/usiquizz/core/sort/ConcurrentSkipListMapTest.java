package fr.xebia.usiquizz.core.sort;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentSkipListMap;

public class ConcurrentSkipListMapTest {

    /**
     * Test 200k melted insertions
     */
    @Test
    public void insertionLoadTest() {
        String content = "sdfdskfmlsgdfsldklgjdmfsgj;mcdfkdfgjmeiorgumdlisgjdimsgljdlfkmsgjdsfv";
        // Compare with TreeSet
        ConcurrentSkipListMap<Integer, byte[]> treeset = new ConcurrentSkipListMap<Integer, byte[]>();
        int size = 1000000;
        int i = 0, j = size;
        long start = System.nanoTime();
        long end;
        while (i <= j) {
            treeset.put(i, content.getBytes());
            treeset.put(j, content.getBytes());
            i++;
            j--;
        }
        end = System.nanoTime();
        System.out.println("Insert " + size + " in ConcurrentSkipListMap take : " + (end - start) / 1000000 + " ms");

    }


}
