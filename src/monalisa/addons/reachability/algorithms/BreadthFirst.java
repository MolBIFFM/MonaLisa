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
                fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, 0, null);
                return;
            }
            
            fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
            int counter = 0;
            HashSet<ReachabilityNode> vertices = new HashSet<>();
            ArrayList<ReachabilityNode> verticesList = new ArrayList<>();
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
            ArrayList<Transition> backtrack = new ArrayList<>();
            reachabilityNodesList.addAll(rNodeList);
            while(!rNodeList.isEmpty()){
                 ReachabilityNode workingNode = rNodeList.get(0);//Nimm den workingNode und gucke ihn an
                 
                 rNodeList.remove(rNodeList.get(0));// Angefasste nodes müssen aus der Liste entfernt werden
                 
                 System.out.println("workingNode: "+workingNode.getMarking()+" NodeListSize: "+rNodeList.size());
                 // Iterate over active transitions. If transition is active between 
                 // two nodes, add edge in Reachabilitygraph
                 for(Transition t : activeTransitions){
                     
                     
                     System.out.println("TRANSITION: "+t+" "+" input: "+t.inputs()+" output: "+t.outputs());
                     //System.out.println("input: "+t.inputs()+" workingNode: "+workingNode.getMarking().keySet());
                     // If workingNode and transition input are equal -> create edge in reachabilitygraph 
                     // between those two nodes
                     HashMap<Place, Long> vNew = eTarget;
                     if(t.inputs().get(0).equals(workingNode.getMarking().keySet().iterator().next())){
                         System.out.println("Input: "+t.inputs().iterator().next().toString()+" workingNodeString: "+workingNode.getMarking().toString());
                         vertices.add(workingNode);
                         System.out.println("Track: "+vertices.iterator().next().getMarking());
                         if(!backtrack.contains(t)){
                            counter +=1;
                            backtrack.add(t);
                        }
                         
                         //edge.add(new ReachabilityEdge(workingNode, workingNode.getPrev(), t));
                         // Somewhere here seems to be a problem with the quere
                         // Algo terminates, but success doesn't show...but it's there
                         System.out.println("IF: "+t.inputs().get(0)+" "+vNew.keySet()+" "+t.inputs().get(0).equals(vNew.keySet().iterator().next()));
                         if(t.inputs().get(0).equals(vNew.keySet().iterator().next())){
                             //Last node create edge
                             edge.add(new ReachabilityEdge(workingNode, workingNode.getPrev(), t));
                             vertices.add(workingNode);
                             System.out.println("LAST EDGE: "+edge.toString()+" VERTICES: "+vertices.toString());
                             g = new ReachabilityGraph(vertices, edge);
                             // Need to put the success somewhere else
                             // if graph exists start backtrack to check if path exists
                             //fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack);
                             //return;
                         }
                         // t.input is prenode
                         //Collections.reverse(verticesList);
                         for(ReachabilityNode v : verticesList){
                             
                             System.out.println("v.getMarking: "+v.getMarking()+" v.getPrev.getMarking: "+v.getPrev().getMarking());
                             System.out.println("T: "+t.toString()+" t.Input: "+t.inputs()+" t.outputs: "+t.outputs());
                            // If node has prenode and transition is active, add edge and mark as visited
                              System.out.println("If input=v.prev: "+t.inputs().equals(v.getPrev().getMarking().keySet()));
                            if(v.getPrev().getMarking().keySet().contains(t.inputs().get(0))){
                                edge.add(new ReachabilityEdge(v.getPrev(), v, t));
                                // System.out.println("VERTICE1: "+v.getMarking()+" PrevNode: "+v.getPrev().getMarking());
                                 v.setVisited();
                                 v.getPrev().setVisited();
                                }
                            if((eStart.equals(v.getPrev().getMarking())|| eStart.equals(v.getMarking()))){
                                 System.out.println("SUCCI: "+eStart+" Prev: "+v.getPrev().getMarking()+" P: "+v.getMarking());
                                 if(v.getPrev().getVisited()|| v.getVisited()){
                                     System.out.println("VisitOBEN: "+v.getPrev().getVisited()+" "+v.getVisited());
                                    fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack); 
                                     
                                 }
                             }

                                
                           
                            
                         }
                         boolean check = false;
                         for(ReachabilityNode v : verticesList){
                             System.out.println("visitedPrev: "+v.getPrev().getVisited()+" visited V: "+v.getVisited());

                             
                             if((eStart.equals(v.getPrev().getMarking())|| eStart.equals(v.getMarking()))){
                                 System.out.println("Success2: "+eStart+" Prev: "+v.getPrev().getMarking()+" P: "+v.getMarking());
                                 if(v.getPrev().getVisited()|| v.getVisited()){
                                     System.out.println("VisS: "+v.getPrev().getVisited()+" "+v.getVisited());
                                    //fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack); 
                                     
                                 }
                             }
                                 
                            
                              if((eStart.equals(v.getPrev().getMarking())&& v.getPrev().getVisited()== false)||(eStart.equals(v.getMarking())&& v.getVisited()== false)){
                                  System.out.println("Failure: "+eStart+" "+v.getPrev().getMarking()+" "+v.getMarking());
                                  fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, backtrack);
                            }
               
                         }
                        
                             
                     
                         
                     }

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
