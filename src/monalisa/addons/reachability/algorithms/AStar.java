/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability.algorithms;

import java.util.ArrayList;
import java.util.Collections;
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
 * @author Marcel Gehrmann
 */
public class AStar extends AbstractReachabilityAlgorithm {

    private final String heur;
    private static final Logger LOGGER = LogManager.getLogger(AStar.class);

    public AStar(Pathfinder pf, PetriNetFacade pnf, HashMap<Place, Long> marking, HashMap<Place, Long> target, String heur) {
        super(pf, pnf, marking, target);
        this.heur = heur; // Currently a dummy as there is only one implemented heuristic.
    }

    @Override
    public void run() {
        LOGGER.info("Starting A* Algorithm.");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        // initialize for m0 as root
        ReachabilityNode root = new ReachabilityNode(marking, null);
        tar = new ReachabilityNode(target, null);
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        workingList.add(root);
        while (!workingList.isEmpty() && !isInterrupted()) {
            LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            ReachabilityNode workingNode = workingList.get(0);
            workingList.remove(workingNode);
            vertices.add(workingNode);
            LOGGER.debug("Expanding new marking with priority " + workingNode.getPriority());
            HashSet<Transition> activeTransitions = pf.computeActive(pnf.transitions(), workingNode.getMarking());
            for (Transition t : activeTransitions) {
                LOGGER.debug("Created new node by firing transition " + t.getProperty("name") + ".");  // debug                                    
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                /*for (Place p : pnf.places()) {                
                    LOGGER.info(p.getProperty("name") + "\t" + newNode.getMarking().get(p));
                }*/
                if (newNode.equals(tar)) {                    
                    tar = newNode;
                    vertices.add(tar);
                    edges.add(new ReachabilityEdge(workingNode, tar, t));
                    g = new ReachabilityGraph(vertices, edges);
                    fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack());                    
                    LOGGER.info("Target marking has been reached.");                  
                    return;
                }
                boolean unvisited = true;
                // Has the node been seen before?
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
                // If it hasn't been seen before, add it to vertices and workingList
                if (unvisited) {
                    insertNode(newNode, workingList);
                    vertices.add(newNode);
                    edges.add(new ReachabilityEdge(workingNode, newNode, t));
                } // If it has been seen before, check if it has been expanded yet
                else {
                    for (ReachabilityNode v : workingList) {
                        if (v.equals(newNode)) {
                            // If it hasn't been expanded, the priority might have to be updated.
                            if (v.getDepth() > newNode.getDepth()) {
                                v.setPrev(workingNode);
                                updatePosition(v, workingList);
                                break;
                            }
                        }
                    }
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

    private void updatePosition(ReachabilityNode node, ArrayList<ReachabilityNode> workingList) {
        workingList.remove(node);
        insertNode(node, workingList);
    }

    private void insertNode(ReachabilityNode node, ArrayList<ReachabilityNode> workingList) {
        computePriority(node);
        int pos = findPos(workingList, node);
        LOGGER.debug("Current priority: " + node.getPriority());
        workingList.add(pos, node);
    }

    @Override
    public void computePriority(ReachabilityNode node) {
        double prio = node.getDepth();
        HashMap<Place, Long> diff = tar.getDifference(node);
        /*for (Place p : pnf.places()) {
            LOGGER.debug(p.getProperty("name") + "\t" + node.getMarking().get(p)
            + "\t" + tar.getMarking().get(p) + "\t" + diff.get(p));
        }*/
        HashSet<Double> placewise = new HashSet<>();
        for (Place p : pnf.places()) {
            HashSet<Double> intermediate = new HashSet<>();
            ArrayList<Transition> validTransitions = new ArrayList<>();
            // The place still has too many tokens compared to the target marking
            LOGGER.debug(p.getProperty("name") + "\t" + diff.get(p));
            if (diff.get(p) < 0) {
                validTransitions.addAll(p.outputs());
                for (Transition t : validTransitions) {
                    intermediate.add(Math.floor(diff.get(p) / (-1 * pnf.getArc(p, t).weight())));
                }
                placewise.add(Collections.min(intermediate));                
            } // The place still has too few tokens compared to the target marking
            else if (diff.get(p) > 0) {
                validTransitions.addAll(p.inputs());
                for (Transition t : validTransitions) {
                    intermediate.add(Math.floor(diff.get(p) / pnf.getArc(t, p).weight()));
                }
                placewise.add(Collections.min(intermediate));                
            }
            // find smallest element in intermediate and add it to placewise            
            /*double smallest = Double.MAX_VALUE;
            for (Double entry : intermediate) {
                if (entry < smallest) {
                    smallest = entry;
                }
            }
            placewise.add(smallest);*/
        }
        // find maximal value in placewise and add it to priority
        /*double largest = Double.MIN_VALUE;
        for (Double entry : placewise) {
            if (entry > largest) {
                largest = entry;
            }
        }*/
        LOGGER.debug("Depth: " + Double.toString(prio) + " Heur: " + Double.toString(Collections.max(placewise)));
        prio += Collections.max(placewise); // Add heuristic
        node.setPriority(prio);
    }
}
