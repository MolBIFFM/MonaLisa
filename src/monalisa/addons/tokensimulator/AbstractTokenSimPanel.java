/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator;

import javax.swing.JPanel;

/**
 * Abstract base class for all TokenSimPanels. Manages start and stop of
 * simulation and firing. Also needs to be locked and unlocked before and after
 * simulation.
 *
 * @author Marcel Gehrmann
 */
public abstract class AbstractTokenSimPanel extends JPanel {

    /**
     * Sets the name of the simulation to 'name'
     *
     * @param name
     */
    public abstract void setSimName(String name);

    /**
     * Unlocks the UI once the simulation is done or aborted.
     */
    public abstract void unlock();

    /**
     * Makes all necessary preparations and then starts the simulator.
     */
    public abstract void startSim();

    /**
     * Closes the simulator after simulation has been completed.
     */
    public abstract void endSim();

    /**
     * Starts firing for the simulation.
     */
    protected abstract void startFiring();

    /**
     * Stops firing for the simulation.
     */
    protected abstract void stopFiring();
}
