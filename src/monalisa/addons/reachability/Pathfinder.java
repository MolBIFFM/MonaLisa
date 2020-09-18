/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.reachability;

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
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
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

    /**
     * Constructor used for algorithms without a heuristic.
     *
     * @param pnf PetriNetFacade for the net.
     * @param marking The run marking. This will be changed, so it should be a
     * newly created marking.
     * @param target The target marking for reachability algorithms.
     * @param alg The selected algorithm.
     */
    public Pathfinder(PetriNetFacade pnf, Map<Place, Long> marking, HashMap<Place, Long> target, String alg) {
        LOGGER.info("Initializing pathfinder for reachability analysis.");
        this.pnf = pnf;
        this.marking = new HashMap<>();
        this.marking.putAll(marking);
        this.target = new HashMap<>();
        this.target.putAll(target);
        this.alg = alg;
        initializeAlgorithm(alg, null);
    }

    /**
     * Constructor used for algorithms that use a heuristic
     * @param pnf PetriNetFacade for the net.
     * @param marking The run marking. This will be changed, so it should be a
     * newly created marking.
     * @param target The target marking for reachability algorithms.
     * @param alg The selected algorithm.
     * @param heuristic The selected heuristic.
     */
    public Pathfinder(PetriNetFacade pnf, Map<Place, Long> marking, HashMap<Place, Long> target, String alg, String heuristic) {
        LOGGER.info("Initializing pathfinder for reachability analysis.");
        this.pnf = pnf;
        this.marking = new HashMap<>();
        this.marking.putAll(marking);
        this.target = new HashMap<>();
        this.target.putAll(target);
        this.alg = alg;
        initializeAlgorithm(alg, heuristic);
    }

    private void initializeAlgorithm(String alg, String heuristic) {
        LOGGER.info("Initializing algorithm: " + alg + ".");
        if (heuristic != null) {
            LOGGER.info("Initializing with heuristic: " + heuristic);
            switch (alg) {
                case "A*":
                    this.algorithm = new AStar(this, pnf, marking, target, heuristic);
                    break;
                case "Best First Search":
                    this.algorithm = new BestFirst(this, pnf, marking, target, heuristic);
                    break;
            }
        } else {
            switch (alg) {
                case "Breadth First Search":
                    this.algorithm = new BreadthFirst(this, pnf, marking, target);
                    break;
                // Move FullReach and FullCover into separate classes and treat like algorithms
                case "FullReach": {
                    this.algorithm = new FullReachability(this, pnf, marking, target);
                    break;
                }
                case "FullCover": {
                    this.algorithm = new FullCoverability(this, pnf, marking, target);
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

    public HashSet<Transition> computeActive(Collection<Transition> toCheck, HashMap<Place, Long> m) {
        LOGGER.debug("Computing active transitions.");  // debug
        HashSet<Transition> activeTransitions = new HashSet<>();
        for (Transition t : toCheck) {
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

    public HashMap<Place, Long> computeMarking(HashMap<Place, Long> old, Transition t) {
        LOGGER.debug("Computing new marking.");  // debug
        HashMap<Place, Long> mNew = new HashMap<>();
        mNew.putAll(old);
        // deduct tokens from input places
        for (Place p : t.inputs()) {
            long oldToken = old.get(p);
            mNew.put(p, oldToken - pnf.getArc(p, t).weight());
        }
        // add tokens to output places
        for (Place p : t.outputs()) {
            long oldToken = old.get(p);
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

    public void stopAlgorithm() {
        LOGGER.warn("Interrupting the reachability algorithm.");
        algoThread.interrupt();
    }

    public void addListenerToAlgorithm(ReachabilityListener listener) {
        algorithm.addListener(listener);
    }
}
