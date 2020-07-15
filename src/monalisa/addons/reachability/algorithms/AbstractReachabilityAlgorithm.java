/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import monalisa.addons.reachability.Pathfinder;
import monalisa.addons.reachability.ReachabilityEvent;
import monalisa.addons.reachability.ReachabilityEvent.Status;
import monalisa.addons.reachability.ReachabilityGraph;
import monalisa.addons.reachability.ReachabilityListener;
import monalisa.addons.reachability.ReachabilityNode;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;

/**
 *
 * @author Marcel
 */
public abstract class AbstractReachabilityAlgorithm extends Thread implements ReachabilityAlgorithm {

    protected final Pathfinder pf;
    protected final PetriNetFacade pnf;
    protected final HashMap<Place, Long> marking;
    protected final HashMap<Place, Long> target;
    protected ReachabilityNode tar;
    protected ReachabilityGraph g;
    private List<ReachabilityListener> listeners = new ArrayList<>();

    public AbstractReachabilityAlgorithm(Pathfinder pf, PetriNetFacade pnf, HashMap<Place, Long> marking, HashMap<Place, Long> target) {
        this.pf = pf;
        this.pnf = pnf;
        this.marking = marking;
        this.target = target;
    }

    @Override
    public ArrayList<Transition> backtrack() {
        // Start from m*. Go back using m*.getPrev(). Always shortest path for BFS
        ArrayList<Transition> path = new ArrayList<>();
        ReachabilityNode currentNode = tar;
        while (currentNode.getPrev() != null) {
            Transition t = g.getEdge(currentNode.getPrev(), currentNode).getTransition();
            path.add(0, t);
            currentNode = currentNode.getPrev();
        }
        /* Path can be null for two reasons: m0 = m* or there exists no path from m0 to m*.
           If m0 = m*, one should check whether any T-Invariant is feasible in m0.
           All feasible TIs are valid paths, and smallest TI is shortest path.
         */
        return path;
    }

    /**
     * Finds the position a node needs to be inserted in based on its priority
     * using binary search.
     *
     * @param list in which the position needs to be found
     * @param node for which a position needs to be found
     * @return the position to insert the node at
     */
    public int findPos(ArrayList<ReachabilityNode> list, ReachabilityNode node) {
        if (list.isEmpty()) {
            return 0;
        }
        int l = 0;
        int r = list.size();
        while (l < r) {
            int m = (int) Math.floor((l + r) / 2);
            if (list.get(m).getPriority() > node.getPriority()) {
                r = m;
            } else {
                l = m + 1;
            }
        }
        return r;
    }

    @Override
    public void addListener(ReachabilityListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(ReachabilityListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public void fireReachabilityUpdate(Status status, int steps, ArrayList<Transition> backtrack) {
        for (ReachabilityListener listener : listeners) {
            listener.update(new ReachabilityEvent(status, steps, backtrack));
        }
    }
}
