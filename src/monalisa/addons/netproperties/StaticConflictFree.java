/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netproperties;

import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;

/**
 * Checks if a net is static conflict free.
 * @author daniel
 */

public class StaticConflictFree extends NetPropertieAlgorithm<Boolean> {

    public StaticConflictFree(PetriNetFacade pn) {
        super(pn);
    }
    
    
    /**
     * If every place has just one post transition, the net is static conflict
     * free.
     */
    @Override
    public void runAlgorithm() {
        algorithmName = "static conflict free";
        algorithmValue = true;
        for(Place p : petriNet.places()) { //checks all Places.
            if(p.outputs().size() >= 2){
                algorithmValue = false;
                break;
            }
        }
        
    }
    
}
