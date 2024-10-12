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
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.security.util.Debug;

/**
 * @author Marcel Gehrmann
 * @author Kristin Haas
 */
public class BreadthFirst extends AbstractReachabilityAlgorithm {

    ConstraintFrame cf;
    private static final Logger LOGGER = LogManager.getLogger(BreadthFirst.class);
    private static ReachabilityGraph reachabilityGraph = null;
   
  
   /**
    * Used for part one of the program. Maps and lists to keep track of
    * updated nodes and fired transitions.
    */
    private static HashMap<Place, Long> updateFrame = new HashMap<>();
    private static ArrayList<Transition> usedTransitions = new ArrayList<>();
    private static HashMap<Place, Long> visitedNodes = new HashMap<>();
    private static ArrayList<ReachabilityNode> reachabilityNodes = new ArrayList<>();
   /**
    * Used for part two of the program. Also to keep track of used nodes
    * and fired transitions. 
    */
    private static HashMap<Place, Long> updatedMarking = new HashMap<>();
    private static HashSet<Transition> enabledTransitions = new HashSet<>();
  
    private static ArrayList<ReachabilityNode> nodes = new ArrayList<>();
    private static ArrayList<Transition> tBacktrack = new ArrayList<>();
    // Indicates if a node is found.
  
    public static ReachabilityGraph getReachabilityGraph(){
        return reachabilityGraph;
    }
 
    
    /**
     * 
     * @param p
     * @param oldVal
     * @param newVal
     * @return 
     */
    public static HashMap<Place, Long> putUpdateMarking(Place p, long oldVal, long newVal){
       updatedMarking.replace(p, oldVal, newVal);
       return updatedMarking;
    }
    
    /**
     * 
     * @return 
     */
    public static HashMap<Place, Long> getUpdatedMarking(){
       return updatedMarking;
    }
    
   /**
    * 
    * @return 
    */
    public static ArrayList<ReachabilityNode> getReachabilityNodeList(){
       return reachabilityNodes;
    }
   
    /**
     * 
     * @return 
     */
    public static HashMap<Place, Long> getVisitiedNodes(){
       return visitedNodes;
    }
   
    /**
     * 
     * @param place 
     */
    public static void addToVisitedNodes(HashMap<Place, Long> place){
       visitedNodes.putAll(place);
    }
   
    /**
     * 
     * @return 
     */
    public ArrayList<Transition> getUsedTransitions(){
       return tBacktrack;
    }
   
    /**
     * 
     * @param transition 
     */
    public static void addUsedTransition(Transition transition){
       usedTransitions.add(transition);
    }
   
    /**
     * 
     * @return 
     */    
    public static HashMap<Place, Long> getUpdateFrame(){
       return updateFrame;
    }
   
    /**
     * 
     * @param p
     * @param oldVal
     * @param newVal
     * @return 
     */
    public static HashMap<Place, Long> putUpdateFrame(Place p, long newVal){
       updateFrame.replace(p, newVal);
        System.out.println("Method: "+p+" "+newVal+" "+updateFrame);
       return updateFrame;
    }
   
    /**
     * 
     * @return 
     */
    public static ArrayList<Transition> getTransitions(){
       return usedTransitions;
    }
   
   /**
    * Clears Reachabilitynodes
    */
    public static void clearReachabilityNodes(){
       reachabilityNodes.clear();
    }
   /**
    * Clears visited nodes map.
    */
    public static void clearVisitedNodes(){
       visitedNodes.clear();
    }
   
   /**
    * Clears tbacktrack list with fired transitions.
    */
    public static void clearTBacktrack(){
      tBacktrack.clear();
    }
   
   /**
    * Clears enabled transitions list.
    */
    public static void clearEnabledTransitions(){
       enabledTransitions.clear();
    }
   
   /**
    * Clears map
    */
    public static void clearMarkingHashMap(){
       updatedMarking.clear();
    }
   
   /**
    * Clears node list
    */
    public static void clearNodeslist(){
       nodes.clear();
    }
   
    /**
    * Clears map with updated marking.
    */
    public static void clearUpdatedMarking(){
       updatedMarking.clear();
    }
  
    /**
    * Clears list with fired transitions.
    */
    public static void clearUsedTransitions(){
       usedTransitions.clear();
    }
   
    /**
    * Clears updateFrame map, which keeps track of marking.
    */
    public static void clearUpdateFrame(){
       updateFrame.clear();
    }
   /**
    * Deletes transitions that occurre more than once.
    * @return 
    */
    public static ArrayList<Transition> forcedTransitionBacktrack(){
        ArrayList<Transition> cleanTransitions = new ArrayList<>();
        for(Transition t : tBacktrack){
            if(!cleanTransitions.contains(t)){
                cleanTransitions.add(t);
            }
        }
        return cleanTransitions;
    }
             
