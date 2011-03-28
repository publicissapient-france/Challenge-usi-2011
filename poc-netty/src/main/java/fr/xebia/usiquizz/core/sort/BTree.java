package fr.xebia.usiquizz.core.sort;



import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Implements a simple BTreeTest that stores its nodes in a NodeStore so each
 * node keeps the parent, the left and the right node store keys to be able to retrieve
 * these nodes inside the store.
 *
 * Minimum key is stored in the bottom right (minimumKey.compareTo(anyOther) < 0)
 *
 * Added value storing in node and node store to persist node in a separated store
 * Replaced recursion by iteration for insertion and deletion
 *
 *
 * Source code imported from BTreeTest implementation found on jBixbe.com
 * jBixbe debuggee: test insert and delete operation of a balanced tree data
 * structure. Using integer values read from keyboard as tree elements.
 *
 * @author ds-emedia
 * @author slm
 */
public class BTree<T extends Comparable<T>> {
    
    private Node<T> root;

    private Node<T> max;

    private Node<T> min;

    private NodeStore<T> store;

    private Long size;

    /**
     * Creates an empty balanced tree.
     */
    public BTree() {
        root = null;
        store = new DefaultNodeStore<T>();
        size = 0l;
    }

    public BTree(NodeStore<T> _store){
        this();
        store = _store;
    }

    public NodeStore<T> getStore(){
        return store;
    }


    /**
     * Inserts an element into the tree.
     */
    public void insert(T info ) {
        store.startModification();
        insertIterative(info);
        store.finishModification();
    }

    /**
     * Checks whether the given element is already in the tree.
     */
    public boolean isMember(T info) {
        return isMemberP(info);
    }

    /**
     * Removes an elememt from the tree.
     */
    public void delete(T info) {
        store.startModification();
        Node<T> node = find(info);
        if (node == null) {
            throw new NoSuchElementException("No entry for "+ info);
        } else if (info.compareTo(node.information) == 0) {


                // Fix Min and Max if necessary
            if (info.equals(max.information)){
                max = store.get(max.parent);
                store.updateMax(max);
            }
            if (info.equals(min.information)){
                min = store.get(min.parent);
                store.updateMin(min);
            }
            deleteNode(node);
        }
        store.finishModification();
    }


    public long size(){
        return size;
    }

    /**
     * Returns a text representation of the tree.
     */
    public String toString() {
        return inOrder();
    }

    /**
     * Returns all elements of the tree in in-order traversing.
     */
    public String inOrder() {
        return inOrder(root);
    }

    /**
     * Returns all elements of the tree in pre-order traversing.
     */
    public String preOrder() {
        return preOrder(root);
    }

    /**
     * Returns all elements of the tree in post-order traversing.
     */
    public String postOrder() {
        return postOrder(root);
    }

    /**
     * Returns the height of the tree.
     * @return maximum tree depth
     */
    public int getHeight() {
        return getHeight(root);
    }

    public T get(T info){
        Node<T> res = find(info);
        return res == null ? null : res.information;
    }


     /**
      * Iterative Insertion
      * @param info
      */
     private void insertIterative(T info) {

         Node<T> ptr = root;
         Node<T> res;

         if (ptr == null){
             size++;
             root = new Node<T>(info, null);
             store.update(root);
             store.updateRoot(root);
             max = min = root;
             store.updateMin(min);
             store.updateMax(max);
             return;
         }

         while (true){

             int cmp = info.compareTo(ptr.information);

             if (cmp == 0){
                 store.update(ptr);
             } else if (cmp < 0) {
                 if (ptr.left != null){
                     ptr = store.get(ptr.left);
                 }else {
                     size++;
                     ptr.left = info;
                     res = new Node<T>(info, ptr.information);
                     store.update(res);
                     store.update(ptr);
                     restructInsert(res);
                     return;
                 }

             } else { // cmp > 0

                 if (ptr.right != null){
                     ptr = store.get(ptr.right);
                 }else {
                     size++;
                     ptr.right = info;
                     res = new Node<T>(info, ptr.information);
                     store.update(res);
                     store.update(ptr);
                     restructInsert(res);
                     return;
                 }
             }
         }

     }


    private boolean isMemberP(T info) {

        return find(info) == null;

    }


