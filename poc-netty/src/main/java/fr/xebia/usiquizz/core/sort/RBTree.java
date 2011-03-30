package fr.xebia.usiquizz.core.sort;
/* Copyright (c) 2011 the authors listed at the following URL, and/or
the authors of referenced articles or incorporated external code:
http://en.literateprograms.org/Red-black_tree_(Java)?action=history&offset=20100112141306

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Retrieved from: http://en.literateprograms.org/Red-black_tree_(Java)?oldid=16622
*/

import java.util.HashMap;
import java.util.Map;


public class RBTree<K extends Comparable<K>> {


    static final int BLACK = 0;
    static final int RED = 1;

    public Node<K> root;

    Node<K> min;
    Node<K> max;

    NodeStore<K> store;


    public RBTree() {
        root = null;
        min = null;
        max = null;
        store = new DefaultNodeStore<K>();
    }

    public RBTree(NodeStore<K> store) {
        root = null;
        this.store = store;
    }

    public NodeSet getMaxSet() {
        return new DefaultNodeSet(store.getMax());
    }


    public NodeSet getMinSet() {
        return new DefaultNodeSet(store.getMin());
    }


    public NodeSet getSet(K info) {
        return new DefaultNodeSet(store.get(info));
    }


    private int nodeColor(Node<?> n) {
        return n == null ? BLACK : n.color;
    }

    private int nodeColor(K n) {

        return nodeColor(store.get(n));
    }

    private Node<K> lookupNode(K key) {
        Node<K> n = root;
        while (n != null) {
            int compResult = key.compareTo(n.key);
            if (compResult == 0) {
                return n;
            } else if (compResult < 0) {
                n = store.get(n.left);
            } else {
                assert compResult > 0;
                n = store.get(n.right);
            }
        }
        return n;
    }

    public K lookup(K key) {
        Node<K> n = lookupNode(key);
        return n == null ? null : n.key;
    }

    private void rotateLeft(Node<K> n) {
        Node<K> r = store.get(n.right);
        replaceNode(n, r);
        n.right = r.left;
        if (r.left != null) {
            Node<K> left = store.get(r.left);
            left.parent = n.key;
            store.update(left);
        }
        r.left = n.key;
        n.parent = r.key;
        store.update(r);
        store.update(n);
    }

    private void rotateRight(Node<K> n) {
        Node<K> l = store.get(n.left);
        replaceNode(n, l);
        n.left = l.right;
        if (l.right != null) {
            Node<K> right = store.get(l.right);
            right.parent = n.key;
            store.update(right);
        }
        l.right = n.key;
        n.parent = l.key;
        store.update(n);
        store.update(l);
    }

    private void replaceNode(Node<K> oldn, Node<K> newn) {
        if (oldn.parent == null) {
            root = newn;
            store.updateRoot(root);
        } else {
            Node<K> parent = store.get(oldn.parent);
            if (oldn.key.equals(parent.left))
                parent.left = (newn != null ? newn.key : null);
            else
                parent.right = (newn != null ? newn.key : null);
            store.update(parent);
        }
        if (newn != null) {
            newn.parent = oldn.parent;
            store.update(newn);
        }
    }

    public void insert(K key) {
        store.startModification();

        Node<K> insertedNode = new Node<K>(key, RED, null, null, store);
        if (root == null) {
            min = max = root = insertedNode;

            store.updateMin(root);
            store.updateMax(root);
            store.updateRoot(root);

        } else {
            Node<K> n = root;
            while (true) {
                int compResult = key.compareTo(n.key);
                if (compResult == 0) {
                    return;
                } else if (compResult < 0) {
                    if (n.left == null) {
                        n.left = insertedNode.key;
                        store.update(n);
                        break;
                    } else {
                        n = store.get(n.left);
                    }
                } else {
                    assert compResult > 0;
                    if (n.right == null) {
                        n.right = insertedNode.key;
                        store.update(n);
                        break;
                    } else {
                        n = store.get(n.right);
                    }
                }
            }
            insertedNode.parent = n.key;
            store.update(insertedNode);
        }
        insertCase1(insertedNode);

        // Update min and max if necessary
        if (insertedNode.key.compareTo(max.key) > 0) {
            max = insertedNode;
            store.updateMax(max);
        }

        if (insertedNode.key.compareTo(min.key) < 0) {
            min = insertedNode;
            store.updateMin(min);
        }

        store.finishModification();
    }

