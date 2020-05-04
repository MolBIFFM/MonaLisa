/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.listeners;

import java.util.EventListener;

/**
 * A listener for GuiEvents that updates the GUI.
 * @author Marcel Gehrmann
 */
public interface GuiListener extends EventListener {

    /**
     * Handles GuiEvents.
     * @param e
     */
    void guiUpdateCall(GuiEvent e);
}
