/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import java.io.File;
import java.io.FileNotFoundException;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.util.HighQualityRandom;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * The implementation of this class defines the rules and performs the
 * simulation.
 *
 * @author Pavel Balazki
 */
public abstract class AbstractTokenSim {

    //BEGIN VARIABLES DECLARATION
    /**
     * Stores all transitions that should be checked for their active state. In
     * the default implementation of computing the active transitions, all
     * transitions in this list will be checked.
     */
    protected Set<Transition> transitionsToCheck;
    protected PetriNetFacade petriNet;
    protected SimulationManager simulationMan;
    /**
     * Random number generator for simulation tasks.
     */
    protected HighQualityRandom random = new HighQualityRandom();
    /**
     * Number of steps simulated so far.
     */
    protected int stepsSimulated = 0;
    private static final Logger LOGGER = LogManager.getLogger(AbstractTokenSim.class);

    //END VARIABLES DECLARATION
    //BEGIN CONSTRUCTORS
    /**
     * Prevent using non-parameterized constructor.
     */
    private AbstractTokenSim() {

    }

    /**
     * New instance of AbstractTokenSim gets the TokenSimulator-object and uses
     * its VisualizationViewer and petriNet.
     *
     * @param simulationMan
     */
    public AbstractTokenSim(SimulationManager simulationMan) {
        this.simulationMan = simulationMan;
        this.petriNet = simulationMan.getPetriNet();
        this.transitionsToCheck = new HashSet<>();

        init();
    }
    //END CONSTRUCTORS    

    //BEGIN ABSTRACT METHODS
    /**
     * This method is always called from constructor; may be empty. Use it to
     * perform some custom initialization.
     */
    protected abstract void init();

    /**
     * Update preferences after they were changed in preferencesPanel and
     * save-button was pressed.
     */
    protected abstract void updatePreferences();

    /**
     * Load current settings to the Preferences-Panel.
     */
    protected abstract void loadPreferences();

    /**
     * AbstractTokenSim object receives the message that simulation mode has
     * been started. Variables can be initialized or controls can be set active.
     */
    protected abstract void startSim();

    /**
     * AbstractTokenSim object receives the message that simulation mode has
     * been stopped. Controls can be set inactive.
     */
    protected abstract void endSim();

    /**
     * This method chooses which transition will be fired next.
     *
     * @return Transition which will be fired in the next step.
     */
    public abstract Transition getTransitionToFire();

    /**
     * Saves simulation setup, including marking, firing rates, constant-flags
     * for places etc to a XML-file.
     */
    protected abstract void exportSetup(File outfile) throws ParserConfigurationException, TransformerException;

    /**
     * Loads simulation setup, including marking, firing rates, constant-flags
     * for places etc from a XML-file.
     */
    protected abstract void importSetup(File inFile) throws FileNotFoundException, XMLStreamException;

    /**
     * Get the time which was simulated.
     *
     * @return In timed PNs can be seconds, in non-timed PNs number of simulated
     * steps.
     */
    public abstract double getSimulatedTime();
    //END ABSTRACT METHODS

    /**
     * This method implements the determination of all transitions that can fire
     * (i.e. active and not forbidden). It takes marking-map from TokenSimulator
     * tokenSim and computes the states of transitions based on active
     * transitions rules.
     *
     * This method can be overridden to provide alternative active states
     * descriptions. Still, it should use the checkTransitions-ArrayList. Each
     * time a token number for a place changes, all post-transitions of this
     * place are added to checkTransitions.
     */
    public void computeActiveTransitions() {
        /*
         * Iterate through all transitions that should be checked.
         */
        LOGGER.info("Checking for all transitions if they are fireable");
        for (Transition transition : this.transitionsToCheck) {
            /*
             * Assume that the transition is active.
             */
            boolean active = true;
            /*
             * Iterate through the pre-places of current transition.
             */
            for (Place place : transition.inputs()) {
                /*
                 * If any pre-palce of the transition has less tokens that the weight of the arc between the pre-place and transition,
                 * the transition cannot be active and all other pre-places must not be checked.
                 */
                long tokens = this.getTokens(place.id());
                if (this.getPetriNet().getArc(place, transition).weight() > tokens) {
                    active = false;
                    break;
                }
            }
            if (active) {
                this.getSimulationMan().activeTransitions.add(transition);
            } else {
                //Ensure that previous active-marked transition is not active any more.
                this.getSimulationMan().activeTransitions.remove(transition);
            }
        }
        LOGGER.info("Found all active Transitions");
        LOGGER.debug("Checking for constant places in preposition to transition, to make sure these transitions are checked for in every step");
        /*
        After active transitions were computed, clear the transitionsToCheck-list. However, post-transitions of constant
        places must be retained as they should be checked every step.
         */
        this.transitionsToCheck.retainAll(this.getSimulationMan().constantPlacesPostTransitions);
    }

    /**
     * Add received transitions to the checkTransitions-list, so they can be
     * checked.
     *
     * @param transitions Transitions which pre-places marking was changed.
     */
    public void addTransitionsToCheck(Transition... transitions) {
        transitionsToCheck.addAll(Arrays.asList(transitions));
    }

    /**
     * Return the number of tokens on the place.
     *
     * @param id ID of the place.
     * @return Number of tokens. For constant places mathematical expression is
     * evaluated first, using current marking of non-constant places and current
     * simulated time.
     */
    public long getTokens(int id) {
        if (!petriNet.findPlace(id).isConstant()) {
            return this.getSimulationMan().getMarking().get(id);
        } else {
            Map<Integer, Double> markingDouble = new HashMap<>();
            for (Entry<Integer, Long> entr : getSimulationMan().getMarking().entrySet()) {
                markingDouble.put(entr.getKey(), entr.getValue().doubleValue());
            }
            MathematicalExpression mathExp = getSimulationMan().getMathematicalExpression(id);
            return Math.round(mathExp.evaluateML(markingDouble, this.getSimulatedTime()));
        }
    }

    /**
     * @return the random
     */
    public HighQualityRandom getRandom() {
        return random;
    }

    /**
     * set the random
     */
    public void setRandom(HighQualityRandom seeded) {
        this.random = seeded;
    }

    /**
     * @return the petriNet
     */
    public PetriNetFacade getPetriNet() {
        return petriNet;
    }

    /**
     * @return the simulationManager
     */
    public SimulationManager getSimulationMan() {
        return simulationMan;
    }
}
