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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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
    private Set<Transition> transitionsToFire;
    /**
     * Thread which executes simulation.
     */
    private SimulationSwingWorker simSwingWorker;
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SynchronousTokenSim.class);
    //END VARIABLES DECLARATION

    //BEGIN INNER CLASSES
    /**
     * Thread which performs the simulation sequence.
     */
    private class SimulationSwingWorker extends SwingWorker {

        /**
         * Number of steps this thread should perform.
         */
        private int stepsLeft;
        /**
         * Indicates whether the simulation is still running. Can be set to
         * "false" upon termination call.
         */
        private boolean isRunning;

        public SimulationSwingWorker(int nrOfStepsN) {
            this.stepsLeft = nrOfStepsN;
        }

        @Override
        protected Void doInBackground() {
            //time delay between single firings
            int timeDelay = (Integer) tokenSim.preferences.get("Time delay");
            //how often visual output will be updated
            final int updateInterval = (Integer) tokenSim.preferences.get("Update interval");
            tsPanel.progressBar.setMaximum(stepsLeft);
            /*
             * Set the running state of this runnable to "true".
             */
            this.isRunning = true;
            /*
             * Perform firing until aborted.
             */
            while (isRunning) {
                try {
                    transitionsToFire = getTransitionsToFire();
                    /*
                     * If no transitions are active, abort simulation.
                     */
                    if (transitionsToFire.isEmpty() || this.isCancelled()) {
                        LOGGER.info("No active Transitions found, simulation stopping");
                        this.isRunning = false;
                        break;
                    }
                    /*
                     * Perform firing of transitions.
                     */
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            LOGGER.info("Simulate the found active Transitions synchronously ");
                            tokenSim.fireTransitions(transitionsToFire.toArray(new Transition[transitionsToFire.size()]));
                            //If updateInterval is positive, check whether visual output must be updated in current step.
                            if (updateInterval > 0) {
                                if (tokenSim.getSimulatedSteps() % updateInterval == 0) {
                                    tokenSim.updateVisualOutput();
                                }
                            }
                        }
                    });

                    /*
                     * If continuous mode is not selected, reduce the number of steps left by one.
                     */
                    if (!tsPanel.continuousModeCheckBox.isSelected()) {
                        tsPanel.progressBar.setValue(tsPanel.progressBar.getMaximum() - --stepsLeft);
                        /*
                         * Abort simulation if no more steps are left.
                         */
                        if (stepsLeft <= 0) {
                            this.isRunning = false;
                        }
                    }
                    Thread.sleep(timeDelay);
                } catch (InterruptedException | InvocationTargetException ex) {
                    LOGGER.error("Interrupted simulation or invalid target invocated while calculating synchronous simulations", ex);
                }
            }
            return null;
        }

        @Override
        protected void done() {
            /*
             * Reset the progress bar.
             */
            LOGGER.info("Synchronous simulation is done, resetting the progress bar and unlocking GUI");
            tsPanel.progressBar.setMaximum(0);
            tsPanel.progressBar.setValue(0);

            //unlock GUI
            tsPanel.fireTransitionsButton.setText(TokenSimulator.strings.get("ATSFireTransitionsB"));
            tsPanel.fireTransitionsButton.setToolTipText(TokenSimulator.strings.get("ATSFireTransitionsBT"));

            if (!tsPanel.continuousModeCheckBox.isSelected()) {
                tsPanel.stepField.setEnabled(true);
            }
            tsPanel.continuousModeCheckBox.setEnabled(true);
            tokenSim.lockGUI(false);
            tokenSim.flushLog();
        }

        /**
         * Signal to stop simulation.
         */
        public void stopSequence() {
            this.isRunning = false;
            //update visual output
            tokenSim.updateVisualOutput();
        }
    }
    //END INNER CLASSES

    //BEGIN CONSTRUCTORS
    public SynchronousTokenSim(TokenSimulator tsN) {
        super(tsN);
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
        this.tokenSim.preferences.put("Time delay", 0);
        this.tokenSim.preferences.put("Update interval", 1);
        this.tokenSim.preferences.put("Fire at once", 100);
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
        this.tokenSim.lockGUI(true);

        //try to parse number of steps to perform from stepField. If no integer is entered, create a warning popup and do nothing
        try {
            //number of steps that will be performed
            int steps = Integer.parseInt(tsPanel.stepField.getText());
            if (steps < 1) {
                steps = 1;
                tsPanel.stepField.setText("1");
            }
            //Create new thread that will perform all firing steps.
            simSwingWorker = new SimulationSwingWorker(steps);
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
            this.tokenSim.preferences.put("Time delay", timeDelay);
        }
        /*
         * Update update interval
         */
        int updateInterval = Integer.parseInt(this.prefPanel.updateIntervalFormattedTextField.getText());
        if (updateInterval >= 0) {
            this.tokenSim.preferences.put("Update interval", updateInterval);
        }
        /*
         * Update how many transition to fire at once.
         */
        this.tokenSim.preferences.put("Fire at once", Integer.parseInt((String) this.prefPanel.fireAtOnceComboBox.getSelectedItem()));
    }

    @Override
    protected void loadPreferences() {
        this.prefPanel.timeDelayJFormattedTextField.setText(((Integer) this.tokenSim.preferences.get("Time delay")).toString());
        this.prefPanel.updateIntervalFormattedTextField.setText(((Integer) this.tokenSim.preferences.get("Update interval")).toString());
        this.prefPanel.fireAtOnceComboBox.setSelectedItem((String) this.tokenSim.preferences.get("Fire at once").toString());
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
        this.tokenSim.tokenSimPanel.saveSetupButton.setEnabled(false);
        this.tokenSim.tokenSimPanel.loadSetupButton.setEnabled(false);
        tokenSim.lockGUI(true);
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
    private Set<Transition> getTransitionsToFire() {
        LOGGER.debug("Calculating the Transitions that shall fire out of the active Transitions");
        /*
        this ArrayList saves all active transitions that can be fired synchron,
        i.e. no firing of a transition of this list can disable another transition in the list.
         */
        Set<Transition> outSet = new HashSet<>();
        ArrayList<Transition> activeTransitions = new ArrayList<>(tokenSim.getActiveTransitions());
        /*
         * compute how many transitions can be shoot at once, depending on percentage set
         */
        int maxTransitions = (int) Math.round((activeTransitions.size() * 0.01 * (int) this.tokenSim.preferences.get("Fire at once")));
        /*
         * for every transition in activeTransitions, check whether it shares any pre-place with other transitions
         */
        while (!activeTransitions.isEmpty() && (outSet.size() < maxTransitions)) {
            //randomly choose a transition from activeTransitions list.
            int i = random.nextInt(activeTransitions.size());
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
                            tokens -= this.petriNet.getArc(p, placePosttransition).weight();
                        }
                    }
                    /*
                    Check if both transitions can fire.
                     */
                    if ((tokens - this.petriNet.getArc(p, t).weight()) >= this.petriNet.getArc(p, concurrentT).weight()) {
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
                        isActive = random.nextBoolean();
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
