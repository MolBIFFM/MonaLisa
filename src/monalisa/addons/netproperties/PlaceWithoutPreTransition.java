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
import java.util.ArrayList;

/**
 * returns all places without pre transitions.
 * @author daniel
 */
public class PlaceWithoutPreTransition extends NetPropertieAlgorithm <ArrayList<Place>>{

    public PlaceWithoutPreTransition(PetriNetFacade pn) {
        super(pn);
    }

    @Override
    public void runAlgorithm() {
        algorithmName = "place withouth pre transition";
        algorithmValue = new ArrayList();
        for(Place p : petriNet.places()){
            if(p.inputs().isEmpty()){
                algorithmValue.add(p);
            }
        }
    }
    
}