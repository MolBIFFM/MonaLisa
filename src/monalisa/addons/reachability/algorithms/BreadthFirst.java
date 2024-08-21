/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability.algorithms;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import monalisa.addons.reachability.Pathfinder;
import monalisa.addons.reachability.ReachabilityEdge;
import monalisa.addons.reachability.ReachabilityEvent;
import monalisa.addons.reachability.ReachabilityGraph;
import monalisa.addons.reachability.ReachabilityNode;
import monalisa.addons.reachability.constraints.ConstraintFrame;
import monalisa.data.pn.PetriNet;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.security.util.Debug;

/**
 *
 * @author Marcel Gehrmann
 */
public class BreadthFirst extends AbstractReachabilityAlgorithm {

   private static final Logger LOGGER = LogManager.getLogger(BreadthFirst.class);
   private HashMap<Place, Long> eTarget;
   private HashMap<Place, Long> eStart;
   public static HashMap<Place, Long> updateFrame = new HashMap<>();
   public static ArrayList<Transition> usedTransitions = new ArrayList<>();
   public static HashMap<Place, Long> firstNode = new HashMap<>();
   public static HashMap<Place, Long> visitedNodes = new HashMap<>();
   
   public static HashMap<Place, Long> getUpdateFrame(){
       return updateFrame;
   }
   
