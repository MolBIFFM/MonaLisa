package monalisa.addons.reachability.algorithms;

import java.io.BufferedWriter;
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
 *
 * @author Bo
 */

public class StochDijk extends AbstractReachabilityAlgorithm {

    private final PetriNetFacade pnf;
    // private final String heur;
    private final HashMap<Transition, Double> firingRates; 
    private final int maxPaths;

    public StochDijk(Pathfinder pf, PetriNetFacade pnf, HashMap<Place, Long> marking, HashMap<Place, Long> target,
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
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        long startTime = System.nanoTime();
        int counter = 0;
        int foundPaths = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        ArrayList <ReachabilityNode> targets = new ArrayList<>();
        ArrayList <ReachabilityNode> expanded = new ArrayList<>();
        ArrayList <ReachabilityNode> addedtoQ = new ArrayList<>();
        // initialize for m0 as root
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        // root.setTime(0);
        root.setCost(0);
        root.setRealTime(0);
        tar = new ReachabilityNode(target, null);
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        workingList.add(root);
        // targets.add(root);

        String filePath = "C:\\Users\\61634\\Desktop\\Salmonella_output\\dijkstra.csv";
        String filePath_expanded = "C:\\Users\\61634\\Desktop\\Salmonella_output\\dijkstra_expanded.csv";
        String filePath_addedtoQ = "C:\\Users\\61634\\Desktop\\Salmonella_output\\dijkstra_addedtoQ.csv";


        while (!workingList.isEmpty() && !isInterrupted()) {
            // LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            ReachabilityNode workingNode = workingList.get(0);
            // System.out.println("working node probability:" + workingNode.getProbability());
            workingList.remove(workingNode);

            if (maxPaths != -1  && targets.size() == maxPaths 
                    && workingList.get(0).getCost()>=targets.get(maxPaths-1).getCost()){
                        long endTime = System.nanoTime();
                        long duration = endTime - startTime;
                        System.out.println("Dijkstra Number of paths has been reached: " + (targets.size()));
                        // g = new ReachabilityGraph(vertices, edges);
                        fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack());                    
                        // LOGGER.debug("Target marking has been reached.");
                        exportTargetsToCSV(targets, filePath);
                       
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                            writer.write("Type,Time(ms),founded Paths");//new added nodes //newvisited nodes
                            // Visited,Expanded,Stored,
                            writer.newLine();
                            writer.write("Dijkstra" + "," + (duration / 1_000_000.0) + "," + foundPaths);
                            //  counter + "," + counter_expanded + "," + matchedNodes.size() + "," +
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
            // LOGGER.debug("Expanding new marking with priority " + workingNode.getPriority());
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());
            HashMap<Transition, Double> rates = new HashMap<>();
            double ratesSum = 0;
            for (Transition t : activeTransitions) {
                // compute reaction rate
                double rate = pf.computeReactionRate(t, workingNode.getMarking(), firingRates);
                rates.put(t, rate);
                ratesSum += rate;
            }
            for (Transition t : activeTransitions) {
                // LOGGER.debug("Created new node by firing transition " + t.getProperty("name") + ".");  // debug                                    
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                double prob_t = rates.get(t) / ratesSum;
                double prob_node = workingNode.getProbability() * prob_t;
                newNode.setProbability(prob_node);
                double cost_t = -Math.log(prob_t);
                double newCost = workingNode.getCost() + cost_t;//priority for Dijkstra's algorithm
                newNode.setCost(newCost);
                double reactionRealTime = 1 / ratesSum ;
                newNode.setRealTime(workingNode.getRealTime() + reactionRealTime);
                // double reactionTime = 1 / rates.get(t);
                // newNode.setTime(workingNode.getTime() + reactionTime);
                //changed at 28.04
                if (targets.size() == maxPaths) {
                    double worstTargetCost = targets.get(maxPaths - 1).getCost();
                    if (newCost >= worstTargetCost) {
                        continue;
                    }
                }
                if (newNode.equals(tar)) {
                    // System.out.println("Probability of this path to target: " + newNode.getProbability());
                    // System.out.println("Cost of this path to target: " + newNode.getCost());
                    // targets.add(newNode);
                    insertAndMaintainTopK(targets, newNode, maxPaths); //changed at 28.04
                    foundPaths += 1;
                    tar = newNode;
                    vertices.add(tar);
                    edges.add(new ReachabilityEdge(workingNode, tar, t, prob_t));
                    g = new ReachabilityGraph(vertices, edges);
                    // if (maxPaths != -1 && foundPaths >= maxPaths){
                    // changed at 28.04
                    // if (maxPaths != -1  && targets.size() == maxPaths 
                    // && workingList.get(0).getCost()>=targets.get(maxPaths-1).getCost()){
                    //     long endTime = System.nanoTime();
                    //     long duration = endTime - startTime;
                    //     System.out.println("Dijkstra Number of paths has been reached: " + (targets.size()));
                    //     // g = new ReachabilityGraph(vertices, edges);
                    //     fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack());                    
                    //     // LOGGER.debug("Target marking has been reached.");
                    //     exportTargetsToCSV(targets, filePath);
                       
                    //     try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                    //         writer.write("Type,Time(ms),founded Paths");//new added nodes //newvisited nodes
                    //         // Visited,Expanded,Stored,
                    //         writer.newLine();
                    //         writer.write("Dijkstra" + "," + (duration / 1_000_000.0) + "," + foundPaths);
                    //         //  counter + "," + counter_expanded + "," + matchedNodes.size() + "," +
                    //         writer.newLine();
                    //     } catch (IOException e) {
                    //         e.printStackTrace();
                    //     } 
                    //     exportTargetsToCSV(expanded, filePath_expanded);
                    //     exportTargetsToCSV(addedtoQ, filePath_addedtoQ);
                    //     return;
                    // }
                    // continue;
                }
                insertNode(newNode, workingList);
                addedtoQ.add(newNode);
                vertices.add(newNode);
                edges.add(new ReachabilityEdge(workingNode, newNode, t, prob_t));
                //-------------------------------------
                // boolean unvisited = true;
                // // Has the node been seen before?
                // for (ReachabilityNode v : vertices) {
                //     if (v.equals(newNode)) {
                //         unvisited = false;
                //         edges.add(new ReachabilityEdge(workingNode, v, t, prob_t));
                //         // Potentially update depth
                //         if (v.getCost() > newNode.getCost()) {
                //             v.setPrev(workingNode);
                //             v.setCost(newCost);
                //             v.setProbability(prob_node);
                //         }
                //         break;
                //     }
                // }
                // // If it hasn't been seen before, add it to vertices and workingList
                // if (unvisited) {
                //     insertNode(newNode, workingList);
                //     vertices.add(newNode);
                //     edges.add(new ReachabilityEdge(workingNode, newNode, t, prob_t));
                // } // If it has been seen before, check if it has been expanded yet
                // else {
                //     for (ReachabilityNode v : workingList) {
                //         if (v.equals(newNode)) {
                //             // If it hasn't been expanded, the priority might have to be updated.
                //             if (v.getCost() > newNode.getCost()) {
                //                 v.setPrev(workingNode);
                //                 v.setCost(newCost);
                //                 v.setProbability(prob_node);
                //                 updatePosition(v, workingList);
                //                 break;
                //             }
                //         }
                //     }
                // }
                //------------------------------------
            }
        }
         if (!targets.isEmpty()) {
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            System.out.println("Dijkstra Search ends or aborted, founded paths: " + targets.size());     
            g = new ReachabilityGraph(vertices, edges);     
            exportTargetsToCSV(targets, filePath);
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write("Type,Time(ms),founded Paths");//new added nodes //newvisited nodes
                // Visited,Expanded,Stored,
                writer.newLine();
                writer.write("Dijkstra" + "," + (duration / 1_000_000.0) + "," + foundPaths);
                //  counter + "," + counter_expanded + "," + matchedNodes.size() + "," +
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
            // LOGGER.warn("Execution has been aborted.");
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            // LOGGER.info("Target marking could not be reached from start marking.");
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
        // LOGGER.debug("Current priority: " + node.getPriority());
        workingList.add(pos, node);
    }

