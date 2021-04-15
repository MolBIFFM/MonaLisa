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
 * @author Marcel Gehrmann
 */
public class BestFirst extends AbstractReachabilityAlgorithm {

    private final String priority;
    private static final Logger LOGGER = LogManager.getLogger(BestFirst.class);

    public BestFirst(Pathfinder pf, HashMap<Place, Long> marking, HashMap<Place, Long> target, String heur) {
        super(pf, marking, target);
        this.priority = heur;
    }

    @Override
    public void run() {
        LOGGER.debug("Starting Best First Algorithm.");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        // initialize for m0 as root
        ReachabilityNode root = new ReachabilityNode(marking, null);
        tar = new ReachabilityNode(target, null);
        vertices.add(root);
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        workingList.add(root);
        while (!workingList.isEmpty() && !isInterrupted()) {
            LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            ReachabilityNode workingNode = workingList.get(0);
            workingList.remove(0);
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());
            for (Transition t : activeTransitions) {
                LOGGER.debug("Created new node by firing transition " + t.getProperty("name") + ".");  // debug                
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                // Algorithm terminates on finding m*
                if (newNode.equals(tar)) {
                    tar = newNode;
                    vertices.add(tar);
                    edges.add(new ReachabilityEdge(workingNode, tar, t));
                    g = new ReachabilityGraph(vertices, edges);
                    fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack());
                    LOGGER.debug("Target marking has been reached.");
                    return;
                }
                boolean unvisited = true;
                for (ReachabilityNode v : vertices) {
                    if (v.equals(newNode)) {
                        unvisited = false;
                        edges.add(new ReachabilityEdge(workingNode, v, t));
                        // Potentially update depth
                        if (v.getDepth() > newNode.getDepth()) {
                            v.setPrev(workingNode);
                        }
                        break;
                    }
                }
                if (unvisited) {
                    vertices.add(newNode);
                    edges.add(new ReachabilityEdge(workingNode, newNode, t));
                    computePriority(newNode); // Compute priority
                    int pos = findPos(workingList, newNode); // Compute position using binary search?

                    workingList.add(pos, newNode);
                }
            }
        }
        if (isInterrupted()) {
            LOGGER.warn("Execution has been aborted.");
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            LOGGER.info("Target marking could not be reached from start marking.");
            g = new ReachabilityGraph(vertices, edges);
            fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, null);
        }
    }

    /**
     * By default, priority is the sum of token differences across all places
     * between the node and the target node. LOWER priority values are of higher
     * priority.
     *
     * @param node for which priority needs to be computed.
     */
    @Override
    public void computePriority(ReachabilityNode node) {
        // TODO - Implement other functions and choices. Also weighting?
        double prio = 0;
        if (priority.equals("Default")) {
            // The closer a marking is to the target, the more it is prioritized.
            // High priority value = Smaller priority.
            for (Place p : tar.getMarking().keySet()) {
                prio += Math.abs(tar.getMarking().get(p) - node.getMarking().get(p));
            }
        } else if (priority.equals("Weighted Default")) {
            // Default, but places are weighted inversely by their degree.
            // Effectively, places with a high degree increase priority value less.
            for (Place p : tar.getMarking().keySet()) {
                prio += ((1f / (p.inputs().size() + p.outputs().size()))
                        * Math.abs(tar.getMarking().get(p) - node.getMarking().get(p)));                        
            }
        }
        node.setPriority(prio);
    }
}
