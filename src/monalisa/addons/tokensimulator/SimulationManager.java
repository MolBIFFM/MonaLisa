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

import monalisa.addons.tokensimulator.utils.Statistic;
import monalisa.addons.tokensimulator.utils.Snapshot;
import monalisa.addons.tokensimulator.utils.MathematicalExpression;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import monalisa.addons.tokensimulator.exceptions.PlaceConstantException;
import monalisa.addons.tokensimulator.exceptions.PlaceNonConstantException;
import monalisa.addons.tokensimulator.listeners.GuiEvent;
import monalisa.addons.tokensimulator.listeners.GuiListener;
import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import monalisa.resources.ResourceManager;
import monalisa.resources.StringResources;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Core class of the token simulator. An instance of this class is created on
 * NetViewer-start. This class handles the basic functions of a simulator
 * including GUI, firing of transitions, history etc. An implementation of
 * AbstractTokenSim provides the specific features such as calculating active
 * transitions and choosing a transition to fire.
 *
 * @author Pavel Balazki
 */
public class SimulationManager {

    //START VARIABLES DECLARATION
    //resources
    public static final ResourceManager resources = ResourceManager.instance();
    public static final StringResources strings = resources.getDefaultStrings();

    //communication with the rest of MonaLisa
    //current project
    //petri net of the project
    private PetriNetFacade petriNet;

    /*
     * When true, all GUI components should be disabled to prevent user input.
     */
    private boolean lockGUI;

    //Instance of the selected simulator mode. implements firing behavior.
    private AbstractTokenSim tokenSim;

    //simulation
    //Links token numbers to place-id's. Does not contain constant places.
    public Map<Integer, Long> marking;
    /*
     * Constant places have a MathematicalExpression defining the number of tokens.
     */
    private final Map<Integer, MathematicalExpression> constantPlaces = new HashMap<>();
    /**
     * Set of transitions which have constant places as pre-places. Updated
     * every time a place constant status changes.
     */
    protected final Set<Transition> constantPlacesPostTransitions = new HashSet<>();
    /**
     * Set of transitions which are active in current state.
     */
    protected Set<Transition> activeTransitions;
    //history
    //define how many steps are to be stored for the history.
    private static final int MAX_HISTORY = 100;
    //List of an array of Transitions. Each array saves transition that were fired in one step.
    public ArrayList<Transition[]> historyArrayList;
    //ListModel of the historyList; stores names of fired transitions for each step.
    public DefaultListModel<String> historyListModel;
    //saves the index of the last performed step in the historyList
    public int lastHistoryStep;

    //A snapshot of the state will be created after every saveStateSnapshotInterval steps are performed.
    private int snapshotsInterval;

    //statistic of the current state.
    public Statistic currStatistic;
    //saves how much steps were performed at current state.
    public int totalStepNr = 0;

    /**
     * A map of custom markings. User is able to save current token numbers on
     * the places in custom markings. The keys are the names of the marking
     * (string), the values are the markings. In contrast to the global
     * marking-map, which only stores token numbers of non-constant places, the
     * maps in this map store token numbers for all places.
     */
    public Map<String, Map<Integer, Long>> customMarkingsMap = new HashMap<>();

    //log file output
    //name of the log-file of current simulation.
    protected String logName;
    /*
    Writes simulation results into the log-file.
     */
    private PrintWriter logWriter = null;

    //START Preferences
    /**
     * Stores the preferences of simulation. Custom simulation mode should also
     * store its preferences here.
     */
    private HashMap<String, Object> preferences = new HashMap<>();

    private List<GuiListener> listeners = new ArrayList<>();

    private static final Logger LOGGER = LogManager.getLogger(SimulationManager.class);
    //END VARIABLES DECLARATION

