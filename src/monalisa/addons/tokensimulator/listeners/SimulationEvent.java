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
public final class SimulationEvent extends EventObject {
    public static final String INIT = "INIT";
    public static final String UPDATE_VISUAL = "UPDATE_VISUAL";
    public static final String UPDATE_PROGRESS = "UPDATE_PROGRESS";
    public static final String DONE = "DONE";
    public static final String STOPPED = "STOPPED";    
    
    private final String type;
    private final Object value;

    public SimulationEvent(Object source, String type, Object value) {
        super(source);
        this.type = type;
        this.value = value;
    }
    
    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
