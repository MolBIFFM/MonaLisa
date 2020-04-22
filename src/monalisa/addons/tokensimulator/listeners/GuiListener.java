/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.listeners;

import java.util.EventListener;

/**
 *
 * @author Marcel
 */
public interface GuiListener extends EventListener {
    
    void guiUpdateCall(GuiEvent e);
}