    /**
     * Implements the firing of given transitions. All transitions will be fired
     * in proper sequence; every firing computes the new marking.
     *
     * @param transitions Transition to be fired
     * @param history Whether the fired step (can contain multiple transitions)
     * should be added to the history or not. If the step is fired by picking an
     * entry from the history or by activating stepForwardButton/stepBackButton,
     * this parameter should be set to false
     */
    public void fireTransitions(boolean history, Transition... transitions) {
        /*
        Ensure that die initial marking is in the plot series.
         */
 /*
        Add values to plot series if plotting is enabled
         */
        if (this.totalStepNr == 0 && (boolean) this.getPreferences().get("EnablePlotting")) {
            fireGuiUpdateCall(GuiEvent.UPDATE_PLOT);
        }
        LOGGER.info("Fire transitions that are supposed to fire");
        for (Transition transition : transitions) {
            //tells the currStatistic that transition was fired
            this.currStatistic.transitionFired(transition);

//            //animate tokens movement
//            if(this.tokenSimPanel.animateTokensCheckBox.isSelected()){
//                final NetViewerNode transitionNode = this.netViewer.getTransitionMap().get(transition.id());
//                final ArrayList<NetViewerNode> prePlaceNodes = new ArrayList<NetViewerNode>();
//                for (Place place : this.petriNet.getInputPlacesFor(transition)){
//                    prePlaceNodes.add(this.netViewer.getPlaceMap().get(place.id()));
//                }
//                final ArrayList<NetViewerNode> postPlaceNodes = new ArrayList<NetViewerNode>();
//                for (Place place : this.petriNet.getPlacesFor(transition)){
//                    postPlaceNodes.add(this.netViewer.getPlaceMap().get(place.id()));
//                }
//                this.moveTokens(transitionNode, prePlaceNodes.toArray(new NetViewerNode[0]), postPlaceNodes.toArray(new NetViewerNode[0]));
//            }
            //START computing new state
            //Saves the token number for the place before firing a transition.
            long newToken;
            //Substract tokens from all input places. If a place is constant, skip this step.
            LOGGER.debug("Subtract tokens from the input places to the currently processed transition");
            for (Place place : this.petriNet.getInputPlacesFor(transition)) {
                if (!place.isConstant()) {
                    //copmute tokens for preplace
                    newToken = this.marking.get(place.id()) - this.petriNet.getArc(place, transition).weight();
                    this.marking.put(place.id(), newToken);
                    //add all post-transitions of the place to checkTransitions
                    this.getTokenSim().addTransitionsToCheck(this.petriNet.getTransitionsFor(place).toArray(new Transition[0]));
                }
            }
            //add tokens to all output places
            LOGGER.debug("Add tokens to the output places from the currently processed transition");
            for (Place place : this.petriNet.getPlacesFor(transition)) {
                //compute tokens for preplace. If a place is constant, skip this step.
                if (!place.isConstant()) {
                    newToken = this.marking.get(place.id()) + this.petriNet.getArc(transition, place).weight();
                    this.marking.put(place.id(), newToken);
                    //add all post-transitions of the place to checkTransitions
                    this.getTokenSim().addTransitionsToCheck(this.petriNet.getTransitionsFor(place).toArray(new Transition[0]));
                }
            }
            //END computing new state
        }

        //compute active transitions after firing complete step
        LOGGER.debug("Compute active transitions after firing the transitions");
        this.getTokenSim().computeActiveTransitions();

        if (history) {
            this.addHistoryEntry(transitions);
        }

        this.totalStepNr++;
        this.currStatistic.incrementSteps();
        //check whether to save snapshot of current state
        try {
            int k = MAX_HISTORY;
            this.snapshotsInterval = (0 > k) ? 0 : k;
        } catch (NumberFormatException E) {
            LOGGER.error(E);
        }
        if (this.snapshotsInterval > 0) {
            if ((boolean) this.getPreferences().get("SaveSnapshots") && this.totalStepNr % this.snapshotsInterval == 0) {
                fireGuiUpdateCall(GuiEvent.SNAPSHOT);
            }
        }
        LOGGER.info("After all transitions fired writing the results into the 'log'file");
        /*
        Actualize the log file if loggin is enabled.
         */
        if ((boolean) this.getPreferences().get("LogEnabled")) {
            this.writeLog(transitions);
        }
        /*
        Add values to plot series if plotting is enabled
         */
        if ((boolean) this.getPreferences().get("EnablePlotting")) {
            fireGuiUpdateCall(GuiEvent.UPDATE_PLOT);
        }
    }

