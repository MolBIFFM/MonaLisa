/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.listeners;

import java.util.EventListener;

/**
 * Listener for simulation events to synchronize UI with progress etc.
 *
 * @author Marcel Gehrmann
 */
public interface SimulationListener extends EventListener {

    /**
     * Handles SimulationEvents to synchronize UI and update progress.
     * @param e
     */
    void simulationUpdated(SimulationEvent e);
}
