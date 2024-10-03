/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import java.util.HashMap;
import monalisa.data.pn.Place;

/**
 * @author Marcel Gehrmann
 * @author Kristin Haas
 */
public class ReachabilityNode {

    private final HashMap<Place, Long> marking;
    private ReachabilityNode prev;
    private double priority = 0;
    private int depth;
    private boolean visited = false;
    private boolean secondVisit = false;

    public ReachabilityNode(HashMap<Place, Long> marking, ReachabilityNode prev) {
        this.marking = marking;
        this.prev = prev;
        if (prev == null) {
            this.depth = 0;
        }
    }
    
    /**
     * 
     * @return 
     */
    public boolean setSecondVisit(){
        return secondVisit = true;
    }
    
    /**
     * 
     * @return 
     */
    public boolean setSecondUnvisited(){
        return secondVisit = false;
    }
    
    /**
     * 
     * @return 
     */
    public boolean getSecondVisit(){
        return secondVisit;
    }
    
    /**
     * 
     * @return 
     */
    public boolean setVisited(){
        return visited = true;
    }
    
    /**
     * 
     * @return 
     */
    public boolean setUnvisited(){
        return visited = false;
    }
    
    /**
     * 
     * @return 
     */
    public boolean getVisited(){
        return visited;
    }

    /**
     * @return the marking.
     */
    public HashMap<Place, Long> getMarking() {
        return marking;
    }

    /**
     * 
     * @return 
     */
    public int getDepth() {
        return alternateDepth();
    }

    /**
     * 
     * @return 
     */
    public int alternateDepth() {
        if (prev != null) {
            return prev.alternateDepth() + 1;
        } else {
            return 0;
        }
    }

    /**
     * 
     * @param prev 
     */
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

    /**
     * 
     * @param priority 
     */
    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * 
     * @return 
     */
    public double getPriority() {
        return priority;
    }

    /**
     * 
     * @param other
     * @return 
     */
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

    /**
     * 
     * @param other
     * @return 
     */
    public HashMap<Place, Long> getDifference(ReachabilityNode other) {
        HashMap<Place, Long> diff = new HashMap();
        for (Place p : this.marking.keySet()) {
            Long p_diff = this.marking.get(p) - other.getMarking().get(p);
            diff.put(p, p_diff);
        }
        return diff;
    }

    /**
     * 
     * @param other
     * @return 
     */
    public boolean equals(ReachabilityNode other) {
        for (Place p : marking.keySet()) {
            if (!marking.get(p).equals(other.getMarking().get(p))) {
                return false;
            }
        }
        return true;
    }
}
