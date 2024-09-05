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
 *
 * @author Marcel Gehrmann
 */
public class BreadthFirst extends AbstractReachabilityAlgorithm {

    ConstraintFrame cf;
   private static final Logger LOGGER = LogManager.getLogger(BreadthFirst.class);
   
   private HashMap<Place, Long> eTarget;
   private HashMap<Place, Long> eStart;
   
   private static HashMap<Place, Long> updateFrame = new HashMap<>();
   //usedTransition part 1 of program
   private static ArrayList<Transition> usedTransitions = new ArrayList<>();
   public static HashMap<Place, Long> firstNode = new HashMap<>();
   public static HashMap<Place, Long> visitedNodes = new HashMap<>();
   private Transition mustTransition = null;
   
   private static  HashMap<Place,Long> tUpdateFrame = new HashMap<>();
   public  ArrayList<Transition> tUsedTransition = new ArrayList<>();
   public  HashMap<Place, Long> tVisitedNodes = new HashMap<>();
   
   private static ArrayList<HashMap<Place, Long>> forConstraintFrame = new ArrayList<>();
   private static ArrayList<ArrayList<Transition>> allUsedTransitions = new ArrayList<>();
   private static ArrayList<HashMap<Place, Long>> allVisitedNodes = new ArrayList<>();
   
   private static int count = 0;
   private static HashSet<Transition> enabledTransitions = new HashSet<>();
   private static HashMap<Place, Long> updatedMarking = new HashMap<>();
   private static ArrayList<ReachabilityNode> nodes = new ArrayList<>();
   private static ArrayList<Transition> tBacktrack = new ArrayList<>();
   
   public static void clearTBacktrack(){
      tBacktrack.clear();
   }
   
   public static void clearEnabledTransitions(){
       enabledTransitions.clear();
   }
   
   public static void clearMarkingHashMap(){
       updatedMarking.clear();
   }
   
   public static void clearNodeslist(){
       nodes.clear();
   }
   
   public static void clearTUpdatFrame(){
       tUpdateFrame.clear();
   }
   
   public static void clearUpdatedMarking(){
       updatedMarking.clear();
   }
   
   public static void clearUsedTransition(){
       usedTransitions.clear();
   }
   
   public ArrayList<Transition> getUsedTransitions(){
       return tBacktrack;
   }
   
   public static void addUsedTransition(Transition transition){
       usedTransitions.add(transition);
   }
   
   public static void clearUsedTransitions(){
       usedTransitions.clear();
   }
   
   public static ArrayList<Transition> getTransitions(){
       return usedTransitions;
   }
   
   private static HashMap<Place, Long> getForcedUpdateFrame(){
       return tUpdateFrame;
   }
   
   public static ArrayList<HashMap<Place, Long>> returnForConstraintFrame(){
       return forConstraintFrame;
   }
   
   public static ArrayList<ArrayList<Transition>> returnForAllUsedTransitions(){
       return allUsedTransitions;
   }
   
   public static  ArrayList<HashMap<Place, Long>> returnForAllVisitedNodes(){
       return allVisitedNodes;
   }
   
   public static HashMap<Place, Long> getTUpdateFrame(){
       return tUpdateFrame;
   }
   
   public static void clearUpdateFrame(){
       updateFrame.clear();
   }
   
   public static ArrayList<Transition> forcedTransitionBacktrack(){
       ArrayList<Transition> cleanTransitions = new ArrayList<>();
       for(Transition t : tBacktrack){
           if(!cleanTransitions.contains(t)){
               cleanTransitions.add(t);
           }
       }
       return cleanTransitions;
   }
   
         
   public static HashMap<Place, Long> getUpdateFrame(){
       return updateFrame;
   }
   
