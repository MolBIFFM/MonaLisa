/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

import java.util.ArrayList;
import monalisa.addons.reachability.algorithms.BestFirst;
import monalisa.addons.reachability.algorithms.AStar;
import monalisa.addons.reachability.algorithms.BreadthFirst;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import monalisa.addons.reachability.algorithms.AbstractReachabilityAlgorithm;
import monalisa.addons.reachability.algorithms.FullCoverability;
import monalisa.addons.reachability.algorithms.FullReachability;
import monalisa.data.pn.PInvariant;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.results.PInvariants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marcel Gehrmann Should extend Thread class for parallelization later
 * on
 * @author Kristin Haas
 */
public class Pathfinder {
    private final HashMap<Place, Long> marking; // Start marking
    private final HashMap<Place, Long> target; // Target marking
    private final PetriNetFacade pnf;
    private PetriNetFacade backup;
    private final String alg;
    private AbstractReachabilityAlgorithm algorithm;
    private Thread algoThread;
    private final static Logger LOGGER = LogManager.getLogger(Pathfinder.class);
    private final HashMap<Place, Long> capacities;
    private final boolean capacities_active;
    private final HashSet<Transition> transitions;
    private HashMap<Place, Long> eStart;
    private HashMap<Place, Long> eTarget;
    private HashSet<Transition> knockout;

    /**
     * Constructor used for algorithms without a heuristic.
     *
     * @param pnf PetriNetFacade for the net.
     * @param marking The run marking. This will be changed, so it should be a
     * newly created marking.
     * @param target The target marking for reachability algorithms.
     * @param capacities The maximum capacities of all places
     * @param alg The selected algorithm.
     */
    public Pathfinder(PetriNetFacade pnf, Map<Place, Long> marking, HashMap<Place, Long> target, HashMap<Place, Long> capacities, HashSet<Transition> knockouts, String alg) {
        LOGGER.info("Initializing pathfinder for reachability analysis without a heuristic.");
        this.pnf = pnf;
        this.marking = new HashMap<>();
        this.marking.putAll(marking);
        this.target = new HashMap<>();
        this.target.putAll(target);
        this.capacities = capacities;
        this.capacities_active = checkCapacityActive();
        this.transitions = new HashSet<>();
        this.transitions.addAll(pnf.transitions());
        this.transitions.removeAll(knockouts);
        this.alg = alg;
        initializeAlgorithm(alg, null);
        LOGGER.info("Successfully initialized pathfinder for reachability analysis without a heuristic.");
    }

    /**
     * Constructor used for algorithms that use a heuristic
     * @param pnf PetriNetFacade for the net.
     * @param marking The run marking. This will be changed, so it should be a
     * newly created marking.
     * @param target The target marking for reachability algorithms.
     * @param capacities The maximum capacities of all places
     * @param knockouts
     * @param alg The selected algorithm.
     * @param heuristic The selected heuristic.
     */
    public Pathfinder(PetriNetFacade pnf, Map<Place, Long> marking, HashMap<Place, Long> target, HashMap<Place, Long> capacities, HashSet<Transition> knockouts, String alg, String heuristic) {
        LOGGER.info("Initializing pathfinder for reachability analysis with a heuristic.");
        this.pnf = pnf;
        this.marking = new HashMap<>();
        this.marking.putAll(marking);
        this.target = new HashMap<>();
        this.target.putAll(target);
        this.capacities = capacities;
        this.capacities_active = checkCapacityActive();
        this.transitions = new HashSet<>();
        this.transitions.addAll(pnf.transitions());
        LOGGER.warn(this.transitions.toString());
        this.transitions.removeAll(knockouts);
        LOGGER.info("Knocked out transitions: " + knockouts);
        this.alg = alg;
        initializeAlgorithm(alg, heuristic);
        LOGGER.info("Successfully initialized pathfinder for reachability analysis with a heuristic.");        
    }

    public Pathfinder(HashMap<Place, Long> marking, HashMap<Place, Long> target, PetriNetFacade pnf, String alg, HashMap<Place, Long> capacities, boolean capacities_active, HashSet<Transition> transitions) {
        this.marking = marking;
        this.target = target;
        this.pnf = pnf;
        this.alg = alg;
        this.capacities = capacities;
        this.capacities_active = capacities_active;
        this.transitions = transitions;
    }
    
