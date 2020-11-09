/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;
import monalisa.addons.tokensimulator.listeners.SimulationEvent;
import monalisa.addons.tokensimulator.listeners.SimulationListener;

/**
 * General base model for SimulationSwingWorkers. This class is used by
 * TokenSims to facilitate the actual simulation of the Petri net. Callbacks to
 * the UI are done by firing SimulationEvents.
 *
 * @author Marcel Gehrmann
 */
public abstract class AbstractSimulationSwingWorker extends SwingWorker {

    private final List<SimulationListener> simulationListeners = new ArrayList<>();
    protected boolean continuous;

    /**
     * Sets whether the simulation should be continuous or only for a number of
     * steps.
     *
     * @param continuous
     */
    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    /**
     * Adds a SimulationListener to the SimulationSwingWorker.
     *
     * @param sl
     */
    public synchronized void addSimulationListener(SimulationListener sl) {
        if (!simulationListeners.contains(sl)) {
            simulationListeners.add(sl);
        }
    }

    /**
     * Removes a SimulationListener from the SimulationSwingWorker.
     *
     * @param sl
     */
    public synchronized void removeSimulationListener(SimulationListener sl) {
        simulationListeners.remove(sl);
    }

    /**
     * Fires a SimulationEvent to all registered SimulationListeners.
     *
     * @param type type of event
     * @param value value, if needed for event, else -1
     */
    protected final void fireSimulationEvent(String type, int value) {
        List<SimulationListener> listeners = getSimulationListenersCopy(simulationListeners);
        SimulationEvent event = new SimulationEvent(this, type, value);
        for (SimulationListener sl : listeners) {
            sl.simulationUpdated(event);
        }
    }

    /**
     * Returns a copy of the SimulationListeners registered with the
     * SimulationSwingWorker.
     *
     * @param listeners
     * @return
     */
    private synchronized List<SimulationListener> getSimulationListenersCopy(
            List<SimulationListener> listeners) {
        return new ArrayList<>(listeners);
    }
}