    private Node<T> find(T info){
        Node<T> ptr = root;

        while (true){
            if (ptr == null) {
              return null;
            } else if (info.compareTo(ptr.information) == 0) {
                return ptr;
            } else if (info.compareTo(ptr.information) > 0) {
                ptr = store.get(ptr.right);
            } else {
                ptr = store.get(ptr.left);
            }
        }
    }



    private void deleteNode(Node<T> node) {
        size --;
        Node<T> eNode, minMaxNode, delNode = null, tmp;
        boolean rightNode = false;
        T initialKey = node.information;

        if (node.isLeaf()) {
            if (node.parent == null) {
                root = null;
                store.updateRoot(null);
            } else if (node.isRightNode(store)) {
                Node<T> nd = store.get(node.parent);
                nd.right = null;
                store.update(nd);
                rightNode = true;
            } else if (node.isLeftNode(store)) {
                Node<T> nd = store.get(node.parent);
                nd.left = null;
                store.update(nd);
            }
            delNode = node;

        } else if (node.hasLeftNode()) {
            minMaxNode = store.get(node.left);
            for (eNode = store.get(node.left); eNode != null; eNode = store.get(eNode.right)) {
                minMaxNode = eNode;
            }
            delNode = minMaxNode;
            node.information = minMaxNode.information;

            if (store.get(node.left).right != null) {
                tmp = store.get(minMaxNode.parent);
                tmp.right = minMaxNode.left;
                store.update(tmp);
                rightNode = true;
            } else {
                tmp = store.get(minMaxNode.parent);
                tmp.left = minMaxNode.left;
                store.update(tmp);
            }

            if (minMaxNode.left != null) {
                tmp = store.get(minMaxNode.left);
                tmp.parent = minMaxNode.parent;
                store.update(tmp);
            }
            store.update(node);
        } else if (node.hasRightNode()) {
            minMaxNode = store.get(node.right);
            delNode = minMaxNode;
            rightNode = true;

            node.information = minMaxNode.information;
            node.right = minMaxNode.right;
            if (node.right != null) {
                tmp = store.get(node.right);
                tmp.parent = node.information;
                store.update(tmp);
            }
            node.left = minMaxNode.left;
            if (node.left != null) {
                tmp = store.get(node.left);
                tmp.parent = node.information;
            }
            store.update(node);
        }

        if(delNode != null){
            restructDelete(store.get(delNode.parent), rightNode);
        }
        store.delete(initialKey);

    }

    private int getHeight(Node<T> node) {
        int height;

        if (node == null) {
            height = -1;
        } else {
            height = 1 + Math.max(getHeight(store.get(node.left)), getHeight(store.get(node.right)));
        }
        return height;
    }

    private String inOrder(Node<T> node) {

        String result = "";
        if (node != null) {
            result = result + inOrder(store.get(node.left)) + " ";
            result = result + node.information.toString();
            result = result + inOrder(store.get(node.right));
        }
        return result;
    }

    private String preOrder(Node<T> node) {

        String result = "";
        if (node != null) {
            result = result + node.information.toString() + " ";
            result = result + preOrder(store.get(node.left));
            result = result + preOrder(store.get(node.right));
        }
        return result;
    }

    private String postOrder(Node<T> node) {

        String result = "";
        if (node != null) {
            result = result + postOrder(store.get(node.left));
            result = result + postOrder(store.get(node.right));
            result = result + node.information.toString() + " ";
        }
        return result;
    }


    private void restructInsert(Node<T> node) {

        boolean wasRight = false;
        Node<T> parent;

            // Update min and max if necessary
        if (node.information.compareTo(max.information) > 0){
            max = node;
            store.updateMax(max);
        }

        if (node.information.compareTo(min.information) < 0){
            min = node;
            store.updateMin(min);
        }
        while (true) {

            if (node.parent == null) {
                return;
            }

            if (node.information.equals(root.information)) {
                return;
            }

            parent = store.get(node.parent);

            switch (parent.balance){
                case '_': {
                    boolean right = false;
                    if (node.information.equals(parent.left)) {
                        parent.balance = '/';
                    } else {
                        parent.balance = '\\';
                        right =  true;
                    }
                    store.update(parent);
                    node = parent;
                    wasRight = right;
                    break;
                }
                case '/' : {
                    if (node.information.equals(parent.right)) {
                        parent.balance = '_';
                        store.update(parent);
                    } else {
                        if (!wasRight) {
                            rotateRight(parent);
                        } else {
                            doubleRotateRight(parent);
                        }
                    }
                    return;

                }
                case '\\': {
                    if (node.information.equals(parent.left)) {
                        parent.balance = '_';
                        store.update(parent);
                    } else {
                        if (wasRight) {
                            rotateLeft(parent);
                        } else {
                            doubleRotateLeft(parent);
                        }
                    }
                    return;
                }
            }
        }
    }