    /**
     * Pathfinder used by ConstrainFrame
     * @param pnf
     * @param marking
     * @param target
     * @param capacities
     * @param knockout
     * @param alg 
     * @param eStart
     * @param eTarget
     * @throws java.lang.InterruptedException
     */
    public Pathfinder(PetriNetFacade pnf, Map<Place, Long> marking, HashMap<Place, Long> target, HashMap<Place, Long> capacities,HashSet<Transition> knockout , String alg, HashMap<Place, Long> eStart, HashMap<Place,Long> eTarget) throws InterruptedException {
        LOGGER.info("Initializing pathfinder for reachability analysis with specific start and target.");
        this.pnf = pnf;
        this.backup = pnf;
        this.marking = new HashMap<>();
        this.marking.putAll(marking);
        this.target = new HashMap<>();
        this.target.putAll(target);
        this.capacities = capacities;
        this.capacities_active = checkCapacityActive();
        this.transitions = new HashSet<>();
        this.transitions.addAll(pnf.transitions());
        LOGGER.warn(this.transitions.toString());
        this.eStart = eStart;
        this.eTarget = eTarget;
        this.transitions.removeAll(knockout);
        this.alg = alg;
        initializeAlgorithmExplicit(alg, null);
        LOGGER.info("Successfully initialized pathfinder for reachability analysis.");        
    }
    
    /**
     * 
     * @return 
     */
    public PetriNetFacade getBackup(){
        return backup;
    }

    private void initializeAlgorithm(String alg, String heuristic) {
        LOGGER.info("Initializing algorithm: " + alg + ".");
        if (heuristic != null) {
            LOGGER.info("Initializing with heuristic: " + heuristic);
            if (alg == null) return;
            switch (alg) {
                case "A*":
                    this.algorithm = new AStar(this, pnf, marking, target, heuristic);
                    break;
                case "Best First Search":
                    this.algorithm = new BestFirst(this, marking, target, heuristic);
                    break;
            }
        } else {
            if (alg == null) return;
            switch (alg) {
                case "Breadth First Search":
                    this.algorithm = new BreadthFirst(this, marking, target);
                    break;
                // Move FullReach and FullCover into separate classes and treat like algorithms
                case "FullReach": {
                    this.algorithm = new FullReachability(this, marking, target);
                    break;
                }
                case "FullCover": {
                    this.algorithm = new FullCoverability(this, marking, target);
                    break;
                }
                default:
                    this.algorithm = null;
                    break;
            }
        }
        if (this.algorithm != null) {
            LOGGER.info("Successfully initialized algorithm: " + alg + ".");
        } else {
            LOGGER.info("Failed to initialize algorithm: " + alg + ".");
        }
    }

    /**
     * Initializes algorithm. Should be used when explicit start and target 
     * are needed.
     * @param alg
     * @param heuristic 
     */
    private void initializeAlgorithmExplicit(String alg, String heuristic ) throws InterruptedException {
        // Reset 
       
        LOGGER.info("Initializing algorithm: " + alg + ".");
        if (heuristic != null) {
            LOGGER.info("Initializing with heuristic: " + heuristic);
            if (alg == null) return;
            switch (alg) {
                case "A*":
                    this.algorithm = new AStar(this, pnf, marking, target, heuristic);
                    break;
                case "Best First Search":
                    this.algorithm = new BestFirst(this, marking, target, heuristic);
                    break;
            }
        } else {
            if (alg == null) return;
            switch (alg) {
                case "Breadth First Search":
                    this.algorithm = new BreadthFirst(this,marking, target);
                    break;
                // Move FullReach and FullCover into separate classes and treat like algorithms
                case "FullReach": {
                    this.algorithm = new FullReachability(this, marking, target);
                    break;
                }
                case "FullCover": {
                    this.algorithm = new FullCoverability(this, marking, target);
                    break;
                }
                default:
                    this.algorithm = null;
                    break;
            }
        }
        if (this.algorithm != null) {
            LOGGER.info("Successfully initialized algorithm: " + alg + ".");
        } else {
            LOGGER.info("Failed to initialize algorithm: " + alg + ".");
        }
    }
    
    /**
     * 
     * @return 
     */
    public HashSet<Transition> allTransitions(){
        return transitions;
    }
    