   public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking, HashMap<Place, Long> target) {
        super(pf, marking, target);
        
    }

   
    /**
     * @author Kristin Haas
     * @param pf
     * @param marking
     * @param target
     * @param eStart
     * @param eTarget 
     */
    public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking,HashMap<Place, Long> target, HashMap<Place, Long> eStart, HashMap<Place, Long> eTarget ) throws InterruptedException{
        super(pf, marking, target);
        this.eStart = eStart;
        this.eTarget = eTarget;
        
    }
    
    /**
     * include query  to ask for specific source and tagret
     */
    @Override
   public void run()  {
       /**
        * @author Kristin Haas
        */
        if(eStart != null && eTarget !=null){
            resetAll();
            
            LOGGER.debug("Starting BFS with specific start and target node");
            // If start and target are equal. Stop program.
            PetriNet newPN = new PetriNet();
            
            /**if(eStart.equals(eTarget)){
                fireReachabilityUpdate(ReachabilityEvent.Status.EQUALNODE, 0, null);
                return;
            }*/
            fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
            int counter = 0;
            
            HashMap<Place, Long> collect = new HashMap<>();
            
            HashSet<ReachabilityNode> vertices = new HashSet<>();
            HashSet<ReachabilityEdge> edge = new HashSet<>();
           
            ArrayList<ReachabilityNode> verticesList = new ArrayList<>();
            ArrayList<ReachabilityNode> rNodeList = new ArrayList<>();
            
            ReachabilityNode root = new ReachabilityNode(null, null);
            HashMap<Place, Long> newMarkingMap = new HashMap<>();
            /**
             * Every node is initialized as reachabilitynode
             * With prenode
             */
            //TODO: Include here computeMarking?
            for(Map.Entry<Place, Long> entry: marking.entrySet()){
                newMarkingMap.put(entry.getKey(), entry.getValue());
                HashMap<Place, Long> work = new HashMap<>();

                if(rNodeList.isEmpty()){
                    work.put(entry.getKey(), entry.getValue());
                    ReachabilityNode firstNode = new ReachabilityNode(work, null);
                    rNodeList.add(firstNode);
                    root = firstNode;
                    

                }
                ReachabilityNode lastNode = rNodeList.getLast();
                work.put(entry.getKey(), entry.getValue());
                ReachabilityNode currNode = new ReachabilityNode(work, lastNode);
                rNodeList.add(currNode);
                

            }
            // If first node has a prev-node exchange
            // Here pre node is last node -> has to be more complex and not hard coded
            
            if(rNodeList.get(0).getMarking().keySet().equals(rNodeList.get(1).getMarking().keySet())){
                rNodeList.remove(0);
                ReachabilityNode exchange = new ReachabilityNode(rNodeList.getFirst().getMarking(), rNodeList.getLast());
                //rNodeList.remove(1);
                rNodeList.remove(0);
                rNodeList.add(0, exchange);
                
               
            }
            verticesList.addAll(rNodeList);
            // Getting active transitions
            HashSet<Transition> activeTransitions =  pf.computeActiveTransitions(marking);
            
            
            // Iterate over activated transitions
            // rNodeList contents reachabilitynodes with prenode
            ArrayList<ReachabilityNode> reachabilityNodesList = new ArrayList<>();
            ArrayList<Transition> backtrack = new ArrayList<>();
            //reachabilityNodesList.addAll(rNodeList);
            
            
            
            HashMap<Place, Long> resetMap = new HashMap<>();
            firstNode = eStart;
            

            while(!rNodeList.isEmpty() && !isInterrupted()){   
                counter +=1;
                updateFrame.putAll(newMarkingMap);
                
                if (counter % 100 == 0) {
                    fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
                }
                 // Grab node out of rNodeList. Remove same node out of list.
                ReachabilityNode workingNode = rNodeList.get(0);//Nimm den workingNode und gucke ihn an
                 //rNodeList.remove(rNodeList.get(0));// Angefasste nodes müssen aus der Liste entfernt werden
                reachabilityNodesList.add(workingNode);
                System.out.println("StartWork: "+workingNode.getMarking()+" visit: "+workingNode.getVisited());
                 // Examine transitions. Look for t that belongs to working node.
                System.out.println("");
                System.out.println("");
                System.out.println("");
                for(Transition t : activeTransitions){
                    System.out.println("Start ForLoop Transition: "+t+ " boolean: "+t.getActive()+" used: "+t.getUsed());
                    if(t.getUsed()== false){
                     // If workingNode and transition input are equal -> create edge in reachabilitygraph 
                     // between those two nodes
                     
                     // If no prenode exists. Transition only updates output.
                    if(t.inputs().isEmpty() && !t.outputs().isEmpty()){
                         //Hier: for all outputs aktualisiere places
                        System.out.println("----------Only Output exist--------------");
                        for(Place p : t.outputs()){
                            System.out.println("P: "+p+" "+p.inputs());
                            HashMap<Place, Long> updateNode = new HashMap<>();
                            // Iterate through latest HashMap.Take 
                            for(Map.Entry<Place, Long> entry : newMarkingMap.entrySet()){
                                if(p.equals(entry.getKey())){
                                    updateNode.put(entry.getKey(), entry.getValue());
                                    HashMap<Place, Long> newMarkingOutputPlace = pf.computeSingleMarking(updateNode, t).getLast(); //workingnode.getMarking().getLast
                                    newMarkingMap.putAll(newMarkingOutputPlace);
                                    counter +=1;
                                    updateFrame.putAll(newMarkingOutputPlace);
                                    t.setUsed();
                                    System.out.println("START: "+eStart+" Target: "+eTarget+" bool "+eStart.equals(eTarget));
                                    
                                    // Can be deleted?
                                    if(eStart.equals(eTarget)&& workingNode.getSecondVisit()==true){
                                        workingNode.setUnvisited();
                                        rNodeList.addLast(workingNode);
                                        System.out.println("SecondVisit: "+workingNode.getSecondVisit()+" Node: "+workingNode.getMarking());
                                        updateFrame.putAll(newMarkingMap);// Token are right
                                        System.out.println("TABLE: "+updateFrame);
                                        t.setUsed();
                                        usedTransitions = backtrack;
                                        fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrackList(backtrack));
                                        return;
                                     }
                                   /** if(eStart.equals(eTarget)&& workingNode.getSecondVisit()==false){
                                         workingNode.setSecondVisit();
                                         rNodeList.addLast(workingNode);
                                         
                                     }*/
                                    if(!eStart.equals(eTarget)){
                                        if(updateNode.equals(eTarget)){
                                            //t.setUsed();
                                          
                                            rNodeList.forEach((a)->System.out.println("rNodeListLAST: "+a.getMarking()));
                                            resetMap.entrySet().forEach(a -> System.out.println("## "+a.getKey()+" value: "+a.getValue()));
                                            updateFrame.putAll(newMarkingMap);// Token are right
                                            System.out.println("TABLE: "+updateFrame);
                                            t.setUsed();
                                            usedTransitions = backtrack;
                                            fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrackList(backtrack));
                                            return;
                                    }               }
                                    // Update reachabilitynode value
                                    for(ReachabilityNode n : reachabilityNodesList){
                                        if(n.getMarking().keySet().equals(newMarkingOutputPlace.keySet())){
                                            n.getMarking().put(n.getMarking().keySet().iterator().next(), newMarkingOutputPlace.values().iterator().next());
                                            
                                        }
                                    }
                                }
                             }
                     }
                    t.setUsed();
                    if(t.getUsed()==true && t.getActive()==true){
                            backtrack.add(t);
                            System.out.println("BACKTRACK: "+t);
                            edge.add(new ReachabilityEdge(workingNode.getPrev(), workingNode, t));
                        }
                    
                    System.out.println("--------------------------------");
                    System.out.println("--- NewMarking: "+newMarkingMap+" ---");
                    System.out.println("--------------------------------");
                    System.out.println("--- WorkingNode: "+workingNode.getMarking()+" First visit: "+workingNode.getVisited()+" second visit: "+workingNode.getSecondVisit()+"------");
                    System.out.println("--------------------------------");
                    System.out.println("---- PreNode: "+workingNode.getPrev().getMarking()+" visited once: "+workingNode.getPrev().getVisited()+" visited twice: "+workingNode.getPrev().getSecondVisit());
                    System.out.println("---------- "+t+" is used: "+t.getUsed()+" ------------");
                    System.out.println("----------Only Output End--------------");
                    System.out.println("");
                    System.out.println("");
                    System.out.println("");
                }
                
                                                
                    /**
                     * If transition has input
                     * Check if output exist. Either update in and output or just input
                     */
                    if(!t.inputs().isEmpty() && !t.outputs().isEmpty()){ // if transition has input and out
                        // iterate through both and update marking
                        System.out.println("----------In/Output exist--------------");
                        for(Place pIN : t.inputs()){
                            for(Place pOUT : t.outputs()){
                                HashMap<Place, Long> updateNodeIN = new HashMap<>();
                                HashMap<Place, Long> updateNodeOUT = new HashMap<>();
                                for(Map.Entry<Place, Long> entry : newMarkingMap.entrySet()){
                                    if(pIN.equals(entry.getKey())){
                                        System.out.println("Input before: "+entry.getKey()+" Value: "+entry.getValue());
                                        updateNodeIN.put(entry.getKey(), entry.getValue());
                                        HashMap<Place, Long> newMarkingInputPlace = pf.computeSingleMarking(updateNodeIN, t).getFirst();
                                        newMarkingMap.putAll(newMarkingInputPlace);
                                        updateFrame.putAll(newMarkingInputPlace);
                                        visitedNodes.putAll(newMarkingInputPlace);
                                        System.out.println("Input: "+newMarkingInputPlace);
                                    }
                                    if(pOUT.equals(entry.getKey())){
                                        System.out.println("Output before: "+entry.getKey()+" Value: "+entry.getValue());
                                        updateNodeOUT.put(entry.getKey(), entry.getValue());
                                        HashMap<Place, Long> newMarkingOutputPlace = pf.computeSingleMarking(updateNodeOUT, t).getLast();
                                        newMarkingMap.putAll(newMarkingOutputPlace);
                                        updateFrame.putAll(newMarkingOutputPlace);
                                        ReachabilityNode rNode = new ReachabilityNode(updateNodeOUT, workingNode);
                                        System.out.println("Output: "+newMarkingOutputPlace);
                                        System.out.println("START: "+eStart+" Target: "+eTarget+" bool: "+eStart.equals(eTarget));
                                       
                                        
                                        
                                        if(!eStart.equals(eTarget)){
                                            System.out.println("HIER: "+!eStart.equals(eTarget));
                                            if(updateNodeOUT.equals(eTarget)){
                                                t.setUsed();
                                                if(t.getUsed()==true && t.getActive()==true){
                                                    backtrack.add(t);
                                                    System.out.println("BACKTRACK: "+t);
                                                    edge.add(new ReachabilityEdge(workingNode.getPrev(), workingNode, t));
                                                }
                                                //HIER gucken
                                                rNodeList.forEach((a)->System.out.println("rNodeListLAST: "+a.getMarking()));
                                                resetMap.entrySet().forEach(a -> System.out.println("## "+a.getKey()+" value: "+a.getValue()));
                                                updateFrame.putAll(newMarkingMap);// Token are right
                                                System.out.println("TABLE: "+updateFrame);
                                                
                                                usedTransitions = backtrack;
                                                fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrackList(backtrack));
                                                return;
                                            }
                                        
                                      }
                                    }
                                    
                                }
                                if(eStart.equals(eTarget)&& workingNode.getPrev().getSecondVisit()==true && updateNodeOUT.equals(eTarget)){
                            rNodeList.addLast(workingNode);
                            updateFrame.putAll(newMarkingMap);// Token are right
                            
                            System.out.println("TABLE: "+updateFrame);
                            t.setUsed();
                            backtrack.add(t);
                            usedTransitions = backtrack;
                            fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrackList(backtrack));
                            return;
                         }
                            }
                            
                        }
                        if(eStart.equals(eTarget)&& workingNode.getSecondVisit()==false){
                            workingNode.setSecondVisit();
                            rNodeList.addLast(workingNode);
                        }
                        
                        workingNode.setVisited();
                        t.setUsed();
                        if(t.getUsed()==true && t.getActive()==true){
                                backtrack.add(t);
                                System.out.println("BACKTRACK: "+t);
                                edge.add(new ReachabilityEdge(workingNode.getPrev(), workingNode, t));
                            }
                        System.out.println("--------------------------------");
                        System.out.println("--- NewMarking: "+newMarkingMap+" ---");
                        System.out.println("--------------------------------");
                        System.out.println("--- WorkingNode: "+workingNode.getMarking()+" First visit: "+workingNode.getVisited()+" second visit: "+workingNode.getSecondVisit()+"------");
                        System.out.println("--------------------------------");
                        System.out.println("---- PreNode: "+workingNode.getPrev().getMarking()+" visited once: "+workingNode.getPrev().getVisited()+" visited twice: "+workingNode.getPrev().getSecondVisit());
                        System.out.println("--------------------------------");
                        System.out.println("---------- "+t+" is used: "+t.getUsed()+" ------------");
                        System.out.println("----------In/Output End--------------");
                        System.out.println("");
                        System.out.println("");
                        System.out.println("");
                    }
                    
                    
                    
                    
                    if(t.outputs().isEmpty() && !t.inputs().isEmpty()){
                        System.out.println("----------Only INput exists--------------");
                        for(Place p : t.inputs()){
                            HashMap<Place, Long> updateNode = new HashMap<>();
                            for(Map.Entry<Place, Long> entry : newMarkingMap.entrySet()){
                                if(p.equals(entry.getKey())){
                                    updateNode.put(entry.getKey(), entry.getValue());
                                    HashMap<Place, Long> newMarkingInputPlace = pf.computeSingleMarking(updateNode, t).getFirst();
                                    newMarkingMap.putAll(newMarkingInputPlace);
                                    updateFrame.putAll(newMarkingInputPlace);
                                }
                            }
                        }
                        t.setUsed();
                        System.out.println("--------------------------------");
                        System.out.println("--- NewMarking: "+newMarkingMap+" ---");
                        System.out.println("--------------------------------");
                        System.out.println("---------- "+t+" is used: "+t.getUsed()+" ------------");
                        System.out.println("----------Input End--------------");
                        System.out.println("");
                        System.out.println("");
                        System.out.println("");
                    }
                    
                    }
                    
                
                         
                    
                    if(t.getUsed()==true && workingNode.getVisited()==true){
                        if(!eStart.equals(eTarget)){
                            if(t.getUsed()==true && t.getActive()==true){
                        backtrack.add(t);
                        System.out.println("BACKTRACK: "+t);
                        edge.add(new ReachabilityEdge(workingNode.getPrev(), workingNode, t));
                    }
                        updateFrame.putAll(newMarkingMap);// Token are right
                        usedTransitions = backtrack;
                        System.out.println("UNTEN: "+usedTransitions);
                        fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, backtrackList(backtrack));
                        return;
                        }
}
                  
                   
                }  
                 visitedNodes.putAll(workingNode.getMarking());
                 rNodeList.get(0).setVisited();
                 vertices.add(workingNode);
                 workingNode.setVisited();
                 rNodeList.remove(0);
                 /**System.out.println("START: "+eStart+" Target: "+eTarget);
                 if(eStart.toString()== eTarget.toString()){
                     workingNode.setUnvisited();
                     rNodeList.addLast(workingNode);
                 }*/
                 
                 HashSet<Transition> activeTransitionsUpdate =  pf.computeActiveTransitions(newMarkingMap);
                 System.out.println("Update Transition "+activeTransitions);
                 activeTransitions = activeTransitionsUpdate;
                 System.out.println("Updated Transitions: "+activeTransitions);
                 rNodeList.forEach((a)->System.out.println("#"+a.getMarking()));
                 System.out.println("REAL: "+reachabilityNodesList);
                 activeTransitions.forEach((a)->System.out.println("active and used: "+a+" #"+a.getUsed()+" #"+a.getActive()));
                 System.out.println("Visited: "+workingNode.getVisited()+" Node: "+workingNode.getMarking());
                 System.out.println("WORK: "+workingNode.getMarking()+" visit: "+workingNode.getVisited());
                /** if(workingNode.getMarking().keySet().equals(eTarget.keySet()) ){
                     System.out.println("ENDE "+workingNode.getMarking());
                     if(eStart.equals(eTarget)){
                         
                             rNodeList.forEach((a)->System.out.println("rNodeListLAST: "+a.getMarking()));
                             resetMap.entrySet().forEach(a -> System.out.println("## "+a.getKey()+" value: "+a.getValue()));
                             updateFrame.putAll(resetMap);// Token are right
                             System.out.println("TABLE: "+updateFrame);
                             usedTransitions = backtrack;
                             fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrackList(backtrack));
                             return;
                         
                     }if(!eStart.equals(eTarget)){
                     rNodeList.forEach((a)->System.out.println("rNodeListLAST: "+a.getMarking()));
                     resetMap.entrySet().forEach(a -> System.out.println("## "+a.getKey()+" value: "+a.getValue()));
                     updateFrame.putAll(newMarkingMap);// Token are right
                     System.out.println("TABLE: "+updateFrame);
                     usedTransitions = backtrack;
                     fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrackList(backtrack));
                     return;
                 }}*/
                 if(workingNode.getMarking().keySet().equals(eTarget.keySet())&& workingNode.getVisited()==false){
                     fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, backtrackList(backtrack));
                     return;
                 }
                 
                
            }
         
            
        }

        
        else{
            
        /**
         * @author Marcel Gehrmann
         */
        LOGGER.debug("Starting Breadth First Algorithm.");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        // initialize for m0 as root
        ReachabilityNode root = new ReachabilityNode(marking, null);
        tar = new ReachabilityNode(target, null);
        vertices.add(root);
        ArrayList<ReachabilityNode> workingList = new ArrayList<>();
        workingList.add(root);
        while (!workingList.isEmpty() && !isInterrupted()) {
            LOGGER.debug("Starting expansion for a new node."); // debug
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
            ReachabilityNode workingNode = workingList.get(0);
            LOGGER.debug("Current marking:" + workingNode.getMarking().toString());
            workingList.remove(0);
            HashSet<Transition> activeTransitions = pf.computeActive(workingNode.getMarking());
            for (Transition t : activeTransitions) {
                LOGGER.debug("Created new node by firing transition " + t.getProperty("name") + ".");  // debug
                HashMap<Place, Long> mNew = pf.computeMarking(workingNode.getMarking(), t);
                ReachabilityNode newNode = new ReachabilityNode(mNew, workingNode);
                // Algorithm terminates on finding m*
                if (newNode.equals(tar)) {
                    tar = newNode;
                    vertices.add(tar);
                    edges.add(new ReachabilityEdge(workingNode, tar, t));
                    g = new ReachabilityGraph(vertices, edges);
                    fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack());
                    LOGGER.debug("Target marking has been reached.");
                    
                    return;
                }
                boolean unvisited = true;
                for (ReachabilityNode v : vertices) {
                    if (v.equals(newNode)) {
                        unvisited = false;
                        edges.add(new ReachabilityEdge(workingNode, v, t));
                        break;
                    }
                }
                if (unvisited) {
                    vertices.add(newNode);
                    workingList.add(newNode);
                    edges.add(new ReachabilityEdge(workingNode, newNode, t));
                }
            }
        }
        if (isInterrupted()) {
            LOGGER.warn("Execution has been aborted.");
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, counter, null);
        } else {
            LOGGER.info("Target marking could not be reached from start marking.");
            g = new ReachabilityGraph(vertices, edges);
            fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, null);
        }
        }
    }
   
   public void resetAll(){
       
       for(Transition t : pf.getBackup().transitions()){
           t.resetTransitions();
       }
   }


    @Override
    public void computePriority(ReachabilityNode node) {
        // Breadth First Search does not use a priority.
    }

    
   
}
