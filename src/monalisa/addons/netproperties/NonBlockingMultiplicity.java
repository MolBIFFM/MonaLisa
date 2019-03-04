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
 *
 * @author daniel
 */
public class NonBlockingMultiplicity extends NetPropertieAlgorithm<Boolean>{
    
    public NonBlockingMultiplicity(PetriNetFacade pn){
        super(pn);
    }
    
    /**
     * Checks if for each place p, the minimum of multiplicities of the arcs 
     * ending at p is not less than the maximum of multiplicities of the arcs
     * starting at p.
     */
    @Override
    public void runAlgorithm(){
        algorithmName = "non-blocking multiplicity";
        algorithmValue = true;
        for(Place p : petriNet.places()) { //checks all Places.
            int minMultiplicity = Integer.MAX_VALUE;
            for(Transition t : p.inputs()) { //checks all input Transitions of the Place p.
                if(petriNet.getArc(t, p).weight() < minMultiplicity){
                    minMultiplicity = petriNet.getArc(t, p).weight();
                }
            }
            for(Transition t : p.outputs()) { //checks all output Transitions of the Place p.
                if(minMultiplicity < petriNet.getArc(p, t).weight()){
                    algorithmValue = false;
                    break;
                }
            }
        }
    }
}
