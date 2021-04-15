/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import java.util.HashMap;
import monalisa.data.pn.Place;

/**
 *
 * @author Marcel Gehrmann
 */
public class ReachabilityNode {

    private final HashMap<Place, Long> marking;
    private ReachabilityNode prev;
    private double priority = 0;
    private int depth;

    public ReachabilityNode(HashMap<Place, Long> marking, ReachabilityNode prev) {
        this.marking = marking;
        this.prev = prev;
        if (prev == null) {
            this.depth = 0;
        }
    }

    /**
     * @return the marking.
     */
    public HashMap<Place, Long> getMarking() {
        return marking;
    }

    public int getDepth() {
        return alternateDepth();
    }

    public int alternateDepth() {
        if (prev != null) {
            return prev.alternateDepth() + 1;
        } else {
            return 0;
        }
    }

    public void setPrev(ReachabilityNode prev) {
        this.prev = prev;
        this.depth = prev.getDepth() + 1;
    }

    /**
     * @return the previous marking's node.
     */
    public ReachabilityNode getPrev() {
        return prev;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public double getPriority() {
        return priority;
    }

    public boolean largerThan(ReachabilityNode other) {
        // this is larger than other, if all places in this have at least the same amount of tokens
        boolean larger = false;
        for (Place p : marking.keySet()) {
            if (marking.get(p) > other.getMarking().get(p)) {
                larger = true;
            } else if (marking.get(p) < other.getMarking().get(p)) {
                return false;
            }
        }
        return larger;
    }

    public HashMap<Place, Long> getDifference(ReachabilityNode other) {
        HashMap<Place, Long> diff = new HashMap();
        for (Place p : this.marking.keySet()) {
            Long p_diff = this.marking.get(p) - other.getMarking().get(p);
            diff.put(p, p_diff);
        }
        return diff;
    }

    public boolean equals(ReachabilityNode other) {
        for (Place p : marking.keySet()) {
            if (!marking.get(p).equals(other.getMarking().get(p))) {
                return false;
            }
        }
        return true;
    }
}