    /**
     * @param pf
     * @param marking
     * @param target
  
     */
    public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking,HashMap<Place, Long> target ){
        super(pf, marking, target);
    }
    
    
    
   
    @Override
    public void computePriority(ReachabilityNode node) {
        // Breadth First Search does not use a priority.
    }
    
    /**
     * 
     * @param old 
     */
    public void fillUpdateFrame(HashMap<Place, Long> old){
        HashMap<Place, Long> oldy = old;
        for(Map.Entry<Place, Long> entry : oldy.entrySet()){
            updateFrame.put(entry.getKey(), entry.getValue());
        }
    }
    
    public void setTransitionsUnused(){
        HashSet<Transition> transition = new HashSet<>();
        
    }
    
    /**
     * include query  to ask for specific source and tagret
     */
    @Override
    public void run()  {
       /**
        * Check if explicit start and target are used.
        * Additionally ask if a specific transition must be visited.
        */
        boolean myFrame = true;
        if(myFrame){
            
           bfs(ConstraintFrame.getChosenTransition());
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
           // fillUpdateFrame(start);
        } else {
            LOGGER.info("Target marking could not be reached from start marking.");
            g = new ReachabilityGraph(vertices, edges);
            fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, null);
        }
        }
    }
    
  
    
  

    public void bfs(Transition forceTransition){
        int counter = 0;
        Pathfinder.setUnused();
        Transition chosenTransition = forceTransition;
        PetriNet newPN = new PetriNet();
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        HashSet<ReachabilityEdge> edges = new HashSet<>();
        ReachabilityNode rootNode = new ReachabilityNode(marking, null);
        // tar in AbstractReachabilityAlgorithm 
        tar = new ReachabilityNode(target, null);
        vertices.add(rootNode);
        ArrayList<ReachabilityNode> markingList = new ArrayList<>();
        markingList.add(rootNode);
        HashMap<Place, Long> mapForFrame = new HashMap<>();
        System.out.println("MARK: "+markingList.getFirst().getMarking());
        while(!markingList.isEmpty() || !isInterrupted()){
            counter += 1;
            if (counter % 100 == 0) {
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
             if(ConstraintFrame.getStopProgram() == true){
                fillUpdateFrame(mapForFrame);
                fireReachabilityUpdate(ReachabilityEvent.Status.STOPED, counter, backtrack());
                return;
            }
            ReachabilityNode currNode = markingList.getFirst();
            markingList.removeFirst();
            HashSet<Transition> activeTransitions = pf.computeActive(currNode.getMarking());
            for(Transition transition : activeTransitions){
                HashMap<Place, Long> newMarking = pf.computeMarking(currNode.getMarking(), transition);
                ReachabilityNode newNode = new ReachabilityNode(newMarking, currNode);
                // Set transition used
                transition.setUsed();
                usedTransitions.add(transition);
                mapForFrame = newNode.getMarking();
                if(chosenTransition == null){
                    // if target marking is found
                    if(newNode.equals(tar)){
                        tar = newNode;
                        vertices.add(tar);
                        edges.add(new ReachabilityEdge(currNode, tar, transition));
                        // g also defined in AbstractReachabilityAlgorithm 
                        g = new ReachabilityGraph(vertices, edges);
                        reachabilityGraph = g;
                        fillUpdateFrame(newNode.getMarking());
                        fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack());
                        return;
                    }
                }
                // If a transition is chosen to be forced to fire.
                if(chosenTransition != null ){
                    // If target marking equals current marking and transition has been fired.
                    if(newNode.equals(tar) && chosenTransition.getUsed() == true){
                        tar = newNode;
                        vertices.add(tar);
                        edges.add(new ReachabilityEdge(currNode, tar, transition));
                        // g also defined in AbstractReachabilityAlgorithm 
                        g = new ReachabilityGraph(vertices, edges);
                        reachabilityGraph = g;
                        fillUpdateFrame(newNode.getMarking());
                        fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, counter, backtrack());
                        return;
                    }
                }
                
                for(ReachabilityNode vertice : vertices){
                    if(vertice.equals(newNode)){
                        vertice.setVisited();
                        edges.add(new ReachabilityEdge(currNode, vertice, transition));
                        break;
                    }
                }
                vertices.add(newNode);
                markingList.add(newNode);
                edges.add(new ReachabilityEdge(currNode, newNode, transition));
                }
            
            if(markingList.isEmpty()){
                g = new ReachabilityGraph(vertices, edges);
                fillUpdateFrame(mapForFrame);
                fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, counter, backtrack());
                LOGGER.info("Target marking could not be reached from start marking.");
            }
            
        }
          
            
    }
}
   
  
   


   


    

    
   

