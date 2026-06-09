package monalisa.addons.reachability.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import monalisa.addons.reachability.Pathfinder;
import monalisa.addons.reachability.ReachabilityEdge;
import monalisa.addons.reachability.ReachabilityEvent;
import monalisa.addons.reachability.ReachabilityGraph;
import monalisa.addons.reachability.ReachabilityNode;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;

/**
 * Stochastic A* search for stochastic Petri nets. 
 * This implementation performs a time-based search with a top-K path constraint 
 * over the reachability tree, 
 * @author Bo
 */
public class StochAStar extends AbstractReachabilityAlgorithm {

    private final PetriNetFacade pnf;
    private final HashMap<Transition, Double> firingRates; 
    private final int maxPaths; // K

    public StochAStar(Pathfinder pf, PetriNetFacade pnf,HashMap<Place, Long> marking, HashMap<Place, Long> target,
            HashMap<Transition, Double> firingRates, int maxPaths) 
            {
        super(pf, marking, target);
        this.pnf = pnf;
        this.firingRates = firingRates;
        this.maxPaths = maxPaths;
    }

    @Override
    public void run() {
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);

        // Initialization
        long startTime = System.nanoTime();
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        ArrayList <ReachabilityNode> targets = new ArrayList<>();
        ArrayList <ReachabilityNode> expanded = new ArrayList<>();
        ArrayList <ReachabilityNode> addedtoQ = new ArrayList<>();
        int foundPaths = 0;
        double minGInOpen = Double.POSITIVE_INFINITY;
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        root.setTime(0);
        root.setRealTime(0);
        root.setCost(0);
        tar = new ReachabilityNode(target, null);
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        workingList.add(root);

        File dir = new File("output");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        String filePath = "output/astar.csv";
        String filePath_expanded = "output/astar_expanded.csv";
        String filePath_addedtoQ = "output/astar_addedtoQ.csv";
        
        while (!workingList.isEmpty() && !isInterrupted()) {
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            ReachabilityNode workingNode = workingList.get(0);
            workingList.remove(workingNode);
            
            // Update min path cost g() after popping the node with the smallest f()
            if (Math.abs(workingNode.getRealTime() - minGInOpen) < 1e-9) {
                double min = Double.POSITIVE_INFINITY;
                for (ReachabilityNode n : workingList) {
                    min = Math.min(min, n.getRealTime());
                }
                minGInOpen = min;
            }

            // termination condition
            if (maxPaths != -1  && targets.size() == maxPaths 
                    && minGInOpen>=targets.get(maxPaths-1).getRealTime()){
                long endTime = System.nanoTime();
                long duration = endTime - startTime;
                System.out.println("Astar Number of paths has been reached: " + (targets.size()));

                // Output export
                exportTargetsToCSV(targets, filePath);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                    writer.write("Type,Time(ms),founded Paths");
                    writer.newLine();
                    writer.write("Astar" + "," + (duration / 1_000_000.0)+ ","+ foundPaths);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                exportTargetsToCSV(expanded, filePath_expanded);
                exportTargetsToCSV(addedtoQ, filePath_addedtoQ);
                fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, null);
                return; 
            }

            // pruning condition
            if (targets.size() == maxPaths) {
                double worstTargetCost = targets.get(maxPaths - 1).getRealTime();
                if (workingNode.getRealTime() > worstTargetCost ) {
                    continue;
                }
            }