    /**
     * Fires given transitions backwards, i.e. for each transition, tokens will
     * be added to its pre-places and subtracted from the post-places.
     * Transitions will be fired from the last transition to first. This method
     * is used for navigation through the history.
     *
     * @param transitions
     */
    public void reverseFireTransitions(Transition... transitions) {
        LOGGER.info("Reverse Firing of Transitions for backtracking of Simulations");
        this.totalStepNr--;
        this.currStatistic.decrementSteps();
        for (Transition transition : transitions) {
            LOGGER.debug("Changing the current mode of Simulation to reverse");
            this.currStatistic.transitionReverseFired(transition);              //tells the currStatistic object that transition-firing was canceled
            long newToken;
            //add tokens to all input places
            LOGGER.debug("Add tokens on the output places to the currently processed transition");
            for (Place place : this.petriNet.getInputPlacesFor(transition)) {
                if (!place.isConstant()) {
                    newToken = this.marking.get(place.id()) + this.petriNet.getArc(place, transition).weight();
                    this.marking.put(place.id(), newToken);
                    //add all post-transitions of the place to checkTransitions
                    this.getTokenSim().addTransitionsToCheck(this.petriNet.getTransitionsFor(place).toArray(new Transition[0]));
                }
            }
            //remove tokens from all output places
            LOGGER.debug("Subtract tokens from the input places to the currently processed transition");
            for (Place place : this.petriNet.getPlacesFor(transition)) {
                if (!place.isConstant()) {
                    newToken = this.marking.get(place.id()) - this.petriNet.getArc(transition, place).weight();
                    this.marking.put(place.id(), newToken);
                    //add all post-transitions of the place to checkTransitions
                    this.getTokenSim().addTransitionsToCheck(this.petriNet.getTransitionsFor(place).toArray(new Transition[0]));
                }
            }
            //compute active transitions after firing complete step
            LOGGER.debug("Compute active transitions after firing one");
            this.getTokenSim().computeActiveTransitions();
        }
    }

    /**
     * Given transitions are considered to be fired in one step and will be
     * added to historyArrayList and historyListModel as one step
     *
     * @param transitions
     */
    private void addHistoryEntry(Transition... transitions) {
        LOGGER.info("Adding a new entry into the history of the simulation");
        /*
         * If the new step is not a part of the history, and the last fired step is not
         * the last in the historyArrayList, so perform the step and delete all history entries after.
         * I.e., if the history is interrupted with a new step, the newer history becomes not actual.
         */
        if (this.lastHistoryStep < this.historyArrayList.size() - 1) {
            this.historyListModel.removeRange(this.lastHistoryStep + 1, this.historyArrayList.size() - 1);
            while (this.lastHistoryStep < this.historyArrayList.size() - 1) {
                this.historyArrayList.remove(this.historyArrayList.size() - 1);
            }
        }

        //check whether the limit size of history is reached. If it is, remove the first history entry
        if (this.historyListModel.getSize() >= SimulationManager.MAX_HISTORY) {
            this.historyListModel.remove(0);
            this.historyArrayList.remove(0);
            this.lastHistoryStep--;
        }
        StringBuilder historyOutSB = new StringBuilder();
        Arrays.sort(transitions);
        for (Transition t : transitions) {
            historyOutSB.append(", ").append(t.toString());
        }
        //add the fired transition to the history list
        this.historyListModel.addElement(historyOutSB.substring(2));
        this.historyArrayList.add(transitions);
        this.historyArrayList.trimToSize();

        this.lastHistoryStep++;

        fireGuiUpdateCall(GuiEvent.HISTORY);
    }

