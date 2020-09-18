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
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marcel
 */
public class FullReachability extends AbstractReachabilityAlgorithm {

    private static final Logger LOGGER = LogManager.getLogger(FullReachability.class);

    public FullReachability(Pathfinder pf, PetriNetFacade pnf, HashMap<Place, Long> marking, HashMap<Place, Long> target) {
        super(pf, pnf, marking, target);
    }

    @Override
    public void run() {
        LOGGER.info("Starting Full Reachability Algorithm");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        // begin expanding the reachability graph from m0
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        workingList.add(new ReachabilityNode(marking, null));
        while (!workingList.isEmpty() && !isInterrupted()) {
            LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            // get a node to expand
            ReachabilityNode workingNode = workingList.get(0);
            workingList.remove(workingNode);
            HashSet<Transition> activeTransitions = pf.computeActive(pnf.transitions(), workingNode.getMarking());
            for (Transition t : activeTransitions) {
                // compute new marking
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                // Check for boundedness
                ReachabilityNode mBack = workingNode;
                while ((mBack != null) && (!newNode.largerThan(mBack))) {
                    mBack = mBack.getPrev();
                }
                if (mBack != null) {
                    LOGGER.error("Graph has been determined to be unbounded. Aborting algorithm.");
                    fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
                    return;
                } else {
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
        }
        if (isInterrupted()) {
            LOGGER.warn("Execution has been aborted.");
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            LOGGER.info("Completed creation of Reachability Graph.");
            g = new ReachabilityGraph(vertices, edges);
            fireReachabilityUpdate(ReachabilityEvent.Status.FINISHED, counter, null);
        }
    }

    protected static void testprint_larger(ReachabilityNode newNode, ReachabilityNode mBack) {
        LOGGER.info(newNode.largerThan(mBack));
        for (Place p : newNode.getMarking().keySet()) {
            LOGGER.warn(p.getProperty("name") + "\t" + newNode.getMarking().get(p) + "\t" + mBack.getMarking().get(p));
        }
    }

    protected static void testprint_marking(ReachabilityNode newNode) {
        for (Place p : newNode.getMarking().keySet()) {
            LOGGER.warn(p.getProperty("name") + "\t" + newNode.getMarking().get(p));
        }
    }

    private void oldRun() {
        LOGGER.info("Starting Full Reachability Algorithm");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        // initialize for m0 as root
        HashSet<Transition> activeTransitions = pf.computeActive(pnf.transitions(), marking);
        ReachabilityNode root = new ReachabilityNode(marking, null);
        vertices.add(root);
        // begin expanding the reachability graph from m0
        expandMarking(vertices, edges, root, activeTransitions);
        g = new ReachabilityGraph(vertices, edges);
    }

    private void expandMarking(HashSet vertices, HashSet edges, ReachabilityNode root, HashSet<Transition> activeTransitions) {
        for (Transition t : activeTransitions) {
            // compute new marking
            HashMap<Place, Long> mNew = pf.computeMarking(root.getMarking(), t);
            ReachabilityNode newNode = new ReachabilityNode(mNew, root);

            if (vertices.contains(newNode)) {
                edges.add(new ReachabilityEdge(root, newNode, t));
            } else {
                ReachabilityNode mBack = root;
                while ((mBack != null) && (mBack.largerThan(newNode))) {
                    mBack = mBack.getPrev();
                }
                if (mBack == null) {
                    vertices.add(newNode);
                    edges.add(new ReachabilityEdge(root, newNode, t));

                    // compute active transitions for next step
                    HashSet<Transition> actNew = new HashSet<>();
                    actNew.addAll(activeTransitions);
                    // remove transitions that are now disabled            
                    for (Place p : t.inputs()) {
                        for (Transition tOut : p.outputs()) {
                            if (mNew.get(p) < pnf.getArc(p, tOut).weight()) {
                                actNew.remove(tOut);
                            }
                        }
                    }

                    HashSet<Transition> toCheck = new HashSet<>();
                    // add all transitions that could now be active
                    for (Place p : t.outputs()) {
                        toCheck.addAll(p.outputs());
                    }
                    // remove transitions that are known to be active already
                    toCheck.removeAll(actNew);

                    // check for remaining transitions whether they are active
                    actNew.addAll(pf.computeActive(toCheck, mNew));

                    // recursive call for subtree
                    expandMarking(vertices, edges, newNode, actNew);
                } else {
                    LOGGER.error("Graph has been determined to be unbounded. Aborting algorithm.");
                    fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, 0, null);
                    throw new RuntimeException("Unbounded Graph");
                }
            }
        }
    }

    @Override
    public void computePriority(ReachabilityNode node) {
        // Does not use a priority.
    }
}
