package fr.xebia.usiquizz.core.sort;

/**
 * Created by IntelliJ IDEA.
 * User: slm
 * Date: 27/03/11
 * Time: 19:42
 * To change this template use File | Settings | File Templates.
 */
public interface NodeStore<T extends Comparable<T>> {

    /**
     * Retrieves the node in the Store from it's key
     * @param key
     * @return
     */
    Node<T> get(T key);

    /**
     * Update the node in the store
     * @param node
     */
    void update(Node<T> node);

    /**
     * deletes node from the store
     * @param key
     */
    void delete(T key);

    void updateMax(Node<T> max);

    void updateMin(Node<T> min);

    void updateRoot(Node<T> root);

    Node<T> getRoot();

    Node<T> getMax();

    Node<T> getMin();

    /**
     * Flags the dataStore to mark running Tree modification
     */
    void startModification();

    /**
     * Flags the dataStore so the started modification is known to be finished correctly
     */
    void finishModification();

    /**
     * Tells wether the tree is currently in modification
     * @return
     */
    boolean hasRunningModification();
    
}