    private void insertCase1(Node<K> n) {
        if (n.parent == null) {
            n.color = BLACK;
            store.update(n);
        } else
            insertCase2(n);
    }

    private void insertCase2(Node<K> n) {
        if (nodeColor(store.get(n.parent)) == BLACK)
            return; // Tree is still valid
        else
            insertCase3(n);
    }

    void insertCase3(Node<K> n) {
        if (nodeColor(n.uncle(store)) == RED) {
            Node<K> parent = store.get(n.parent);
            parent.color = BLACK;
            store.update(parent);
            Node<K> uncle = n.uncle(store);
            uncle.color = BLACK;
            store.update(uncle);
            Node<K> grandparent = n.grandparent(store);
            grandparent.color = RED;
            store.update(grandparent);
            insertCase1(n.grandparent(store));
        } else {
            insertCase4(n);
        }
    }

    void insertCase4(Node<K> n) {
        Node<K> parent = store.get(n.parent);
        Node<K> grandPa = n.grandparent(store);
        if (n.key.equals(parent.right) && n.parent.equals(grandPa.left)) {
            rotateLeft(parent);
            n = store.get(n.left);
        } else if (n.key.equals(parent.left) && n.parent.equals(grandPa.right)) {
            rotateRight(parent);
            n = store.get(n.right);
        }
        insertCase5(n);
    }

    void insertCase5(Node<K> n) {
        Node<K> parent = store.get(n.parent);
        parent.color = BLACK;
        store.update(parent);
        Node<K> gparent = n.grandparent(store);
        gparent.color = RED;
        store.update(gparent);
        if (n.key.equals(parent.left) && n.parent.equals(gparent.left)) {
            rotateRight(gparent);
        } else {
            assert n.key.equals(parent.right) && parent.equals(gparent.right);
            rotateLeft(gparent);
        }
    }

    /**
     * Clear the complete Tree 
     */
    public void clear(){
        store.clear();
        min = max = root = null;
    }


    public void delete(K key) {
        store.startModification();
        Node<K> n = lookupNode(key);
        if (n == null)
            return;  // Key not found, do nothing

        // Fix Min and Max if necessary
        if (n.key.equals(max.key)) {
            max = store.get(max.parent);
            store.updateMax(max);
        }
        if (n.key.equals(min.key)) {
            min = store.get(min.parent);
            store.updateMin(min);
        }

        deleteEntry(n);
        store.delete(key);

        store.finishModification();

    }


    /**
     * From TreeMap
     */
    private void deleteEntry(Node<K> p) {
        // decrementSize();

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.left != null && p.right != null) {
            Node<K> s = successor(p);
            updateNodeKey(p, s.key);
            p = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        Node<K> replacement = (p.right == null) ? store.get(p.left) : store.get(p.right);

        if (replacement != null) {
            // Link replacement to parent
            updateParent(p, replacement);

            if (p.parent == null) {
                root = replacement;
                store.updateRoot(root);
            } else if (p.key.equals(leftOf(p.parent).key)) {
                Node<K> parent = store.get(p.parent);
                parent.left = replacement.key;
                store.update(parent);
            } else {
                Node<K> parent = store.get(p.parent);
                parent.right = replacement.key;
                store.update(parent);
            }
            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = p.right = p.parent = null;
            store.update(p);

            // Fix replacement
            if (p.color == BLACK)
                fixAfterDeletion(replacement);
        } else if (p.parent == null) { // return if we are the only node.
            root = null;
            store.updateRoot(null);
            store.updateMin(null);
            store.updateMax(null);
        } else { // No children. Use self as phantom replacement and unlink.
            if (p.color == BLACK)
                fixAfterDeletion(p);

            if (p.parent != null) {
                Node<K> parent = store.get(p.parent);
                if (p.key.equals(parent.left)) {
                    parent.left = null;

                } else if (p.key.equals(parent.right)) {
                    parent.right = null;
                }
                store.update(parent);
                p.parent = null;
            }
        }
        store.delete(p.key);
    }


