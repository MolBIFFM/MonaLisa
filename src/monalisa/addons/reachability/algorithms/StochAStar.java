package monalisa.addons.reachability.algorithms;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        // System.out.println("\nStarting Stochastic A* Algorithm.");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        long startTime = System.nanoTime();
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        ArrayList <ReachabilityNode> targets = new ArrayList<>();
        ArrayList <ReachabilityNode> expanded = new ArrayList<>();
        ArrayList <ReachabilityNode> addedtoQ = new ArrayList<>();
        int foundPaths = 0;
        double minGInOpen = Double.POSITIVE_INFINITY;
        // int xeno_counter = 0;
        // initialize for m0 as root
        ReachabilityNode root = new ReachabilityNode(marking, null);
        root.setProbability(1);
        root.setTime(0);
        root.setRealTime(0);
        root.setCost(0);
        // root.setXeno_counter(0);
        tar = new ReachabilityNode(target, null);
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        workingList.add(root);
        // targets.add(root);

        String filePath = "C:\\Users\\61634\\Desktop\\Salmonella_output\\astar.csv";
        String filePath_expanded = "C:\\Users\\61634\\Desktop\\Salmonella_output\\astar_expanded.csv";
        String filePath_addedtoQ = "C:\\Users\\61634\\Desktop\\Salmonella_output\\astar_addedtoQ.csv";
        
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
            
            // changed at 28.04 
            if (Math.abs(workingNode.getRealTime() - minGInOpen) < 1e-9) {
                double min = Double.POSITIVE_INFINITY;
                for (ReachabilityNode n : workingList) {
                    min = Math.min(min, n.getRealTime());
                }
                minGInOpen = min;
            }

             if (maxPaths != -1  && targets.size() == maxPaths 
                    && minGInOpen>=targets.get(maxPaths-1).getRealTime()){
                        long endTime = System.nanoTime();
                        long duration = endTime - startTime;
                        System.out.println("Astar Number of paths has been reached: " + (targets.size()));
                        // g = new ReachabilityGraph(vertices, edges);
                        exportTargetsToCSV(targets, filePath);//, expanded, addedtoQ, 
                        
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                            writer.write("Type,Time(ms),founded Paths");//new added nodes //newvisited nodes
                            // Visited,Expanded,Stored,
                            writer.newLine();
                            writer.write("Astar" + "," + (duration / 1_000_000.0)+ ","+ foundPaths);
                            //  counter + "," + counter_expanded + "," + matchedNodes.size() + "," +
                            writer.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        exportTargetsToCSV(expanded, filePath_expanded);
                        exportTargetsToCSV(addedtoQ, filePath_addedtoQ);
                        fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, null);
                        return; 
                    }

            
            // changed at 28.04
            if (targets.size() == maxPaths) {
                    double worstTargetCost = targets.get(maxPaths - 1).getRealTime();
                    if (workingNode.getRealTime() > worstTargetCost ) {
                        continue;
                    }
            }


            expanded.add(workingNode);
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
            // System.out.println("-------------");
            for (Transition t : activeTransitions) {
                // LOGGER.debug("Created new node by firing transition " + t.getProperty("name") + ".");  // debug                                    
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                double prob_t = rates.get(t) / ratesSum;
                double prob_node = workingNode.getProbability() * prob_t;
                newNode.setProbability(prob_node);
                // double reactionTime = 1 / (rates.get(t)* prob_t);
                double reactionTime = 1 / rates.get(t);
                // for (Place p: t.inputs()){
                //     if (pnf.getArc(p, t).weight() > 1){
                //         reactionTime = reactionTime / pnf.getArc(p, t).weight();
                //     }
                // }
                newNode.setTime(workingNode.getTime() + reactionTime);
                double reactionRealTime = 1 / ratesSum ;
                newNode.setRealTime(workingNode.getRealTime() + reactionRealTime);
                // System.out.println("reaction rate of "+t.toString()+":" + rates.get(t));
                // System.out.println("reaction rate sum:" + ratesSum);
                
                double cost_t = -Math.log(prob_t);
                double newCost = workingNode.getCost() + cost_t;
                newNode.setCost(newCost);
                // if(t.toString().equals("xeno_deg")){
                //     xeno_counter = workingNode.getXeno_counter() + 1;
                //     newNode.setXeno_counter(xeno_counter);
                // }else{
                //     newNode.setXeno_counter(workingNode.getXeno_counter());
                // }

                // changed at 28.04 
                // if (targets.size() == maxPaths) {
                //     double worstTargetCost = targets.get(maxPaths - 1).getRealTime();
                //     if (newNode.getRealTime() > worstTargetCost) {
                //         continue;
                //     }
                // }

                if (newNode.equals(tar)) {
                    // System.out.println("Probability of this path to target: " + newNode.getProbability());
                    // targets.add(newNode);
                    insertAndMaintainTopK(targets, newNode, maxPaths);// changed at 28.04 
                    foundPaths += 1;
                    // tar = newNode;
                    vertices.add(tar);
                    edges.add(new ReachabilityEdge(workingNode, tar, t, prob_t));
                    g = new ReachabilityGraph(vertices, edges);
                    // if (maxPaths != -1 && foundPaths >= maxPaths){
                    // changed at 28.04 
                    // if (maxPaths != -1  && targets.size() == maxPaths 
                    // && minGInOpen>=targets.get(maxPaths-1).getRealTime()){
                    //     long endTime = System.nanoTime();
                    //     long duration = endTime - startTime;
                    //     System.out.println("Astar Number of paths has been reached: " + (targets.size()));
                    //     // g = new ReachabilityGraph(vertices, edges);
                    //     exportTargetsToCSV(targets, filePath);//, expanded, addedtoQ, 
                        
                    //     try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                    //         writer.write("Type,Time(ms),founded Paths");//new added nodes //newvisited nodes
                    //         // Visited,Expanded,Stored,
                    //         writer.newLine();
                    //         writer.write("Astar" + "," + (duration / 1_000_000.0)+ ","+ foundPaths);
                    //         //  counter + "," + counter_expanded + "," + matchedNodes.size() + "," +
                    //         writer.newLine();
                    //     } catch (IOException e) {
                    //         e.printStackTrace();
                    //     }
                    //     exportTargetsToCSV(expanded, filePath_expanded);
                    //     exportTargetsToCSV(addedtoQ, filePath_addedtoQ);
                    //     fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, null);
                    //     return; 
                    // }

                    
                                       
                    // LOGGER.debug("Target marking has been reached.");
                    // return;
                    // continue;
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
                    minGInOpen = Math.min(minGInOpen, newNode.getRealTime());// changed at 28.04 
                    addedtoQ.add(newNode);
                    // System.out.println("Marking via Transition: "+t.toString()+"; getPriority: "+newNode.getPriority());
                    vertices.add(newNode);
                    edges.add(new ReachabilityEdge(workingNode, newNode, t, prob_t));
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
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            System.out.println("Astar Search ends or aborted, founded paths: " + (targets.size()));     
            g = new ReachabilityGraph(vertices, edges);     
            exportTargetsToCSV(targets, filePath);//targets,addedtoQ
           
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write("Type,Time(ms),founded Paths");//new added nodes //newvisited nodes
                // Visited,Expanded,Stored,
                writer.newLine();
                writer.write("Astar" + "," + (duration / 1_000_000.0) + "," + foundPaths);
                //  counter + "," + counter_expanded + "," + matchedNodes.size() + "," +
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            } 
            exportTargetsToCSV(addedtoQ, filePath_addedtoQ);
            exportTargetsToCSV(expanded, filePath_expanded);
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

    // changed at 28.04 
    public void insertAndMaintainTopK(ArrayList<ReachabilityNode> targets,
                                    ReachabilityNode candidate,
                                    int K) {

        double cost = candidate.getRealTime();

        // 👉 1. 如果还没满，直接插入到正确位置
        if (targets.size() < K) {
            int pos = findInsertPosition(targets, cost);
            targets.add(pos, candidate);
            return;
        }

        // 👉 2. 如果已经满了，先判断是否有资格进入
        double worstCost = targets.get(targets.size() - 1).getRealTime();

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
        // double prio = node.getDepth();
        // double prio = node.getTime();
        double prio = node.getRealTime();
        // double prio = node.getCost();
        HashMap<Place, Long> diff = tar.getDifference(node);
        HashSet<Double> placewise = new HashSet<>();
        HashSet<Transition> activeTransitions = pf.computeActive(node.getMarking());
        // HashMap<Transition, Double> rates = new HashMap<>();
        double ratesSum = 0;
        for (Transition t : activeTransitions) {
            // compute reaction rate
            double rate = 0;
            rate = pf.computeReactionRate(t, node.getMarking(), firingRates);
            // rates.put(t, rate);
            ratesSum += rate;
        }

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
                        // double rate = rates.get(t);
                        // System.out.println("transition1: "+t.toString()+"; Rate: "+rate);
                        if (rate != 0){
                            double probPenalty = Math.log(ratesSum / rate);
                            //probPenalty * 
                            intermediate.add(diff.get(p) / (-1* rate * pnf.getArc(p, t).weight()));
                            // intermediate.add(diff.get(p) / (-1* rate ));
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
                            double probPenalty = Math.log(ratesSum / rate);
                            //probPenalty *
                            intermediate.add( diff.get(p) / (1* rate * pnf.getArc(t, p).weight()));
                            // intermediate.add( diff.get(p) / (1* rate ));
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
            // else if (diff.get(p) == 0) {
            //     placewise.add(0.0);
            // }
        }
        //  System.out.println("-------------------------");
        // LOGGER.debug("Depth: " + Double.toString(prio) + " Heur: " + Double.toString(Collections.max(placewise)));
        if(!placewise.isEmpty()){
            double alpha = 1;
            prio += alpha * Collections.max(placewise); // Add heuristic
            // prio += placewise.stream().mapToDouble(Double::doubleValue).sum(); // Add heuristic
        }else if(node.equals(tar)){
            prio += 0;
        }
        else{
            prio = Double.POSITIVE_INFINITY;
            // System.out.println("no valid transitions for heuristic calculation");
        }
        node.setPriority(prio);
    }

    
    // public void computePriority(ReachabilityNode node) {
    //     // double prio = node.getDepth();
    //     double prio = node.getTime();
    //     // double prio = node.getRealTime();
    //     // double prio = node.getCost();
    //     HashMap<Place, Long> diff = tar.getDifference(node);
    //     HashSet<Double> placewise = new HashSet<>();
    //     HashSet<Transition> activeTransitions = pf.computeActive(node.getMarking());
    //     // HashMap<Transition, Double> rates = new HashMap<>();
    //     double ratesSum = 0;
    //     for (Transition t : activeTransitions) {
    //         // compute reaction rate
    //         double rate = 0;
    //         rate = pf.computeReactionRate(t, node.getMarking(), firingRates);
    //         // rates.put(t, rate);
    //         ratesSum += rate;
    //     }

    //     for (Place p : tar.getMarking().keySet()) {
    //         HashSet<Double> intermediate = new HashSet<>();
    //         ArrayList<Transition> validTransitions = new ArrayList<>();
    //         // The place still has too many tokens compared to the target marking
    //         if (diff.get(p) < 0) {
    //             if (!p.outputs().isEmpty()){
    //                 validTransitions.addAll(p.outputs());
    //                 for (Transition t : validTransitions) {
    //                     // intermediate.add(Math.floor(diff.get(p) / (-1 * pnf.getArc(p, t).weight())));
    //                     double rate = pf.computeReactionRate(t, node.getMarking(), firingRates);
    //                     // double rate = rates.get(t);
    //                     // System.out.println("transition1: "+t.toString()+"; Rate: "+rate);
    //                     if (rate != 0){
    //                         double probPenalty = Math.log(ratesSum / rate);
    //                         //probPenalty * 
    //                         intermediate.add(diff.get(p) / (-1* rate * pnf.getArc(p, t).weight()));
    //                         // intermediate.add(diff.get(p) / (-1* rate ));
    //                     }
    //                 }
    //                 // System.out.println("ValidTransitions_1: " + validTransitions);
    //                 // System.out.println("Intermediate: " + intermediate);
    //                 if(!intermediate.isEmpty()){
    //                     placewise.add(Collections.min(intermediate));  
    //                 }
    //                 // else{
    //                 //     placewise.add(Double.POSITIVE_INFINITY);
    //                 // }
    //             }else{
    //             placewise.add(Double.POSITIVE_INFINITY);
    //             }
                              
    //         } // The place still has too few tokens compared to the target marking
    //         else if (diff.get(p) > 0) {
    //             if (!p.inputs().isEmpty()){
    //                 validTransitions.addAll(p.inputs());
    //                 for (Transition t : validTransitions) {
    //                     // intermediate.add(Math.floor(diff.get(p) / pnf.getArc(t, p).weight()));
    //                     double rate = pf.computeReactionRate(t, node.getMarking(), firingRates);
    //                     // System.out.println("transition2: "+t.toString()+"; Rate: "+rate);
    //                     if (rate != 0){
    //                         double probPenalty = Math.log(ratesSum / rate);
    //                         //probPenalty *
    //                         intermediate.add( diff.get(p) / (1* rate * pnf.getArc(t, p).weight()));
    //                         // intermediate.add( diff.get(p) / (1* rate ));
    //                     }
    //                 }
    //                 // System.out.println("ValidTransitions_2: " + validTransitions);
    //                 // System.out.println("Intermediate: " + intermediate);
    //                 if(!intermediate.isEmpty()){
    //                 placewise.add(Collections.min(intermediate));  
    //                 }
    //                 // else{
    //                 // placewise.add(Double.POSITIVE_INFINITY);
    //                 // }   
    //             }else{
    //                 placewise.add(Double.POSITIVE_INFINITY);
    //             }
                             
    //         }
    //         // else if (diff.get(p) == 0) {
    //         //     placewise.add(0.0);
    //         // }
    //     }
    //     //  System.out.println("-------------------------");
    //     // LOGGER.debug("Depth: " + Double.toString(prio) + " Heur: " + Double.toString(Collections.max(placewise)));
    //     if(!placewise.isEmpty()){
    //         double alpha = 1;
    //         prio += alpha * Collections.max(placewise); // Add heuristic
    //         // prio += placewise.stream().mapToDouble(Double::doubleValue).sum(); // Add heuristic
    //     }else if(node.equals(tar)){
    //         prio += 0;
    //     }
    //     else{
    //         prio = Double.POSITIVE_INFINITY;
    //         // System.out.println("no valid transitions for heuristic calculation");
    //     }
    //     node.setPriority(prio);
    // }

    private void exportTargetsToCSV(ArrayList<ReachabilityNode> targets, String filePath) {
    // List<ReachabilityNode> sortedMatchedNodes = new ArrayList<>(matchedNodes);
    // sortedMatchedNodes.sort(Comparator.comparingInt(ReachabilityNode::getDepth));

        try (FileWriter writer = new FileWriter(filePath)) {
            // titles of columns
            writer.append("Depth, Probability, Time, RealTime, H_score, priority, path");
            //RealTime, pathtime, pathRealTime,-log(p)

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

                // ArrayList<String> pathtime = backtrack(node);
                // String pathtimeStr = String.join(",", pathtime);

                // ArrayList<String> pathrealtime = backtrackRealtime(node);
                // String pathrealtimeStr = String.join(",", pathrealtime);

                writer
                    .append(String.valueOf(node.getDepth()))
                    .append(",")
                    .append(String.valueOf(node.getProbability()))
                    .append(",")
                    .append(String.valueOf(node.getTime()))
                    .append(",")
                    .append(String.valueOf(node.getRealTime()))
                    .append(",")
                    // .append(String.valueOf(node.getPriority() - node.getTime())) // heuristic score
                    // .append(",")
                    .append(String.valueOf(node.getPriority() - node.getRealTime())) // heuristic score
                    .append(",")
                    .append(String.valueOf(node.getPriority()))
                    .append(",")
                    .append(String.valueOf("\"" + pathStr + "\""))
                    ;
                    
                    // .append(round1(node.getRealTime()))
                    // .append(",")
                    //.append(",")
                    // .append(String.valueOf("\"" + pathtimeStr + "\""))
                    // .append(",")
                    // .append(String.valueOf("\"" + pathrealtimeStr + "\""))


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

    private static String round1(double val) {
    return String.valueOf(Math.round(val * 10.0) / 10.0);
    }
}