    /**
     * @param m
     * @return 
     */
    public HashSet<Transition> computeActiveTransitions(HashMap<Place, Long> m) {
        LOGGER.debug("Computing active transitions");  // debug
        HashSet<Transition> activeTransitions = new HashSet<>();
        for (Transition t : transitions) {
            t.setActive();
            for (Place p : t.inputs()) {
                if (m.containsKey(p)) {
                }
                if (m.get(p) != null&& m.get(p) < pnf.getArc(p, t).weight()) {
                    t.setNotActive();
                    break;
                        }
                    for(Map.Entry<Place, Long> entry : BreadthFirst.getUpdateFrame().entrySet()){
                        if(p.equals(entry.getKey())){
                            if (m.get(p) < pnf.getArc(p, t).weight()) {
                                t.setNotActive();

                                break;
                            }
                        }
                    }
            }
            if (t.getActive()==true) {
                activeTransitions.add(t);
            }
        }
        LOGGER.debug("Successfully computed active transitions.");  // debug
        return activeTransitions;
    }

    /**
     * Checks if transition is active
     * Creates list with active transitions
     * @param m
     * @return 
     */
     public HashSet<Transition> computeActive(HashMap<Place, Long> m) {
        LOGGER.debug("Computing active transitions");  // debug
        HashSet<Transition> activeTransitions = new HashSet<>();
        for (Transition t : transitions) {
            boolean active = true;
            for (Place p : t.inputs()) {
                if (m.containsKey(p)) {
                }
                if (m.get(p) < pnf.getArc(p, t).weight()) {
                    active = false;
                    break;
                }
            }
           if (active) {
                activeTransitions.add(t);
            }
        }
        
        LOGGER.debug("Successfully computed active transitions.");  // debug
        return activeTransitions;
    }

    public HashSet<Transition> removeOverCapacity (HashSet<Transition> activeTransitions, HashMap<Place, Long> m) {
        HashSet<Transition> toRemove = new HashSet<>();
        for (Transition t : activeTransitions) {
            for (Place p : t.outputs()) {
                if(m.get(p)!= null){
                    if (((m.get(p) + pnf.getArc(t, p).weight()) > capacities.get(p)) && capacities.get(p) != 0) {
                        toRemove.add(t);
                        break;
                    }
                }
            }
        }
        LOGGER.debug("Transitions removed for causing a place to go over capacity: " + toRemove.toString());
        activeTransitions.removeAll(toRemove);
        return activeTransitions;
    }
    
