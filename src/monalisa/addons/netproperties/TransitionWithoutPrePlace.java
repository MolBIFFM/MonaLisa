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

import monalisa.data.pn.Transition;
import java.util.ArrayList;
import monalisa.data.pn.PetriNetFacade;

/**
 * returns all transitions without pre places.
 * @author daniel
 */
public class TransitionWithoutPrePlace extends NetPropertieAlgorithm <ArrayList<Transition>>{

    public TransitionWithoutPrePlace(PetriNetFacade pn) {
        super(pn);
    }

    @Override
    public void runAlgorithm() {
        algorithmName = "transition withouth pre place";
        algorithmValue = new ArrayList();
        for(Transition t : petriNet.transitions()){
            if(t.inputs().isEmpty()){
                algorithmValue.add(t);
            }
        }
    }
    
    
}
