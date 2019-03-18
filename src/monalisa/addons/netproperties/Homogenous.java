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
import monalisa.data.pn.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author daniel
 */

public class Homogenous extends NetPropertyAlgorithm<Boolean>{

    private static final Logger LOGGER = LogManager.getLogger(Homogenous.class);

    public Homogenous (PetriNetFacade pn) {
        super(pn);
    }


    /**
     * A net is homogenous, if for any place p, all arcs starting at p have the
     * same multiplicity.
     */
    @Override
    public void runAlgorithm(){
        LOGGER.info("Checking whether net is homogenous");
        algorithmName = "homogenous";
        algorithmValue = true;
        int arcValue;
        for(Place p : petriNet.places()) { //checks all Places.
            if(p.outputs().isEmpty()){
                continue;
            }
            else{
                arcValue = petriNet.getArc(p, p.outputs().get(0)).weight();
            }
            for(Transition t : p.outputs()) { //checks all output Transitions of the Place p.
                if(arcValue != petriNet.getArc(p, t).weight()){
                    algorithmValue = false;
                    break;
                }
            }
        }
        LOGGER.info("Successfully checked whether net is homogenous");
    }
}
