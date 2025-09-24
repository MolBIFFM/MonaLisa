package monalisa.addons.reachability.algorithms;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import monalisa.addons.reachability.Pathfinder;
import monalisa.addons.reachability.ReachabilityEdge;
import monalisa.addons.reachability.ReachabilityEvent;
import monalisa.addons.reachability.ReachabilityGraph;
import monalisa.addons.reachability.ReachabilityNode;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;

/**
 *
 * @author Bo
 */

public class StochAStar extends AbstractReachabilityAlgorithm {

    private final PetriNetFacade pnf;
    // private final String heur;
    private final HashMap<Transition, Double> firingRates; 
    private final int maxPaths;

    public StochAStar(Pathfinder pf, PetriNetFacade pnf,HashMap<Place, Long> marking, HashMap<Place, Long> target,
            HashMap<Transition, Double> firingRates, int maxPaths) // String heur,  
            {
        super(pf, marking, target);
        this.pnf = pnf;
        this.firingRates = firingRates;
        this.maxPaths = maxPaths;
    }

    @Override
    public void run() {
        // LOGGER.debug("Starting AplusG Algorithm.");
        System.out.println("\nStarting Stochastic A* Algorithm.");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        ArrayList <ReachabilityNode> targets = new ArrayList<>();
        int foundPaths = 0;
        // initialize for m0 as root
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        root.setTime(0);
        tar = new ReachabilityNode(target, null);
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        workingList.add(root);

        String filePath = "C:\\Users\\61634\\Desktop\\Salmonella_output\\astar.csv";

        while (!workingList.isEmpty() && !isInterrupted()) {
            // LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            ReachabilityNode workingNode = workingList.get(0);
            // if(workingNode.getPrev()!= null){
            //     g = new ReachabilityGraph(vertices, edges);
            //     System.out.println("------------");
            //     System.out.println("Via Transition: "+g.getEdge(workingNode.getPrev(), workingNode).getTransition().toString());
            // }
            
            // System.out.println("working node depth:" + workingNode.getDepth());
            // System.out.println("working node probability:" + workingNode.getProbability());
            
            workingList.remove(workingNode);
            // vertices.add(workingNode);
            // LOGGER.debug("Expanding new marking with priority " + workingNode.getPriority());
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());
            HashMap<Transition, Double> rates = new HashMap<>();
            double ratesSum = 0;
            for (Transition t : activeTransitions) {
                // compute reaction rate
                double rate = 0;
                rate = pf.computeReactionRate(t, workingNode.getMarking(), firingRates);
                rates.put(t, rate);
                ratesSum += rate;
            }
            // ------Transition filter-------------
            // HashMap<Place, Long> diff = tar.getDifference(workingNode);
            // HashSet<Transition> valid_activeTs = new HashSet<>();
            // for (Place p : tar.getMarking().keySet()) {
            //     if (diff.get(p) < 0) {
            //         if (!p.outputs().isEmpty()){
            //             valid_activeTs.addAll(p.outputs());
            //         } 
            //     }
            //     if (diff.get(p) > 0) {
            //         if (!p.inputs().isEmpty()){
            //             valid_activeTs.addAll(p.inputs());
            //         }
            //     }
            // }
            // // 复制一份 activeTransitions
            // HashSet<Transition> candidateTs = new HashSet<>(activeTransitions);
            // // 和 valid_activeTs 取交集
            // candidateTs.retainAll(valid_activeTs);
            // for (Transition t : candidateTs) {
            //-------------------------------------
            for (Transition t : activeTransitions) {
                // LOGGER.debug("Created new node by firing transition " + t.getProperty("name") + ".");  // debug                                    
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                double probability = rates.get(t) / ratesSum;
                double prob_node = workingNode.getProbability() * probability;
                newNode.setProbability(prob_node);
                double reactionTime = 1 / rates.get(t);
                newNode.setTime(workingNode.getTime() + reactionTime);
                if (newNode.equals(tar)) {
                    // System.out.println("Probability of this path to target: " + newNode.getProbability());
                    targets.add(newNode);
                    foundPaths += 1;
                    if (maxPaths != -1 && foundPaths >= maxPaths){
                        System.out.println("Number of paths has been reached: " + targets.size());
                        exportPathsToCSV(targets, filePath);
                        fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, null);
                        return; 
                    }

                    // tar = newNode;
                    vertices.add(tar);
                    edges.add(new ReachabilityEdge(workingNode, tar, t, probability));
                    g = new ReachabilityGraph(vertices, edges);
                                       
                    // LOGGER.debug("Target marking has been reached.");
                    // return;
                    continue;
                }
                // boolean unvisited = true;
                // Has the node been seen before?
                // for (ReachabilityNode v : vertices) {
                //     if (v.equals(newNode)) {
                //         unvisited = false;
                //         edges.add(new ReachabilityEdge(workingNode, v, t, probability));
                //         // Potentially update depth
                //         if (v.getTime() > newNode.getTime()) {
                //             v.setPrev(workingNode);
                //             v.setTime(newNode.getTime());
                //             v.setProbability(prob_node);
                //         }
                //         break;
                //     }
                // }
                // If it hasn't been seen before, add it to vertices and workingList
                // if (unvisited) {
                    // System.out.println("Current transition:" +t.toString());
                    insertNode(newNode, workingList);
                    // System.out.println("Marking via Transition: "+t.toString()+"; getPriority: "+newNode.getPriority());
                    vertices.add(newNode);
                    edges.add(new ReachabilityEdge(workingNode, newNode, t, probability));
                // } // If it has been seen before, check if it has been expanded yet
                // else {
                    // for (ReachabilityNode v : workingList) {
                    //     if (v.equals(newNode)) {
                    //         // If it hasn't been expanded, the priority might have to be updated.
                    //         if (v.getTime() > newNode.getTime()) {
                    //             v.setPrev(workingNode);
                    //             v.setTime(newNode.getTime());
                    //             v.setProbability(prob_node);
                    //             updatePosition(v, workingList);
                    //             break;
                    //         }
                    //     }
                    // }
                // }
            }
        }
        if (!targets.isEmpty()) {
            System.out.println("Search ends, founded paths: " + targets.size());
            exportPathsToCSV(targets, filePath);
            g = new ReachabilityGraph(vertices, edges);
            // for (ReachabilityNode node : targets) {
            //     System.out.println("Probability of node: "+node.getProbability()+"; Depth"+node.getDepth());
            //     tar = node;
            //     ArrayList<Transition> path = backtrack();
            //     System.out.println("Path of start to tar: ");
            //     for (Transition t : path) {
            //         System.out.print(t.toString() + " ");
            //     }
            //     System.out.println();
            // }
            fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, null);
            return;
        }
        if (isInterrupted()) {
            // LOGGER.warn("Execution has been aborted.");
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            // LOGGER.info("Target marking could not be reached from start marking.");
            // g = new ReachabilityGraph(vertices, edges);
            fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, null);
        }
    }



    private void updatePosition(ReachabilityNode node, ArrayList<ReachabilityNode> workingList) {
        workingList.remove(node);
        insertNode(node, workingList);
    }

    private void insertNode(ReachabilityNode node, ArrayList<ReachabilityNode> workingList) {
        computePriority(node);
        if (node.getPriority() != Double.POSITIVE_INFINITY){
            int pos = findPos(workingList, node);
        // LOGGER.debug("Current priority: " + node.getPriority());
            workingList.add(pos, node);
        }
    }

    @Override
    public void computePriority(ReachabilityNode node) {
        // double prio = node.getDepth();
        double prio = node.getTime();
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
                        // intermediate.add(Math.floor(diff.get(p) / (-1 * pnf.getArc(p, t).weight())));
                        double rate = pf.computeReactionRate(t, node.getMarking(), firingRates);
                        // System.out.println("transition1: "+t.toString()+"; Rate: "+rate);
                        if (rate != 0){
                            intermediate.add(diff.get(p) / (-1* rate * pnf.getArc(p, t).weight()));
                        }
                    }
                    // System.out.println("ValidTransitions_1: " + validTransitions);
                    // System.out.println("Intermediate: " + intermediate);
                    if(!intermediate.isEmpty()){
                        placewise.add(Collections.min(intermediate));  
                    }
                    // else{
                    //     placewise.add(Double.POSITIVE_INFINITY);
                    // }
                }else{
                placewise.add(Double.POSITIVE_INFINITY);
                }
                              
            } // The place still has too few tokens compared to the target marking
            else if (diff.get(p) > 0) {
                if (!p.inputs().isEmpty()){
                    validTransitions.addAll(p.inputs());
                    for (Transition t : validTransitions) {
                        // intermediate.add(Math.floor(diff.get(p) / pnf.getArc(t, p).weight()));
                        double rate = pf.computeReactionRate(t, node.getMarking(), firingRates);
                        // System.out.println("transition2: "+t.toString()+"; Rate: "+rate);
                        if (rate != 0){
                            intermediate.add(diff.get(p) / (1* rate * pnf.getArc(t, p).weight()));
                        }
                    }
                    // System.out.println("ValidTransitions_2: " + validTransitions);
                    // System.out.println("Intermediate: " + intermediate);
                    if(!intermediate.isEmpty()){
                    placewise.add(Collections.min(intermediate));  
                    }
                    // else{
                    // placewise.add(Double.POSITIVE_INFINITY);
                    // }   
                }else{
                    placewise.add(Double.POSITIVE_INFINITY);
                }
                             
            }
        }
        //  System.out.println("-------------------------");
        // LOGGER.debug("Depth: " + Double.toString(prio) + " Heur: " + Double.toString(Collections.max(placewise)));
        if(!placewise.isEmpty()){
            prio += Collections.max(placewise); // Add heuristic
        }
        else{
            prio = Double.POSITIVE_INFINITY;
            // System.out.println("no valid transitions for heuristic calculation");
        }
        node.setPriority(prio);
    }

    private void exportPathsToCSV(ArrayList<ReachabilityNode> targets, String filePath) {
    // List<ReachabilityNode> sortedMatchedNodes = new ArrayList<>(matchedNodes);
    // sortedMatchedNodes.sort(Comparator.comparingInt(ReachabilityNode::getDepth));

    try (FileWriter writer = new FileWriter(filePath)) {
        // titles of columns
        writer.append("Time, Depth, Probability");
        for (Place place : pnf.places()) {
            writer.append(",").append(place.toString()); 
        }
        writer.append("\n");

        // 写每行数据
        for (ReachabilityNode node : targets) {
            writer.append(String.valueOf(node.getTime()))
                .append(",")
                .append(String.valueOf(node.getDepth()))
                .append(",")
                .append(String.valueOf(node.getProbability()));

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
