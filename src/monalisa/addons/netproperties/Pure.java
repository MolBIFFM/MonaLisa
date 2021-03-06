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
 * Checks if a net is pure.
 *
 * @author daniel
 */
public class Pure extends NetPropertyAlgorithm<Boolean> {

    private static final Logger LOGGER = LogManager.getLogger(Pure.class);

    public Pure(PetriNetFacade pn) {
        super(pn);
    }

    /**
     * Checks if the net has no transitions, for which a pre-place is also a
     * post place. If there are n
     */
    @Override
    public void runAlgorithm() {
        LOGGER.info("Checking whether net is pure");
        algorithmName = "pure";
        algorithmValue = true;
        for (Transition t : petriNet.transitions()) { //checks all Transitions.
            for (Place p : t.inputs()) { //checks all input Places of the Transition t.
                if (t.outputs().contains(p)) {
                    algorithmValue = false;
                    break;
                }
            }
        }
        LOGGER.info("Successfully checked whether net is pure");
    }

}
