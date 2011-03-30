package fr.xebia.usiquizz.core.sort;

/**
 * Created by IntelliJ IDEA.
 * User: slm
 * Date: 27/03/11
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class Node<T extends Comparable<T>> {

    public T key;

    T parent;

    T left;

    T right;

    public int color;

    public Node(T information, T parent) {
        this.key = information;
        this.parent = parent;
        this.left = null;
        this.right = null;
    }

    public Node(T key, int nodeColor, T left, T right, NodeStore<T> store) {
        this.key = key;
        this.color = nodeColor;
        this.left = left;
        this.right = right;
        if (left  != null){
            Node<T> nLeft = store.get(left);
            nLeft.parent = this.key;
            store.update(nLeft);
        }
        if (right != null){
            Node<T> nRight = store.get(right);
            nRight.parent = this.key;
            store.update(nRight);
        }
        this.parent = null;
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

    boolean isLeftNode(NodeStore<T> ns) {
        return (key.equals(ns.get(parent).left));
    }

    boolean isRightNode(NodeStore<T> ns) {
        return (key.equals(ns.get(parent).right));
    }


    public Node<T> grandparent(NodeStore<T> store) {
        assert parent != null; // Not the root node
        Node<T> nParent = store.get(parent);
        assert nParent.parent != null; // Not child of root
        return store.get(nParent.parent);
    }

    public Node<T> sibling(NodeStore<T> store) {
        assert parent != null; // Root node has no sibling
        Node<T> nParent = store.get(parent);
        if (this.key.equals(nParent.left))
            return store.get(nParent.right);
        else
            return store.get(nParent.left);
    }

    public Node<T> uncle(NodeStore<T> store) {
        assert parent != null; // Root node has no uncle
         Node<T> nParent = store.get(parent);
        assert nParent.parent != null; // Children of root have no uncle
        return nParent.sibling(store);
    }
}
