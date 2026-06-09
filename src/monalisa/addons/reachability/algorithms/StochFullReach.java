package monalisa.addons.reachability.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
 * Stochastic full reachability graph construction for stochastic Petri nets.
 * This implementation builds a full reachability graph with probabilistic transitions
 * based on firing rates, and exports marking-level statistics for analysis.
 * @author Bo
 */
public class StochFullReach extends AbstractReachabilityAlgorithm{
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
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        
        // Initialization
        long startTime = System.nanoTime();
        int counter = 0;
        int counter_visited = 0; // number of nodes dequeued
        int counter_expanded = 0; // number of nodes with successors
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        HashSet<ReachabilityNode> leafNodes = new HashSet<>(); 
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        workingList.add(root);
        vertices.add(root);
    
        // Expansion loop
        while (!workingList.isEmpty() && !isInterrupted() ) {
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            // get a node to expand
            ReachabilityNode workingNode = workingList.get(0);
            counter_visited += 1;
            workingList.remove(workingNode);
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());
            if(activeTransitions.isEmpty()){
                leafNodes.add(workingNode);
                continue;
            }else{
                counter_expanded += 1;
            }

            // calculate the sum of all active transitions
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
                // get new marking
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                // compute probability for reachability node
                double prob_node = workingNode.getProbability() * probability;
                // generate new reachability node
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                newNode.setProbability(prob_node);

                // Cycle detection
                boolean isCycle = false;
                ReachabilityNode cycleCheck = workingNode;
                while (cycleCheck != null) {
                    if (newNode.equals(cycleCheck)) {
                        isCycle = true;
                        break;
                    }
                    cycleCheck = cycleCheck.getPrev();
                }
                if (isCycle) {
                    edges.add(new ReachabilityEdge(workingNode, newNode, t, probability));
                    // do not add further node to working list
                    continue;
                }

                edges.add(new ReachabilityEdge(workingNode, newNode, t, probability));
                boolean unvisited = true;
                for (ReachabilityNode v : vertices) {
                    if (v.equals(newNode)) {
                        // converging node, update probability
                        unvisited = false;
                        v.setProbability(v.getProbability()+prob_node);
                    }
                }
                if (unvisited) {
                    vertices.add(newNode);
                    workingList.add(newNode);
                }
            }
        }
        if (isInterrupted()) {
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            g = new ReachabilityGraph(vertices, edges);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            // Output export
            File dir = new File("output");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = "output/fullreach.csv";
            exportNodesToCSV(vertices, leafNodes, filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write("Type,Visited,Expanded,Stored,Time(ms)");
                writer.newLine();
                writer.write("Reach" + "," + counter_visited + "," + counter_expanded + "," + vertices.size() + "," + (duration / 1_000_000.0));
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

    private void exportNodesToCSV(HashSet<ReachabilityNode> vertices, HashSet<ReachabilityNode> leafNodes, String filePath) {
        List<ReachabilityNode> sortedNodes  = new ArrayList<>(vertices);
        sortedNodes.sort(Comparator.comparingInt(ReachabilityNode::getDepth));

        try (FileWriter writer = new FileWriter(filePath)) {
            // header
            writer.append("Depth,Probability,LeafNode");
            for (Place place : pnf.places()) {
                writer.append(",").append(place.toString()); // 或 place.getId()
            }
            writer.append("\n");

            // write node data
            for (ReachabilityNode node : sortedNodes) {
                boolean isLeaf = leafNodes.contains(node);
                writer.append(String.valueOf(node.getDepth()))
                    .append(",")
                    .append(String.valueOf(node.getProbability()))
                    .append(",")
                    .append(isLeaf ? "leaf" : "");

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