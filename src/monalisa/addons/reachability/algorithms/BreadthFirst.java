/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability.algorithms;

import com.sun.source.tree.Tree;
import edu.uci.ics.jung.visualization.util.LabelWrapper;
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
public class BreadthFirst extends AbstractReachabilityAlgorithm {

    private static final Logger LOGGER = LogManager.getLogger(BreadthFirst.class);
    private HashMap<Place, Long> eStart;
    private HashMap<Place, Long> eTarget;

    public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking, HashMap<Place, Long> target) {
        super(pf, marking, target);
    }

    /**
     * @author Kristin Haas
     * @param pf
     * @param start
     * @param target 
     *
    public BreadthFirst(Pathfinder pf, ReachabilityNode start,  ReachabilityNode target) {
        super(pf, start, target);
    }*/
    
    public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking,HashMap<Place, Long> target, HashMap<Place, Long> eStart, HashMap<Place, Long> eTaget ){
        super(pf, marking, target);
        this.eStart = eStart;
        this.eTarget = eTaget;
        
    }
    
    /**
     * include query  to ask for specific source and tagret
     */
    @Override
    public void run() {
        LOGGER.debug("Starting Breadth First Algorithm.");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        // initialize for m0 as root
        // If explicit start and target exist
        
        if(eStart != null && eTarget != null){
            if(eStart.keySet().iterator().next().toString() == eTarget.keySet().iterator().next().toString()){
                //System.out.println("TEST "+eStar+" "+eTar);
                fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, null);
                return;
                
    
            }
            //Somewhere here seems to be the problem
            ReachabilityNode root = new ReachabilityNode(marking, null);
            System.out.println("REACHABILITYNODE: "+root.getMarking());
           // ReachabilityNode root = new ReachabilityNode(start, null);
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
                System.out.println("COUNTER: "+counter+" WorkingList: "+workingList.get(0).getMarking()+" 0 "+pf.computeActive(workingNode.getMarking()));
                LOGGER.debug("Current marking:" + workingNode.getMarking().toString());
                workingList.remove(0);
                HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());
                for (Transition t : activeTransitions) {
                    if(workingNode.getMarking()!= null && t != null){
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
                                break;
                            }
                        }
                        if (unvisited) {
                            vertices.add(newNode);
                            workingList.add(newNode);
                            edges.add(new ReachabilityEdge(workingNode, newNode, t));
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
        else{

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

                LOGGER.debug("Current marking:" + workingNode.getMarking().toString());
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
                            break;
                        }
                    }
                    if (unvisited) {
                        vertices.add(newNode);
                        workingList.add(newNode);
                        edges.add(new ReachabilityEdge(workingNode, newNode, t));
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
         
    }

    @Override
    public void computePriority(ReachabilityNode node) {
        // Breadth First Search does not use a priority.
    }
}
