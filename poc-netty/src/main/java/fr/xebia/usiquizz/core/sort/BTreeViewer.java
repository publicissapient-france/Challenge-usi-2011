package fr.xebia.usiquizz.core.sort;

import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;

import java.lang.annotation.Target;

/**
 *
 * This class allows to retrieve data stored in the tree and Navigate over it
 *
 * User: slm
 * Date: 28/03/11
 * Time: 01:23
 */
public class BTreeViewer<T extends Comparable<T>> {

    private final NodeStore<T> store;

    public BTreeViewer(NodeStore<T> _store){
        store = _store;
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
            return node != null ? node.information: null;
        }

        @Override
        public T prev() {
            if (!visited){
                visited = true;
                return node.information;
            }
            node = predecessor(node);
            return node != null ? node.information: null;
        }
    }

}