   public static HashMap<Place, Long> putUpdateFrame(Place p, long oldVal, long newVal){
       updateFrame.replace(p, oldVal, newVal);
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
    
    public BreadthFirst(Pathfinder pf, HashMap<Place, Long> marking,HashMap<Place, Long> target, HashMap<Place, Long> eStart, HashMap<Place, Long> eTarget, Transition transition ) throws InterruptedException{
        super(pf, marking, target);
        this.eStart = eStart;
        this.eTarget = eTarget;
        this.mustTransition = transition;
    }
    
    public BreadthFirst testBreadthFirst(HashMap<Place, Long> tStart, HashMap<Place, Long> tTarget ){
        return null;
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
        * Check ist explicit start and target are used.
        * Additionally ask if a specific transition must be visited
        */
        if(eStart != null && eTarget !=null){
            if(ConstraintFrame.chooseTransition != null && ConstraintFrame.forcedTransition== true){
                specificTransition();
                
                
                
            }
            if(ConstraintFrame.chooseTransition == null){
                explicitStartAndTarget();
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
   
   
  
   
   
   /**
    * @author Kristin Haas
    * Compute reachability with explicit start and target.
    */
   private static boolean found = false;
   public static boolean setFoundTrue(){
       return found = true;
   }
   public static boolean setFoundFalse(){
       return found = false;
   }
   public void explicitStartAndTarget(){
        resetAll(); 
            
        LOGGER.debug("Starting BFS with specific start and target node");
        PetriNet newPN = new PetriNet();
        
        /**If start and target are equal. Stop program.
         * Can be used. Now computes complete path through PN
         * back to the startnode.
        if(eStart.equals(eTarget)){
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


        visitedNodes.clear();//VISITCLEAR
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
       
        while(!rNodeList.isEmpty() && counter<rNodeList.size()){   
            counter +=1;
            rNodeList.forEach(a->System.out.println("NodeList: "+a.getMarking()));
            if (counter % 100 == 0) {
               fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
            }
             // Grab node out of rNodeList. Remove same node out of list.
            ReachabilityNode workingNode = rNodeList.get(0);//Nimm den workingNode und gucke ihn an
            reachabilityNodesList.add(workingNode);
             // Examine transitions. Look for t that belongs to working node.
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
                    pf.computeSingleMarking(t);
                    t.setUsed();
                    addUsedTransition(t);
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
                        fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, count, backtrack);
                        return;
                    }
                        
            }
            }
            if(workingNode.equals(eTarget)){
                fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, count, backtrack);
            }
            


            }
        fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, count, backtrack);
   }
   
   /**
    * @author Kristin Haas
    * If a specific transition shall be fired:
    *   first do BFS from target to transition
    *   and at the same time start to transition.
    * If path exists go through path and check reachability.
    * -> Put path as "new" PN into explicitStartAndTarget
    */
   private static boolean stopPart = false;
   public void specificTransition(){
            
        resetAll(); 
            
        LOGGER.debug("Starting BFS with specific transition to be used.");
        PetriNet newPN = new PetriNet();
        
        
        
        fireReachabilityUpdate(ReachabilityEvent.Status.STARTED, 0, null);

        HashSet<ReachabilityNode> tVertices = new HashSet<>();
        HashSet<ReachabilityEdge> tEdges = new HashSet<>();
        
        ArrayList<ReachabilityNode> tVerticesList = new ArrayList<>();
        ArrayList<ReachabilityNode> reachabilityNodes = new ArrayList<>();
        
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
        
        //tVisitedNodes.clear();
        HashMap<Place, Long> tResetMap = new HashMap<>();
        // Zeile 254 firstNode = eStart
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
        // Use all input nodes as target
        while(!inputNodes.isEmpty()){
            for(HashMap<Place, Long> inputMap : inputNodes){
              //  if(inputMap)
               // BFS(start, inputMap, reachabilityNodes);
                System.out.println("BFS input: "+inputMap);
                String stringBFS = BFS(eStart, inputMap, reachabilityNodes, finished, secondPart);
                inputString.add(stringBFS);
                if(inputMap.keySet().equals(target.keySet())){
                    fireReachabilityUpdate(ReachabilityEvent.Status.PROBLEM, count, tBacktrack);
                    return;
                }
                //inputNodes.remove(0);
                if(stringBFS == "Success"){
                    firstPart = true;
                }
                if(stringBFS == "Problem"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.PROBLEM, count, backtrackList(tBacktrack));
                    return;
                }
                if(stringBFS == "Failure"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, count, backtrackList(tBacktrack));
                    return;
                }
                if(stringBFS == "Aborted"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, count, tBacktrack);
                    return;
                }
                if(stringBFS == "Aborted"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.ABORTED, count, tBacktrack);
                    return;
                }
                
                //inputNodes.remove(0);
                
        }
            inputNodes.remove(0);
        }
        
        
        ArrayList<ReachabilityNode> nodesForSecondPart = new ArrayList<>();
       
        // Use all output nodes as start
        for(ReachabilityNode r: reachabilityNodes){
            for(Map.Entry<Place, Long> entry : BreadthFirst.updatedMarking.entrySet()){
                if(r.getMarking().keySet().iterator().next().equals(entry.getKey())){
                    System.out.println("Value: "+entry.getValue());
                    HashMap<Place, Long> replaceMap = new HashMap<>();
                    replaceMap.put(entry.getKey(), entry.getValue());
                    ReachabilityNode change = new ReachabilityNode(replaceMap, r.getPrev());
                    r = change;
                    System.out.println("CHange: "+change.getMarking());
                    nodesForSecondPart.add(change);
                }
            }
        }
        nodesForSecondPart.forEach(a-> System.out.println("außen: "+a.getMarking()));
        if(firstLastNode != null){
            outputNodes.addLast(firstLastNode.getMarking());
        }
        tBacktrack.forEach(a -> System.out.println("Transition used: "+a.getUsed()));
        while(!outputNodes.isEmpty() && firstPart == true){
            secondPart = true;
            for(HashMap<Place, Long> outputMap : outputNodes){
                if(outputMap.equals(outputNodes.getLast())){
                    finished = true;
                }
                System.out.println("BFS OutputMap: "+outputMap+" vertices "+BreadthFirst.nodes+" "+BreadthFirst.tBacktrack);
               /** if(BreadthFirst.tBacktrack.size()>0){
                    BreadthFirst.tBacktrack.removeLast();
                }*/
                String stringBFS = BFS(outputMap, eTarget,nodesForSecondPart , finished, secondPart);
                outputString.add(stringBFS);
                //outputNodes.remove(0);
                if(stringBFS == "Failure"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.FAILURE, count, backtrackList(tBacktrack));
                    System.out.println("Case: failure: ");
                    return;
                }
                if(stringBFS == "Problem"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.PROBLEM, count, backtrackList(tBacktrack));
                    return;
                }
                if(stringBFS == "Success"){
                    fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, count, backtrackList(tBacktrack));
                    System.out.println("Case: Success1");
                    allUsedTransitions.forEach(a-> System.out.println("allUsed: "+a.getFirst()));
                    return;
                }
              //outputNodes.remove(0);  
            }
           outputNodes.remove(0);
           if(firstPart == true){
            System.out.println("ENDE Target: "+eTarget);
               System.out.println("Case: Success2");
            fireReachabilityUpdate(ReachabilityEvent.Status.SUCCESS, count, backtrackList(tBacktrack));
            return;
        }
        }
  
   }
   
   
   
   public String BFS(HashMap<Place, Long> start, HashMap<Place, Long> target, ArrayList<ReachabilityNode> nodeList, boolean finish, boolean secondPart){
       int counter = 0;
       System.out.println("START: "+start+" target: "+target+" NodeList: ");
       nodeList.forEach(a->System.out.println(a.getMarking()));
       
       for(Map.Entry<Place,Long> entry : updatedMarking.entrySet()){
           System.out.println("MAP: "+entry.getKey()+" Value: "+entry.getValue());
          
       }
       HashSet<ReachabilityNode> vertices = new HashSet<>();
       ArrayList<ReachabilityNode> reachNodeList = new ArrayList<>();
       HashSet<ReachabilityEdge> edge = new HashSet<>();
       //ArrayList<Transition> tBacktrack = new ArrayList<>();
       
       HashMap<ReachabilityEvent.Status, String> returnValue = new HashMap<>();
       
       if(secondPart == true){
           HashSet<Transition>  enabledTransitions = pf.computeActiveTransitions(updatedMarking);
           BreadthFirst.enabledTransitions.addAll(enabledTransitions);
           
       }
       if(secondPart == false){
           HashSet<Transition> enabledTransitions = pf.computeActiveTransitions(marking);
           BreadthFirst.enabledTransitions.addAll(enabledTransitions);
       }
       BreadthFirst.enabledTransitions.forEach(b-> System.out.println("Trans: "+ b));
       if(updatedMarking.isEmpty()){
           //HashSet<Transition> enabledTransitions = pf.computeActiveTransitions(marking);
          for(ReachabilityNode r : nodeList){
               updatedMarking.put(r.getMarking().keySet().iterator().next(), r.getMarking().values().iterator().next());
           }
       }
       
       // Needs if condition. Should be unvisited when both while loops are finished
       if(finish == true){
           for(ReachabilityNode r : nodeList){
               r.setUnvisited();
               updatedMarking.put(r.getMarking().keySet().iterator().next(), r.getMarking().values().iterator().next());
           }
           tBacktrack.clear();
       }
       
       while(!nodeList.isEmpty()){
           counter += 1;
           tUpdateFrame.putAll(updatedMarking);
           
           if(counter % 100 == 0){
               fireReachabilityUpdate(ReachabilityEvent.Status.PROGRESS, counter, null);
           }
           
           ReachabilityNode workingNode = nodeList.get(0);
           reachNodeList.add(workingNode);
           nodes.addAll(reachNodeList);
           
           
           for(Transition transition : enabledTransitions){
               if(transition.getUsed() == false){
                   if(transition.inputs().isEmpty() && ! transition.outputs().isEmpty()){
                   System.out.println("---------Only Output Exists----------");
                   for(Place p : transition.outputs()){
                       HashMap<Place, Long> updateNode = new HashMap<>();
                       
                       for(Map.Entry<Place, Long> entry : updatedMarking.entrySet()){
                           if(p.equals(entry.getKey())){
                               updateNode.put(entry.getKey(), entry.getValue());
                               HashMap<Place, Long> newMarkingOutputPlace = null;
                               updatedMarking.putAll(newMarkingOutputPlace);
                               
                               counter +=1;
                               tUpdateFrame.putAll(newMarkingOutputPlace);
                               transition.setUsed();
                               
                               if(start.equals(target)&& workingNode.getSecondVisit()==true){
                                   workingNode.setUnvisited();
                                   nodeList.addLast(workingNode);
                                   tUpdateFrame.putAll(updatedMarking);
                                   transition.setUsed();
                                   usedTransitions = tBacktrack;
                                   allUsedTransitions.add(tBacktrack);
                                   allVisitedNodes.add(updatedMarking);
                                   count = counter;
                                   return "Success";
                               }
                               if(start.equals(target)&& secondPart == false){
                                   tBacktrack.add(transition);
                                   return "Success";
                                   
                               }
                               
                               if(!start.equals(target)){
                                   if(updateNode.equals(target)){
                                       
                                       tUpdateFrame.putAll(updatedMarking);
                                       transition.setUsed();
                                       tBacktrack.add(transition);
                                       usedTransitions = tBacktrack;
                                       allUsedTransitions.add(tBacktrack);
                                       allVisitedNodes.add(updatedMarking);
                                       count = counter;
                                       return "Success";
                                   }
                               }
                                   for(ReachabilityNode n : reachNodeList){
                                       if(n.getMarking().keySet().equals(newMarkingOutputPlace.keySet())){
                                           n.getMarking().put(n.getMarking().keySet().iterator().next(), newMarkingOutputPlace.values().iterator().next() );
                                       }
                                   }
                               }
                            }
                       }
                       transition.setUsed();
                       if(transition.getUsed() == true && transition.getActive() == true){
                           tBacktrack.add(transition);
                           edge.add(new ReachabilityEdge(workingNode.getPrev(), workingNode, transition));
                       }
                       
                       HashSet<Transition> updateEnableTransitions = pf.computeActiveTransitions(updatedMarking);
                       enabledTransitions = updateEnableTransitions;
                   }
                   
                   
                   if(!transition.inputs().isEmpty() && !transition.outputs().isEmpty()){
                       System.out.println("-----------IN/OUTput exist-----------------");
                       
                       for(Place pIN : transition.inputs()){
                           for(Place pOUT : transition.outputs()){
                               HashMap<Place, Long> updateNodeIN = new HashMap<>();
                               HashMap<Place, Long> updateNodeOut = new HashMap<>();
                               for(Map.Entry<Place, Long> entry : updatedMarking.entrySet()){
                                    if(pIN.equals(entry.getKey()) && updateNodeIN.size() == 0){
                                       updateNodeIN.put(entry.getKey(), entry.getValue());
                                       HashMap<Place, Long>  newMarkingInputPlace =null; //pf.computeSingleMarking(updateNodeIN, transition, tUpdateFrame).getFirst();
                                       updatedMarking.put(newMarkingInputPlace.keySet().iterator().next(), newMarkingInputPlace.values().iterator().next());
                                       tUpdateFrame.put(newMarkingInputPlace.keySet().iterator().next(), newMarkingInputPlace.values().iterator().next());
                                       updateNodeIN = newMarkingInputPlace;
                                   }
                                   
                                   
                                    if(pOUT.equals(entry.getKey())){
                                        updateNodeOut.put(entry.getKey(), entry.getValue());
                                        HashMap<Place, Long> newMarkingOutputPlace = null;//pf.computeSingleMarking(updateNodeOut, transition, tUpdateFrame).getLast();
                                        HashMap<Place, Long> newMarkingInputPlace =null;// pf.computeSingleMarking(updateNodeOut, transition, tUpdateFrame).getFirst();
                                        updatedMarking.put(newMarkingOutputPlace.keySet().iterator().next(), newMarkingOutputPlace.values().iterator().next());
                                        tUpdateFrame.put(newMarkingOutputPlace.keySet().iterator().next(), newMarkingOutputPlace.values().iterator().next());
                                        ReachabilityNode rNode = new ReachabilityNode(updateNodeIN, workingNode);
                                        transition.setUsed();
                                        tBacktrack.add(transition);
                                        if(updateNodeIN.size() == 0){
                                            updateNodeIN.put(newMarkingInputPlace.keySet().iterator().next(), newMarkingInputPlace.values().iterator().next());
                                            updatedMarking.put(newMarkingInputPlace.keySet().iterator().next(), newMarkingInputPlace.values().iterator().next());
                                            tUpdateFrame.put(newMarkingInputPlace.keySet().iterator().next(), newMarkingInputPlace.values().iterator().next());
                                        }
                                        if(newMarkingOutputPlace.keySet().iterator().next().equals(target.keySet().iterator().next())){
                                           transition.setUsed();
                                                if(secondPart == false && (!transition.equals(ConstraintFrame.chooseTransition)) ){
                                                    String compare = ConstraintFrame.getSelectedTargetNode().substring(0, ConstraintFrame.getSelectedTargetNode().indexOf("="));
                                                    if((updateNodeOut.keySet().iterator().next().toString().equals(compare))){
                                                        return "Aborted";
                                                       }
                                                   }
                                                if(transition.getUsed() == true && transition.getActive() ==  true ){
                                                    tBacktrack.add(transition);
                                                    edge.add(new ReachabilityEdge(workingNode.getPrev(), workingNode, transition));
                                                    if(secondPart == true && (!transition.equals(ConstraintFrame.chooseTransition))){
                                                        return "Aborted";
                                                }
                                                   
                                                }
                                                 if(secondPart == false && (!transition.equals(ConstraintFrame.chooseTransition)) ){
                                                     String compare = ConstraintFrame.getSelectedTargetNode().substring(0, ConstraintFrame.getSelectedTargetNode().indexOf("="));
                                                     if((updateNodeOut.keySet().iterator().next().toString().equals(compare))){
                                                           return "Aborted";
                                                       }
                                                   }
                                                

                                                return "Success";
                                        }
                                     
                                        
                                        
                                    }//TEST
                                 
                                 
                                    
                                        if(!start.equals(target)){
                                            if(updateNodeOut.keySet().equals(target.keySet())){
                                                transition.setUsed();
                                                if(transition.getUsed() == true && transition.getActive() ==  true ){
                                                    tBacktrack.add(transition);
                                                    edge.add(new ReachabilityEdge(workingNode.getPrev(), workingNode, transition));
                                                }
                                                
                                                tUpdateFrame.putAll(updatedMarking);

                                                tUsedTransition = tBacktrack;
                                                allUsedTransitions.add(tBacktrack);
                                                allVisitedNodes.add(updatedMarking);
                                                count = counter;
                                                return "Success";
                                            }
                                        }
                               }
                               if(start.keySet().iterator().next().equals(target.keySet().iterator().next())&& workingNode.getPrev().getSecondVisit()== true && updateNodeOut.keySet().iterator().next().equals(target.keySet().iterator().next())){
                                   nodeList.addLast(workingNode);
                                   tUpdateFrame.putAll(updatedMarking);
                                   transition.setUsed();
                                   count = counter;
                                   allUsedTransitions.add(tBacktrack);
                                   allVisitedNodes.add(updatedMarking); 
                                  return "Success";
                               }
                           
                            }
                       }
                       if(start.keySet().equals(target.keySet()) && workingNode.getSecondVisit()==false){
                           workingNode.setSecondVisit();
                           nodeList.addLast(workingNode);
                        }
                       workingNode.setVisited();
                       transition.setUsed();
                       
                       if(transition.getUsed() == true && transition.getActive() == true){
                           tBacktrack.add(transition);
                           edge.add(new ReachabilityEdge(workingNode.getPrev(), workingNode, transition));
                       }
                       HashSet<Transition> activeTransitions = pf.computeActiveTransitions(updatedMarking);
                       enabledTransitions = activeTransitions;
                   }
                   
                   if(transition.outputs().isEmpty() && !transition.inputs().isEmpty()){
                       for(Place p : transition.inputs()){
                           HashMap<Place, Long> updateNode = new HashMap<>();
                           for(Map.Entry<Place, Long> entry : updateNode.entrySet()){
                               if(p.equals(entry.getKey())){
                                   updateNode.put(entry.getKey(), entry.getValue());
                                   HashMap<Place, Long> newMarkingInputPlace = null;//pf.computeSingleMarking(updateNode, transition, tUpdateFrame).getFirst();
                                   updatedMarking.putAll(newMarkingInputPlace);
                                   tUpdateFrame.putAll(newMarkingInputPlace);
                               }
                           }
                       }
                       transition.setUsed();
                   }
               }
           }
           visitedNodes.putAll(workingNode.getMarking());
           nodeList.get(0).setVisited();
           vertices.add(workingNode);
           workingNode.setVisited();
           nodeList.remove(0);
           
           int usedTCounter = 0;
           for(Transition k : enabledTransitions){
               if(k.getUsed()==true && k.getActive()== true){
                   usedTCounter += 1;
                   if(usedTCounter == enabledTransitions.size()){
                       tUpdateFrame.putAll(updatedMarking);
                       tUsedTransition = tBacktrack;
                       allUsedTransitions.add(tBacktrack);
                       allVisitedNodes.add(updatedMarking);
                       count = counter;
                       return "Failure";
                   }
               }
           }
           if(workingNode.getMarking().keySet().iterator().next().equals(target.keySet().iterator().next()) && workingNode.getVisited() == false){
               allUsedTransitions.add(tBacktrack);
               allVisitedNodes.add(updatedMarking);
               count = counter;
               return "Failure";
           }
       
       }
       return null;
   }

   

    

    
   
}