    /**
     * Loads the given snapshot and makes it to current state
     *
     * @param snapshot
     */
    public void loadSnapshot(Snapshot snapshot) {
        LOGGER.debug("Loading an older snapshot of the simulation");
        this.totalStepNr = snapshot.getStepNr();

        //deep copy of historyListModel
        this.historyListModel.clear();
        for (String entryName : snapshot.getHistoryListModelNames()) {
            this.historyListModel.addElement(entryName);
        }

        //deep copy of historyArrayList
        this.historyArrayList.clear();
        for (Transition[] tt : snapshot.getHistoryArrayList()) {
            Transition[] temp = new Transition[tt.length];
            int i = 0;
            for (Transition t : tt) {
                temp[i++] = t;
            }
            this.historyArrayList.add(temp);
        }

        //Copy the marking of the snapshot.
        this.marking.putAll(snapshot.getMarking());
        //Copy values of constant places
        this.constantPlaces.putAll(snapshot.getConstantPlaces());
        //after loading a snapshot, active transitions must be calculated
        this.getTokenSim().addTransitionsToCheck(this.petriNet.transitions().toArray(new Transition[0]));
        this.getTokenSim().computeActiveTransitions();

        this.lastHistoryStep = this.historyArrayList.size() - 1;
    }

    /**
     * Write the simulation step to the log file.
     */
    private void writeLog(Transition... transitions) {
        LOGGER.info("Writing current simulation step into the log file");
        StringBuilder logSB = new StringBuilder();
        /*
        If the method is called for the first time, create new log writer.
         */
        if (this.logWriter == null) {
            /*
             * Create default directory for log-files
             */
            new File((String) this.getPreferences().get("LogPath")).mkdir();
            //create a name of the log-file based on current date
            Calendar cal = Calendar.getInstance();
            this.logName = "Sim_log_" + cal.getTime().toString().replaceAll(" ", "_");

            /*
            Write the header of the log.
             */
            try {
                this.logWriter = new PrintWriter(new BufferedWriter(
                        new FileWriter((String) this.getPreferences().get("LogPath") + File.separator + this.logName + ".csv")));
                logSB.append("PlaceName\t\t\t");
                for (Place place : this.petriNet.places()) {
                    logSB.append((String) place.getProperty("name")).append("\t");
                }
                logSB.append("\nPlaceID\t\t\t");
                for (Place place : this.petriNet.places()) {
                    logSB.append(place.id()).append("\t");
                }
                logSB.append("\n----------------\nStep\tTime\tFired transition(s)");
            } catch (IOException ex) {
                LOGGER.error("IOException while writing log header", ex);
            }
        }
        logSB.append("\n").append(this.totalStepNr).append("\t").append(this.getTokenSim().getSimulatedTime()).append("\t");
        //Append names of fired transitions
        for (Transition t : transitions) {
            logSB.append((String) t.getProperty("name")).append(";");
        }
        logSB.deleteCharAt(logSB.length() - 1);
        logSB.append("\t");
        for (Place place : this.petriNet.places()) {
            logSB.append(this.getTokenSim().getTokens(place.id())).append("\t");
        }
        logWriter.print(logSB);
    }

    /**
     * Forces the data in the log-output stream to be written to file.
     */
    public void flushLog() {
        if (logWriter != null) {
            logWriter.flush();
        }
    }

    /**
     * Switches from NetViewer-mode to Simulator-mode
     */
    protected void startSimulator() {
        LOGGER.info("Starting a token simulator");

        //load current tokens from Petri net
        for (Place p : this.petriNet.places()) {
            try {
                if (!p.isConstant()) {
                    this.setTokens(p.id(), this.petriNet.getTokens(p));
                }
            } catch (PlaceConstantException ex) {
                LOGGER.error("PlaceConstantException while loading current tokens from Petri net", ex);
            }
        }
    }

    /**
     * Returns the number of steps simulated so far.
     *
     * @return Number of simulated steps.
     */
    public int getSimulatedSteps() {
        return this.totalStepNr;
    }

    /**
     * Switches from Simulator-mode to NetViewer-mode
     */
    public void endSimulator() {
        LOGGER.info("Ending the current token simulator");
        if (this.getTokenSim() == null) {
            return;
        }

        /*
        Close writer.
         */
        try {
            if (logWriter != null) {
                this.logWriter.close();
            }
        } catch (Exception ex) {
            LOGGER.error("General Exception while trying to close the logWriter", ex);
        }
        this.logWriter = null;
        //clear plot
    }

