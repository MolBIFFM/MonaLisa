/*
 *
 *  This file ist part of the software MonaLisa.
 *  MonaLisa is free software, dependend on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institue of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */

package monalisa.addons.netproperties;

import monalisa.data.pn.PetriNetFacade;
import monalisa.data.pn.Place;
import java.util.ArrayList;

/**
 * returns all places without post transitions.
 * @author daniel
 */
public class PlaceWithoutPostTransition extends NetPropertieAlgorithm <ArrayList<Place>>{

    public PlaceWithoutPostTransition(PetriNetFacade pn) {
        super(pn);
    }

    /**
     *
     */
    public void runAlgorithm() {
        algorithmName = "place withouth post transition";
        algorithmValue = new ArrayList();
        for(Place p : petriNet.places()){
            if(p.outputs().isEmpty()){
                algorithmValue.add(p);
            }
        }
    }
}