    /**
     * From CLR *
     */
    private void fixAfterDeletion(Node<K> x) {
        while (x.key.equals(root.key) && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Node<K> sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib)) == BLACK &&
                        colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else { // symmetric
                Node<K> sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK &&
                        colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK);
    }

    private int colorOf(Node<K> n) {
        return nodeColor(n);
    }

    private void setColor(Node<K> node, int color) {
        node.color = color;
        store.update(node);
    }

    private int colorOf(K n) {
        return nodeColor(n);
    }

    private Node<K> parentOf(Node<K> node) {

        return store.get(node.parent);
    }

    private Node<K> leftOf(K node) {
        return leftOf(store.get(node));
    }

    private Node<K> leftOf(Node<K> node) {
        if (node == null)
            return null;
        return store.get(node.left);
    }

    private Node<K> rightOf(Node<K> node) {
        if (node == null)
            return null;
        return store.get(node.right);
    }

    /**
     * @param node
     * @param newKey
     */
    private void updateNodeKey(Node<K> node, K newKey) {
        Node<K> parent = parentOf(node);
        if (parent != null) {
            if (node.key.equals(parent.right)) {
                parent.right = newKey;
            } else if (node.key.equals(parent.left)) {
                parent.left = newKey;
            }
            store.update(parent);
        }

        if (node.left != null) {
            parent = leftOf(node);
            parent.parent = newKey;
            store.update(parent);
        } else if (node.right != null) {
            parent = rightOf(node);
            parent.parent = newKey;
            store.update(parent);
        }
        node.key = newKey;
        store.update(node);
    }


    private void updateParent(Node<K> old, Node<K> newn) {
        newn.parent = old.parent;
        Node<K> parent = parentOf(old);
        if (parent != null) {
            if (newn.key.equals(parent.right)) {
                parent.right = newn.key;
            } else if (newn.key.equals(parent.left)) {
                parent.left = newn.key;
            }
            store.update(parent);
            store.update(newn);
        }
    }


    /* Tree set visitors */

    /**
     * Retrieves the next value in tree
     *
     * @param ptr
     * @return
     */
    private Node<K> successor(Node<K> ptr) {

        if (ptr == null) {
            return null;
        } else if (ptr.right != null) {
            Node<K> node = store.get(ptr.right);
            while (node.left != null) {
                node = store.get(node.left);
            }
            return node;
        } else {
            Node<K> node = store.get(ptr.parent);
            Node<K> nch = ptr;
            while (node != null && nch.key.equals(node.right)) {
                nch = node;
                node = store.get(node.parent);
            }
            return node;
        }
    }


    /**
     * Retrieves the previous value in tree
     *
     * @param ptr
     * @return
     */
    private Node<K> predecessor(Node<K> ptr) {

        if (ptr == null) {
            return null;
        } else if (ptr.left != null) {
            Node<K> node = store.get(ptr.left);
            while (node.right != null) {
                node = store.get(node.right);
            }
            return node;
        } else {
            Node<K> node = store.get(ptr.parent);
            Node<K> nch = ptr;
            while (node != null && nch.key.equals(node.left)) {
                nch = node;
                node = store.get(node.parent);
            }
            return node;
        }
    }

    private class DefaultNodeStore<T extends Comparable<T>> implements NodeStore<T> {

        private final Map<T, Node> table;

        private Node<T> max;

        private Node<T> min;

        private Node<T> root;

        private boolean modification = false;


        public DefaultNodeStore() {
            this.table = new HashMap<T, Node>(10000);
        }

        @Override
        public Node<T> get(T key) {
            return table.get(key);  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void update(Node<T> node) {
            table.put(node.key, node);
        }

        @Override
        public void updateRoot(Node<T> node) {
            root = node;
        }

        @Override
        public void updateMax(Node<T> max) {
            this.max = max;
        }

        @Override
        public void updateMin(Node<T> min) {
            this.min = min;
        }

        @Override
        public void delete(T key) {
            table.remove(key);
        }


        @Override
        public Node<T> getMax() {
            return max;
        }

        @Override
        public Node<T> getMin() {
            return min;
        }

        @Override
        public Node<T> getRoot() {
            return this.root;
        }

        @Override
        public void startModification() {
            modification = true;
        }

        @Override
        public void finishModification() {
            modification = false;
        }

        @Override
        public boolean hasRunningModification() {
            return modification;
        }

        @Override
        public void clear() {
            max = min = root = null;
        }
    }

    private class DefaultNodeSet implements NodeSet<K> {

        private Node<K> node;
        private boolean visited = false;


        public DefaultNodeSet(Node<K> node) {
            this.node = node;
        }


        @Override
        public K next() {
            if (!visited) {
                visited = true;
                return node.key;
            }
            node = successor(node);
            return node != null ? node.key : null;
        }

        @Override
        public K prev() {
            if (!visited) {
                visited = true;
                return node.key;
            }
            node = predecessor(node);
            return node != null ? node.key : null;
        }
    }
}

