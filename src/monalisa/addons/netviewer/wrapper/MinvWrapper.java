/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.netviewer.wrapper;

import monalisa.data.pn.MInvariant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author daniel
 */
public class MinvWrapper {
    private final MInvariant minv;
    private String name;
    private static final Logger LOGGER = LogManager.getLogger(TinvWrapper.class);

    
    public MinvWrapper(MInvariant input, String TinvType) {
        this.minv = input;
        this.name = TinvType;
        LOGGER.debug("Created new MinvWrapper " + (this.minv.id()+1)+" ("+this.minv.size()+")");
    }
    
    public MinvWrapper(MInvariant input) {
        this.minv = input;
        LOGGER.debug("Created new MinvWrapper " + (this.minv.id()+1)+" ("+this.minv.size()+")");
    }

    public MInvariant getMinv() {
        return minv;
    }

    @Override
    public String toString() {
        if(this.minv.id() >= 0)
            return "Manatee Invariant "+(this.minv.id()+1)+" ("+this.minv.size()+")";
        else
            return name;
    }
}

