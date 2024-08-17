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
    public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking, HashMap<Place, Long> target) {
        super(pf, marking, target);
        
    }

   
    /**
     * 
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
        //runExplicit();
        
    }
    
    /**
     * include query  to ask for specific source and tagret
     */
    @Override
   public void run()  {
        if(eStart != null && eTarget !=null){
            LOGGER.debug("Starting BFS with specific start and target node");
            // If start and target are equal. Stop program.
            if(eStart.equals(eTarget)){
                fireReachabilityUpdate(ReachabilityEvent.Status.EQUALNODE, 0, null);
                return;
            }
            fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
            int counter = 0;
            
            HashMap<Place, Long> collect = new HashMap<>();
            
            HashSet<ReachabilityNode> vertices = new HashSet<>();
            HashSet<ReachabilityEdge> edge = new HashSet<>();
           
            ArrayList<ReachabilityNode> verticesList = new ArrayList<>();
            ArrayList<ReachabilityNode> rNodeList = new ArrayList<>();
            
            ReachabilityNode root = new ReachabilityNode(null, null);
            /**
             * Every node is initialized as reachabilitynode
             * With prenode
             */
            //TODO: Include here computeMarking?
            for(Map.Entry<Place, Long> entry: marking.entrySet()){
                System.out.println("Erstellen: "+ entry);
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
                System.out.println("After: "+exchange.getMarking()+" b: "+exchange.getPrev().getMarking());
                //rNodeList.remove(1);
                rNodeList.remove(0);
                rNodeList.add(0, exchange);
                
               
            }
            verticesList.addAll(rNodeList);
            
            // Getting active transitions
            HashSet<Transition> activeTransitions =  pf.computeActive(marking);
            System.out.println("Transition: "+ activeTransitions);
            
            // Iterate over activated transitions
            // rNodeList contents reachabilitynodes with prenode
            ArrayList<ReachabilityNode> reachabilityNodesList = new ArrayList<>();
            ArrayList<Transition> backtrack = new ArrayList<>();
            //reachabilityNodesList.addAll(rNodeList);
            HashMap<Place, Long> newMarkingMap = new HashMap<>();
           // while(!rNodeList.isEmpty() && !isInterrupted() && reachabilityNodesList.size()<= rNodeList.size()){
            while(!rNodeList.isEmpty() && !isInterrupted()){   
                counter +=1;
                if (counter % 100 == 0) {
                    fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
                }
                 // Grab node out of rNodeList. Remove same node out of list.
                 ReachabilityNode workingNode = rNodeList.get(0);//Nimm den workingNode und gucke ihn an
                 //rNodeList.remove(rNodeList.get(0));// Angefasste nodes müssen aus der Liste entfernt werden
                 reachabilityNodesList.add(workingNode);
                 rNodeList.forEach((a)->System.out.println("rNodeList: "+a.getMarking()));
                 System.out.println("DepthOben: "+workingNode.getDepth());
                 // Examine transitions. Look for t that belongs to working node.
                 for(Transition t : activeTransitions){
                     System.out.println("Start ForLoop Transition: "+t+ " boolean: "+t.getActive());
                     if(t.getUsed()== false){
                     System.out.println("Number of transitions: "+activeTransitions.size()+" Transition: "+t);
                     // If workingNode and transition input are equal -> create edge in reachabilitygraph 
                     // between those two nodes
                     HashMap<Place, Long> vNew = eTarget;
                     // [Prenode] Compute new marking for input and output of t.
                     //HashMap<Place, Long> newMarkingInputPlace = pf.computeSingleMarking(workingNode.getMarking(), t).getFirst();
                     // [Postnode]
                     //HashMap<Place, Long> newMarkingOutputPlace = pf.computeSingleMarking(workingNode.getMarking(), t).getLast();
                     // Put these in If clauses?
                     //ReachabilityNode newReachabilityInputNode = new ReachabilityNode(newMarkingInputPlace, null);
                     //ReachabilityNode newReachabilityOutputNode = new ReachabilityNode(newMarkingOutputPlace, newReachabilityInputNode);
                     
                     System.out.println("T: "+t+" input: "+t.inputs()+" outputs: "+t.outputs()+" workingNode: "+workingNode.getMarking());
                     // If no prenode exists. Transition only updates output.
                   
                    if(t.inputs().isEmpty()){
                         //Hier: for all outputs aktualisiere places
                     if(t.outputs().get(0).equals(workingNode.getMarking().keySet().iterator().next())){
                         HashMap<Place, Long> newMarkingOutputPlace = pf.computeSingleMarking(workingNode.getMarking(), t).getLast();
                         System.out.println("New outputMarking: "+ newMarkingOutputPlace);
                         for(ReachabilityNode n : reachabilityNodesList){
                             if(n.getMarking().keySet().equals(newMarkingOutputPlace.keySet())){
                                 System.out.println("Old: "+n.getMarking());
                                 n.getMarking().put(n.getMarking().keySet().iterator().next(), newMarkingOutputPlace.values().iterator().next());
                                 System.out.println("New: "+n.getMarking());
                                 // if marking is updated. Replace in rNodeList. Clear other list
                                 for(ReachabilityNode old : rNodeList){
                                     if(n.getMarking().keySet().equals(old.getMarking().keySet())){
                                        System.out.println("rNodeOld: "+old.getMarking());
                                         //n = old;7
                                        old.getMarking().put(old.getMarking().keySet().iterator().next(), newMarkingOutputPlace.values().iterator().next());
                                        rNodeList.addFirst(old);
                                        newMarkingMap.put(old.getMarking().keySet().iterator().next(), newMarkingOutputPlace.values().iterator().next());
                                         
                                         //System.out.println("NeueActive: "+ transition);
                                        System.out.println("rNodeNew: "+old.getMarking());
                                        break; // Not sure if that's needed
                                     }
                                     
                                 }
                                 //break;
                             }
                             //break; doesnt do anything
                         }
                         

                     }
                     
                     rNodeList.forEach(v->System.out.println("END1: "+v.getMarking()));
                     rNodeList.remove(0);
                     rNodeList.forEach(v->System.out.println("ENDDanach: "+v.getMarking()));
                     }t.setUsed();
                    /**
                     * If transition has input
                     * Check if output exist. Either update in and output or just input
                     */
                    if(!t.inputs().isEmpty()){
                        if(!t.outputs().isEmpty()){
                            if(t.inputs().get(0).equals(workingNode.getMarking().keySet().iterator().next())){
                                System.out.println("VORallem: "+workingNode.getMarking()+" t: "+t+ " tInput: "+t.inputs()+" tout: "+t.outputs());
                                HashMap<Place, Long> newMarkingInputPlace = pf.computeSingleMarking(workingNode.getMarking(), t).getFirst();
                                HashMap<Place, Long> newMarkingOutputPlace = pf.computeSingleMarking(workingNode.getMarking(), t).getLast();
                                System.out.println("NeueIf newMarkingInput: "+newMarkingInputPlace+" newOutput: "+newMarkingOutputPlace);
                                // New input and output TODO update in list and compute active again
                                for(ReachabilityNode old : rNodeList){
                                    if(old.getMarking().keySet().equals(newMarkingInputPlace.keySet())){
                                        //Update input in list
                                        old.getMarking().put(old.getMarking().keySet().iterator().next(), newMarkingInputPlace.values().iterator().next());
                                        System.out.println("New Token for input: "+old.getMarking());
                                        newMarkingMap.put(old.getMarking().keySet().iterator().next(), newMarkingInputPlace.values().iterator().next());
                                    }
                                    if(old.getMarking().keySet().equals(newMarkingOutputPlace.keySet())){
                                        old.getMarking().put(old.getMarking().keySet().iterator().next(), newMarkingOutputPlace.values().iterator().next());
                                        System.out.println("New Token for output: "+old.getMarking());
                                        newMarkingMap.put(old.getMarking().keySet().iterator().next(), newMarkingOutputPlace.values().iterator().next());
                                    }
                                    
                                }
                            }//End t.inputs.get(0).equals
                        }//End if(!t.inputs().isEmpty())
                        
                        // if output is empty only update input places
                        if(t.outputs().isEmpty()){
                            HashMap<Place, Long> newMarkingInputPlace = pf.computeSingleMarking(workingNode.getMarking(), t).getFirst();
                            for(ReachabilityNode old : rNodeList){
                                    if(old.getMarking().keySet().equals(newMarkingInputPlace.keySet())){
                                        //Update input in list
                                        old.getMarking().put(old.getMarking().keySet().iterator().next(), newMarkingInputPlace.values().iterator().next());
                                        System.out.println("New Token for input: "+old.getMarking());
                                        newMarkingMap.put(old.getMarking().keySet().iterator().next(), newMarkingInputPlace.values().iterator().next());
                                    }
                        }
                        }
                    }t.setUsed();// End !t.output.isEmpty()
                    if(t.getUsed()==true){
                        backtrack.add(t);
                    }
                   
                     }
                 }
                 
                 rNodeList.remove(0);
                 HashSet<Transition> activeTransitionsUpdate =  pf.computeActiveTransitions(newMarkingMap);
                 System.out.println("Update Transition "+activeTransitions);
                 activeTransitions = activeTransitionsUpdate;
                 System.out.println("Updated Transitions: "+activeTransitions);
                 rNodeList.forEach((a)->System.out.println("#"+a.getMarking()));
                 
                 activeTransitions.forEach((a)->System.out.println("active and used: "+a+" #"+a.getUsed()+" #"+a.getActive()));
                 if(workingNode.getMarking().keySet().equals(eTarget.keySet())){
                     backtrack.reversed();
                     fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack);
                 }
            }
         
        }
        else{
            
        
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


    @Override
    public void computePriority(ReachabilityNode node) {
        // Breadth First Search does not use a priority.
    }

    
    public void runExplicit() throws InterruptedException {
        LOGGER.debug("Starting BFS with specific start and target node");
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edge = new HashSet<>();
        
        
        ArrayList<ReachabilityNode> rNodeList = new ArrayList<>();
        System.out.println("MarkingSize; "+marking.size());
        ReachabilityNode checkNode = new ReachabilityNode(null, null);
        /**
         * Every node is initialized as reachabilitynode
         * With prenode
         */
        for(Map.Entry<Place, Long> entry: marking.entrySet()){
            System.out.println("Erstellen: "+ entry);
            HashMap<Place, Long> work = new HashMap<>();
            
            if(rNodeList.isEmpty()){
                work.put(entry.getKey(), entry.getValue());
                ReachabilityNode firstNode = new ReachabilityNode(work, null);
                rNodeList.add(firstNode);
                checkNode = firstNode;
                
            }
            ReachabilityNode lastNode = rNodeList.getLast();
            work.put(entry.getKey(), entry.getValue());
            ReachabilityNode currNode = new ReachabilityNode(work, lastNode);
            rNodeList.add(currNode);
              
        }
        
        rNodeList.remove(1);// Not sure if needed
        
        for(int i = 0; i < rNodeList.size(); i++){
            try {
                System.out.println("LISTE: "+ rNodeList.get(i).getMarking()+" Prev: "+rNodeList.get(i).getPrev().getMarking());
                
            } catch (NullPointerException e) {
                System.out.println("LISTE: "+ rNodeList.get(i).getMarking()+ " Prev: none");
                
            }
        }
        // Getting active transitions
        HashSet<Transition> activeTransitions =  pf.computeActive(marking);
        System.out.println("Transition: "+ activeTransitions);
        // Iterate over activated transitions
        // rNodeList contents reachabilitynodes with prenode
        ArrayList<ReachabilityNode> reachabilityNodesList = new ArrayList<>();
        reachabilityNodesList.addAll(rNodeList);
        while(!rNodeList.isEmpty()){
             ReachabilityNode workingNode = rNodeList.get(0);//Nimm den workingNode und gucke ihn an
             rNodeList.remove(workingNode);// Angefasste nodes müssen aus der Liste entfernt werden
             System.out.println("workingNode: "+workingNode.getMarking());
             // Iterate over active transitions. If transition is active between 
             // two nodes, add edge in Reachabilitygraph
             for(Transition t : activeTransitions){
                 //System.out.println("input: "+t.inputs()+" workingNode: "+workingNode.getMarking().keySet());
                 // If workingNode and transition input are equal -> create edge in reachabilitygraph 
                 // between those two nodes
                 HashMap<Place, Long> vNew = eTarget;
                 if(t.inputs().get(0).equals(workingNode.getMarking().keySet().iterator().next())){
                     System.out.println("Input: "+t.inputs().iterator().next().toString()+" workingNodeString: "+workingNode.getMarking().toString());
                     vertices.add(workingNode);
                     //edge.add(new ReachabilityEdge(workingNode, workingNode.getPrev(), t));
                     if(t.inputs().get(0).equals(vNew.keySet())){
                         g = new ReachabilityGraph(vertices, edge);
                         fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack());
                         return;
                     }
                     boolean unvisited = true;
                     for(ReachabilityNode v : vertices){
                         System.out.println("VERTICE: "+v.getMarking());
                         if(v.equals(workingNode.getMarking().keySet().iterator().next())){
                             unvisited = false;
                             edge.add(new ReachabilityEdge(workingNode, workingNode.getPrev(), t));
                             break;
                         }
                     }
                 }
                     
             }
             
             
        }
        // When ReachabilityGraph fully exists -> BFS from startnode to targetnode
        for(ReachabilityNode v : vertices){
            
        }
    
        
    }
}