    private void restructDelete(Node<T> z, boolean wasRight) {

        Node<T> parent;
        boolean isRight;
        boolean climb;
        boolean canClimb;

        while (true){
            isRight = false;
            climb = false;


            if (z == null) {
                return;
            }

            parent = store.get(z.parent);
            canClimb = (parent != null);

            if (canClimb) {
                isRight = z.isRightNode(store);
            }

            if (z.balance == '_') {
                if (wasRight) {
                    z.balance = '/';
                } else {
                    z.balance = '\\';
                }
                store.update(z);
            } else if (z.balance == '/') {
                if (wasRight) {
                    if (store.get(z.left).balance == '\\') {
                        doubleRotateRight(z);
                        climb = true;
                    } else {
                        rotateRight(z);
                        if (z.balance == '_') {
                            climb = true;
                        }
                    }
                } else {
                    z.balance = '_';
                    store.update(z);
                    climb = true;
                }
            } else {
                if (wasRight) {
                    z.balance = '_';
                    store.update(z);
                    climb = true;
                } else {
                    if (store.get(z.right).balance == '/') {
                        doubleRotateLeft(z);
                        climb = true;
                    } else {
                        rotateLeft(z);
                        if (z.balance == '_') {
                            climb = true;
                        }
                    }
                }
            }

            if (canClimb && climb) {
                z = parent;
                wasRight = isRight;
            }else {
                return;
            }
        }
    }

    private void rotateLeft(Node<T> a) {

        Node<T> b = store.get(a.right);

        if (a.parent == null) {

            root = b;
            store.updateRoot(b);
        } else {
            Node<T> parent = store.get(a.parent);
                    // isLeftNode
            if (a.information.equals(parent.left)) {
                parent.left = b.information;

            } else {
                parent.right = b.information;
            }
            store.update(parent);
        }

        a.right = b.left;
        if (a.right != null) {
            Node<T> right = store.get(a.right);
            right.parent = a.information;
            store.update(right);
        }

        b.parent = a.parent;
        a.parent = b.information;
        b.left = a.information;

        if (b.balance == '_') {
            a.balance = '\\';
            b.balance = '/';
        } else {
            a.balance = '_';
            b.balance = '_';
        }
        store.update(a);
        store.update(b);
    }

    private void rotateRight(Node<T> a) {

        Node<T> b = store.get(a.left);

        if (a.parent == null) {
            root = b;
            store.updateRoot(root);
        } else {
            Node<T> parent = store.get(a.parent);
            // A is left of Parent
            if (a.information.equals(parent.left)) {
                parent.left = b.information;
            } else {
                parent.right = b.information;
            }
            store.update(parent);
        }

        a.left = b.right;
        if (a.left != null) {
            Node<T> left = store.get(a.left);
            left.parent = a.information;
            store.update(left);
        }

        b.parent = a.parent;
        a.parent = b.information;
        b.right = a.information;

        if (b.balance == '_') {
            a.balance = '/';
            b.balance = '\\';
        } else {
            a.balance = '_';
            b.balance = '_';
        }
        store.update(a);
        store.update(b);
    }

    private void doubleRotateLeft(Node<T> a) {

        Node<T> b = store.get(a.right);
        Node<T> c = store.get(b.left);

        if (a.parent == null) {
            root = c;
            store.updateRoot(root);
        } else {
            Node<T> parent = store.get(a.parent);
            if (parent.left.equals(a)) {
                parent.left = c.information;
            } else {
                parent.right = c.information;
            }
            store.update(parent);
        }

        c.parent = a.parent;

        a.right = c.left;
        if (a.right != null) {
            Node<T> right = store.get(a.right);
            right.parent = a.information;
            store.update(right);
        }
        b.left = c.right;
        if (b.left != null) {
            Node<T> left = store.get(a.left);
            left.parent = b.information;
            store.update(left);
        }

        c.left = a.information;
        c.right = b.information;

        a.parent = c.information;
        b.parent = c.information;

        if (c.balance == '/') {
            a.balance = '_';
            b.balance = '\\';
        } else if (c.balance == '\\') {
            a.balance = '/';
            b.balance = '_';
        } else {
            a.balance = '_';
            b.balance = '_';
        }

        c.balance = '_';

        store.update(a);
        store.update(b);
        store.update(c);
    }

