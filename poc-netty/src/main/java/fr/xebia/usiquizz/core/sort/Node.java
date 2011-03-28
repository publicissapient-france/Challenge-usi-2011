package fr.xebia.usiquizz.core.sort;

/**
 * Created by IntelliJ IDEA.
 * User: slm
 * Date: 27/03/11
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class Node<T extends Comparable<T>, V> {

    T information;

    V value;


    T parent;

    T left;

    T right;

    char balance;

    public Node(T information, T parent, V value) {
        this.information = information;
        this.parent = parent;
        this.left = null;
        this.right = null;
        this.balance = '_';
        this.value = value;
    }

    boolean isLeaf() {
        return ((left == null) && (right == null));
    }

    boolean isNode() {
        return !isLeaf();
    }

    boolean hasLeftNode() {
        return (null != left);
    }

    boolean hasRightNode() {
        return (right != null);
    }

    boolean isLeftNode(NodeStore<T,V> ns) {
        return (information.equals(ns.get(parent).left));
    }

    boolean isRightNode(NodeStore<T,V> ns) {
        return (information.equals(ns.get(parent).right));
    }

}
