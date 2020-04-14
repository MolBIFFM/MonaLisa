/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.gillespie;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import monalisa.addons.tokensimulator.TokenSimulator;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Thread which performs the simulation sequence.
 */
public class GillespieSimulationSwingWorker extends SwingWorker {

    /**
     * Number of steps this thread should perform.
     */
    private int stepsLeft;
    /**
     * Indicates whether the simulation is still running. Can be set to
     * "false" upon termination call.
     */
    private boolean isRunning;
    private final TokenSimulator ts;
    private final GillespieTokenSim gill;
    private final GillespieTokenSimPanel tsPanel;
    private static final Logger LOGGER = LogManager.getLogger(GillespieSimulationSwingWorker.class);

    public GillespieSimulationSwingWorker(TokenSimulator ts, GillespieTokenSim gill, GillespieTokenSimPanel tsPanel, int nrOfStepsN) {
        this.ts = ts;
        this.gill = gill;
        this.tsPanel = tsPanel;
        this.stepsLeft = nrOfStepsN;
    }

    @Override
    protected Void doInBackground() {
        //time delay between single firings
        int timeDelay = (Integer) ts.getPreferences().get("Time delay");
        //how often visual output will be updated
        final int updateInterval = (Integer) ts.getPreferences().get("Update interval");
        tsPanel.getProgressBar().setMaximum(stepsLeft);
        /*
         * Set the running state of this runnable to "true".
         */
        this.isRunning = true;

        //all active transitions that can be fired right now
        Set<Transition> activeTransitions = ts.getActiveTransitions();

        /*
         * Perform firing until aborted.
         */
        while (isRunning) {
            try {
                /*
                 * If no transitions are active, abort simulation.
                 */
                if (activeTransitions.isEmpty() || this.isCancelled()) {
                    this.isRunning = false;
                    LOGGER.debug("Gillespie Simulation stopped since no more transitions are active");
                    break;
                }
                /*
                 * Perform firing of transitions.
                 */
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        ts.fireTransitions(gill.getTransitionToFire());
                        //If updateInterval is positive, check whether visual output must be updated in current step.
                        if (updateInterval > 0) {
                            if (ts.getSimulatedSteps() % updateInterval == 0) {
                                LOGGER.debug("Visual output in gillespie simulation updated since updateinterval is positive");
                                ts.updateVisualOutput();
                            }
                        }
                    }
                });

                /*
                 * If continuous mode is not selected, reduce the number of steps left by one.
                 */
                if (!tsPanel.isContinuous()) {
                    tsPanel.getProgressBar().setValue(tsPanel.getProgressBar().getMaximum() - --stepsLeft);
                    /*
                     * Abort simulation if no more steps are left.
                     */
                    if (stepsLeft <= 0) {
                        LOGGER.debug("Gillespie simulation stopped, since there are no more steps left to do");
                        this.isRunning = false;
                    }
                }
                Thread.sleep(timeDelay);
            } catch (InterruptedException | InvocationTargetException ex) {
                LOGGER.error("Simulation that should have been stopped couldnt be found", ex);
            }
        }
        return null;
    }

    @Override
    protected void done() {
        /*
         * Reset the progress bar.
         */
        LOGGER.info("Gillespie simulation is done, setting everything back and unlocking GUI");
        tsPanel.getProgressBar().setMaximum(0);
        tsPanel.getProgressBar().setValue(0);

        //unlock GUI
        tsPanel.unlock();
        ts.lockGUI(false);
        ts.flushLog();
        tsPanel.simTimeLabel.setText("Simulated time: " + gill.getSimulatedTime());
    }

    /**
     * Signal to stop simulation.
     */
    public void stopSequence() {
        LOGGER.info("Gillespie simulation has been stopped");
        this.isRunning = false;
        //update visual output
        ts.updateVisualOutput();
    }
}