    private void doubleRotateRight(Node<T> a) {

        Node<T> b = store.get(a.left);
        Node<T> c = store.get(b.right);

        if (a.parent == null) {
            root = c;
            store.updateRoot(root);
        } else {
            Node<T> parent = store.get(a.parent);
            if (parent.left.equals(a.information)) {
                parent.left = c.information;
            } else {
                parent.right = c.information;
            }
            store.update(parent);
        }

        c.parent = a.parent;

        a.left = c.right;
        if (a.left != null) {
            Node<T> left = store.get(a.left);
            left.parent = a.information;
            store.update(left);
        }
        b.right = c.left;
        if (b.right != null) {
            Node<T> right = store.get(a.right);
            right.parent = b.information;
        }

        c.right = a.information;
        c.left = b.information;

        a.parent = c.information;
        b.parent = c.information;

        if (c.balance == '/') {
            b.balance = '_';
            a.balance = '\\';
        } else if (c.balance == '\\') {
            b.balance = '/';
            a.balance = '_';
        } else {
            b.balance = '_';
            a.balance = '_';
        }
        c.balance = '_';

        store.update(a);
        store.update(b);
        store.update(c);
    }

    public NodeSet getMaxSet(){
         return new DefaultNodeSet(store.getMax());
     }


     public NodeSet getMinSet(){
         return new DefaultNodeSet(store.getMin());
     }



     public NodeSet getSet(T info){
         return new DefaultNodeSet(store.get(info));
     }

     /**
      * Retrieves the next value in tree
      * @param ptr
      * @return
      */
     private Node<T> successor(Node<T> ptr){

         if (ptr == null){
             return null;
         }
         else if (ptr.right !=null){
             Node<T> node = store.get(ptr.right);
             while(node.left != null){
                 node = store.get(node.left);
             }
             return node;
         } else {
             Node<T> node = store.get(ptr.parent);
             Node<T> nch = ptr;
             while(node != null && nch.information.equals(node.right)){
                 nch = node;
                 node = store.get(node.parent);
             }
             return node;
         }
     }


     /**
      * Retrieves the previous value in tree
      * @param ptr
      * @return
      */
     private Node<T> predecessor(Node<T> ptr){

         if (ptr == null){
             return null;
         }
         else if (ptr.left !=null){
             Node<T> node = store.get(ptr.left);
             while(node.right != null){
                 node = store.get(node.right);
             }
             return node;
         } else {
             Node<T> node = store.get(ptr.parent);
             Node<T> nch = ptr;
             while(node != null && nch.information.equals(node.left)){
                 nch = node;
                 node = store.get(node.parent);
             }
             return node;
         }
     }


   private class DefaultNodeStore<T extends Comparable<T>> implements NodeStore<T>{

       private final Map<T, Node> table;

       private Node<T> max;

       private Node<T> min;

       private Node<T> root;

       private boolean modification = false;


       public DefaultNodeStore() {
           this.table = new HashMap<T,Node>();
       }

       @Override
       public Node<T> get(T key) {
           return table.get(key);  //To change body of implemented methods use File | Settings | File Templates.
       }

       @Override
       public void update(Node<T> node) {
          table.put(node.information, node);
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
       public Node<T> getRoot(){
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
   }



       private class DefaultNodeSet implements NodeSet<T>{

        private Node<T> node;
        private boolean visited = false;


        public DefaultNodeSet(Node<T> node){
            this.node = node;
        }


        @Override
        public T next() {
            if (!visited){
                visited = true;
                return node.information;
            }
            node = successor(node);
            return node.information;
        }

        @Override
        public T prev() {
            if (!visited){
                visited = true;
                return node.information;
            }
            node = predecessor(node);
            return node.information;
        }
    }
}