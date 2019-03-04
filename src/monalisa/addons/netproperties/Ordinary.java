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
import monalisa.data.pn.Transition;

/**
 * A Algorithm to check if the multiplicity of every arc equals one
 *
 * @author daniel
 */
public class Ordinary extends NetPropertieAlgorithm<Boolean> {

    public Ordinary(PetriNetFacade pn) {
        super(pn);
    }

    /**
     * A net is ordinary, if the multiplicity of every arc equals one.
     *
     * @return true if the petri net is ordinary, otherwise false.
     */
    @Override
    public void runAlgorithm() {
        algorithmName = "ordinary";
        algorithmValue = true;
        for(Place p : petriNet.places()) { //checks all Places.
            for(Transition t : p.inputs()) { //checks all input Transitions of the Place p.
                if(petriNet.getArc(t, p).weight() != 1) {
                    algorithmValue = false;
                    break;
                }
            }
            for(Transition t : p.outputs()) { //checks all output Transitions of the Place p.
                if(petriNet.getArc(p, t).weight() != 1) {
                    algorithmValue = false;
                    break;
                }
            }
        }
    }  
}
