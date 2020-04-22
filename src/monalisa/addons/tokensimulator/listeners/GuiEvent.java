/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.listeners;

import java.util.EventObject;

/**
 *
 * @author Marcel
 */
public class GuiEvent extends EventObject {

    public static final String UPDATE_PLOT = "UPDATE_PLOT";    
    public static final String UPDATE_VISUAL = "UPDATE_VISUAL";
    public static final String REPAINT = "REPAINT";
    public static final String LOCK = "LOCK";
    public static final String UNLOCK = "UNLOCK";
    public static final String SNAPSHOT = "SNAPSHOT";
    public static final String HISTORY = "HISTORY";

    private final String type;
    
    public GuiEvent(Object source, String type) {
        super(source);
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
}
