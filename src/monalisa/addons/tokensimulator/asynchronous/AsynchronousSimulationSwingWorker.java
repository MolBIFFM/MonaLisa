/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.asynchronous;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import javax.swing.SwingUtilities;
import monalisa.addons.tokensimulator.AbstractSimulationSwingWorker;
import monalisa.addons.tokensimulator.SimulationManager;
import monalisa.addons.tokensimulator.listeners.SimulationEvent;
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Thread which performs the simulation sequence for asynchronous simulation.
 */
public class AsynchronousSimulationSwingWorker extends AbstractSimulationSwingWorker {

    private final SimulationManager simulationMan;
    private final AsynchronousTokenSim async;
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

    public AsynchronousSimulationSwingWorker(SimulationManager simulationManager, AsynchronousTokenSim sync, boolean cont, int nrOfStepsN) {
        this.simulationMan = simulationManager;
        this.async = sync;
        this.continuous = cont;
        this.stepsLeft = nrOfStepsN;
    }

    @Override
    protected Void doInBackground() {
        //time delay between single firings
        int timeDelay = (Integer) simulationMan.getPreferences().get("Time delay");
        //how often visual output will be updated
        final int updateInterval = (Integer) simulationMan.getPreferences().get("Update interval");
        fireSimulationEvent(SimulationEvent.INIT, stepsLeft); // ProgressBar init
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
                activeTransitions = simulationMan.getActiveTransitions();
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
                        simulationMan.fireTransitions(async.getTransitionToFire());
                        //If updateInterval is positive, check whether visual output must be updated in current step.
                        if (updateInterval > 0) {
                            if (simulationMan.getSimulatedSteps() % updateInterval == 0) {
                                fireSimulationEvent(SimulationEvent.UPDATE_VISUAL, -1); // Update visuals
                            }
                        }
                    }
                });

                /*
                 * If continuous mode is not selected, reduce the number of steps left by one.
                 */
                if (!continuous) { // Actually need to pass this
                    --stepsLeft;
                    fireSimulationEvent(SimulationEvent.UPDATE_PROGRESS, stepsLeft); // Update ProgressBar
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
        fireSimulationEvent(SimulationEvent.DONE, -1); // Done
        simulationMan.lockGUI(false);
        simulationMan.flushLog();
        LOGGER.info("Successfully unlocked GUI after asynchronous simulation.");
    }

    /**
     * Signal to stop simulation.
     */
    public void stopSequence() {
        this.isRunning = false;
        fireSimulationEvent(SimulationEvent.STOPPED, -1); // Update visuals
    }
}