    /**
     * @param old
     * @param t
     * @return 
     */
    private HashMap<Place, Long> newInput = new HashMap<>();
    private HashMap<Place, Long> newOutput = new HashMap<>();
    public void computeSingleMarking(Transition t, HashMap<Place,Long> targetNode){
        LOGGER.debug("Computing new marking.");  // debug
        HashMap<Place, Long> newTarget = targetNode;
        
        System.out.println("Input: "+newInput);
        try {
        for(Place p : t.inputs()){
            
            long oldToken = BreadthFirst.getUpdateFrame().get(p);
            if(oldToken == 0){
                System.out.println("OLD: "+oldToken+" frame: "+BreadthFirst.getUpdateFrame());
            }
            else{
                newInput.put(p,  (oldToken - (pnf.getArc(p, t).weight())));
                long newToken = (oldToken - (pnf.getArc(p, t).weight()));
                BreadthFirst.putUpdateFrame(p, oldToken, newToken);
                BreadthFirst.putUpdateMarking(p, oldToken, newToken);
                HashMap<Place, Long> addToVisited = new HashMap<>();
                addToVisited.put(p, newToken);
                BreadthFirst.addToVisitedNodes(addToVisited);
                if(p.toString() == newTarget.keySet().iterator().next().toString()){
                    newTarget = eTarget;
                }
            }
        }
        
        } catch (NullPointerException e) {
   }
        try {
  
            for(Place p : t.outputs()){
                long oldToken = BreadthFirst.getUpdateFrame().get(p);
                newOutput.put(p, (oldToken +( pnf.getArc(t, p).weight())));
                long newToken = newOutput.put(p, (oldToken +( pnf.getArc(t, p).weight())));
                HashMap<Place, Long> addToVisited = new HashMap<>();
                addToVisited.put(p, newToken);
                BreadthFirst.addToVisitedNodes(addToVisited);
                BreadthFirst.putUpdateFrame(p, oldToken, newToken);
                BreadthFirst.putUpdateMarking(p, oldToken, newToken);
                System.out.println("FRAME: "+BreadthFirst.getUpdateFrame());
                if(p.toString()== newTarget.keySet().iterator().next().toString()){
                    BreadthFirst.setFoundTrue();
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Exception_Out: "+e);
        }
    }

    /**
     * Adjusts tokens 
     * @param old
     * @param t
     * @return 
     * 
     */
      public HashMap<Place, Long> computeMarking(HashMap<Place, Long> old, Transition t) {
        LOGGER.debug("Computing new marking.");  // debug
        HashMap<Place, Long> mNew = new HashMap<>();
        mNew.putAll(old);
        // deduct tokens from input places
        for (Place p : t.inputs()) {
            long oldToken = mNew.get(p);
            mNew.put(p, oldToken - pnf.getArc(p, t).weight());
        }
        // add tokens to output places
        for (Place p : t.outputs()) {
            long oldToken = mNew.get(p);
            mNew.put(p, oldToken + pnf.getArc(t, p).weight());
        }
        LOGGER.debug("Successfully computed new marking.");  // debug
        return mNew;
    }

    public HashMap<Place, Long> computeOmegaMarking(HashMap<Place, Long> old, Transition t) {
        LOGGER.debug("Computing new omega marking.");
        HashMap<Place, Long> mNew = new HashMap<>();
        mNew.putAll(old);
        // deduct tokens from input places
        for (Place p : t.inputs()) {
            long oldToken = old.get(p);
            if (oldToken != Long.MAX_VALUE) {
                mNew.put(p, oldToken - pnf.getArc(p, t).weight());
            }
        }
        // add tokens to output places
        for (Place p : t.outputs()) {
            long oldToken = old.get(p);
            if (oldToken != Long.MAX_VALUE) {
                mNew.put(p, oldToken + pnf.getArc(t, p).weight());
            }
        }
        LOGGER.debug("Successfully computed new omega marking.");
        return mNew;
    }

    public HashMap<Place, Long> omegaComputation(HashMap<Place, Long> oldMarking, HashMap<Place, Long> newMarking) {
        for (Place p : newMarking.keySet()) {
            Long newToken = newMarking.get(p) - oldMarking.get(p);
            if (newToken != 0) {
                newMarking.put(p, Long.MAX_VALUE);
            }
        }
        return newMarking;
    }

    public void run() {
        LOGGER.info("Running the algorithm: " + alg + ".");
        algoThread = new Thread() {
            @Override
            public void run() {
                algorithm.start();
                try {
                    algorithm.join();
                } catch (InterruptedException e) {
                    algorithm.interrupt();
                    LOGGER.warn("Interrupt caught.");
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Interrupt propagated.");
                    return;
                }
                LOGGER.info("Algorithm " + alg + " finished running.");
            }
        };
        algoThread.start();
    }
    
    public void runExplicit() {
        LOGGER.info("Running the algorithm: " + alg + ".");
        algoThread = new Thread() {
            
            public void runExplixit() {
                algorithm.start();
                try {
                    algorithm.join();
                } catch (InterruptedException e) {
                    algorithm.interrupt();
                    LOGGER.warn("Interrupt caught.");
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Interrupt propagated.");
                    return;
                }
                LOGGER.info("Algorithm " + alg + " finished running.");
            }
        };
        algoThread.start();
    }

    public void stopAlgorithm() {
        LOGGER.warn("Interrupting the reachability algorithm.");
        algoThread.interrupt();
    }

    public void addListenerToAlgorithm(ReachabilityListener listener) {
        algorithm.addListener(listener);
    }

    public boolean checkPIs(PInvariants pinvs, HashMap<Place, Long> start, HashMap<Place, Long> target) {
        LOGGER.info("Checking Place Invariants before starting reachability analysis.");
        for (Object pinv : pinvs.toArray()) {
            PInvariant pin = (PInvariant) pinv;
            int startSum = 0;
            int targetSum = 0;
            for (Place p : pin) {
                startSum += pin.factor(p) * start.get(p);
                targetSum += pin.factor(p) * target.get(p);
            }
            if (startSum != targetSum) {
                LOGGER.warn("Sums for start and target marking do not match.");
                return false;
            }
            LOGGER.warn(pin.places().toString());
            LOGGER.warn(pin.asVector().toString());
        }
        LOGGER.info("Sums for start and target marking match.");
        return true;
    }

    private boolean checkCapacityActive() {
        for (Place p : capacities.keySet()) {
            if (capacities.get(p) != 0) {
                return true;
            }
        }
        return false;
    }
}
