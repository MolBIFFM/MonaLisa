/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.stochastic;

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
public class StochasticSimulationSwingWorker extends SwingWorker {

    private final TokenSimulator ts;
    private final StochasticTokenSim stoc;
    private final StochasticTokenSimPanel tsPanel;
    private final Logger LOGGER = LogManager.getLogger(StochasticSimulationSwingWorker.class);
    /**
     * Number of steps this thread should perform.
     */
    private int stepsLeft;
    /**
     * Indicates whether the simulation is still running. Can be set to "false"
     * upon termination call.
     */
    private boolean isRunning;

    public StochasticSimulationSwingWorker(TokenSimulator ts, StochasticTokenSim stoc, StochasticTokenSimPanel tsPanel, int nrOfStepsN) {
        this.ts = ts;
        this.stoc = stoc;
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
                    LOGGER.debug("No more active transitions, therefore stopping the stochastic token simulation");
                    this.isRunning = false;
                    break;
                }
                /*
                    * Perform firing of transitions.
                 */
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        ts.fireTransitions(stoc.getTransitionToFire());
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
                LOGGER.error("Simulation interupted or InvocationTargetException", ex);
            }
        }
        return null;
    }

    @Override
    protected void done() {
        /*
            * Reset the progress bar.
         */
        tsPanel.getProgressBar().setMaximum(0);
        tsPanel.getProgressBar().setValue(0);

        //unlock GUI
        tsPanel.unlock();
        ts.lockGUI(false);
        ts.flushLog();
    }

    /**
     * Signal to stop simulation.
     */
    public void stopSequence() {
        LOGGER.debug("Stopping the sequence and updating the visual output");
        this.isRunning = false;
        //update visual output
        ts.updateVisualOutput();
    }
}