            expanded.add(workingNode);
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());

            // calculate the sum of all active transitions
            HashMap<Transition, Double> rates = new HashMap<>();
            double ratesSum = 0;
            for (Transition t : activeTransitions) {
                // compute reaction rate
                double rate = 0;
                rate = pf.computeReactionRate(t, workingNode.getMarking(), firingRates);
                rates.put(t, rate);
                ratesSum += rate;
            }
           
            for (Transition t : activeTransitions) {    
                // get new marking                                
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                // transfrom reaction rate to probability
                double prob_t = rates.get(t) / ratesSum;
                // compute probability for reachability node
                double prob_node = workingNode.getProbability() * prob_t;
                newNode.setProbability(prob_node);
                double reactionTime = 1 / rates.get(t);
                newNode.setTime(workingNode.getTime() + reactionTime);
                double reactionRealTime = 1 / ratesSum ; // cost for the Stochastic A*
                newNode.setRealTime(workingNode.getRealTime() + reactionRealTime);
                double cost_t = -Math.log(prob_t); // cost for the Stochastic Dijkstra
                double newCost = workingNode.getCost() + cost_t;
                newNode.setCost(newCost);
            
                // maintain top K targets
                if (newNode.equals(tar)) {
                    insertAndMaintainTopK(targets, newNode, maxPaths);
                    foundPaths += 1;
                    vertices.add(tar);
                    edges.add(new ReachabilityEdge(workingNode, tar, t, prob_t));
                    g = new ReachabilityGraph(vertices, edges);
                }
               
                insertNode(newNode, workingList);
                minGInOpen = Math.min(minGInOpen, newNode.getRealTime());
                addedtoQ.add(newNode);
                vertices.add(newNode);
                edges.add(new ReachabilityEdge(workingNode, newNode, t, prob_t));
            }
        }
        if (!targets.isEmpty()) {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            System.out.println("Astar Search ends or aborted, founded paths: " + (targets.size()));     
            g = new ReachabilityGraph(vertices, edges);    
            
            // Output export
            exportTargetsToCSV(targets, filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write("Type,Time(ms),founded Paths");
                writer.newLine();
                writer.write("Astar" + "," + (duration / 1_000_000.0) + "," + foundPaths);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            } 
            exportTargetsToCSV(addedtoQ, filePath_addedtoQ);
            exportTargetsToCSV(expanded, filePath_expanded);
            fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, null);
            return;
        }
        if (isInterrupted()) {
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, null);
        }
    }

    private void insertNode(ReachabilityNode node, ArrayList<ReachabilityNode> workingList) {
        computePriority(node);
        // only insert nodes with finite priority
        if (node.getPriority() != Double.POSITIVE_INFINITY){
            int pos = findPos(workingList, node);
            workingList.add(pos, node);
        }
    }

    public void insertAndMaintainTopK(ArrayList<ReachabilityNode> targets,
                                    ReachabilityNode candidate,
                                    int K) {

        double cost = candidate.getRealTime();
        if (targets.size() < K) {
            int pos = findInsertPosition(targets, cost);
            targets.add(pos, candidate);
            return;
        }

        double worstCost = targets.get(targets.size() - 1).getRealTime();
        if (cost >= worstCost) {
            return; 
        }

        // insert candidate, remove the worst target
        int pos = findInsertPosition(targets, cost);
        targets.add(pos, candidate);
        targets.remove(targets.size() - 1);
    }
 
    private int findInsertPosition(ArrayList<ReachabilityNode> targets, double cost) {
        int left = 0;
        int right = targets.size();

        while (left < right) {
            int mid = (left + right) / 2;

            if (targets.get(mid).getRealTime() < cost) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return left;
    }

    @Override
    public void computePriority(ReachabilityNode node) {
        double prio = node.getRealTime();
        HashMap<Place, Long> diff = tar.getDifference(node);
        HashSet<Double> placewise = new HashSet<>();

        for (Place p : tar.getMarking().keySet()) {
            HashSet<Double> intermediate = new HashSet<>();
            ArrayList<Transition> validTransitions = new ArrayList<>();
            // The place still has too many tokens compared to the target marking
            if (diff.get(p) < 0) {
                if (!p.outputs().isEmpty()){
                    validTransitions.addAll(p.outputs());
                    for (Transition t : validTransitions) {
                        double rate = pf.computeReactionRate(t, node.getMarking(), firingRates);
                        if (rate != 0){
                            intermediate.add(diff.get(p) / (-1* rate * pnf.getArc(p, t).weight()));
                        }
                    }
                    if(!intermediate.isEmpty()){
                        placewise.add(Collections.min(intermediate));  
                    }
                }else{
                placewise.add(Double.POSITIVE_INFINITY);
                }                   
            } 
            // The place still has too few tokens compared to the target marking
            else if (diff.get(p) > 0) {
                if (!p.inputs().isEmpty()){
                    validTransitions.addAll(p.inputs());
                    for (Transition t : validTransitions) {
                        double rate = pf.computeReactionRate(t, node.getMarking(), firingRates);
                        if (rate != 0){
                            intermediate.add( diff.get(p) / (1* rate * pnf.getArc(t, p).weight()));
                        }
                    }
                    if(!intermediate.isEmpty()){
                    placewise.add(Collections.min(intermediate));  
                    }  
                }else{
                    placewise.add(Double.POSITIVE_INFINITY);
                }
                             
            }
        }
        if(!placewise.isEmpty()){
            double alpha = 1;
            prio += alpha * Collections.max(placewise); // Add heuristic score
        }else if(node.equals(tar)){
            prio += 0;
        }
        else{
            prio = Double.POSITIVE_INFINITY;
        }
        node.setPriority(prio);
    }

    private void exportTargetsToCSV(ArrayList<ReachabilityNode> targets, String filePath) {

        try (FileWriter writer = new FileWriter(filePath)) {
            // header
            writer.append("Depth, Probability, Time, RealTime, H_score, priority, path");

            for (Place place : pnf.places()) {
                writer.append(",").append(place.toString()); 
            }
            writer.append("\n");

            // write node data
            for (ReachabilityNode node : targets) {

                //Reconstruct the path from root to the current node
                tar = node;
                ArrayList<Transition> path = backtrack();
                String pathStr = path.stream()
                        .map(t -> t.toString())  
                        .collect(Collectors.joining(","));

                writer
                    .append(String.valueOf(node.getDepth()))
                    .append(",")
                    .append(String.valueOf(node.getProbability()))
                    .append(",")
                    .append(String.valueOf(node.getTime()))
                    .append(",")
                    .append(String.valueOf(node.getRealTime()))
                    .append(",")
                    .append(String.valueOf(node.getPriority() - node.getRealTime())) // heuristic score
                    .append(",")
                    .append(String.valueOf(node.getPriority()))
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