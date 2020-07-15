/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import java.util.ArrayList;
import monalisa.data.pn.Transition;

/**
 *
 * @author Marcel Gehrmann
 */
public class ReachabilityEvent {

    private final Status status;
    private final int steps;
    private final ArrayList<Transition> backtrack;

    public enum Status {
        STARTED,
        SUCCESS,
        FAILURE,
        PROGRESS,
        ABORTED,
        FINISHED // Finished is only used for creating a full graph.
    }

    public ReachabilityEvent(Status status, int steps, ArrayList<Transition> backtrack) {
        this.status = status;
        this.steps = steps;
        this.backtrack = backtrack;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return the steps
     */
    public int getSteps() {
        return steps;
    }

    /**
     * @return the backtrack
     */
    public ArrayList<Transition> getBacktrack() {
        return backtrack;
    }
}