    //START PUBLIC METHODS
    /**
     * The visual state of the simulator will be updated, i.e. all vertices and
     * edges are repainted, showing the actual marking. It should be called
     * every time the new state should be painted, e.g. after firing a
     * transition, a set of transitions of firing multiple transitions in one
     * step. Updating visual output slows the firing frequence down, so
     * sometimes it should be better not to update after every firing.
     */
    public void updateVisualOutput() {
        fireGuiUpdateCall(GuiEvent.UPDATE_VISUAL);
    }

    /**
     * Fires given transitions in one step and adds this step to the history
     *
     * @param transitions
     */
    public void fireTransitions(Transition... transitions) {
        fireTransitions(true, transitions);
    }

    /**
     * Enables to lock GUI elements such as history or snapshots list by calling
     * lockGUI(true). The GUI should be locked before performing a sequence of
     * firing to avoid firing sequence being disrupt by user input. After firing
     * task is complete the GUI should be unlocked by calling lockGUI(false)
     *
     * @param lock
     */
    public void lockGUI(Boolean lock) {
        this.lockGUI = lock;
        if (lock) {
            fireGuiUpdateCall(GuiEvent.LOCK);
        } else {
            fireGuiUpdateCall(GuiEvent.UNLOCK);
        }
    }

    /**
     * Assign the given token number to a non-constant place with given id
     *
     * @param id ID of a non-constant place.
     * @param tokens
     * @throws
     * monalisa.addons.tokensimulator.TokenSimulator.PlaceConstantException
     */
    public void setTokens(int id, long tokens) throws PlaceConstantException {
        /*
        Get the place with passed ID.
         */
        Place p = petriNet.findPlace(id);
        if (!petriNet.findPlace(id).isConstant()) {
            this.marking.put(id, tokens);
            //add all post-transitions of the place to checkTransitions and compute active transitions
            this.getTokenSim().addTransitionsToCheck(this.petriNet.getTransitionsFor(p).toArray(new Transition[0]));
            this.getTokenSim().computeActiveTransitions();
            fireGuiUpdateCall(GuiEvent.REPAINT);
        } //if the place is constant, trow an exception.
        else {
            throw new PlaceConstantException();
        }
    }

    /**
     * Assign the mathematical expression to a constant place with given id.
     * Usualy it should describe concentration in M.
     *
     * @param id
     * @param mathExp
     * @throws
     * monalisa.addons.tokensimulator.TokenSimulator.PlaceNonConstantException
     */
    public void setMathExpression(int id, MathematicalExpression mathExp) throws PlaceNonConstantException {
        /*
        Get the place with passed ID.
         */
        Place p = petriNet.findPlace(id);
        if (p.isConstant()) {
            this.constantPlaces.put(id, mathExp);
            //add all post-transitions of the place to checkTransitions and compute active transitions
            this.getTokenSim().addTransitionsToCheck(this.petriNet.getTransitionsFor(p).toArray(new Transition[0]));
            this.getTokenSim().computeActiveTransitions();
            fireGuiUpdateCall(GuiEvent.REPAINT);
        } //if the place is non-constant, trow an exception.
        else {
            throw new PlaceNonConstantException();
        }
    }

    /**
     * Return the mathematical expression which describes the number of tokens
     * on the place with id.
     *
     * @param id ID of the place.
     * @return If the place is constant, its MathematicalExpression.
     */
    public MathematicalExpression getMathematicalExpression(int id) {
        if (petriNet.findPlace(id).isConstant()) {
            return this.constantPlaces.get(id);
        }
        return null;
    }

    /**
     * Return the map with non-constant places and the number of tokens on them.
     * Does not contain constant places.
     *
     * @return Copy of the marking-Map.
     */
    public Map<Integer, Long> getMarking() {
        return new HashMap<>(marking);
    }

    /**
     * Get the map of constant places with their mathematical expressions of
     * token number.
     *
     * @return Copy of the constantPlaces-Map.
     */
    public Map<Integer, MathematicalExpression> getConstantPlaces() {
        return new HashMap(this.constantPlaces);
    }

