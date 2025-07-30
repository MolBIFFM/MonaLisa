/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import monalisa.data.pn.Place;
// import monalisa.addons.reachability.ReachabilityLoop;

/**
 *
 * @author Marcel Gehrmann
 */
public class ReachabilityNode {

    private final HashMap<Place, Long> marking;
    private ReachabilityNode prev;
    private double priority = 0;
    private int depth;
    private double probability;
    private double time;

    public ReachabilityNode(HashMap<Place, Long> marking, ReachabilityNode prev) {
        this.marking = marking;
        this.prev = prev;
        if (prev == null) {
            this.depth = 0;
        }
    }

    // public ReachabilityNode(HashMap<Place, Long> marking, ReachabilityNode prev, double  probability) {
    //     this.marking = marking;
    //     this.prev = prev;
    //     if (prev == null) {
    //         this.depth = 0;
    //     }
    //     this.probability = probability;
    // }

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

    public boolean strictlyequals(ReachabilityNode other){
        if (!this.equals(other)){
            return false;
        }
        if (prev == null && other.getPrev() == null){
            return true;
        }
        if (prev == null || other.getPrev() == null){
            return false;
        }
        return prev.equals(other.getPrev());
    }

//    public boolean extremlyequals(ReachabilityNode other) {
//    ReachabilityNode thisNode = this;
//    ReachabilityNode otherNode = other;
//
//    while (thisNode != null && otherNode != null) {
//        if (!thisNode.equals(otherNode)) {
//            return false;
//        }
//        thisNode = thisNode.getPrev();
//        otherNode = otherNode.getPrev();
//    }
//
//    // If both reached null at the same time, paths are identical
//    return thisNode == null && otherNode == null;
//    }
    
    // @Override
    // public int hashCode() {
    //     int result = 17;

    //     // 标记部分哈希
    //     for (Map.Entry<Place, Long> entry : marking.entrySet()) {
    //         result = 31 * result + entry.getKey().hashCode();
    //         result = 31 * result + entry.getValue().hashCode();
    //     }

    //     // 如果路径也需要考虑，则加上 prev 的 hash
    //     if (prev != null) {
    //         result = 31 * result + prev.hashCode();
    //     }

    //     return result;
    // }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    // public boolean inAnyLoop(List<ReachabilityLoop> loops) {
    //     for (ReachabilityLoop loop : loops) {
    //         if (loop.contains(this)) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    /**check if the marking of this node is in any loop
     */
    public boolean inAnyLoop() {
        ReachabilityNode mBack = this.getPrev();
        while (mBack != null) {
            if (this.equals(mBack)) {
                return true;
            }
            mBack = mBack.getPrev();
        }
        return false;
    }

    /**get the accumulated reaction time
     */
    public double getTime(){
        return time;
    }

    public void setTime(double time){
        this.time = time;
    }
}
