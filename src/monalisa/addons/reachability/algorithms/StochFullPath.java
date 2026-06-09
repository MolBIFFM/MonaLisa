package monalisa.addons.reachability.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.io.BufferedWriter;
import java.io.File;
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
 * Stochastic reachability tree construction for stochastic Petri nets.
 * This implementation builds a reachability tree with probabilistic transitions
 * based on firing rates, and exports path-level statistics for analysis.
 * @author Bo
 */
public class StochFullPath extends AbstractReachabilityAlgorithm{
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
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);

        // Initialization
        long startTime = System.nanoTime();
        int counter = 0;
        int counter_expanded = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        HashSet<ReachabilityNode> leafNodes = new HashSet<>();
        HashSet<ReachabilityNode> deadNodes = new HashSet<>();
        ArrayList <ReachabilityNode> matchedNodes = new ArrayList<>();
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        root.setCost(0);
        root.setRealTime(0);
        workingList.add(root);
        vertices.add(root);
        matchedNodes.add(root);

        // Expansion loop
        while (!workingList.isEmpty() && !isInterrupted() ) {
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
          
            // get a node to expand
            ReachabilityNode workingNode = workingList.get(0);
            workingList.remove(workingNode);
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());
            if(activeTransitions.isEmpty()){
                leafNodes.add(workingNode);
                deadNodes.add(workingNode);
                continue;
            }else{
                counter_expanded += 1;
            }

            // Depth constraint (used for bounded exploration)
            if (maxDepth != -1 && workingNode.getDepth() >= maxDepth) {
                    leafNodes.add(workingNode);
                    continue;
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
                double prob_t = rates.get(t) / ratesSum;
                // get new marking
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                // compute probability for reachability node
                double prob_node = workingNode.getProbability() * prob_t;
                // generate new reachability node
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                double cost_t = -Math.log(prob_t); // cost for the Stochastic Dijkstra
                double newCost = workingNode.getCost() + cost_t;
                newNode.setCost(newCost);
                double reactionRealTime = 1 / ratesSum ; // cost for the Stochastic A*
                newNode.setRealTime(workingNode.getRealTime() + reactionRealTime);
                newNode.setProbability(prob_node);
                matchedNodes.add(newNode);
                edges.add(new ReachabilityEdge(workingNode, newNode, t, prob_t));
                vertices.add(newNode);
                workingList.add(newNode);
            }
        }
        if (isInterrupted()) {
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            // Output export
            File dir = new File("output");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = "output/fullpath.csv";
            g = new ReachabilityGraph(vertices, edges);
            exportNodesToCSV(matchedNodes, leafNodes, filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write("Type,Visited,Expanded,Stored,Time(ms)");
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

    private void exportNodesToCSV(ArrayList<ReachabilityNode> matchedNodes, HashSet<ReachabilityNode> leafNodes,String filePath) {

        try (FileWriter writer = new FileWriter(filePath)) {
            // header
            writer.append("Depth, Probability, -log(p), RealTime, LeafNode, path");//
            for (Place place : pnf.places()) {
                writer.append(",").append(place.toString()); // 或 place.getId()
            }
            writer.append("\n");

            // write node data
            for (ReachabilityNode node : matchedNodes) {

                //Reconstruct the path from root to the current node
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