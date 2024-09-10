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
   
    private HashMap<Place, Long> eTarget;
    private HashMap<Place, Long> eStart;
   
  
   /**
    * Used for part one of the program. Maps and lists to keep track of
    * updated nodes and fired transitions.
    */
    private static HashMap<Place, Long> updateFrame = new HashMap<>();
    private static ArrayList<Transition> usedTransitions = new ArrayList<>();
    private static HashMap<Place, Long> firstNode = new HashMap<>();
    private static HashMap<Place, Long> visitedNodes = new HashMap<>();
    private static ArrayList<ReachabilityNode> reachabilityNodes = new ArrayList<>();
   /**
    * Used for part two of the program. Also to keep track of used nodes
    * and fired transitions. 
    */
    private static int count = 0;
    private static HashMap<Place, Long> updatedMarking = new HashMap<>();
    private static HashSet<Transition> enabledTransitions = new HashSet<>();
    private static boolean transitionFound = false;
    private static boolean transitionBeforeTarget = false;
    private static ArrayList<ReachabilityNode> nodes = new ArrayList<>();
    private static ArrayList<Transition> tBacktrack = new ArrayList<>();
    // Indicates if a node is found.
    private static boolean found = false;
  
    /**
    * 
    * @return 
    */
    public static boolean setFoundTrue(){
       return found = true;
    }
    
    /**
     * 
     * @return 
     */
    public static boolean setFoundFalse(){
       return found = false;
   }
   
    /**
     * 
     * @return 
     */
    public static boolean setFoundTransitionFalse(){
       return transitionFound = false;
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
    public static HashMap<Place, Long> putUpdateFrame(Place p, long oldVal, long newVal){
       updateFrame.replace(p, oldVal, newVal);
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
     * @param eStart
     * @param eTarget 
     */
    public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking,HashMap<Place, Long> target, HashMap<Place, Long> eStart, HashMap<Place, Long> eTarget ) throws InterruptedException{
        super(pf, marking, target);
        this.eStart = eStart;
        this.eTarget = eTarget;
        
    }
    
    /**
     * 
     * @param pf
     * @param marking
     * @param target
     * @param eStart
     * @param eTarget
     * @param transition
     * @throws InterruptedException 
     */
    public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking,HashMap<Place, Long> target, HashMap<Place, Long> eStart, HashMap<Place, Long> eTarget, Transition transition ) throws InterruptedException{
        super(pf, marking, target);
        this.eStart = eStart;
        this.eTarget = eTarget;
    }
    
    /**
     * 
     * @param pf
     * @param marking
     * @param target 
     */
    public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking, HashMap<Place, Long> target) {
        super(pf, marking, target);
        
    }

    @Override
    public void computePriority(ReachabilityNode node) {
        // Breadth First Search does not use a priority.
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
        if(eStart != null && eTarget !=null){
            // If a transition is chosen that must be use.
            if(ConstraintFrame.chooseTransition != null && ConstraintFrame.forcedTransition== true){
                specificTransition();
            }
            // If no specific transition is chosen.
            if(ConstraintFrame.chooseTransition == null){
                explicitStartAndTarget(eTarget);
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
   
   /**
    * Resets used transitions/marks them as not used.
    */
    public void resetAll(){
       for(Transition t : pf.getBackup().transitions()){
           t.resetTransitions();
       }
    }

   /**
    * Compute reachability with explicit start and target.
    */
    public void explicitStartAndTarget(HashMap<Place,Long> targetNode){
        resetAll(); 
        LOGGER.debug("Starting BFS with specific start and target node");
        PetriNet newPN = new PetriNet();
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

        visitedNodes.clear();
        HashMap<Place, Long> resetMap = new HashMap<>();
        firstNode = eStart;
        //Set all nodes back to not visited
        for(ReachabilityNode r : rNodeList){
            r.setUnvisited();
        }

        /**
         * Actual algorithm starts here.
         * Go through all nodes and compute its transitions.
         */
        updateFrame.putAll(newMarkingMap);
        int interrupt = rNodeList.size();
        while(!rNodeList.isEmpty() && counter < interrupt){   
            counter +=1;
            
            rNodeList.forEach(a->System.out.println("NodeList: "+a.getMarking()));
            if (counter % 100 == 0) {
               fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
             // Take node out of rNodeList. Remove same node out of list.
            ReachabilityNode workingNode = rNodeList.get(0);//Nimm den workingNode und gucke ihn an
            System.out.println("Anfang Node: "+ workingNode.getMarking());
            // init previsited node
            ReachabilityNode preNode = workingNode;
            // if preNode is visited. Set current node as previouse node.
            ReachabilityNode currNode = null;
            if(preNode.getVisited()== true){
                currNode = workingNode;
                currNode.setPrev(preNode);
            }
            System.out.println("WorkingNode: "+workingNode.getMarking());
            System.out.println("");
            System.out.println("");
            System.out.println("");
         
            for(Transition t : activeTransitions){
                if(t.getUsed()== false){
                     System.out.println("Transition: "+t.toString());
                    System.out.println("BFS Frame: "+getUpdateFrame());
                     // If workingNode and transition input are equal -> create edge in reachabilitygraph 
                     // between those two nodes
                     // If no prenode exists. Transition only updates output.
                    pf.computeSingleMarking(t, targetNode);
                    t.setUsed();
                    if(!usedTransitions.contains(t)){
                       addUsedTransition(t);
                    }
                    System.out.println("--------------------------------");
                    System.out.println("--- NewMarking: "+getUpdateFrame()+" ---");
                    System.out.println("--------------------------------");
                    System.out.println("--- WorkingNode: "+workingNode.getMarking()+" First visit: "+workingNode.getVisited()+" second visit: "+workingNode.getSecondVisit()+"------");
                    System.out.println("--------------------------------");
                    System.out.println("---- PreNode: "+workingNode.getPrev().getMarking()+" visited once: "+workingNode.getPrev().getVisited()+" visited twice: "+workingNode.getPrev().getSecondVisit());
                    System.out.println("---------- "+t+" is used: "+t.getUsed()+" ------------");
                    System.out.println("-------------------End--------------");
                    System.out.println("");
                    System.out.println("");
                    HashSet<Transition> activeTransitionsUpdate =  pf.computeActiveTransitions(getUpdateFrame());
                    activeTransitions = activeTransitionsUpdate;
                    System.out.println("activeTransition: "+activeTransitions);
                    rNodeList.remove(workingNode);
                    
                    if(found == true){
                        workingNode.setVisited();
                        vertices.add(workingNode);
                        if(currNode.getVisited()== true && preNode.getVisited()== true){
                            edge.add(new ReachabilityEdge(preNode, currNode, t));
                        }
                        if(transitionBeforeTarget == true){
                            fireReachabilityUpdate(ReachabilityEvent.Status.RESTRICTED, count, backtrack);
                            transitionFound = false;
                            return;
                        }
                        fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, count, backtrack);
                  
                        return;
                    }
                    workingNode.setVisited();
                    vertices.add(workingNode);
                    rNodeList.remove(workingNode);
                    if(currNode != null && preNode.getVisited()== true && currNode.getVisited()== true){
                        edge.add(new ReachabilityEdge(preNode, currNode, t));
                        if(currNode.getMarking().keySet().iterator().next().equals(firstNode.keySet().iterator().next())){
                            currNode.setSecondVisit();
                            reachabilityNodesList.add(currNode);
                            
                        }
                    }
                }
           
            }

        }
        /**
         * Termination condition
         */
        if( counter >= interrupt){
            fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, count, backtrack);
            return;
        }
        if(rNodeList.isEmpty()){
            fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, count, backtrack);
            return;
        }
    }

    /**
    * Method to force a chosen transition to be fired.
    */
    public void specificTransition(){
        //transitionFound = false;
        transitionBeforeTarget = false;
        resetAll(); 
            
        LOGGER.debug("Starting BFS with specific transition to be used.");
        PetriNet newPN = new PetriNet();

        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);

        HashSet<ReachabilityNode> tVertices = new HashSet<>();
        HashSet<ReachabilityEdge> tEdges = new HashSet<>();
        ArrayList<ReachabilityNode> tVerticesList = new ArrayList<>();
        ArrayList<ReachabilityNode> reachabilityNodes = new ArrayList<>();
        ArrayList<ReachabilityNode> nodesCount = new ArrayList<>();
        ReachabilityNode tRoot = new ReachabilityNode(null, null);
        
        HashMap<Place, Long> updatedMarking = new HashMap<>();
        ReachabilityNode firstLastNode = null;
        // Init every node as Reachabilitynode
        for(Map.Entry<Place, Long> entry : marking.entrySet()){
            updatedMarking.put(entry.getKey(), entry.getValue());
            HashMap<Place, Long> work = new HashMap<>();
            
            if(reachabilityNodes.isEmpty()){
                work.put(entry.getKey(), entry.getValue());
                ReachabilityNode firstNode = new ReachabilityNode(work, null);
                reachabilityNodes.add(firstNode);
                tRoot = firstNode;
            }
            ReachabilityNode lastNode = reachabilityNodes.getLast();
            work.put(entry.getKey(), entry.getValue());
            ReachabilityNode currNode = new ReachabilityNode(work, lastNode);
            reachabilityNodes.add(currNode);
        }
        if(reachabilityNodes.get(0).getMarking().keySet().equals(reachabilityNodes.get(1).getMarking().keySet())){
            reachabilityNodes.remove(0);
            ReachabilityNode exchange = new ReachabilityNode(reachabilityNodes.getFirst().getMarking(), reachabilityNodes.getLast());
            reachabilityNodes.remove(0);
            reachabilityNodes.add(0,exchange);
            firstLastNode = reachabilityNodes.get(0);
        }
        
        tVertices.addAll(reachabilityNodes);
        HashSet<Transition> tActiveTransitions = pf.computeActive(marking);
        
        ArrayList<ReachabilityNode> tReachabilityNodesList = new ArrayList<>();
        ArrayList<Transition> tBacktrack = new ArrayList<>();
        
        HashMap<Place, Long> tResetMap = new HashMap<>();
        for(ReachabilityNode r : reachabilityNodes){
            r.setUnvisited();
        }
        ArrayList<HashMap<Place, Long>> inputNodes = new ArrayList<>();
        ArrayList<HashMap<Place, Long>> outputNodes = new ArrayList<>();
       
        // Get all input places of musthave transition
        for(Place input : ConstraintFrame.chooseTransition.inputs()){
            for(Map.Entry<Place, Long> entry : marking.entrySet()){
                if(entry.getKey().equals(input)){
                    HashMap<Place, Long> loadInput = new HashMap<>();
                    loadInput.put(entry.getKey(), entry.getValue());
                    inputNodes.add(loadInput);
                }
            }
        }
        // Get all outputNodes of musthave transition
        for(Place output : ConstraintFrame.chooseTransition.outputs()){
            for(Map.Entry<Place, Long> entry : marking.entrySet()){
                if(entry.getKey().equals(output)){
                    HashMap<Place, Long> loadOutput = new HashMap<>();
                    loadOutput.put(entry.getKey(), entry.getValue());
                    outputNodes.add(loadOutput);
                }
            }
        }
        boolean secondPart = false;
        boolean firstPart = false;
        boolean finished = false;
        
        ArrayList<String> inputString = new ArrayList<>();
        ArrayList<String> outputString = new ArrayList<>();
        
        // Special case: chosen transition has no input
        if(inputNodes.isEmpty()){
            fireReachabilityUpdate(ReachabilityEvent.Status.ALWAYSUSED, count, tBacktrack);
            return;
        }
        nodesCount.forEach(a-> System.out.println("n: "+a.toString()));
        // Use all input nodes as target
        while(!inputNodes.isEmpty()){
            for(HashMap<Place, Long> inputMap : inputNodes){
                String stringBFS = BFS(eStart, inputMap, reachabilityNodes, secondPart, firstPart);
                inputString.add(stringBFS);
                
                if(stringBFS == "Success"){
                    firstPart = true;
                    if(transitionFound && found ){
                        for(Map.Entry<Place, Long> entry : updateFrame.entrySet()){
                            if(entry.getKey().equals(eTarget.keySet().iterator().next())){
                                if(!(entry.getValue().equals(eTarget.values().iterator().next()))){
                                      fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, count, tBacktrack);
                                        setFoundFalse();
                                        return;
                                }
                            }
                        }
                    }
                    break;
                }
                if(stringBFS == "Problem"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.PROBLEM, count, backtrackList(tBacktrack));
                    
                    return;
                }
                if(stringBFS == "Failure"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, count, backtrackList(tBacktrack));
                    setFoundFalse();
                    return;
                }
            }
            inputNodes.remove(0);
        }
        firstPart = true;
        
        nodesCount.addAll(reachabilityNodes);
        ArrayList<ReachabilityNode> nodesForSecondPart = new ArrayList<>();
        // Use all output nodes as start
        for(ReachabilityNode r: reachabilityNodes){
            for(Map.Entry<Place, Long> entry : BreadthFirst.updateFrame.entrySet()){
                if(r.getMarking().keySet().iterator().next().equals(entry.getKey())){
                    HashMap<Place, Long> replaceMap = new HashMap<>();
                    replaceMap.put(entry.getKey(), entry.getValue());
                    ReachabilityNode change = new ReachabilityNode(replaceMap, r.getPrev());
                    r = change;
                    nodesForSecondPart.add(change);
                }
            }
        }
        nodesForSecondPart.forEach(a-> System.out.println("außen: "+a.getMarking()));
        if(firstLastNode != null){
            outputNodes.addLast(firstLastNode.getMarking());
        }
        tBacktrack.forEach(a -> System.out.println("Transition used: "+a.getUsed()));
        /**
         * If target is reached before transition has been fired.
         * Choose new targetnode -> output node of transition that has to be fired.
         * Use method explicitStartAndTarget.
         */
        if(transitionFound && outputNodes.isEmpty()){
            transitionBeforeTarget = true;
            HashMap<Place, Long> newTarget = new HashMap<>();
            for(Map.Entry<Place, Long> entry : updateFrame.entrySet()){
                if(entry.getKey().equals(ConstraintFrame.chooseTransition.outputs().getFirst())){
                    newTarget.put(entry.getKey(), entry.getValue());
                }
            }
            explicitStartAndTarget(newTarget);
            transitionBeforeTarget = false;
        }
        while(!outputNodes.isEmpty() && firstPart == true){
            secondPart = true;
            for(HashMap<Place, Long> outputMap : outputNodes){
                if(outputMap.equals(outputNodes.getLast())){
                    finished = true;
                }
               
                String stringBFS = BFS(outputMap, eTarget,nodesForSecondPart , secondPart, firstPart);
                outputString.add(stringBFS);
                if(stringBFS == "Failure" && transitionFound == false){
                    fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, count, backtrackList(tBacktrack));
                    transitionFound = false;
                    setFoundFalse();
                    return;
                }
                if(stringBFS == "Failure" && transitionFound == true){
                    fireReachabilityUpdate(ReachabilityEvent.Status.PROBLEM, count, backtrackList(tBacktrack));
                    transitionFound = false;
                    setFoundFalse();
                    return;
                }
                if(stringBFS == "Success"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, count, backtrackList(tBacktrack));
                    transitionFound = false;
                    setFoundFalse();
                    return;
                }
            }
           outputNodes.remove(0);
        }
   }
   
    /**
     * 
     * @param start
     * @param targetNode
     * @param nodeList
     * @param secondPart
     * @param firstPart
     * @return 
     */
    public String BFS(HashMap<Place, Long> start, HashMap<Place, Long> targetNode, ArrayList<ReachabilityNode> nodeList, boolean secondPart, boolean firstPart){
        int counter = 0;
        HashSet<ReachabilityNode> vertices = new HashSet<>();
        ArrayList<ReachabilityNode> reachNodeList = new ArrayList<>();
        HashSet<ReachabilityEdge> edge = new HashSet<>();
        /**
         * If first part is already computed use updatedMarking 
         * to compute active transitions.
         */
        if(secondPart == true){
            HashSet<Transition>  enabledTransitions = pf.computeActiveTransitions(updateFrame);
            BreadthFirst.enabledTransitions.addAll(enabledTransitions);
            updateFrame.putAll(updatedMarking);
        }
        if(secondPart == false){
            HashSet<Transition> enabledTransitions = pf.computeActiveTransitions(marking);
            BreadthFirst.enabledTransitions.addAll(enabledTransitions);
        }
        BreadthFirst.enabledTransitions.forEach(b-> System.out.println("Trans: "+ b));
        if(updatedMarking.isEmpty()){
           for(ReachabilityNode r : nodeList){
                updatedMarking.put(r.getMarking().keySet().iterator().next(), r.getMarking().values().iterator().next());
            }
            updateFrame.putAll(updatedMarking);
        }
       
        int interrupt = nodeList.size();
        while(!nodeList.isEmpty()&& counter < interrupt){
            counter += 1;
            fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);
            if(counter % 100 == 0){
                fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, tBacktrack);
            }
            ReachabilityNode workingNode = nodeList.get(0);
            ReachabilityNode preNode = workingNode;
            ReachabilityNode currNode = null;
            if(preNode.getVisited() == true){
                currNode = workingNode;
                currNode.setPrev(preNode);
            }
            for(Transition t : enabledTransitions){
                if(t.getUsed() == false){
                    pf.computeSingleMarking(t, targetNode);
                    updatedMarking.putAll(updateFrame);
                    t.setUsed();
                    // If transition equals chosen transition, set transitionFound true
                    if(t.equals(ConstraintFrame.chooseTransition)){
                      transitionFound = true;
                    }
                    addUsedTransition(t);
              
                    System.out.println("--------------------------------");
                    System.out.println("------WorkingNode: "+workingNode.getMarking()+" ---------------");
                    System.out.println("------UpdatedMarking: "+updatedMarking+" --------------------------------");
                    System.out.println("------UpdatedFrame: "+updateFrame+" --------------------------------");
                    System.out.println("------- Second: "+secondPart+" -------------");
                    System.out.println("--------------------------------");

                    HashSet<Transition> activeTransitionUpdated = pf.computeActiveTransitions(updatedMarking);
                    enabledTransitions = activeTransitionUpdated;
                    nodeList.remove(0);
                    // If transition used and target found in first part -> terminate
                    if(found  && transitionFound){
                        if(firstPart == false){
                            System.out.println("Ausgabe: "+updateFrame);
                            workingNode.setVisited();
                            vertices.add(workingNode);
                            if(currNode!= null && preNode.getVisited() == true && currNode.getVisited() == true){
                               edge.add(new ReachabilityEdge(preNode, currNode, t));

                           }
                           return "Success";
                        }
                        if(secondPart == true){
                           workingNode.setVisited();
                           vertices.add(workingNode);
                           if(currNode!= null && preNode.getVisited() == true && currNode.getVisited() == true){
                              edge.add(new ReachabilityEdge(preNode, currNode, t));
                            }
                           return "Success";
                       }
                   }
                   if(transitionFound == true && found == false){
                       transitionBeforeTarget = true;
                   }
                   workingNode.setVisited();
                   if(currNode != null && preNode.getVisited() == true){
                       edge.add(new ReachabilityEdge(preNode, currNode, t));
                   }
               }
           }
       }
       /**
        * Termination condition. 
        */
       if(counter >= interrupt){
           return "Aborted";
       }
       if(nodeList.isEmpty()){
           return "Failure";
       }
   return null;
   }
}
   
  
   


   


    

    
   

