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
public class BTreeViewer<T extends Comparable<T>, V> {

    private final NodeStore<T,V> store;

    public BTreeViewer(NodeStore<T,V> _store){
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
    private Node<T, V> successor(Node<T,V> ptr){
        
        if (ptr == null){
            return null;
        }
        else if (ptr.right !=null){
            Node<T,V> node = store.get(ptr.right);
            while(node.left != null){
                node = store.get(ptr.left);
            }
            return node;
        } else {
            Node<T,V> node = store.get(ptr.parent);
            Node<T,V> nch = ptr;
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
    private Node<T, V> predecessor(Node<T,V> ptr){

        if (ptr == null){
            return null;
        }
        else if (ptr.left !=null){
            Node<T,V> node = store.get(ptr.left);
            while(node.right != null){
                node = store.get(ptr.right);
            }
            return node;
        } else {
            Node<T,V> node = store.get(ptr.parent);
            Node<T,V> nch = ptr;
            while(node != null && nch.information.equals(node.left)){
                nch = node;
                node = store.get(node.parent);
            }
            return node;
        }
    }


    private class DefaultNodeSet implements NodeSet<V>{

        private Node<T,V> node;

        public DefaultNodeSet(Node<T,V> node){
            this.node = node;
        }


        @Override
        public V next() {

            node = successor(node);
            return node.value;
        }

        @Override
        public V prev() {
            node = predecessor(node);
            return node.value;  
        }
    }

}
