/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.gillespie;

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
public class GillespieSimulationSwingWorker extends AbstractSimulationSwingWorker {

    /**
     * Number of steps this thread should perform.
     */
    private int stepsLeft;
    /**
     * Indicates whether the simulation is still running. Can be set to "false"
     * upon termination call.
     */
    private boolean isRunning;
    private final SimulationManager simulationMan;
    private final GillespieTokenSim gillTS;
    private static final Logger LOGGER = LogManager.getLogger(GillespieSimulationSwingWorker.class);

    public GillespieSimulationSwingWorker(SimulationManager ts, GillespieTokenSim gill, boolean cont, int nrOfStepsN) {
        this.simulationMan = ts;
        this.gillTS = gill;
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
        Set<Transition> activeTransitions = simulationMan.getActiveTransitions();

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
                        Transition toFire = gillTS.getTransitionToFire();
                        if (toFire == null) {
                            LOGGER.info("Stopping the gillespie simulation because the markingRatesSum is equal to zero and no further reaction can occur");
                            stopSequence();
                        } else {
                            simulationMan.fireTransitions(toFire);
                            //If updateInterval is positive, check whether visual output must be updated in current step.
                            if (updateInterval > 0) {
                                if (simulationMan.getSimulatedSteps() % updateInterval == 0) {
                                    fireSimulationEvent(SimulationEvent.UPDATE_VISUAL, -1); // Update visuals
                                }
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
        fireSimulationEvent(SimulationEvent.DONE, -1); // Done
        simulationMan.lockGUI(false);
        simulationMan.flushLog();
    }

    /**
     * Signal to stop simulation.
     */
    public void stopSequence() {
        LOGGER.info("Gillespie simulation has been stopped");
        this.isRunning = false;
        //update visual output
        fireSimulationEvent(SimulationEvent.STOPPED, -1); // Update visuals
    }
}
