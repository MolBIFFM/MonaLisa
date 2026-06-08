package monalisa.addons.reachability.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
        long startTime = System.nanoTime();
        int counter = 0;
        int counter_expanded = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        HashSet<ReachabilityNode> leafNodes = new HashSet<>();
        HashSet<ReachabilityNode> deadNodes = new HashSet<>();
        // HashSet<ReachabilityNode> matchedNodes = new HashSet<>();
        ArrayList <ReachabilityNode> matchedNodes = new ArrayList<>();
        //initialization of target places for fuzzy search
        int counter_match = 0;
        // DeathSigCyt = pnf.findPlace(22);
        // DeathSigVac = pnf.findPlace(23);
        // NrRuffle = pnf.findPlace(4);
        // NrRuffle = pnf.findPlace(3); // reduced model
        // p1 = pnf.findPlace(0);
        // p2 = pnf.findPlace(1);
        // begin expanding the reachability graph from m0
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        root.setCost(0);
        root.setRealTime(0);
        workingList.add(root);
        vertices.add(root);
        matchedNodes.add(root);
        // boolean depthLimitReached = false;//&& !depthLimitReached
        while (!workingList.isEmpty() && !isInterrupted() ) {
            // LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            // if(counter_match != 0 && counter_match % 10 == 0){
            //     System.out.println("processing.... has found "+counter_match+" hits.");
            // }
            // get a node to expand
            ReachabilityNode workingNode = workingList.get(0);
            workingList.remove(workingNode);
            
            // if (counter % 1000 == 0) {
            //     System.out.println("----------------");
            //     System.out.println("processing.... "+ counter +" nodes have been expanded.");
            //     System.out.println("... has reached "+ workingNode.getDepth()+" depth.");
            //     System.out.println(".... has found "+ counter_match+" hits.");
            // }

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
            }else{
                counter_expanded += 1;
            }
            // set the depth of search
            if (maxDepth != -1 && workingNode.getDepth() >= maxDepth){
                    leafNodes.add(workingNode);
                    continue;
                }
            HashMap<Transition, Double> rates = new HashMap<>();
            double ratesSum = 0;
            //calculate the reaction rates sum of all active transitions
            for (Transition t : activeTransitions) {
                // compute reaction rate
                double rate = pf.computeReactionRate(t, workingNode.getMarking(), firingRates);
                rates.put(t, rate);
                ratesSum += rate;
            }
            for (Transition t : activeTransitions) {
                // transfrom reaction rate to probability
                double prob_t = rates.get(t) / ratesSum;
                // compute new marking
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                // compute probability for reachability node
                double prob_node = workingNode.getProbability() * prob_t;
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                double cost_t = -Math.log(prob_t);//priority for Dijkstra's algorithm
                double newCost = workingNode.getCost() + cost_t;
                newNode.setCost(newCost);
                double reactionRealTime = 1 / ratesSum ;
                newNode.setRealTime(workingNode.getRealTime() + reactionRealTime);
                //For Salmonella model, set a threshold to avoid explosion of state space
                // if (newNode.getMarking().get(NrRuffle)> 4){
                //     continue;
                // }
                newNode.setProbability(prob_node);
                // check for the match
                // if (IsMatch(newNode)){
                    matchedNodes.add(newNode);
                    counter_match += 1;
                // }
                edges.add(new ReachabilityEdge(workingNode, newNode, t, prob_t));
                vertices.add(newNode);
                workingList.add(newNode);
            }
        }
        if (isInterrupted()) {
            // LOGGER.warn("Execution has been aborted.");
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            // System.out.println("Totally has found "+counter_match+" hits.");
            String filePath = "C:\\Users\\61634\\Desktop\\Salmonella_output\\fullpath.csv";
            g = new ReachabilityGraph(vertices, edges);
            exportMatchedNodesToCSV(matchedNodes, leafNodes, filePath);
            // LOGGER.info("Completed creation of Reachability Graph.");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write("Type,Visited,Expanded,Stored,Time(ms)");//new added nodes //newvisited nodes
                writer.newLine();
                writer.write("Reach" + "," + counter + "," + counter_expanded + "," + matchedNodes.size() + "," + (duration / 1_000_000.0));
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fireReachabilityUpdate(ReachabilityEvent.Status.FINISHED, counter, null);
        }
    }

    @Override
    public void computePriority(ReachabilityNode node) {
        // Does not use a priority.
    }

    private Place p1;
    private Place p2;
    private Place DeathSigCyt;
    private Place DeathSigVac;
    private Place NrRuffle;

    // Fuzzy match
    public boolean IsMatch(ReachabilityNode node){
        // if (node.getMarking().get(p1) == 1 || node.getMarking().get(p2) == 1){
        // if (node.getMarking().get(DeathSigCyt) == 1 || node.getMarking().get(DeathSigVac) == 1){
        if (node.getMarking().get(NrRuffle) != 0){
            return true;
        }
        return false;
    }

    private void exportMatchedNodesToCSV(ArrayList<ReachabilityNode> matchedNodes, HashSet<ReachabilityNode> leafNodes,String filePath) {
    // List<ReachabilityNode> sortedMatchedNodes = new ArrayList<>(matchedNodes);
    // sortedMatchedNodes.sort(Comparator.comparingInt(ReachabilityNode::getDepth));

    try (FileWriter writer = new FileWriter(filePath)) {
        // 写标题
        writer.append("Depth, Probability, -log(p), RealTime, LeafNode, path");//
        for (Place place : pnf.places()) {
            writer.append(",").append(place.toString()); // 或 place.getId()
        }
        writer.append("\n");

        // 写每行数据
        for (ReachabilityNode node : matchedNodes) {
            tar = node;
            ArrayList<Transition> path = backtrack();
            String pathStr = path.stream()
                     .map(t -> t.toString())  
                     .collect(Collectors.joining(","));

            boolean isLeaf = leafNodes.contains(node);
            writer.append(String.valueOf(node.getDepth()))
                  .append(",")
                  .append(String.valueOf(node.getProbability()))
                  .append(",")
                  .append(String.valueOf(node.getCost()))
                  .append(",")
                  .append(String.valueOf(node.getRealTime()))
                  .append(",")
                  .append(isLeaf ? "leaf" : "")
                  .append(",")
                  .append(String.valueOf("\"" + pathStr + "\""))
                  ;

            for (Place place : pnf.places()) {
                Long tokens = node.getMarking().get(place);
                writer.append(",").append(String.valueOf(tokens != null ? tokens : 0));
            }
            writer.append("\n");
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    }
}
