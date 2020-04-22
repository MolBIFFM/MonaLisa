/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator;

import javax.swing.JPanel;

/**
 *
 * @author Marcel
 */
public abstract class AbstractTokenSimPanel extends JPanel {

    public abstract void setSimName(String name);

    public abstract void unlock();

    public abstract void startSim();

    public abstract void endSim();

    protected abstract void startFiring();

    protected abstract void stopFiring();
}
