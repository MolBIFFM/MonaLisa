/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import monalisa.data.pn.Transition;

/**
 *
 * @author Marcel
 */
public class ReachabilityEdge {

    private final ReachabilityNode source;
    private final ReachabilityNode target;
    private final Transition t;
    private double probability;//final

    public ReachabilityEdge(ReachabilityNode from, ReachabilityNode to,
            Transition t) {
        this.source = from;
        this.target = to;
        this.t = t;
    }

    public ReachabilityEdge(ReachabilityNode from, ReachabilityNode to, Transition t, double probability) {
        this.source = from;
        this.target = to;
        this.t = t;
        this.probability = probability;
    }

    /**
     * @return the source node.
     */
    public ReachabilityNode getSource() {
        return source;
    }

    /**
     * @return the target node.
     */
    public ReachabilityNode getTarget() {
        return target;
    }

    /**
     * @return the transition converting marking source to marking target.
     */
    public Transition getTransition() {
        return t;
    }

    public double getProbability() {
        return probability;
    }
}
