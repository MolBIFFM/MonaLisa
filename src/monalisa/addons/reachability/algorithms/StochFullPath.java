package monalisa.addons.reachability.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class StochFullPath extends AbstractReachabilityAlgorithm{
    // private static final Logger LOGGER = LogManager.getLogger(FullReachability.class);
    private final PetriNetFacade pnf;
    private final HashMap<Transition, Double> firingRates;
    private final int maxDepth; // -1 for infinite

    public StochFullPath(Pathfinder pf, PetriNetFacade pnf, HashMap<Place, Long> marking, HashMap<Place, Long> target, 
    HashMap<Transition, Double> firingRates, int maxDepth) {
        super(pf, marking, target);
        this.pnf = pnf;
        this.firingRates = firingRates; 
        this.maxDepth = maxDepth;
    }

    @Override
    public void run() {
        // LOGGER.info("Starting Full Reachability Algorithm");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        HashSet<ReachabilityNode> leafNodes = new HashSet<>();
        HashSet<ReachabilityNode> deadNodes = new HashSet<>();
         // begin expanding the reachability graph from m0
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        workingList.add(root);
        vertices.add(root);
        // boolean depthLimitReached = false;//&& !depthLimitReached
        while (!workingList.isEmpty() && !isInterrupted() ) {
            // LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
                // System.out.println("processing.... "+counter+"...");
            }
            // get a node to expand
            ReachabilityNode workingNode = workingList.get(0);
            workingList.remove(workingNode);
            
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());
            if(activeTransitions.isEmpty()){
                leafNodes.add(workingNode);

                // boolean iamdead = false;
                // for (ReachabilityNode dead : deadNodes) {
                //     if (workingNode.equals(dead)) {
                //         iamdead = true;
                //         break;
                //     }
                // }
                // if (!iamdead) {
                deadNodes.add(workingNode);
                // }
                continue;
            }
            // set the depth of search
            if (maxDepth != -1 && workingNode.getDepth() >= maxDepth){
//                    depthLimitReached = true;
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
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                newNode.setProbability(prob_node);
                // check if the new node is the dead node
                // Iterator<ReachabilityNode> deadIterator = deadNodes.iterator();
                // while (deadIterator.hasNext()) {
                //     ReachabilityNode dead = deadIterator.next();
                //     if (newNode.equals(dead)) {
                //         newNode.setProbability(newNode.getProbability() + dead.getProbability());
                //         deadIterator.remove();
                //         deadNodes.add(newNode);
                //         break;
                //     }
                // }
                edges.add(new ReachabilityEdge(workingNode, newNode, t, probability));
                vertices.add(newNode);
                workingList.add(newNode);
                // }
            }
        }
        if (isInterrupted()) {
            // LOGGER.warn("Execution has been aborted.");
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            // LOGGER.info("Completed creation of Reachability Graph.");
            g = new ReachabilityGraph(vertices, edges);
            // System.out.println("vertices has "+vertices.size()+" nodes");
            System.out.println("maxDepth: "+maxDepth);
            System.out.println("=== Reachability Nodes (Vertices) ===");
            List<ReachabilityNode> sortedNodes = new ArrayList<>(vertices);
            sortedNodes.sort(Comparator.comparingInt(ReachabilityNode::getDepth));
            for (ReachabilityNode node : sortedNodes) {
                StringBuilder sb = new StringBuilder("Marking: ");
                sb.append("( ");
                for (Place p : node.getMarking().keySet()) {
                    sb.append(node.getMarking().get(p))
                      .append(", ");
                }
                sb.append(" )"+"; Probability: "+node.getProbability()+"; Depth: "+node.getDepth());
                System.out.println(sb.toString());}
            System.out.println("=== Reachability Edges ===");
            StringBuilder sb = new StringBuilder("Transitions: ");
            for (ReachabilityEdge edge : edges) {
                sb.append(edge.getTransition().toString())
                //   .append(":")
                //   .append(firingRates.get(edge.getTransition()))
                  .append(",  ");
            }
            System.out.println(sb.toString());
            HashMap<ReachabilityNode, ArrayList<Transition>> paths = new HashMap<>();

            System.out.println("=== Dead Nodes ===");
            System.out.println("Dead nodes have "+deadNodes.size()+" nodes.");
            // Step 1: 分组所有具有相同 marking 的 node
            Map<Map<Place, Long>, List<ReachabilityNode>> markingGroups = new HashMap<>();

            for (ReachabilityNode node : deadNodes) {
                Map<Place, Long> marking = node.getMarking();
                markingGroups.computeIfAbsent(marking, k -> new ArrayList<>()).add(node);
            }

            // Step 2: 对每组 marking 的节点按深度统计概率
            for (Map.Entry<Map<Place, Long>, List<ReachabilityNode>> entry : markingGroups.entrySet()) {
                Map<Place, Long> marking = entry.getKey();
                List<ReachabilityNode> nodes = entry.getValue();

                // 深度 -> 当前深度的概率和
                Map<Integer, Double> depthToProb = new HashMap<>();
                for (ReachabilityNode node : nodes) {
                    int depth = node.getDepth();
                    double prob = node.getProbability();
                    depthToProb.put(depth, depthToProb.getOrDefault(depth, 0.0) + prob);
                }

                // 排序深度
                List<Integer> sortedDepths = new ArrayList<>(depthToProb.keySet());
                Collections.sort(sortedDepths);

                // 输出该 marking 的所有累计概率
                System.out.println("Marking:");
                for (Place p : marking.keySet()) {
                    System.out.print(p.toString() + "=" + marking.get(p) + " ");
                }
                System.out.println();

                double cumulative = 0.0;
                for (int depth : sortedDepths) {
                    cumulative += depthToProb.get(depth);
                    System.out.println("  Depth " + depth + ": cumulative probability = " + cumulative);
                }
                System.out.println();
            }

            System.out.println("=== Leaf Nodes ===");
            System.out.println("Leaf nodes have "+leafNodes.size()+" nodes.");
            List<ReachabilityNode> sortedLeaf = new ArrayList<>(leafNodes);
            sortedLeaf.sort(Comparator.comparingInt(ReachabilityNode::getDepth));
            // for (ReachabilityNode leaf : sortedLeaf) {
            //     System.out.println("Probability of leaf node: "+leaf.getProbability()+"; Depth"+leaf.getDepth());
            //     // this.tar = leaf;
            //     // ArrayList<Transition> path = backtrack();
            //     // paths.put(leaf, path);
            //     // System.out.println("Path of start to leaf: ");
            //     // for (Transition t : path) {
            //     //     System.out.print(t.toString() + " ");
            //     // }
            //     // System.out.println();
            // }
            fireReachabilityUpdate(ReachabilityEvent.Status.FINISHED, counter, null);
        }
    }

    @Override
    public void computePriority(ReachabilityNode node) {
        // Does not use a priority.
    }
}
