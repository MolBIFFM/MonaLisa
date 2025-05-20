package monalisa.addons.reachability.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import monalisa.addons.reachability.Pathfinder;
import monalisa.addons.reachability.ReachabilityEdge;
import monalisa.addons.reachability.ReachabilityEvent;
import monalisa.addons.reachability.ReachabilityGraph;
import monalisa.addons.reachability.ReachabilityNode;
import monalisa.addons.tokensimulator.utils.Utilities;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Bo
 */
public class StochFullReach extends AbstractReachabilityAlgorithm{
    // private static final Logger LOGGER = LogManager.getLogger(FullReachability.class);
    private final PetriNetFacade pnf;
    private final HashMap<Transition, Double> firingRates; 

    public StochFullReach(Pathfinder pf, PetriNetFacade pnf, HashMap<Place, Long> marking, HashMap<Place, Long> target, 
    HashMap<Transition, Double> firingRates) {
        super(pf, marking, target);
        this.pnf = pnf;
        this.firingRates = firingRates; 
    }

    @Override
    public void run() {
        // LOGGER.info("Starting Full Reachability Algorithm");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        HashSet<ReachabilityNode> leafNodes = new HashSet<>();
         // begin expanding the reachability graph from m0
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        workingList.add(root);
        // workingList.add(new ReachabilityNode(marking, null, 1));
        // workingList.add(new ReachabilityNode(marking, null));
        while (!workingList.isEmpty() && !isInterrupted()) {
            // LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            // get a node to expand
            ReachabilityNode workingNode = workingList.get(0);
            // System.out.println("Depth: "+workingNode.getDepth());
            workingList.remove(workingNode);
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());
            if(activeTransitions.isEmpty()){
                leafNodes.add(workingNode);
                continue;
            }
            HashMap<Transition, Double> rates = new HashMap<>();
            double ratesSum = 0;
            for (Transition t : activeTransitions) {
                // compute reaction rate
                double rate = pf.computeReactionRate(t, workingNode.getMarking(), firingRates);
                rates.put(t, rate);
                ratesSum += rate;
            }
            for (Transition t : activeTransitions) {
                // transfrom reaction rate to probability
                double probability = rates.get(t) / ratesSum;
                // compute new marking
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                // compute probability for reachability node
                double prob_node = workingNode.getProbability() * probability;
                // ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode, prob_node);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                newNode.setProbability(prob_node);
                // Check for boundedness
                ReachabilityNode mBack = workingNode;
                while ((mBack != null) && (!newNode.largerThan(mBack))) {
                    mBack = mBack.getPrev();
                }
                if (mBack != null) {
                    // LOGGER.error("Graph has been determined to be unbounded. Aborting algorithm.");
                    fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
                    return;
                } else {
                    edges.add(new ReachabilityEdge(workingNode, newNode, t, probability));
//                     System.out.println("Transition: "+t.toString()+"; Probability: " + probability);
//                     System.out.println("------------------------------");
                    boolean unvisited = true;
                    for (ReachabilityNode v : vertices) {
                        if (v.equals(newNode)) {
                            unvisited = false;
                            v.setProbability(v.getProbability()+prob_node);
                        }
                    }
                    if (unvisited) {
                        vertices.add(newNode);
                        workingList.add(newNode);
                    }
                    // if(!vertices.contains(newNode)){
                    //     vertices.add(newNode);
                    //     workingList.add(newNode);
                    // }
                }
            }
        }
        if (isInterrupted()) {
            // LOGGER.warn("Execution has been aborted.");
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            // LOGGER.info("Completed creation of Reachability Graph.");
            g = new ReachabilityGraph(vertices, edges);
            // System.out.println("vertices has "+vertices.size()+" nodes");
            System.out.println("=== Reachability Nodes (Vertices) ===");
            for (ReachabilityNode node : vertices) {
                StringBuilder sb = new StringBuilder("Marking: ");
                sb.append("( ");
                for (Place p : node.getMarking().keySet()) {
                    // sb.append(p.toString())
                    //   .append("=")
                    sb.append(node.getMarking().get(p))
                      .append(", ");
                }
                sb.append(" )"+"; Probability: "+node.getProbability()+"; Depth: "+node.getDepth());
                System.out.println(sb.toString());}
            HashMap<ReachabilityNode, ArrayList<Transition>> paths = new HashMap<>();
            for (ReachabilityNode leaf : leafNodes) {
                System.out.println("Ptobability of leaf node: "+leaf.getProbability()+"; Depth"+leaf.getDepth());
                // this.tar = leaf;
                // ArrayList<Transition> path = backtrack();
                // paths.put(leaf, path);
                // System.out.println("Path of start to leaf: ");
                // for (Transition t : path) {
                //     System.out.print(t.toString() + " ");
                // }
                // System.out.println();
            }
            // System.out.flush();
            fireReachabilityUpdate(ReachabilityEvent.Status.FINISHED, counter, null);
        }
    }
    
    // public double computeReactionRate(Transition t, HashMap<Place, Long> marking) {
    //     //number of distinct combinations of educt molecules.
    //     long h = 1;
    //     for ( Place p : t.inputs()) {
    //        long token = marking.get(p);
    //        long weight = pnf.getArc(p,t).weight();
    //        h *= Utilities.binomialCoefficient(token, weight);
    //     }
    //     return h * firingRates.get(t);
    // }

    @Override
    public void computePriority(ReachabilityNode node) {
        // Does not use a priority.
    }
}
