package fr.xebia.usiquizz.core.sort;

/**
 * Created by IntelliJ IDEA.
 * User: slm
 * Date: 27/03/11
 * Time: 19:42
 * To change this template use File | Settings | File Templates.
 */
public interface NodeStore<T extends Comparable<T>, V> {

    /**
     * Retrieves the node in the Store from it's key
     * @param key
     * @return
     */
    Node<T,V> get(T key);

    /**
     * Update the node in the store
     * @param node
     */
    void update(Node<T,V> node);

    /**
     * deletes node from the store
     * @param key
     */
    void delete(T key);

    void updateMax(Node<T,V> max);

    void updateMin(Node<T,V> min);

    Node<T,V> getMax();

    Node<T,V> getMin();
    
}
