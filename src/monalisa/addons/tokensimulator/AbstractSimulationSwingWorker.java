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
 *
 * @author Marcel
 */
public abstract class AbstractSimulationSwingWorker extends SwingWorker {

    private final List<SimulationListener> simulationListeners = new ArrayList<>();
    protected boolean continuous;

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public synchronized void addSimulationListener(SimulationListener sl) {
        if (!simulationListeners.contains(sl)) {
            simulationListeners.add(sl);
        }
    }

    public synchronized void removeSimulationListener(SimulationListener sl) {
        simulationListeners.remove(sl);
    }

    protected final void fireSimulationEvent(String type, int value) {
        List<SimulationListener> listeners = getSimulationListenersCopy(simulationListeners);
        SimulationEvent event = new SimulationEvent(this, type, value);
        for (SimulationListener sl : listeners) {
            sl.simulationUpdated(event);
        }
    }

    private synchronized List<SimulationListener> getSimulationListenersCopy(
            List<SimulationListener> listeners) {
        return new ArrayList<>(listeners);
    }
}
