/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator;

import javax.swing.JPanel;

/**
 *
 * @author Marcel Gehrmann
 */
public abstract class AbstractTokenSimPrefPanel extends JPanel {

    /**
     * Updates the preferences for the current TokenSim.
     */
    public abstract void updatePreferences();

    /**
     * Loads the preferences for the current TokenSim.
     */
    public abstract void loadPreferences();
}
