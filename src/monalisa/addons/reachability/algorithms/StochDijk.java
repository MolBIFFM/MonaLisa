package monalisa.addons.reachability.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
 * Stochastic Dijkstra search for stochastic Petri nets. 
 * This implementation performs a cost-based search with a top-K path constraint 
 * over the reachability tree, 
 * @author Bo
 */
public class StochDijk extends AbstractReachabilityAlgorithm {

    private final PetriNetFacade pnf;
    private final HashMap<Transition, Double> firingRates; 
    private final int maxPaths; // K

    public StochDijk(Pathfinder pf, PetriNetFacade pnf, HashMap<Place, Long> marking, HashMap<Place, Long> target,
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
        int foundPaths = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        ArrayList <ReachabilityNode> targets = new ArrayList<>();
        ArrayList <ReachabilityNode> expanded = new ArrayList<>();
        ArrayList <ReachabilityNode> addedtoQ = new ArrayList<>();
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        root.setCost(0);
        root.setRealTime(0);
        tar = new ReachabilityNode(target, null);
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        workingList.add(root);

        File dir = new File("output");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        String filePath = "output/dijkstra.csv";
        String filePath_expanded = "output/dijkstra_expanded.csv";
        String filePath_addedtoQ = "output/dijkstra_addedtoQ.csv";

        // Expansion loop
        while (!workingList.isEmpty() && !isInterrupted()) {
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }

            ReachabilityNode workingNode = workingList.get(0);
            workingList.remove(workingNode);

            // termination condition
            if (maxPaths != -1  && targets.size() == maxPaths 
                    && workingList.get(0).getCost()>=targets.get(maxPaths-1).getCost()){
                        long endTime = System.nanoTime();
                        long duration = endTime - startTime;
                        System.out.println("Dijkstra Number of paths has been reached: " + (targets.size()));
                        fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack());     
                        
                        // Output export
                        exportTargetsToCSV(targets, filePath);
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                            writer.write("Type,Time(ms),founded Paths");
                            writer.newLine();
                            writer.write("Dijkstra" + "," + (duration / 1_000_000.0) + "," + foundPaths);
                            writer.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } 
                        exportTargetsToCSV(expanded, filePath_expanded);
                        exportTargetsToCSV(addedtoQ, filePath_addedtoQ);
                        return;
                    }

            expanded.add(workingNode);
            vertices.add(workingNode);

            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());

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
                // get new marking                            
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                // transfrom reaction rate to probability
                double prob_t = rates.get(t) / ratesSum;
                // compute probability for reachability node
                double prob_node = workingNode.getProbability() * prob_t;
                newNode.setProbability(prob_node);
                double cost_t = -Math.log(prob_t); // cost for the Stochastic Dijkstra
                double newCost = workingNode.getCost() + cost_t;
                newNode.setCost(newCost);
                double reactionRealTime = 1 / ratesSum ; // cost for the Stochastic A*
                newNode.setRealTime(workingNode.getRealTime() + reactionRealTime);

                // pruning condition
                if (targets.size() == maxPaths) {
                    double worstTargetCost = targets.get(maxPaths - 1).getCost();
                    if (newCost >= worstTargetCost) {
                        continue;
                    }
                }

                // maintain top K targets
                if (newNode.equals(tar)) {
                    insertAndMaintainTopK(targets, newNode, maxPaths); 
                    foundPaths += 1;
                    tar = newNode;
                    vertices.add(tar);
                    edges.add(new ReachabilityEdge(workingNode, tar, t, prob_t));
                    g = new ReachabilityGraph(vertices, edges);
                }

                // Insert node into priority-ordered working list
                insertNode(newNode, workingList);
                addedtoQ.add(newNode);
                vertices.add(newNode);
                edges.add(new ReachabilityEdge(workingNode, newNode, t, prob_t));
            }
        }
         if (!targets.isEmpty()) {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            System.out.println("Dijkstra Search ends or aborted, founded paths: " + targets.size());     
            g = new ReachabilityGraph(vertices, edges);     
            
            // Output export
            exportTargetsToCSV(targets, filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write("Type,Time(ms),founded Paths");
                writer.newLine();
                writer.write("Dijkstra" + "," + (duration / 1_000_000.0) + "," + foundPaths);
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
            g = new ReachabilityGraph(vertices, edges);
            fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, null);
        }
    }

    private void insertNode(ReachabilityNode node, ArrayList<ReachabilityNode> workingList) {
        computePriority(node);
        int pos = findPos(workingList, node);
        workingList.add(pos, node);
    }

    public void insertAndMaintainTopK(ArrayList<ReachabilityNode> targets,
                                    ReachabilityNode candidate,
                                    int K) {

        double cost = candidate.getCost();
        if (targets.size() < K) {
            int pos = findInsertPosition(targets, cost);
            targets.add(pos, candidate);
            return;
        }

        double worstCost = targets.get(targets.size() - 1).getCost();
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

            if (targets.get(mid).getCost() < cost) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return left;
    }

    @Override
    public void computePriority(ReachabilityNode node) {
       node.setPriority(node.getCost());
    }

    private void exportTargetsToCSV(ArrayList<ReachabilityNode> targets, String filePath) {

        try (FileWriter writer = new FileWriter(filePath)) {
            // header
            writer.append("Depth,  Probability, -log(p), RealTime, path");
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

                writer.append(String.valueOf(node.getDepth()))
                    .append(",")
                    .append(String.valueOf(node.getProbability()))
                    .append(",")
                    .append(String.valueOf(node.getCost()))
                    .append(",")
                    .append(String.valueOf(node.getRealTime()))
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