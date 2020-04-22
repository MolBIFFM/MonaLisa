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
public abstract class AbstractTokenSimPrefPanel extends JPanel {

    public abstract void updatePreferences();

    public abstract void loadPreferences();
}