    /**
     * Set the places status as constant or non-constant. If isConstant == true:
     * -sets the places property to constant. -removes the place from
     * marking-map. -adds the place to constantPlaces-map with the mathematical
     * expression build from the number of tokens of the place while it was
     * non-constant. If isConstant == false: -set the places property to
     * non-constant. -removes the place from the constantPlaces-map. -adds the
     * place to the marking-map and tries to evaluate the mathematical
     * expression without variables. This <b>will</b> fail if the expression has
     * variables. In this case, 0 will be set as the number of tokens for the
     * place.
     *
     * @param id ID of the place.
     * @param isConstant Whether the place is constant or not.
     */
    public void setPlaceConstant(int id, boolean isConstant) {
        //get the place.
        Place p = this.petriNet.findPlace(id);
        if (isConstant) {
            //if the place is already constant, do nothing.
            if (!p.isConstant()) {
                try {
                    p.setConstant(true);
                    long oldVal = this.marking.get(id);
                    this.marking.remove(id);
                    this.constantPlaces.put(id, new MathematicalExpression(String.valueOf(oldVal)));
                    /*
                    Add post-transitions of a constant place to a corresponding map.
                     */
                    for (Transition t : p.outputs()) {
                        this.constantPlacesPostTransitions.add(t);
                    }
                } catch (RuntimeException ex) {
                    LOGGER.error("Issue while setting place to constant: ", ex);
                }
            }
        } else {
            //if the place is already non-constant, do nothing.
            if (p.isConstant()) {
                long value = getTokenSim().getTokens(p.id());
                p.setConstant(false);
                this.constantPlaces.remove(id);
                /*
                Remove post-transitions of a constant place from the corresponding map.
                 */
                for (Transition t : p.outputs()) {
                    this.constantPlacesPostTransitions.remove(t);
                }
                this.marking.put(id, value);
            }
        }
    }

    /**
     *
     * @return PetriNetFacade of the current project
     */
    public PetriNetFacade getPetriNet() {
        return this.petriNet;
    }

    /**
     * A set with active transitions.
     *
     * @return Copy of activeTransitions - set of this TokenSimulator.
     */
    public Set<Transition> getActiveTransitions() {
        return new HashSet(this.activeTransitions);
    }

    /**
     *
     * @return statistic of the current state
     */
    public Statistic getStatistic() {
        return this.currStatistic;
    }

    public Map<String, Map<Integer, Long>> getCustomMarkingsMap() {
        return customMarkingsMap;
    }

    public void setCustomMarkingsMap(Map<String, Map<Integer, Long>> customMarkingsMap) {
        this.customMarkingsMap = customMarkingsMap;
    }

    /**
     * This method is called on initialization of NetViewer.
     *
     * @param netViewer
     * @param projectN
     */
    public void initTokenSimulator(PetriNetFacade petriNet) {
        //netViewer are initialized on creating TokenSimulator and will not be changed.
        this.petriNet = petriNet;
        this.lockGUI = false;
    }
    //END PUBLIC METHODS

    /**
     * @return the tokenSim
     */
    public AbstractTokenSim getTokenSim() {
        return tokenSim;
    }

    /**
     * @param tokenSim the tokenSim to set
     */
    public void setTokenSim(AbstractTokenSim tokenSim) {
        this.tokenSim = tokenSim;
    }

    /**
     * @return the preferences
     */
    public HashMap<String, Object> getPreferences() {
        return preferences;
    }

    public void addGuiListener(GuiListener gl) {
        if (!listeners.contains(gl)) {
            listeners.add(gl);
        }
    }

    public void removeGuiListener(GuiListener gl) {
        listeners.remove(gl);
    }

    public void fireGuiUpdateCall(String type) {
        GuiEvent event = new GuiEvent(this, type);
        for (GuiListener gl : listeners) {
            gl.guiUpdateCall(event);
        }
    }

    /**
     * @return the lockGUI
     */
    public boolean isLockGUI() {
        return lockGUI;
    }
}
