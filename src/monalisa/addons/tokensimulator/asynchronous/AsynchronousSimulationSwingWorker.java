/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.asynchronous;

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
public class AsynchronousSimulationSwingWorker extends SwingWorker {

    private final TokenSimulator ts;
    private final AsynchronousTokenSim async;
    private final AsynchronousTokenSimPanel tsPanel;
    private final Logger LOGGER = LogManager.getLogger(AsynchronousSimulationSwingWorker.class);

    /**
     * Number of steps this thread should perform.
     */
    private int stepsLeft;
    /**
     * Indicates whether the simulation is still running. Can be set to "false"
     * upon termination call.
     */
    private boolean isRunning;
    
    private Set<Transition> transitionsToFire;


    public AsynchronousSimulationSwingWorker(TokenSimulator ts, AsynchronousTokenSim sync, AsynchronousTokenSimPanel tsPanel, int nrOfStepsN) {
        this.ts = ts;
        this.async = sync;
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
        Set<Transition> activeTransitions;

        /*
         * Perform firing until aborted.
         */
        while (isRunning) {
            try {
                activeTransitions = ts.getActiveTransitions();
                /*
                 * If no transitions are active, abort simulation.
                 */
                if (activeTransitions.isEmpty() || this.isCancelled()) {
                    LOGGER.info("Simulation stopped since no more transitions are active");
                    this.isRunning = false;
                    break;
                }
                /*
                 * Perform firing of transitions.
                 */
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        ts.fireTransitions(async.getTransitionToFire());
                        //If updateInterval is positive, check whether visual output must be updated in current step.
                        if (updateInterval > 0) {
                            if (ts.getSimulatedSteps() % updateInterval == 0) {
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
                        this.isRunning = false;
                    }
                }
                Thread.sleep(timeDelay);
            } catch (InterruptedException | InvocationTargetException ex) {
                LOGGER.error("Background process got interrupted or broke because of an invalid target", ex);
            }
        }
        return null;
    }

    @Override
    protected void done() {
        /*
         * Reset the progress bar.
         */
        LOGGER.info("Asynchronous simulation is done, resetting the progress bar and unlocking GUI");        
        tsPanel.getProgressBar().setMaximum(0);
        tsPanel.getProgressBar().setValue(0);

        //unlock GUI
        tsPanel.unlock();
        ts.lockGUI(false);
        ts.flushLog();
        LOGGER.info("Successfully unlocked GUI after asynchronous simulation.");
    }

    /**
     * Signal to stop simulation.
     */
    public void stopSequence() {
        this.isRunning = false;
        //update visual output
        ts.updateVisualOutput();
    }
}
