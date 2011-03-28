package fr.xebia.usiquizz.core.game.gemfire;

import com.gemstone.gemfire.cache.Region;
import fr.xebia.usiquizz.core.persistence.Joueur;
import fr.xebia.usiquizz.core.sort.Node;
import fr.xebia.usiquizz.core.sort.NodeStore;

/**
 * NodeStore using a Gemfire region to maintain nodes
 * User: slm
 * Date: 28/03/11
 * Time: 23:58
 */
public class DistributedNodeScoreStore implements NodeStore<Joueur>{

    private Region<Joueur, Node<Joueur>> region;

    private static final Joueur MAX_KEY = new Joueur(-10, "_","_","_");
    private static final Joueur MIN_KEY = new Joueur(-20, "_","_","_");
    private static final Joueur ROOT_KEY = new Joueur(-30, "_","_","_");



    public DistributedNodeScoreStore (Region<Joueur, Node<Joueur>> region){
        this.region = region;
    }

    @Override
    public Node<Joueur> get(Joueur key) {
        return region.get(key);
    }

    @Override
    public void update(Node<Joueur> joueurNode) {
        region.put(joueurNode.information, joueurNode);
    }

    @Override
    public void delete(Joueur key) {
        region.remove(key);
    }

    @Override
    public void updateMax(Node<Joueur> max) {
        region.put(MAX_KEY, max);
    }

    @Override
    public void updateMin(Node<Joueur> min) {
        region.put(MIN_KEY, min);
    }

    @Override
    public void updateRoot(Node<Joueur> root) {
        region.put(ROOT_KEY, root);
    }

    @Override
    public Node<Joueur> getRoot() {
        return region.get(ROOT_KEY);
    }

    @Override
    public Node<Joueur> getMax() {
        return region.get(MAX_KEY);
    }

    @Override
    public Node<Joueur> getMin() {
        return region.get(MIN_KEY);  
    }

    @Override
    public void startModification() {
        // TODO : implement this
    }

    @Override
    public void finishModification() {
        //Todo implement this
    }

    @Override
    public boolean hasRunningModification() {
        return false;  //Todo implement this
    }
}
