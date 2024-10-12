/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import monalisa.addons.reachability.Pathfinder;
import monalisa.addons.reachability.ReachabilityEdge;
import monalisa.addons.reachability.ReachabilityEvent;
import monalisa.addons.reachability.ReachabilityGraph;
import monalisa.addons.reachability.ReachabilityNode;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marcel
 */
public class FullCoverability extends AbstractReachabilityAlgorithm {

    private static final Logger LOGGER = LogManager.getLogger(FullCoverability.class);

    public FullCoverability(Pathfinder pf, HashMap<Place, Long> marking, HashMap<Place, Long> target) {
        super(pf, marking, target);
    }
   

    @Override
    public void run() {
        LOGGER.info("Starting Full Coverability Algorithm");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        ReachabilityNode root = new ReachabilityNode(marking, null);
        vertices.add(root);
        workingList.add(root);
        while (!workingList.isEmpty() && !isInterrupted()) {
            LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            // get a node to expand
            ReachabilityNode workingNode = workingList.get(0);
            workingList.remove(workingNode);
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());

            for (Transition t : activeTransitions) {
                LOGGER.debug("Created new node by firing transition " + t.getProperty("name") + ".");  // debug                
                // compute new marking
                HashMap<Place, Long> mNew = pf.computeOmegaMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                ReachabilityNode mBack = workingNode;
                while ((mBack != null) && (!newNode.largerThan(mBack))) {
                    mBack = mBack.getPrev();
                }
                if (mBack != null) {
                    newNode = new ReachabilityNode(pf.omegaComputation(workingNode.getMarking(), newNode.getMarking()), workingNode);
                }
                edges.add(new ReachabilityEdge(workingNode, newNode, t));
                boolean unvisited = true;
                for (ReachabilityNode v : vertices) {
                    if (v.equals(newNode)) {
                        unvisited = false;
                    }
                }
                if (unvisited) {
                    vertices.add(newNode);
                    workingList.add(newNode);
                }
            }
        }
        if (isInterrupted()) {
            LOGGER.warn("Execution has been aborted.");
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            LOGGER.info("Completed creation of Coverability Graph. Size is " + vertices.size());
            g = new ReachabilityGraph(vertices, edges);
            fireReachabilityUpdate(ReachabilityEvent.Status.FINISHED, counter, null);
        }
    }
   

    @Override
    public void computePriority(ReachabilityNode node) {
        // Does not use a priority.
    }

    
}
