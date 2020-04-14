/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.synchronous;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import monalisa.addons.tokensimulator.asynchronous.AsynchronousTokenSim;
import monalisa.addons.tokensimulator.TokenSimulator;
import monalisa.data.pn.Place;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Pavel Balazki.
 */
public class SynchronousTokenSim extends AsynchronousTokenSim {

    //BEGIN VARIABLES DECLARATION
    //GUI
    private SynchronousTokenSimPanel tsPanel;
    private SynchronousTokenSimPrefPanel prefPanel;
    //saves all transitions that will be fired in one step
    /**
     * Thread which executes simulation.
     */
    private SynchronousSimulationSwingWorker simSwingWorker;
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SynchronousTokenSim.class);
    //END VARIABLES DECLARATION

    //BEGIN CONSTRUCTORS
    public SynchronousTokenSim(TokenSimulator tsN) {
        super(tsN);
        init();
    }
    //END CONSTRUCTORS

    /**
     * Create GUI and etc, so classes that extend this class can use it too
     */
    @Override
    protected void init() {
        //START CREATING GUI ELEMENTS
        LOGGER.info("Initiating the GUI for the synchronous simulation");
        this.tsPanel = new SynchronousTokenSimPanel(this);
        this.prefPanel = new SynchronousTokenSimPrefPanel(this);
        this.tsPanel.simName.setText(TokenSimulator.strings.get("STSName"));
        //END CREATING GUI ELEMENTS
        this.getTokenSim().getPreferences().put("Time delay", 0);
        this.getTokenSim().getPreferences().put("Update interval", 1);
        this.getTokenSim().getPreferences().put("Fire at once", 100);
        //by default, all transitions will fire at once
        this.prefPanel.fireAtOnceComboBox.setSelectedItem(100);
    }

    /**
     * Start firing sequence.
     */
    @Override
    protected void startFiring() {
        //lock GUI
        LOGGER.info("Synchronous firing started");
        this.tsPanel.stepField.setEnabled(false);
        this.tsPanel.continuousModeCheckBox.setEnabled(false);
        this.getTokenSim().lockGUI(true);

        //try to parse number of steps to perform from stepField. If no integer is entered, create a warning popup and do nothing
        try {
            //number of steps that will be performed
            int steps = Integer.parseInt(tsPanel.stepField.getText());
            if (steps < 1) {
                steps = 1;
                tsPanel.stepField.setText("1");
            }
            //Create new thread that will perform all firing steps.
            simSwingWorker = new SynchronousSimulationSwingWorker(getTokenSim(), this, tsPanel, steps);
            simSwingWorker.execute();
        } catch (NumberFormatException nfe) {
            LOGGER.error("NumberFormatException found while firing in the synchronous Simulator");
            stopFiring();
            JOptionPane.showMessageDialog(null, TokenSimulator.strings.get("TSNumberFormatExceptionM"));
        }
    }

    /**
     * Stop actual firing sequence.
     */
    @Override
    protected void stopFiring() {
        LOGGER.info("Firing in the synchronous simulator stopped");
        if (this.simSwingWorker != null) {
            this.simSwingWorker.stopSequence();
        }
    }

    @Override
    protected JPanel getControlComponent() {
        return this.tsPanel;
    }

    @Override
    protected JPanel getPreferencesPanel() {
        return this.prefPanel;
    }

    @Override
    protected void updatePreferences() {
        /*
         * Update time delay.
         */
        int timeDelay = Integer.parseInt(this.prefPanel.timeDelayJFormattedTextField.getText());
        if (timeDelay >= 0) {
            this.getTokenSim().getPreferences().put("Time delay", timeDelay);
        }
        /*
         * Update update interval
         */
        int updateInterval = Integer.parseInt(this.prefPanel.updateIntervalFormattedTextField.getText());
        if (updateInterval >= 0) {
            this.getTokenSim().getPreferences().put("Update interval", updateInterval);
        }
        /*
         * Update how many transition to fire at once.
         */
        this.getTokenSim().getPreferences().put("Fire at once", Integer.parseInt((String) this.prefPanel.fireAtOnceComboBox.getSelectedItem()));
    }

    @Override
    protected void loadPreferences() {
        this.prefPanel.timeDelayJFormattedTextField.setText(((Integer) this.getTokenSim().getPreferences().get("Time delay")).toString());
        this.prefPanel.updateIntervalFormattedTextField.setText(((Integer) this.getTokenSim().getPreferences().get("Update interval")).toString());
        this.prefPanel.fireAtOnceComboBox.setSelectedItem((String) this.getTokenSim().getPreferences().get("Fire at once").toString());
    }

    @Override
    protected void startSim() {
        LOGGER.info("Starting synchronous Simulation");
        this.tsPanel.stepField.setEnabled(true);
        this.tsPanel.fireTransitionsButton.setEnabled(true);
        this.computeActiveTransitions();
    }

    @Override
    protected void endSim() {
        LOGGER.info("Ending synchronous Simulation");
        try {
            this.simSwingWorker.cancel(true);
        } catch (NullPointerException ex) {
            LOGGER.error("NullPointerException while trying to cancel the simSwingWorker in the synchronous simulation");
        }
        this.tsPanel.stepField.setEnabled(false);
        this.tsPanel.fireTransitionsButton.setEnabled(false);
        this.tsPanel.continuousModeCheckBox.setEnabled(false);
        this.getTokenSim().getTokenSimPanel().disableSetup();
        getTokenSim().lockGUI(true);
    }

    /**
     * Compute what transitions will fire in next step. This will check every
     * active transitions. Is a transitions is not concurrent with other
     * transitions, i.e. it does not share a pre-place with other transitions,
     * it will fire. If a transitions shares a pre-place with another
     * transition, and the token number on the pre-place is insufficient to fire
     * both concurrent transitions, decide randomly which transitions will fire.
     *
     * @return
     */
    public Set<Transition> getTransitionsToFire() {
        LOGGER.debug("Calculating the Transitions that shall fire out of the active Transitions");
        /*
        this ArrayList saves all active transitions that can be fired synchron,
        i.e. no firing of a transition of this list can disable another transition in the list.
         */
        Set<Transition> outSet = new HashSet<>();
        ArrayList<Transition> activeTransitions = new ArrayList<>(getTokenSim().getActiveTransitions());
        /*
         * compute how many transitions can be shoot at once, depending on percentage set
         */
        int maxTransitions = (int) Math.round((activeTransitions.size() * 0.01 * (int) this.getTokenSim().getPreferences().get("Fire at once")));
        /*
         * for every transition in activeTransitions, check whether it shares any pre-place with other transitions
         */
        while (!activeTransitions.isEmpty() && (outSet.size() < maxTransitions)) {
            //randomly choose a transition from activeTransitions list.
            int i = getRandom().nextInt(activeTransitions.size());
            Transition t = activeTransitions.remove(i);
            //Transition t is active and not chosen
            Boolean isActive = true;
            Boolean isChosen = false;

            //Get input places of the current transition t
            List<Place> inPlaces = t.inputs();

            //for each input place of the current transition, all active output transitions are concurrent
            for (Place p : inPlaces) {
                /*
                Get concurrent transitions of transition t.
                 */
                Set<Transition> concurrentTransitions = new HashSet<>(p.outputs());
                /*
                Remove all inactive transitions.
                 */
                concurrentTransitions.retainAll(activeTransitions);

                /*
                Iterator over the set of concurrentTransitions
                 */
                Iterator<Transition> it = concurrentTransitions.iterator();
                while (it.hasNext()) {
                    Transition concurrentT = it.next();
                    /*
                     * If pre-place p has enough tokens to fire both transitions t and concurrentT, remove concurrentT from
                     * concurrentTransitions. Add transition t to list of concurrent transitions of concurrentT for further checks
                     * 
                     */
 /*
                    Number of tokens on place p (for which t and concurrentT are concurrent).
                     */
                    long tokens = getTokens(p.id());
                    /*
                     * For every post-transition of the place p that is chosen to fire, reduce the amount of tokens of this place
                     * by the weight of the arc between the place p and the chosen transition
                     */
                    for (Transition placePosttransition : p.outputs()) {
                        if (outSet.contains(placePosttransition)) {
                            tokens -= this.getPetriNet().getArc(p, placePosttransition).weight();
                        }
                    }
                    /*
                    Check if both transitions can fire.
                     */
                    if ((tokens - this.getPetriNet().getArc(p, t).weight()) >= this.getPetriNet().getArc(p, concurrentT).weight()) {
                        it.remove();
                    }
                }
                /*
                if transition t is not concurrent with any other transition for this place, do nothing and check concurrency for other places. Otherwise
                decide whether transtion t will fire randomly.
                 */
                if (!concurrentTransitions.isEmpty()) {
                    /*
                    If the transition was already chosen to fire during checking concurrency
                    with previous places, remove all concurrent transitions from candidates to fire (activeTransitions).
                     */
                    if (isChosen) {
                        activeTransitions.removeAll(concurrentTransitions);
                    } else {
                        isActive = getRandom().nextBoolean();
                        //if the transition was chosen to be active, add it to transitionsToFire-list and remove all concurrent transitions
                        //from activeTransitions
                        if (isActive) {
                            isChosen = true;
                            outSet.add(t);
                            activeTransitions.removeAll(concurrentTransitions);
                        } //if the transition was chosen not to be active, remove it from activeTransitions and go on with next transition
                        else {
                            activeTransitions.remove(t);
                            break;
                        }
                    }
                }
            }
            if (isActive && !isChosen) {
                outSet.add(t);
            }
        }
        LOGGER.debug("Finished calculating the transitions to fire next in the synchronous simulation");
        return outSet;
    }
}
