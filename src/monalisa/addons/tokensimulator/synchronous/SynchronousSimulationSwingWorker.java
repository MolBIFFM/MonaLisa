/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.synchronous;

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
 * Thread which performs the simulation sequence.
 */
public class SynchronousSimulationSwingWorker extends AbstractSimulationSwingWorker {

    private final SimulationManager simulationMan;
    private final SynchronousTokenSim syncTS;
    private final Logger LOGGER = LogManager.getLogger(SynchronousSimulationSwingWorker.class);
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

    public SynchronousSimulationSwingWorker(SimulationManager simulationManager, SynchronousTokenSim syncTS, boolean cont, int nrOfStepsN) {
        this.simulationMan = simulationManager;
        this.syncTS = syncTS;
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
        /*
        * Perform firing until aborted.
         */
        while (isRunning) {
            try {
                transitionsToFire = syncTS.getTransitionsToFire();
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
                        simulationMan.fireTransitions(transitionsToFire.toArray(new Transition[transitionsToFire.size()]));
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
                if (!continuous) {
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
        fireSimulationEvent(SimulationEvent.DONE, -1); // Done
        simulationMan.lockGUI(false);
        simulationMan.flushLog();
        LOGGER.info("Successfully unlocked GUI after synchronous simulation.");
    }

    /**
     * Signal to stop simulation.
     */
    public void stopSequence() {
        this.isRunning = false;
        fireSimulationEvent(SimulationEvent.STOPPED, -1); // Update visuals
    }
}
