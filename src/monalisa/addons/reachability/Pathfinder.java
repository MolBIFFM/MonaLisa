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
 */
public class Pathfinder {

    private final HashMap<Place, Long> marking; // Start marking
    private final HashMap<Place, Long> target; // Target marking
    private final PetriNetFacade pnf;
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
    
    /**
     * Pathfinder used by ConstrainFrame
     * @param pnf
     * @param marking
     * @param target
     * @param capacities
     * @param knockouts
     * @param alg
     * @param heuristic 
     * @param eStart
     * @param eTarget
     * 
     */
    public Pathfinder(PetriNetFacade pnf, Map<Place, Long> marking, HashMap<Place, Long> target, HashMap<Place, Long> capacities,HashSet<Transition> knockout , String alg, HashMap<Place, Long> eStart, HashMap<Place,Long> eTarget) {
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
        this.eStart = eStart;
        this.eTarget = eTarget;
        this.transitions.removeAll(knockout);
        this.alg = alg;
        initializeAlgorithmExplicit(alg, null);
        LOGGER.info("Successfully initialized pathfinder for reachability analysis with a heuristic.");        
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
    
    private void initializeAlgorithmExplicit(String alg, String heuristic ) {
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
                    System.out.println("Pathfinder>BSF: "+eStart+" "+eTarget);
                    this.algorithm = new BreadthFirst(this,marking, target, eStart, eTarget);
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
     * Checks if transition is active
     * @param m
     * @return 
     */
    public HashSet<Transition> computeActive(HashMap<Place, Long> m) {
        // Problem because m is one place
        LOGGER.debug("Computing active transitions");  // debug
        HashSet<Transition> activeTransitions = new HashSet<>();
        for (Transition t : transitions) {
            boolean active = true;
            System.out.println("TRANSITIONS: "+t);
            for (Place p : t.inputs()) {
                System.out.println("PLACES: "+p.toString()+" m:"+m.toString()+" transition: "+t);
                if (m.containsKey(p)) {
                }
                // Check if input exists
               // if(m.get(p) != null){
                System.out.println("Transition active: "+t+" "+active+" "+m.get(p)+" "+m.keySet().toString());
                System.out.println("M(P): "+m.get(p)+" WEIGHT: "+pnf.getArc(p, t).weight());
                if (m.get(p) < pnf.getArc(p, t).weight()) {
                    active = false;
                    break;
               }
                if (active) {

                    System.out.println("Active transition: "+t+" adding: "+active);
                    activeTransitions.add(t);
                    System.out.println("ActiveList; "+activeTransitions);
                
            }
               // }
            }
            /**if (active) {
                
                System.out.println("Active transition: "+t+" adding: "+active);
                activeTransitions.add(t);
                System.out.println("ActiveList; "+activeTransitions);
                
            }*/
            System.out.println("LIST: "+activeTransitions.size()+" "+activeTransitions.iterator()+" "+activeTransitions);
        }
        if (capacities_active) {
            activeTransitions = removeOverCapacity(activeTransitions, m);
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

    public HashMap<Place, Long> computeMarking(HashMap<Place, Long> old, Transition t) {
        LOGGER.debug("Computing new marking.");  // debug
        HashMap<Place, Long> mNew = new HashMap<>();
        mNew.putAll(old);
        // deduct tokens from input places
        for (Place p : t.inputs()) {
            if(mNew.get(p) != null){
                long oldToken = mNew.get(p);
                mNew.put(p, oldToken - pnf.getArc(p, t).weight());
            }
        }
        // add tokens to output places
        for (Place p : t.outputs()) {
            if(mNew.get(p)!= null){
                long oldToken = mNew.get(p);
                mNew.put(p, oldToken + pnf.getArc(t, p).weight());
            }
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