    // changed at 28.04 
    public void insertAndMaintainTopK(ArrayList<ReachabilityNode> targets,
                                    ReachabilityNode candidate,
                                    int K) {

        double cost = candidate.getCost();

        // 👉 1. 如果还没满，直接插入到正确位置
        if (targets.size() < K) {
            int pos = findInsertPosition(targets, cost);
            targets.add(pos, candidate);
            return;
        }

        // 👉 2. 如果已经满了，先判断是否有资格进入
        double worstCost = targets.get(targets.size() - 1).getCost();

        if (cost >= worstCost) {
            return; // ❌ 比最差的还差，直接丢弃
        }

        // 👉 3. 插入 + 删除最后一个
        int pos = findInsertPosition(targets, cost);
        targets.add(pos, candidate);

        // 保持 size = K
        targets.remove(targets.size() - 1);
    }

    // changed at 28.04 
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
    // List<ReachabilityNode> sortedMatchedNodes = new ArrayList<>(matchedNodes);
    // sortedMatchedNodes.sort(Comparator.comparingInt(ReachabilityNode::getDepth));

    try (FileWriter writer = new FileWriter(filePath)) {
        // titles of columns
        writer.append("Depth,  Probability, -log(p), RealTime, path");
        // 
        for (Place place : pnf.places()) {
            writer.append(",").append(place.toString()); 
        }
        writer.append("\n");

        // 写每行数据
        for (ReachabilityNode node : targets) {
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
